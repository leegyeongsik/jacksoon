package io.jacksoon.filterManagement.store;

import io.jacksoon.init.annotation.Init;

import java.util.Map;

@Init
public class FilterStore {// 여기서 컴파일 저장해놓고
                          // 파일을 따로 저장해놔야겠다 버전별로 관리하는 느낌으로
                            // 버전명
                            // 버전이랑 필터세팅에 버전 저장해놓고 map에 갱신해놓음 그리고 버전묶음을 jar로 통합시키자 디렉토리가 두개 버전별 jar를 버전별 통합 jar들
                          // 통합jar는 filtersetting에 있는거 찾아서 만듬
    int version;
    Map<Object,Object> filterSettingMap;

    // 버전갱신하는거 싱크로나이즈

}
