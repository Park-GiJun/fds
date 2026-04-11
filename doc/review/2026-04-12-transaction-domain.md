# Code Review — transaction-service 도메인 엔티티 (미커밋)

- **대상**: Transaction.kt, DetectionResult.kt, TransactionStatus.kt, RiskLevel.kt
- **날짜**: 2026-04-12
- **변경 파일**: 4개 (신규 작성)
- **기술 심각도**: HIGH
- **품질 심각도**: HIGH

---

## 리뷰어별 요약

| # | 이름 | 역할 | 심각도 | 이슈 수 |
|---|------|------|--------|---------|
| 1 | 강현수 | Architect | HIGH | 3 |
| 2 | 박서진 | Security | HIGH | 2 |
| 3 | 이도윤 | Performance | LOW | 1 |
| 4 | 정하은 | Code Quality | MEDIUM | 3 |
| 5 | 김태현 | Testing | MEDIUM | 2 |
| 6 | 윤지아 | Domain | HIGH | 3 |

## 주요 지적사항

### ARCH-001 [HIGH] RiskLevel 중복 정의
fds-common의 RiskLevel과 transaction-service의 RiskLevel이 동일하게 이중 정의. Kafka 역직렬화 시 ClassCastException 위험.

### ARCH-002 [HIGH] id: Long? 인프라 관심사 침투
JPA PK 개념이 순수 도메인 모델에 노출. transactionId(UUID)만으로 동일성 보장 가능.

### ARCH-003 [MEDIUM] DetectionResult 소속 재검토
detection-service가 생산하는 결과물을 transaction-service 도메인에 정의하면 MSA 경계 모호.

### BIZ-001 [HIGH] riskLevel >= HIGH — enum 순서 의존 비교 위험
enum 재정렬 시 조용히 오동작. 명시적 집합 비교(in BLOCK_LEVELS) 필요.

### BIZ-002 [MEDIUM] riskScore cap(100) 미적용
도메인 불변식 누락. require(riskScore in 0..100) 필요.

### BIZ-003 [LOW] markSuspicious() 전환 조건 미정의
호출 전제조건 문서화 또는 require() 상태 보호 필요.

### SEC-001 [MEDIUM] encryptedCardNumber 도메인 노출
암호화는 infrastructure 관심사. 도메인은 CardNumber VO나 raw 값만 보유 검토.

### TEST-001 [MEDIUM] 도메인 메서드 테스트 전무
applyDetectionResult, markSuspicious 순수 함수 테스트 즉시 가능하나 부재.

---

## Tech Lead (최민준) — HIGH
RiskLevel 이중 정의와 id 인프라 침투는 헥사고날 원칙 위반. 커밋 전 수정 필요.

## Quality Lead (한소율) — HIGH
enum 순서 의존 비교, riskScore cap 미적용은 비즈니스 정확성 결함.

## Action Items
- [ ] RiskLevel 이중 정의 해결 (fds-common에 단일화 또는 별도 결정)
- [ ] riskLevel 비교를 명시적 집합으로 교체
- [ ] riskScore 0~100 범위 검증 추가
- [ ] id: Long? 도메인에서 제거

## Tech Debt
- DetectionResult MSA 경계 재검토
- encryptedCardNumber 도메인 노출 — 암호화 책임을 infrastructure로 이전
- Instant.now() 기본값 — Clock 주입 검토
- markSuspicious() 상태 전환 보호
- 도메인 메서드 단위 테스트 작성
