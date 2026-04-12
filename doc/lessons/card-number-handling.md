# 카드번호 취급 체크리스트

> 리뷰 8cfdaae (2026-04-12) — 4회차 카드번호 관련 보안 이슈 재발에 따른 학습 기록

## 배경
카드번호(PAN)는 PCI-DSS 3.4에 따라 저장/전송/로그 모든 경로에서 보호 필요. 본 프로젝트에서는 다음 4회 리뷰에서 반복 지적되었음:

1. **Baseline (2026-04-11)** — SEC-001: Kafka 이벤트에 원문 전송
2. **1차 리뷰** — TransactionData 내부 필드 원문 잔존
3. **2차 리뷰** — CardMasking 4-4-4 포맷이 PCI-DSS 규격(앞6+뒤4) 불일치
4. **4차 리뷰 (8cfdaae)** — `TransactionPersistenceAdapter`가 `encryptedCardNumber = transaction.cardNumber`로 **평문 저장** (컬럼명은 `encrypted_card_number`로 위장)

## 체크리스트 — 카드번호가 닿는 모든 경로

### 전송 (Network)
- [ ] 외부 요청 바디 수신: Bean Validation은 `@Size`만으로 불충분 — `@Pattern("^[0-9]{13,19}$")` 명시
- [ ] Kafka 이벤트 발행 전 마스킹 처리 (`CardMasking.mask()`)
- [ ] 다운스트림 서비스 호출 시 마스킹 또는 토큰화

### 저장 (Persistence)
- [ ] DB 컬럼 이름과 실제 저장 값 일치 확인 — `encrypted_*` 컬럼에 평문 유입 금지
- [ ] `fromDomain(domain, encryptedCardNumber = ...)` 같은 생성 경로는 **Encryptor 포트 경유 강제**
- [ ] 마이그레이션 시 기존 평문 데이터 일괄 암호화 계획 수립

### 로깅 (Observability)
- [ ] Logback Filter에서 PAN 패턴 마스킹 (정규식 `\b\d{13,19}\b`)
- [ ] Spring Web 예외 로그 바디 출력 억제 또는 마스킹
- [ ] MDC에 PAN 금지

### 표현 (Response)
- [ ] Response DTO는 `maskedCardNumber`만 노출 — domain `cardNumber` 필드 직렬화 금지
- [ ] 도메인 모델이 HTTP에 직접 노출되지 않도록 별도 DTO 유지

## 아키텍처 패턴 — `CardEncryptor` 아웃포트

권장 구조:
```
application/port/outbound/CardEncryptor.kt
  interface CardEncryptor { fun encrypt(plain: String): String; fun decrypt(cipher: String): String }

infrastructure/adapter/outbound/crypto/KmsCardEncryptor.kt
  @Component class KmsCardEncryptor(...) : CardEncryptor { ... }

infrastructure/adapter/outbound/persistence/.../TransactionPersistenceAdapter.kt
  class TransactionPersistenceAdapter(
    private val repo: TransactionJpaRepository,
    private val encryptor: CardEncryptor,
  ) : TransactionRepository {
    override fun save(tx: Transaction) = repo.save(
      TransactionEntity.fromDomain(tx, encryptedCardNumber = encryptor.encrypt(tx.cardNumber))
    ).toDomain()
  }
```

이 구조를 강제하면 "잠시 평문으로" 같은 임시 경로가 아예 막힘.

## 프로세스 개선 제안

- `/ship --merge` Gate Keeper에 `cardNumber` 문자열이 `encryptedCardNumber =` 우변에 나타나는 경우 자동 blocking grep 추가
- `/review` 체크리스트 Security 섹션에 "PAN 저장 경로 암호화 증명" 항목 추가
- `/test` 커맨드 실행 시 `CardEncryptor` 모킹 여부 점검

## 근본 원인 분석
4회차 재발의 공통 원인은 **"카드번호가 보호되어야 한다는 것은 알지만, 도메인 모델/엔티티 경계 중 어디서 보호해야 하는지 경계가 매번 다르게 그어진다"**는 점. 도메인 모델은 `cardNumber`(원문)를 보유하고, 마스킹 필드는 별도로 갖는 하이브리드 구조 때문에 계층마다 "여기선 원문이 허용되지 않나?"라는 판단이 반복됨.

**해결 방향**: `Transaction` 도메인 모델에서 `cardNumber` 원문 필드 제거, 대신 `CardNumber` VO(이미 존재) 또는 `encrypted + masked`만 보유. 저장/전송 어느 경로에서도 원문에 접근할 수 없는 구조로 전환.
