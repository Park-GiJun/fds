# FDS 도메인 용어 사전

> 코드 네이밍과 커뮤니케이션에서 사용하는 공식 용어 목록.
> Domain Expert Reviewer가 관리하며, `/review` 시 네이밍 일관성 검사에 사용.

| 한국어 | 영어 (코드) | 설명 | 사용 위치 |
|--------|------------|------|----------|
| 거래 | Transaction | 카드/결제 단건 거래 이벤트 | fds-transaction-service, fds-common |
| 이상거래 | FraudTransaction | 사기 또는 비정상으로 판정된 거래 | fds-detection-service |
| 탐지 | Detection | 거래의 이상 여부를 규칙 엔진으로 판정하는 행위 | fds-detection-service |
| 탐지 결과 | DetectionResult | 규칙 평가 후 생성되는 리스크 레벨 + 스코어 집합 | fds-detection-service, fds-common |
| 위험도 | RiskLevel | LOW / MEDIUM / HIGH / CRITICAL 4단계 | fds-common (RiskLevel enum) |
| 리스크 스코어 | RiskScore | 0~100, 트리거된 규칙들의 기여 점수 합산 (cap: 100) | fds-detection-service |
| 유저 행동 프로필 | UserBehaviorProfile | Redis에 저장된 유저의 최근 거래 패턴 요약 | fds-detection-service |
| 탐지 규칙 | DetectionRule | 단일 이상 패턴을 평가하는 함수형 인터페이스 | fds-detection-service |
| 규칙 결과 | RuleResult | 단일 규칙의 트리거 여부 + 기여 점수 + 사유 | fds-detection-service |
| 알림 | Alert | 탐지 결과(HIGH/CRITICAL)에 의해 생성된 처리 항목 | fds-alert-service |
| 알림 상태 | AlertStatus | OPEN / REVIEWING / CONFIRMED / DISMISSED | fds-alert-service |
| 거래 상태 | TransactionStatus | PENDING / APPROVED / SUSPICIOUS / BLOCKED | fds-transaction-service |
| 고액 거래 규칙 | HighAmountRule | 평균 거래금액의 5배 초과 시 스코어 40 부여 | fds-detection-service |
| 연속 거래 규칙 | RapidSuccessionRule | 5분 내 5건 이상 시 스코어 30 부여 | fds-detection-service |
| 불가능 이동 규칙 | GeoImpossibleTravelRule | 직전 거래와 900km/h 초과 이동 속도 시 스코어 50 부여 | fds-detection-service |
| 새벽 고액 규칙 | MidnightTransactionRule | 00~05시(KST) + 50만원 이상 시 스코어 25 부여 | fds-detection-service |
| 사기 유형 | FraudType | 제너레이터가 생성하는 이상 거래 시나리오 분류 (HIGH_AMOUNT, RAPID_SUCCESSION, FOREIGN_AFTER_DOMESTIC, MIDNIGHT) | fds-generator |
| 가맹점 | Merchant | 카드 결제가 발생한 사업자 | 전체 |
| 가맹점 카테고리 | MerchantCategory | CAFE, GROCERY, ONLINE, DEPARTMENT, LUXURY 등 업종 분류 | 전체 |
| 카드번호 | CardNumber (PAN) | 16자리 결제 카드 식별 번호. Kafka 전송 시 반드시 마스킹 필요 | fds-common, fds-generator |
| 슬라이딩 윈도우 | Sliding Window | 연속 시간 구간 내 이벤트 집계 방식 (Redis Sorted Set 권장) | fds-detection-service, fds-alert-service |
| 중복 알림 방지 | Alert Deduplication | Redis를 통해 동일 거래/유저에 대한 알림 중복 생성 방지 | fds-alert-service |
| 제너레이터 | Generator | 테스트용 거래 데이터를 생성/전송하는 독립 서비스 | fds-generator |
| 거래 이벤트 | TransactionEvent | Kafka `transaction-events` 토픽에 발행되는 거래 발생 메시지 | fds-common |
| 탐지 결과 이벤트 | DetectionResultEvent | Kafka `detection-results` 토픽에 발행되는 탐지 완료 메시지 | fds-common |

## 알려진 용어 불일치 (수정 필요)

| 현재 | 문제 | 권장 |
|------|------|------|
| `FraudType.FOREIGN_AFTER_DOMESTIC` | `GeoImpossibleTravelRule`과 1:1 매칭이나 의미 불일치 | FraudType도 `GEO_IMPOSSIBLE_TRAVEL`로 통일하거나, 별도 `ForeignAfterDomesticRule` 신설 |
| `GeneratorHandler` | UseCase 구현체의 표준 접미사는 `Service` | `GeneratorService`로 변경 |
| `application.port.in` | in-port 위치가 `domain.port.in` 이어야 한다는 주장과 상충 | 팀 합의 후 확정 필요 |
