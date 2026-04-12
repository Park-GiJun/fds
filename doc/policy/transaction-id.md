# transactionId 생성 책임 정책

**결정일**: 2026-04-12 (리뷰 8cfdaae #95)
**상태**: 확정

## 정책

거래 등록 API(`POST /api/v1/transactions`)의 `transactionId`는 **클라이언트가 생성하여 주입**한다. 서버는 중복 검증만 수행한다.

## 근거

- **Idempotency 보장**: 네트워크 재시도 시 동일 요청을 중복 저장하지 않기 위한 키. 서버 생성 ID로는 재시도를 식별할 수 없음.
- **가맹점 PoS 연동**: 외부 가맹점 PoS 시스템이 자체 거래번호를 생성하여 전송하는 기존 표준과 정합.
- **서버 부담 경감**: 서버 측 ID 생성기(Snowflake 등) 운영 부담 회피.

## 제약 조건

- **형식**: UUID v4 또는 가맹점 고유 포맷 (최대 36자, `@Size(max = 36)`)
- **유일성**: 시스템 전역에서 유일해야 함. DB `unique` 제약 + 애플리케이션 검증 이중 보호.
- **재사용 금지**: 일단 등록된 `transactionId`는 영구히 재사용 불가 (실패 거래 포함).
- **신뢰 경계**: 외부에서 주입되는 값이므로 요청 바디 validation에서 길이/문자 제약 강제.

## 서버 의무

1. `existsByTransactionId` 또는 unique constraint catch로 중복 검사
2. 중복 시 `DomainAlreadyExistsException` → HTTP 409 응답
3. 로그/메트릭에서 `transactionId`를 관찰 가능하게 유지 (디버깅/추적용)

## Anti-patterns

- ❌ 서버에서 `UUID.randomUUID()`로 재생성하여 클라이언트 주입값 무시
- ❌ 중복 요청에 대해 silent success 처리 (멱등성이 아닌 유실)
- ❌ `transactionId`를 auto-increment Long으로 교체

## 관련 이슈
- #88 TransactionHandler 중복 시나리오 테스트
- #93 exists+save race → unique catch 패턴 전환
