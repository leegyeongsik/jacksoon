# Jacksoon
Java NIO 기반으로 구현한 비동기 API Gateway 프로젝트입니다

서비스 레지스트리, 동적 라우팅, 부하 기반 인스턴스 선택, 상태 확인, 동적 필터, 운영 콘솔을 직접 구현하며 API Gateway 내부의 요청 처리 흐름과 네트워크 동시성 문제를 학습하는 것을 목표로 개발했습니다

---

## 1. 프로젝트 소개
일반적인 API Gateway 프레임워크를 사용하지 않고 Java NIO의 `Selector`, `SelectionKey`, `SocketChannel`을 기반으로 요청 수신부터 백엔드 서버 연결 응답 반환까지의 흐름을 구현했습니다
Jacksoon은 다음 기능을 제공합니다
* 서비스 및 인스턴스 동적 등록
* 경로 기반 요청 라우팅
* 다중 백엔드 인스턴스 부하 분산
* 백엔드 인스턴스 상태 확인
* Java/JAR 기반 동적 필터 등록
* 서비스 및 필터 실행 메트릭 수집
* 운영 상태를 확인할 수 있는 웹 콘솔

---


## 2. 개발 배경

Spring Cloud Gateway과 같은 기존 솔루션을 사용하다가 나도 할 수 있을거같은데? 라고 생각하여 구현하였고 또한 서비스가 실행중일때 예기치못한 예외를 만나게됬을때 다시 서비스를 배포하는 과정이 있을 수 있을거라 생각하였고
그것을 필터로 그 예외난 부분만 막으면 이외의 기능은 처리되면서 해당 기능이 해결됬을때 재배포 하는게 더 효율적이지 않을까 생각하여 동적필터를 구현

특히 다음 내용을 중점적으로 다뤘습니다
* 하나의 NIO 스레드가 여러 연결을 처리하는 방식
* 클라이언트 요청과 백엔드 응답을 연결하는 방법
* 실행 중인 서버에 필터를 동적으로 추가하는 방법

---


## 3. 아키텍처
<img width="1189" height="329" alt="라우터 ㅋㅋ" src="https://github.com/user-attachments/assets/de0320e2-9989-4869-9960-e43b2b3d39b6" /> - router 

<img width="1039" height="707" alt="레지스트리" src="https://github.com/user-attachments/assets/3793deac-a93e-4ce3-8ba6-d956a3ce0c78" />- registry

<img width="1137" height="727" alt="필터" src="https://github.com/user-attachments/assets/58bf0096-c545-4856-a09a-435cf0b1ab6d" />- filemanagement

### 주요 구성 요소

| 모듈                          | 역할                             |
| --------------------------- | ------------------------------ |
| `jacksoon-common`           | NIO 네트워크 처리, 파이프라인, 공통 큐와 컨텍스트 |
| `jacksoon-router`           | 요청 파싱, 라우팅, 인스턴스 선택, 응답 반환    |
| `jacksoon-registry`         | 서비스 등록, 인스턴스 관리,라우팅 규칙 관리, Health Check         |
| `jacksoon-filterManagement` | 동적 필터 컴파일, Bundle 생성, 교체 및 삭제         |
| `jacksoon-console`          | 서비스, 필터, 메트릭 조회를 위한 운영 콘솔        |

---


## 4. 요청 처리 흐름

클라이언트 요청은 다음 순서로 처리됩니다

1. Reactor가 클라이언트 채널의 `READ` 이벤트를 확인합니다
2. 읽은 데이터를 버퍼에 누적하고 HTTP 요청이 완성되었는지 확인합니다
3. 완성된 요청을 파싱해 요청 경로, 헤더, Body 정보를 컨텍스트에 저장합니다
4. 현재 단계의 앞뒤에 등록된 필터를 실행합니다
5. Registry Snapshot에서 요청 경로와 일치하는 서비스와 라우팅 규칙을 찾습니다
6. 대상 서비스가 가진 인스턴스별 연결 풀을 비교합니다
7. 현재 대기 중인 요청 수인 `pending` 값이 가장 작은 연결 풀을 선택합니다
8. 선택된 백엔드 연결에 요청 데이터를 전달하고 `WRITE` 이벤트를 등록합니다
9. 요청 전송이 완료되면 백엔드 채널을 `READ` 상태로 변경합니다
10. 백엔드 응답을 모두 수신하면 응답 단계에 등록된 필터를 실행합니다
11. 응답 데이터를 클라이언트 채널에 전달합니다
12. 응답 전송이 끝나면 연결 상태를 확인해 연결을 재사용하거나 종료합니다

