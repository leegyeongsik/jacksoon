# Jacksoon
Java NIO의 Selector 기반으로 구현한 API Gateway

외부 클라이언트의 요청을 Router가 받아 Registry에 등록된 Backend 정보로 라우팅하고 응답을 클라이언트에게 반환

# Architecture
<img width="1200" height="600" alt="img_3" src="https://github.com/user-attachments/assets/cdd0d6e3-3607-4246-871f-5a7512ddb682" />


## Router

클라이언트 요청을 등록해둔 서비스 정보를 보고 들어온 요청을 그에 맞는 Backend로 넘김

주요 역할

* Backend 선택
* Backend 응답 전달
* 비동기 처리 환경에서 클라이언트 응답 순서 보장
* 동적 Filter 실행
* 클라이언트 연결 상태 관리

## Registry

Backend 서비스의 정보를 관리

Backend 등록 이후 주기적으로 Health Check를 수행하며 Router가 사용할 서비스 정보를 제공

## Filter Management

Router에서 실행할 Filter를 동적으로 등록하고 관리

등록된 Filter를 bundle로 구성하며 Router는 최신 bundle을 받아 런타임에 로딩

## Console

Router에서 생성된 Metric 데이터를 수집하고 조회

Registry와 Filter Management의 상태 정보를 저장하는 역할 수행

---

# Request Flow

```text
Client
  │
  │ HTTP Request
  ▼
Router
  │
  ├─ HTTP Parse
  │
  ├─ Route Lookup
  │
  ├─ PRE Filter
  │
  ├─ Backend Selection
  │
  ├─ Request
  ▼
Backend
  │
  │ Response
  ▼
Router
  │
  ├─ POST Filter
  │
  └─ Response Ordering
  ▼
Client
```
---

# Quick Start

## 요구 사항
- Docker
- Docker Compose 2.34+ 

## 실행

`your-password`를 사용할 PostgreSQL 비밀번호로 변경한 뒤 실행

**Windows**
```powershell
$env:POSTGRES_DB="jacksoon_console"
$env:POSTGRES_USER="jacksoon"
$env:POSTGRES_PASSWORD="your-password"
$env:DDL="update"

docker compose -f oci://ghcr.io/leegyeongsik/jacksoon:latest up -d
```

**Linux**
```bash
POSTGRES_DB="jacksoon_console" \
POSTGRES_USER="jacksoon" \
POSTGRES_PASSWORD="your-password" \
DDL="update" \
docker compose -f oci://ghcr.io/leegyeongsik/jacksoon:latest up -d
```

## 실행되는 구성

| 서비스 | 포트 |
|---|---|
| Router | 1012 |
| Registry | 1013 |
| Filter Management | 1011 |
| Console | 1014 |
| PostgreSQL | internal |

---

# Backend Registration

Registry에 백엔드 서비스 정보를 등록

Backend가 Docker Host의 `8081` 포트에서 실행 중인 경우:

```bash
curl -X POST http://localhost:1013/register \
  -H "Content-Type: application/json" \
  -d '{"serviceName":"temp-service","instanceId":"temp-service-8081","endpoint":{"host":"host.docker.internal","port":8081,"protocol":"http","healthPath":"/actuator/health"},"rules":[{"pathPrefix":"/a","stripPrefix":false}]}'
```

등록 정보:

```json
{
  "serviceName": "temp-service",
  "instanceId": "temp-service-8081",
  "endpoint": { 
    "host": "host.docker.internal",
    "port": 8081,
    "protocol": "http",
    "healthPath": "/actuator/health"
  },
  "rules": [
    {
      "pathPrefix": "/a",  
      "stripPrefix": false
    }
  ]
}
```
serviceName : 등록할 서비스 이름 같은 서비스를 여러 인스턴스로 등록하는 경우 동일한 값을 사용

instanceId : 각 Backend 인스턴스를 구분하기 위한 식별자 ( temp-service-8081, temp-service-8082 ) 

endpoint.host : Router에서 접근할 Backend 주소 Backend가 Docker Desktop Host에서 실행 중이라면 host.docker.internal을 사용

endpoint.port : Backend가 실행 중인 포트

endpoint.protocol : Backend와 통신할 프로토콜

endpoint.healthPath : Registry가 Backend 상태를 확인하기 위해 주기적으로 호출하는 Health Check API 경로

rules[].pathPrefix : 해당 서비스로 라우팅할 요청 경로의 Prefix 예를 들어 /a로 등록하면 /a/user, /a/order와 같은 요청이 temp-service로 라우팅

rules[].stripPrefix : Backend에 요청을 전달할 때 pathPrefix를 제거할지 결정

pathPrefix = /a

request    = /a/user

false인 경우:Backend로 전달 → /a/user

true인 경우:Backend로 전달 → /user

# Router Request

Backend 등록 후 Router로 요청을 전달

