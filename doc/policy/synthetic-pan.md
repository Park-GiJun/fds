# 개발/스테이징 환경 Synthetic PAN 정책

**결정일**: 2026-04-12 (리뷰 489688d #133)
**상태**: 확정

## 정책

개발(dev), 스테이징(staging), QA 환경에서는 **합성 테스트 카드번호(synthetic PAN)만** 사용해야 한다. 실제 카드번호의 전체 자리는 어떤 경로로도 유입 금지.

## 배경

현재 `PassthroughCardEncryptor`(@ConditionalOnProperty fds.crypto.passthrough.enabled=true)가 dev/staging에서 활성. 이 환경에서 실제 PAN이 저장되면 DB는 평문 상태 → PCI-DSS 3.4 위반.

## 허용되는 합성 PAN

- Luhn 유효 테스트 카드번호 (Visa: 4111111111111111, MasterCard: 5555555555554444 등)
- 내부 테스트용 임의 생성 (단 16자리 숫자 형식 유지, Luhn 유효 필수)
- fds-generator 서비스가 생성하는 FraudType 시나리오용 카드번호

## 금지

- 개발자 개인 실제 카드번호
- 운영 DB 덤프/부분 덤프
- 파트너/고객사가 제공한 실제 거래 샘플

## 책임

- **개발팀**: 로컬 개발 시 합성 PAN만 사용. 생성기 또는 테스트 fixture 활용.
- **QA팀**: 회귀 테스트 데이터를 synthetic으로 유지. 실제 PAN 반입 요청 거부.
- **운영팀**: prod → staging 데이터 복제 시 PAN 컬럼 마스킹 필수 (pg_dump post-processing 또는 전용 anonymizer 스크립트).

## 관련 이슈
- #86 CardEncryptor 포트 도입
- #118 PassthroughCardEncryptor opt-in 전환
- #120 Transaction 도메인 모델 cardNumber 원문 필드 제거
