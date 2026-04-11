# denyAll(403) vs Unauthorized(401) — Spring Security 상태 코드 구분

## 배운 점
Spring Security에서 `denyAll()`은 **403 Forbidden**을 반환하고,
미인증 요청은 `AuthenticationEntryPoint`가 **401 Unauthorized**를 반환한다.

두 개념을 혼동하면 테스트 단언이 모호해진다:
- 미인증 → `httpBasic` 진입점이 401을 먼저 반환 (인증 자체가 안 됨)
- 인증은 됐으나 권한 없음 → `AccessDeniedHandler`가 403 반환

현재 테스트는 미인증 401만 검증하므로 통과하지만,
인증 후 권한 분리(ADMIN vs USER)가 추가되면 403 검증도 필요하다.

## 적용 방법
보안 설정 테스트 작성 시:
- [ ] 미인증 → 401 확인
- [ ] 인증 + 권한 없음 → 403 확인
- [ ] 인증 + 권한 있음 → 200 확인
- [ ] denyAll 경로 + 인증됨 → 403 확인 (401이 아님)

## 관련 리뷰
- [2026-04-12 bdb802b](../review/2026-04-12-bdb802b.md) — 강현수(Architect), 정하은(Code Quality)