```bash
curl http://localhost:1012/a/load-test
```

Router는 등록된 서비스를 찾고 요청을 전달

```text
Client
  │
  │ GET /a/load-test
  ▼
Router :1012
  │
  │ service Lookup
  ▼
temp-service
  │
  ├─ Backend 8081
  └─ Backend 8082
```

---

# Benchmark

Gateway 처리 비용과 성능을 비교하기 위해 동일한 Backend 애플리케이션을 대상으로 세 가지 환경을 측정

**Backend Direct**

```text
Client ──────────────────────────────> Backend
```

**Spring Cloud Gateway**

```text
Client ──> Spring Cloud Gateway ─────> Backend
```

**Jacksoon**

```text
Client ──> Jacksoon Router ──────────> Backend
```

모든 테스트는 단일 Backend 인스턴스를 사용했으며 동일한 로컬 환경에서 같은 테스트 클라이언트와 Backend API를 대상으로 수행

Spring Cloud Gateway는 별도의 성능 튜닝 없이 기본 설정을 사용

## Test Condition

```text
Backend Instance : 1
Backend Delay    : 0 ms
Connections      : 50 / 100
Requests/connection   : 32 
Warm-up          : 10 sec
Measurement      : 10 sec
Runs             : 5
HTTP             : HTTP/1.1
```

## Result

| Target               | Connections |     Mean Throughput |           p50 |            p95 |            p99 | Failed | Connection Errors |
| -------------------- | ----------: | ------------------: | ------------: | -------------: | -------------: | -----: | ----------------: |
| Backend Direct       |          50 |     57,431.18 req/s |     19.946 ms |      81.249 ms |     147.269 ms |      0 |                 0 |
| Backend Direct       |         100 |     56,890.62 req/s |     42.894 ms |     151.698 ms |     271.860 ms |      0 |                 0 |
| Spring Cloud Gateway |          50 |     14,264.10 req/s |    109.526 ms |     143.154 ms |     192.223 ms |      0 |                 0 |
| Spring Cloud Gateway |         100 |     14,847.46 req/s |    211.327 ms |     261.716 ms |     290.838 ms |      0 |                 0 |
| **Jacksoon**         |      **50** | **45,261.18 req/s** | **33.501 ms** |  **60.410 ms** |  **84.440 ms** |  **0** |             **0** |
| **Jacksoon**         |     **100** | **43,887.20 req/s** | **68.242 ms** | **132.282 ms** | **173.055 ms** |  **0** |             **0** |

## Backend Direct

Gateway를 거치지 않고 Backend에 직접 요청해 기준 처리량을 측정

```text
50 Connections  : 57,431.18 req/s
100 Connections : 56,890.62 req/s
```

## Spring Cloud Gateway

Spring Cloud Gateway의 기본 설정을 사용해 동일한 Backend로 요청을 전달

```text
50 Connections  : 14,264.10 req/s
100 Connections : 14,847.46 req/s
```

## Jacksoon

Jacksoon Router를 통해 동일한 Backend로 요청을 전달

```text
50 Connections  : 45,261.18 req/s
100 Connections : 43,887.20 req/s
```

---

# Stability Test

최대 처리량 테스트와 별도로 일정한 부하를 5분 동안 지속했을 때 요청 실패와 내부 요청 대기가 발생하는지 확인

## Test Condition

```text
Target RPS : 10,000
Duration   : 5 min
HTTP       : HTTP/1.1
Keep-Alive : enabled
```

## Client Result

```text
Sent              : 2,987,983
Success           : 2,987,983
Failure           : 0
Timeout           : 0
Connection errors : 0

Actual throughput : 9,952.47 req/s
Avg latency       : 8.967 ms
```

5분 동안 약 299만 건의 요청을 처리했으며 모든 요청이 성공

목표 부하는 10,000 RPS였으며 실제 평균 처리량은 `9,952.47 req/s` 평균 응답 시간은 `8.967 ms`

## Router Runtime

```text
Max backend pending : 0

Reconnect attempts  : 29,302
Reconnect success   : 29,302
Reconnect failure   : 0

Max router heap used : 189.9 MB
```
Backend의 Keep-Alive 연결 종료에 따라 재연결이 발생했지만 총 29,302회의 재연결이 모두 성공했으며 재연결 실패는 발생하지않음

---

# Tech Stack

```text
Java 21
Java NIO
Spring Boot
PostgreSQL
```

---

# Modules

```text
jacksoon
├─ jacksoon-common
├─ jacksoon-init
├─ jacksoon-router
├─ jacksoon-registry
├─ jacksoon-filterManagement
└─ jacksoon-console
```

---

# About

Jacksoon은 API Gateway 내부에서 요청이 어떻게 전달되고 연결이 어떻게 관리되는지 직접 구현하고 검증하기 위해 개발한 프로젝트입니다
