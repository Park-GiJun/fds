# FDS Policy Documents

프로젝트 운영·보안·아키텍처 정책 일람. 각 문서는 결정 시점에 확정된 원칙을 기록하며, 변경 시 PR 리뷰를 거친다.

## 보안
- [synthetic-pan.md](synthetic-pan.md) — 개발/스테이징 환경 합성 카드번호 사용 정책
- [kms-encryption.md](kms-encryption.md) — CardEncryptor KMS 구현체 설계 가이드 (Envelope Encryption + DEK 캐싱)
- [log-masking-performance.md](log-masking-performance.md) — Logback PAN 마스킹 성능 최적화 옵션

## 도메인
- [transaction-id.md](transaction-id.md) — transactionId 생성 책임 정책 (클라이언트 주입 + idempotency)

## 아키텍처
- [api-response.md](api-response.md) — HTTP Response 래퍼 전략 (CommonApiResponse → 서비스별 ApiResponse 전환 계획)

## 문서 추가 규칙
- 파일명: 소문자 + 하이픈 (kebab-case), `.md` 확장자
- 상단에 "결정일" / "상태" 필수
- "결정 / 근거 / 제약 / 관련 이슈" 섹션 권장
- 신규 문서 추가 시 본 INDEX.md에 카테고리별 추가
