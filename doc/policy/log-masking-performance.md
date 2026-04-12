# Logback CardNumberMaskingConverter 성능 고려사항

**작성일**: 2026-04-12 (리뷰 489688d #132)

## 현재 구현

`CardNumberMaskingConverter`는 모든 로그 라인에 대해 정규식 `\b\d{13,19}\b`를 실행하여 PAN을 마스킹한다 (#121 개선으로 throwable 체인도 포함).

## 잠재 오버헤드

- 로그 1건당 정규식 평가 + (발견 시) 문자열 치환
- 예외 발생 시 스택 트레이스 재귀 순회 (O(depth × frames))
- 10K TPS 환경에서 로그 10K+ 라인/초 → 정규식 풀 초당 10K+회

## 현재 수용

FDS 프로젝트 Phase 1-4 단계에서는 정확성 우선. transaction-service가 **카드번호 취급 핵심 경로**라 **모든 로그 라인 커버**가 필수. 성능 최적화는 부하 테스트 결과에 따라 결정.

## 향후 최적화 옵션 (부하 테스트 후 결정)

### Option A: Logger 기반 범위 축소
카드번호가 노출될 수 있는 logger만 `%mask` 적용:
```xml
<logger name="com.gijun.fds.transaction" level="INFO">
    <appender-ref ref="CONSOLE_MASKED"/>
</logger>
<logger name="org.springframework" level="INFO">
    <appender-ref ref="CONSOLE_PLAIN"/>
</logger>
```
→ 프레임워크 로그 정규식 생략. 단, 프레임워크가 요청 바디를 덤프할 경우 누출 위험.

### Option B: 구조화(JSON) 로깅 전환
- Logstash JSON Encoder 사용
- 구조화 필드 단위로 선별 마스킹 (MDC만 통과, message는 그대로)
- 장점: 파싱/검색 용이, 필드별 정책 가능
- 단점: 도입 비용, 로그 시스템(ELK, Loki 등) 호환성

### Option C: 컴파일된 정규식 재사용 (이미 적용)
- `CardNumberMaskingConverter`의 `PAN_REGEX`는 companion object에 `Regex("""\b\d{13,19}\b""")`로 static 저장 → 호출당 compile 비용 없음. **이미 적용됨.**

### Option D: SIMD/aho-corasick 기반 라이브러리
- Google RE2 또는 Hyperscan 도입
- 정규식 대신 상태 기계 기반 매칭
- 10x 이상 속도 개선 가능
- 도입 복잡도 높음, JVM 친화 라이브러리 제한적

## 측정 방법 (부하 테스트 연동)

1. k6 / gatling으로 transaction-service에 10K TPS 부하
2. JVM metrics: Logback Encoder time, GC pressure
3. `async-profiler`로 CPU hot spot 분석
4. CardNumberMaskingConverter.convert()가 top 10에 들어가면 Option A/B 검토

## 결정 보류 조건

- 현재 RTD-12로 등록. **Phase 5(운영 전환)** 전까지 실측 없이 최적화하지 않음.
- k6 부하 테스트(기존 미구현) 구축 시점에 재평가.

## 관련 이슈
- #91 Logback PAN 마스킹 도입
- #121 예외 스택 마스킹 확장
- RTD-12 CardNumberMaskingConverter 정규식 오버헤드