---


## 5. 주요 기능

### 5.1 서비스 및 인스턴스 동적 등록

백엔드 서비스는 Registry에 자신의 인스턴스 정보와 라우팅 규칙을 등록합니다

```bash
curl.exe -X POST http://localhost:1013/register \
  -H "Content-Type: application/json" \
  -d "{
    \"serviceName\":\"temp-service\",
    \"instanceId\":\"temp-service-8081\",
    \"endpoint\":{
      \"host\":\"localhost\",
      \"port\":8081,
      \"protocol\":\"http\",
      \"healthPath\":\"/actuator/health\"
    },
    \"rules\":[
      {
        \"pathPrefix\":\"/a\",
        \"stripPrefix\":false
      }
    ]
  }"
```

동일한 서비스 이름으로 여러 인스턴스를 등록할 수 있습니다

```text
temp-service
├── temp-service-8081
└── temp-service-8082
```

Router는 변경되는 Registry 데이터를 요청마다 직접 조회하지 않고 현재 Snapshot을 사용합니다

서비스나 인스턴스 정보가 변경되면 새로운 Snapshot을 만들고 기존 Snapshot과 교체합니다

---

### 5.2 경로 기반 라우팅

서비스 등록 시 `pathPrefix`와 `stripPrefix`를 설정할 수 있습니다.

```json
{
  "pathPrefix": "/a",
  "stripPrefix": false
}
```

요청된 path로 서비스를 찾음

---

### 5.3 부하 기반 인스턴스 선택

기본 인스턴스 선택 전략은 현재 처리 대기 중인 요청 수가 가장 적은 인스턴스를 선택하는 방식

```text
Instance A: pending 3
Instance B: pending 1
Instance C: pending 2

선택 결과: Instance B
```

### 5.4 백엔드 연결 풀

라우터는 백엔드 인스턴스별로 연결 풀을 관리

---

### 5.5 Health Check

Registry는 일정 주기로 등록된 백엔드 인스턴스의 Health Check 경로를 호출합니다
정상적인 HTTP 200 응답을 받으면 성공으로 기록합니다
다음 상황에서는 실패로 처리합니다
백엔드 서버 연결 실패
Health Check 응답 timeout
HTTP 200 이외의 상태 코드
Health Check 결과에 따라 Registry의 인스턴스 상태가 변경되고 변경된 정보는 새로운 Snapshot으로 Router에 전달됩니다.

---

### 5.6 동적 필터

Java 소스 파일 또는 JAR 파일을 업로드하여 실행 중인 라우터에 필터를 등록할수 있음

지원 파일 형식:
* `.java`

필터 등록 시 다음 정보를 입력
* 필터 이름
* 필터 클래스의 FQCN
* 실행 Pipeline
* 실행 Timing
* 실행 순서
* 파일 형식

#### Pipeline

```text
REQUEST_PARSE
ROUTING
BACKEND_RESPONSE
```

#### Timing

```text
PRE
POST
```


업로드된 파일을 이용해 새로운 필터 Bundle을 생성하고 필터 클래스 생성과 검증이 정상적으로 끝나면 현재 Bundle을 새로운 Bundle로 교체합니다
Bundle이 교체되는 시점에 처리 중이던 요청은 기존 Bundle을 사용하고 교체 이후 들어온 요청부터 새로운 필터가 적용됩니다

필터를 삭제할 때도 새로운 Bundle을 만든 뒤 현재 Bundle과 교체합니다.
---

### 5.7 필터 실행 예시

다음과 같이 특정 경로의 요청을 차단하는 필터를 등록할 수 있습니다

```java
package io.jacksoon.filter.custom;

import io.jacksoon.common.filter.FilterContext;
import io.jacksoon.common.filter.RouterFilter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class InvalidARequestBackendResponseFilter
        implements RouterFilter {

    @Override
    public boolean isSupport(FilterContext context) {
        return context.getRequest() != null
                && "/a".equals(context.getRequest().getPath());
    }

    @Override
    public void doFilter(FilterContext context) {
        byte[] body = """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <title>잘못된 요청</title>
                </head>
                <body>
                    <h1>잘못된 요청입니다.</h1>
                    <p>/a 경로는 요청할 수 없습니다.</p>
                </body>
                </html>
                """.getBytes(StandardCharsets.UTF_8);

        byte[] header = (
                "HTTP/1.1 400 Bad Request\r\n"
                        + "Content-Type: text/html; charset=UTF-8\r\n"
                        + "Content-Length: " + body.length + "\r\n"
                        + "Connection: keep-alive\r\n"
                        + "\r\n"
        ).getBytes(StandardCharsets.US_ASCII);

        ByteBuffer responseBuffer =
                ByteBuffer.allocate(header.length + body.length);

        responseBuffer.put(header);
        responseBuffer.put(body);
        responseBuffer.flip();

        context.setByteBuffer(responseBuffer);
        context.setEvent("backend-response");
    }
}
```
등록 정보 예시:

Filter Name: InvalidARequestBackendResponseFilter
Pipeline: REQUEST_PARSE
Timing: POST
Order: 1
Path: /a

필터 등록 후 /a 경로로 요청하면 백엔드 서버까지 요청을 전달하지 않고 다음과 같은 응답을 반환하도록 테스트했습니다

잘못된 요청입니다
/a 경로는 요청할 수 없습니다

필터는 먼저 isSupport를 호출해 현재 요청에서 실행할 필터인지 확인하고 true이면 실행합니다

---

### 5.8 메트릭 및 운영 콘솔

Router, Registry, Filter Management에서 발생한 데이터를 운영 콘솔로 전달합니다
매 요청마다 콘솔 서버로 HTTP 요청을 보내면 Router의 요청 처리에도 영향을 줄 수 있기 때문에 메트릭은 메모리에서 일정 개수까지 모은 후 배치로 전송합니다
수집하는 항목은 다음과 같습니다

서비스 요청 성공 횟수
서비스 요청 실패 횟수
필터 실행 성공 횟수
필터 실행 실패 횟수
서비스 인스턴스 목록
서비스 라우팅 규칙
등록된 필터 정보
Console 서버는 전달받은 데이터를 PostgreSQL에 저장하고 웹 화면으로 제공합니다
운영 화면에서는 다음 정보를 확인할 수 있습니다

현재 등록된 서비스
서비스별 인스턴스
인스턴스 연결 정보
라우팅 규칙
서비스 성공 및 실패 횟수
등록된 필터
필터 성공 및 실패 횟수

---

## 6. 부하 테스트

부하 테스트는 k6를 이용해 진행했습니다

이번 테스트는 로컬 환경에서 진행했기 때문에 최대 처리량을 측정하기보다는 다음 내용을 확인하는 데 중점을 두었습니다

* 요청량이 증가해도 실패 없이 처리되는지
* 느린 인스턴스가 있을 때 요청 분배가 달라지는지
* 응답 속도 차이가 `pending` 값에 반영되는지
* 테스트가 끝난 후 연결 풀의 상태가 정상적으로 복구되는지

## 테스트 환경

```text
Router: localhost:1012
Backend 1: localhost:8081
Backend 2: localhost:8082
Load Generator: k6
Test Duration: 30초
Request Rate: 20 / 50 / 100 RPS
Request Path: /a/load-test
Request Timeout: 5초
```

백엔드 서버는 요청을 처리한 인스턴스와 설정된 응답 지연 시간을 헤더에 담아 반환하도록 구성했습니다

```http
X-Backend-Port: 8081
X-Response-Delay: 0
```

k6에서는 해당 헤더를 읽어 인스턴스별 요청 처리 횟수를 기록했습니다

---

## 6.1 두 인스턴스가 지연 없이 응답하는 환경

8081과 8082 인스턴스를 모두 별도의 응답 지연 없이 실행했습니다

| RPS | 요청 수  | 성공률  | 평균     | p90    | p95    | 최대     | 8081 | 8082  |
| --- | ----- | ---- | ------ | ------ | ------ | ------ | ---- | ----- |
| 20  | 601   | 100% | 0.74ms | 1.53ms | 1.64ms | 3.00ms | 0    | 601   |
| 50  | 1,501 | 100% | 0.82ms | 1.54ms | 1.78ms | 3.55ms | 5    | 1,496 |
| 100 | 3,001 | 100% | 1.24ms | 2.48ms | 2.94ms | 5.28ms | 5    | 2,996 |

모든 구간에서 요청 실패와 dropped iteration은 발생하지 않았습니다

100 RPS에서도 평균 응답 시간은 약 1.24ms, p95는 약 2.94ms로 측정됐습니다

다만 대부분의 요청이 8082 인스턴스로 전달됐습니다

두 인스턴스가 모두 빠르게 응답하면 다음 요청이 들어오는 시점에는 두 연결 풀의 `pending` 값이 다시 0이 됩니다
현재 구현에서는 `pending` 값이 같은 경우 먼저 조회된 연결 풀이 선택되기 때문에 특정 인스턴스에 요청이 집중되는 결과가 나타났습니다

