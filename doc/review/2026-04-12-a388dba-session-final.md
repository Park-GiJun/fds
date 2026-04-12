# Session-end Review — 489688d..a388dba

**범위**: 21 PR (#134~#155), +948 / -87, 33 files
**목적**: 3회차 remediation 검증 — 이전 리뷰 지적사항 해소 여부 + 새 regression 탐지

---

## 해결 검증 (이전 리뷰 Action Items)

| ID | 설명 | 결과 |
|----|------|------|
| RF-1 logback 복원 | ✅ 해결 (#146) |
| RF-2 PersistenceAdapter 통합 테스트 | 🟡 **@Disabled 스캐폴드** (#153) — 의존성 미추가 |
| RF-3 WebAdapter MockMvc | ✅ 5건 (#152) |
| RF-4 Passthrough opt-in | 🟡 **부분 해결** (#141) — 아래 ACT-1 참조 |
| RF-5 CLAUDE.md @Transactional 예외 | ✅ (#135) |
| RF-6 Transaction.cardNumber 원문 제거 | ✅ (#150) — 단, 포트 시그니처에 새 누출 경로 발생 |
| RTD 13건 | 모두 PR #135~#149로 반영 |

**전체 달성도**: 19/19 이슈 merged. 단, 2건은 의도와 결과가 다름 (아래 HIGH 참조).

---

## 신규 발견

### 🔴 HIGH

**[ACT-1] PassthroughCardEncryptor fail-open 기본값**
`application.yml:17` — `FDS_CRYPTO_PASSTHROUGH_ENABLED:true` (기본값 true).
이슈 #118 본문은 "fail-safe 강화 — 명시적 opt-in"을 요구했으나 에이전트가 "dev 편의상 default true"로 설정. **이슈 의도 역행**. prod에서 환경변수를 명시적으로 `false`로 두지 않으면 여전히 평문 저장 경로 활성. 의도는 반대여야 함: 기본 false, 로컬 개발에서만 명시적 true.

**[ACT-2] `TransactionPersistencePort.save(tx, plainCardNumber)` 시그니처 누출**
`#150`에서 Transaction 도메인 모델의 `cardNumber` 원문 필드는 제거했으나, 포트 메서드에 `plainCardNumber: String`을 별도 파라미터로 추가하여 **plaintext PAN이 application→infrastructure 포트 경계를 통과**. 도메인 모델에서는 제거했지만 API 경계에서는 노출. 개선 방향:
- Handler에서 `CardEncryptor.encrypt(plainCardNumber)` 호출 후 ciphertext만 포트로 전달
- 또는 `EncryptedCardNumber` 타입(seald class/VO)을 정의하여 타입 수준에서 plaintext 금지
- 또는 `Transaction` 도메인에 `encryptedCardNumber` 필드를 추가해 전체 생명주기에서 encrypted 상태 유지

**[ACT-3] `#116 @Disabled` 스캐폴드**
`spring-boot-starter-test` 의존성 1줄 추가로 활성화 가능한데 @Disabled 상태로 master 반영. RF-2 실질 미해결. 후속 이슈로 `build.gradle.kts` 수정 + 활성화 필요.

### 🟠 MEDIUM

**[T1] `handleBeanValidation` 필드 목록 길이 제한 없음**
`GlobalExceptionHandler.kt` — `fieldErrors.joinToString(", ") { it.field }`. 필드 5개+ 실패 시 응답 메시지가 길어지고 API 스키마 노출 표면 확대. 최대 3개 + `...` 제한 권장.

**[T2] `CardNumberMaskingConverter` 예외 포맷 Logback 기본 비호환**
`#145` 구현이 throwable을 인라인 렌더링하며 Logback의 `%wEx{full, reason, message}` 같은 표준 포맷 옵션을 우회. 디버깅 시 코드 위치 정보 손실 가능. 테스트에는 반영 안 됨 (#128은 `throwableProxy = null` 경우만 커버).

**[T3] Command DTO 자체 검증 부재**
`RegisterTransactionCommand` — web Request 레이어의 Bean Validation에만 의존. 비-web 호출자(Kafka Consumer, 내부 스케줄러 등)가 Command를 직접 생성하면 검증 우회. `init { require(...) }` 추가 또는 factory 전용화 권장.

**[T4] Transaction.create 호출부 String 유지 — VO 효과 제한**
`#151`로 Transaction 도메인 필드가 `CurrencyCode`/`CountryCode`가 됐으나 `Transaction.create(currency = String, country = String)`로 내부 래핑. 호출자가 잘못된 값을 넣으면 여전히 runtime require()로 실패. Command/Request 레벨부터 VO 사용 또는 팩토리 오버로드 추가 권장.

### 🟢 LOW

**[T5] doc/policy 인덱스 부재**
`doc/policy/` 하위에 `api-response.md`, `kms-encryption.md`, `log-masking-performance.md`, `synthetic-pan.md`, `transaction-id.md` 5건. 진입점 INDEX.md로 일람 제공 권장.

---

## 긍정 변화

- **테스트 커버리지**: 0개 → **36개** (Handler 4 / WebAdapter 5 / GlobalExceptionHandler 8 / MaskConverter 7 / VO 12)
- **카드번호 vector**: 저장 경로 + 도메인 필드 양방향 차단
- **컨벤션 명문화**: `{Domain}{Infra}Port`, UseCase/DTO 분리, @Transactional 예외, Passthrough opt-in 원칙 모두 CLAUDE.md 반영
- **정책 문서화**: transactionId / synthetic-pan / api-response / kms-encryption / log-masking-performance 정책 5건 수립

---

## 반복 실수 패턴 갱신

- **"Remediation creates new debt"**: **3회차 지속**. 이번 라운드에서 ACT-1 (fail-open default) + ACT-2 (포트 plaintext 누출) 신규 유입. ⚠️ **3-cycle 고착**.
- **🆕 "Agent interpretation drift"**: 이슈 #118 본문은 "명시적 opt-in (fail-closed)"을 명시했으나 에이전트가 "dev 편의상 default true"로 구현. 프롬프트에 의도 명시했어도 에이전트가 재해석. **에이전트 프롬프트에 기본값까지 직접 지정 필요**.
- **🆕 "Dead scaffold shipping"**: #116이 @Disabled 스캐폴드로 shipping. 의존성 1줄로 활성화 가능한데 defer. **scaffold 패턴은 실질 가치 0**.
- **"신규 계층 테스트 0건"**: ✅ **해결** — 이번 라운드로 36건 추가됐고 GlobalExceptionHandler/Converter/VO/Handler/WebAdapter 전부 커버.

---

## 리드 종합

### 최민준 — Tech Lead: **MEDIUM**
- CRITICAL 영역(카드번호 평문)은 거의 차단됐으나 ACT-1 fail-open default가 같은 risk 재도입 위험
- ACT-2 포트 plaintext 누출은 도메인 모델 정화 작업의 불완전한 적용
- "3-cycle 고착" 패턴이 가장 우려되는 signal — **추가 이슈 발행보다 remediation 정지 + 구조 검토**가 필요

### 한소율 — Quality Lead: **LOW → MEDIUM** (상향)
- 테스트 커버리지 급상승은 긍정적
- 그러나 "agent interpretation drift"와 "dead scaffold shipping" 2종 신규 품질 패턴 발생
- lessons.md에 "agent 프롬프트의 명시성"이라는 새 주제 추가 필요

---

## Action Items

| ID | 영역 | 설명 | 심각도 |
|----|------|------|--------|
| AF-1 | Security | `FDS_CRYPTO_PASSTHROUGH_ENABLED` 기본값 false + 로컬 dev 프로파일에서만 true | HIGH |
| AF-2 | Architecture | `TransactionPersistencePort.save` 시그니처 plaintext 파라미터 제거 — Handler에서 pre-encrypt | HIGH |
| AF-3 | Testing | `spring-boot-starter-test` 의존성 추가 + `#116` 테스트 활성화 | HIGH |

## Tech Debt

| ID | 영역 | 설명 | 심각도 |
|----|------|------|--------|
| TF-1 | Security | `handleBeanValidation` 필드 목록 최대 3개 제한 | MEDIUM |
| TF-2 | Observability | `CardNumberMaskingConverter` throwable 포맷 Logback 호환성 | MEDIUM |
| TF-3 | Architecture | `RegisterTransactionCommand` init require() 검증 추가 | MEDIUM |
| TF-4 | Architecture | `Transaction.create` 호출부 VO 사용 강제 | MEDIUM |
| TF-5 | Docs | `doc/policy/INDEX.md` 추가 | LOW |

## 판정 요약

- **기술 (Tech Lead)**: MEDIUM (remediation 3-cycle 고착 우려)
- **품질 (Quality Lead)**: MEDIUM (테스트 약진 vs agent drift)
- **Action Items**: 3건
- **Tech Debt**: 5건
- **총 8건** (이전 리뷰 대비 19 → 8로 축소 — 고착 사이클 차단 시도)
