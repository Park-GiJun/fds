# HTTP Response 래퍼 전략

**결정일**: 2026-04-12 (리뷰 489688d #123)
**상태**: 잠정 확정 — 서비스별 분리 진행, 전환 완료 시점에 재검토

## 현황

- `fds-common/web/CommonApiResponse` — 과거 공유 래퍼 (이제 migration 중)
- `fds-transaction-service/.../web/response/ApiResponse` — 2026-04-12 #96에서 분리된 로컬 래퍼
- `fds-alert-service`, `fds-detection-service`, `fds-gateway` — 아직 `CommonApiResponse` 사용

## 결정

1. **신규 서비스 구현은 로컬 `ApiResponse`를 채택**한다. fds-common/web 의존 금지.
2. **기존 서비스는 해당 서비스의 첫 번째 WebAdapter 도입 시점에 분리**한다. 미리 마이그레이션 강제하지 않음.
3. **fds-common/web/CommonApiResponse는 deprecated로 표시하되 즉시 제거하지 않는다**. 알림은 주석으로만.

## 근거

- fds-common은 Kafka 이벤트 스키마 전용이어야 함 (project-context 명시). HTTP 응답 래퍼는 infrastructure 관심사.
- 동시에 alert/detection/gateway의 기존 구현체를 즉시 마이그레이션하면 8+개 파일 변경 + 테스트 연쇄 수정 → 기회비용 대비 이득 적음.
- 각 서비스가 WebAdapter를 본격 구현하는 시점에 자연스럽게 분리되는 것이 ROI 높음.

## 마이그레이션 체크리스트 (각 서비스)

- [ ] `infrastructure/adapter/inbound/web/response/ApiResponse.kt` 생성
- [ ] WebAdapter / ExceptionHandler import 변경
- [ ] 기존 `CommonApiResponse` import 제거
- [ ] 컴파일 + 테스트 통과 확인
- [ ] fds-common 의존성 영향 범위 확인

## 완료 조건

모든 서비스(alert, detection, gateway, transaction, generator)가 로컬 `ApiResponse`로 전환되면 `CommonApiResponse`를 fds-common에서 삭제한다.

## 관련 이슈
- #96 transaction-service 분리
- #124 GlobalExceptionHandler 로컬 ApiResponse 전환
