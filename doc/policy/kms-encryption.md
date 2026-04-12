# CardEncryptor KMS 구현체 설계 가이드

**작성일**: 2026-04-12 (리뷰 489688d #126)
**상태**: 설계 가이드 — 실제 구현 전 참조

## 배경

현재 `PassthroughCardEncryptor`(@ConditionalOnProperty)가 dev/staging에서 활성. prod 배포를 위해서는 실제 KMS(AWS KMS, HashiCorp Vault Transit 등) 기반 `CardEncryptor` 구현체가 필요하다. 본 문서는 10K TPS 목표를 충족하기 위한 설계 제약을 정리한다.

## 성능 목표

- **처리량**: 10,000 TPS (transaction-service 목표)
- **지연**: encrypt() p99 < 5ms (저장 경로 전체 목표 p99 100ms 중 5% 예산)
- **KMS 호출 비용**: AWS KMS Encrypt API는 호출당 요금 발생 ($0.03/10K calls) — raw encrypt 호출 시 월간 비용 무시 못함

## 설계 원칙

### 1. Envelope Encryption + DEK 캐싱

- **KEK**(Key Encryption Key): KMS 내부 관리, 회전 자동
- **DEK**(Data Encryption Key): 로컬에서 생성하거나 KMS `GenerateDataKey` 1회 호출 후 메모리 캐시
- **암호화**: DEK로 AES-256-GCM 로컬 암호화 (KMS 호출 없음)
- **DEK TTL**: 1시간 + 최대 100만 건 사용 → 새 DEK 요청

```kotlin
class KmsCardEncryptor(
    private val kmsClient: KmsClient,
    private val keyId: String,
) : CardEncryptor {
    private val dekCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofHours(1))
        .maximumSize(1)
        .build<String, CachedDek>()

    override fun encrypt(plain: String): String {
        val dek = dekCache.get(keyId) { requestNewDek() }
        return aesGcmEncrypt(dek, plain).toBase64()
    }
    // ...
}
```

### 2. Fail-fast 기동 검증

- 기동 시 KMS 연결 + 1회 `GenerateDataKey` 호출로 권한/네트워크 검증
- 실패 시 기동 중단 (health check FAIL)
- prod 배포 시 `fds.crypto.passthrough.enabled=false` 필수 + KMS 구현체 @Primary 등록

### 3. 재시도 + Circuit Breaker

- KMS 호출(`GenerateDataKey`) 실패 시 3회 재시도(Exponential backoff)
- 실패 지속 시 Circuit Breaker Open → 기존 DEK 만료 전까지 암호화는 성공, 만료 후 모든 요청 차단
- SLA: KMS 장애 시 최대 1시간(DEK TTL) 서비스 지속

### 4. 멀티리전 고려

- prod 배포 시 regional KMS 엔드포인트 사용 (DR용 cross-region replica key)
- DEK 캐시는 리전별로 독립

## 반영해야 할 Tech Debt
- RTD-6: KMS key caching — 본 문서 설계 기준 구현
- 기존 Caffeine 도입 이력(RateLimitFilter)과 일관성 있게 사용

## 선택지 비교

| 전략 | encrypt() 지연 | KMS 호출량 | 비용 | 복잡도 |
|------|---------------|-----------|------|-------|
| Direct KMS Encrypt | 10-30ms | 1/tx | 높음 | 낮음 |
| DEK Cache (1시간) | < 1ms | ~10/hour | 낮음 | 중간 |
| Static Key (KMS only on startup) | < 1ms | 1/boot | 매우 낮음 | 높음(회전 불가) |

→ **DEK Cache 선택**: 지연/비용 최적, 회전 지원, 구현 복잡도 수용 가능.

## 구현 대상 파일 (예정)

- `application/port/outbound/CardEncryptor.kt` (기존 유지)
- `infrastructure/adapter/outbound/crypto/KmsCardEncryptor.kt` (신규, @Primary, prod 조건)
- `infrastructure/adapter/outbound/crypto/CachedDek.kt` (신규)
- `infrastructure/config/KmsConfig.kt` (신규, KmsClient + keyId 주입)

## 관련 이슈
- #86 CardEncryptor 포트 도입
- #118 PassthroughCardEncryptor opt-in
- RTD-6 KMS key caching — 본 문서 기준 구현