이 테스트를 통해 Least Pending 방식이 현재 처리 중인 요청 수는 반영하지만, 부하가 같은 인스턴스 사이의 요청을 균등하게 분배하는 방식은 아니라는 점을 확인했습니다

---

## 6.2 8082 인스턴스에 500ms 지연을 준 환경

8081은 지연 없이 응답하도록 설정하고 8082에는 500ms의 응답 지연을 적용했습니다

```text
Backend 8081: delay 0ms
Backend 8082: delay 500ms
```

| RPS | 요청 수  | 성공률  | 평균      | p90    | p95      | 최대       | 8081  | 8082 |
| --- | ----- | ---- | ------- | ------ | -------- | -------- | ----- | ---- |
| 20  | 601   | 100% | 49.45ms | 4.01ms | 514.56ms | 530.09ms | 544   | 57   |
| 50  | 1,501 | 100% | 21.10ms | 1.56ms | 2.54ms   | 693.63ms | 1,442 | 59   |
| 100 | 3,000 | 100% | 11.92ms | 2.54ms | 3.04ms   | 889.38ms | 2,941 | 59   |

인스턴스별 요청 처리 비율은 다음과 같습니다

| RPS | 8081 요청 비율 | 8082 요청 비율 |
| --- | ---------- | ---------- |
| 20  | 90.5%      | 9.5%       |
| 50  | 96.1%      | 3.9%       |
| 100 | 98.0%      | 2.0%       |

8082가 요청을 처리하는 동안에는 약 500ms 동안 pending 값이 유지됩니다 반면 8081은 응답이 빠르기 때문에 요청을 처리한 뒤 pending 값이 빠르게 감소합니다
따라서 새로운 요청이 들어오는 시점에는 대부분 8081의 pending 값이 더 작았고 요청도 주로 8081로 전달됐습니다
RPS가 증가할수록 8082가 처리한 요청 비율은 9.5%에서 2.0%까지 감소했습니다

20 RPS에서는 느린 인스턴스가 전체 요청의 약 9.5%를 처리하면서 p95가 약 514.56ms로 측정됐습니다

50 RPS와 100 RPS에서는 느린 인스턴스가 처리한 요청 비율이 5% 아래로 감소하면서 p95가 각각 약 2.54ms와 3.04ms로 유지됐습니다

최대 응답 시간은 요청량이 증가하면서 높아졌지만, 모든 테스트에서 다음 조건을 만족했습니다

* 요청 성공률 100%
* 요청 실패율 0%
* 알 수 없는 백엔드 응답 0건
* dropped iteration 0건

---

## 6.3 테스트 결과

* 20, 50, 100 RPS 구간에서 요청 실패 없이 동작했습니다
* 응답이 느린 인스턴스는 pending 값이 더 오래 유지됐습니다
* 새로운 요청은 대부분 pending 값이 빠르게 감소하는 8081로 전달됐습니다
* 요청량이 증가할수록 느린 인스턴스가 처리하는 요청 비율은 감소했습니다
* Least Pending 방식이 인스턴스의 응답 속도 차이를 요청 분배에 반영하는 것을 확인했습니다
* 두 인스턴스의 `pending` 값이 같으면 특정 인스턴스가 계속 선택될 수 있다는 점도 확인했습니다

현재 방식은 느린 인스턴스에 요청을 계속 균등하게 전달하지 않고 처리 가능한 인스턴스로 더 많은 요청을 보내는 방식으로 동작했습니다
다만 두 인스턴스의 응답 속도와 pending 값이 같은 상황에서는 요청이 한쪽에 집중될 수 있습니다

---

## 7. 기술 스택

### Backend

* Java 21
* Java NIO
* Spring Boot
* JPA
* QueryDSL
* Maven

### Database

* PostgreSQL

### Test
* k6

## 8. 프로젝트 구조

```text
jacksoon
├── jacksoon-common
├── jacksoon-router
├── jacksoon-registry
├── jacksoon-filterManagement
├── jacksoon-console
```

---

## 9. 개선 계획
* Docker Compose 기반 통합 실행 환경 구성
* 자동화된 통합 테스트 작성
* 예외 상황과 연결 상태에 대한 안정성 보완
* 테스트 코드 추가
* 다양한 요청 및 장애 상황을 가정한 테스트 시나리오 작성
* 백엔드 서버 중단 및 복구 상황에 대한 테스트
  
---
