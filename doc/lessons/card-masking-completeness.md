# 카드번호 마스킹 — 도입만으로는 보안이 완성되지 않는다

## 배운 점
CardMasking 유틸리티를 도입하여 Kafka 이벤트와 HTTP 전송 시 카드번호를 마스킹했지만,
다음 두 가지 경로에서 원본 카드번호가 여전히 노출될 수 있었다:

1. **toString() 미적용**: data class의 자동 생성 toString()이 원본 cardNumber를 출력
2. **원본 필드 잔존**: TransactionData.cardNumber가 String 원본을 그대로 보유하여 copy()/직렬화/equals() 등으로 유출 가능

또한 마스킹 형식 자체도 PCI-DSS v4 §3.3.1 기준에 부적합했다.
(현재: `****1234`, 표준: `123456******1234`)

## 적용 방법
민감 데이터 마스킹 도입 시 다음 체크리스트를 반드시 확인:
- [ ] 해당 필드가 toString()에서 마스킹되는가?
- [ ] 해당 필드가 직렬화(JSON, Kafka)에서 마스킹되는가?
- [ ] 해당 필드가 copy()/equals()/hashCode()에서 원본 노출되지 않는가?
- [ ] 마스킹 형식이 관련 규정(PCI-DSS 등)을 준수하는가?
- [ ] 마스킹 유틸리티에 대한 테스트가 존재하는가?

## 관련 리뷰
- [2026-04-12 ebab85d](../review/2026-04-12-ebab85d.md) — 윤지아(Domain), 정하은(Code Quality)
