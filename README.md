# FDS (Fraud Detection System) - 이상거래 탐지 시스템

실시간 카드/결제 트랜잭션을 수집하고 이상 거래를 탐지하는 MSA 기반 백엔드 시스템

---

## 1. 프로젝트 개요

### 1.1 목표

- 초당 수만 건의 거래 데이터를 실시간으로 수집/분석하여 이상 거래를 탐지
- MSA + 이벤트 기반 아키텍처로 서비스 간 느슨한 결합
- 헥사고날 아키텍처(Port & Adapter)로 도메인 로직과 인프라를 분리
- k6 부하 테스트로 처리량과 응답 속도를 정량적으로 측정

### 1.2 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Kotlin 2.2, Java 24 |
| Framework | Spring Boot 4.0, Spring Cloud 2025.1 |
| Build | Gradle (Kotlin DSL), buildSrc |
| Database | PostgreSQL 17, JPA/Hibernate |
| Cache | Redis 7 |
| Search | Elasticsearch 8.17 |
| Messaging | Apache Kafka 4.0 (KRaft) |
| HTTP Client | Ktor Client 3.1 |
| API Gateway | Spring Cloud Gateway Server WebMVC |
| Service Discovery | Netflix Eureka |
| Circuit Breaker | Resilience4j |
| DB Migration | Flyway |
| API Docs | SpringDoc OpenAPI 3.0 (Swagger UI) |
| Observability | Micrometer + Prometheus + Zipkin |
| Test | k6 (부하 테스트), JUnit 5 |

---

## 2. 시스템 아키텍처

### 2.1 전체 시스템 구성도

```
                         ┌──────────────┐
                         │   k6 / 부하   │
                         │   테스트      │
                         └──────┬───────┘
                                │
                                ▼
┌──────────────┐       ┌──────────────┐
│  Generator   │──────▶│   Gateway    │
│  :8090       │       │   :8080      │
└──────────────┘       └──────┬───────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
     ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
     │ Transaction  │ │  Detection   │ │    Alert     │
     │ Service      │ │  Service     │ │   Service    │
     │ :8081        │ │  :8082       │ │   :8083      │
     └──────┬───────┘ └──────┬───────┘ └──────┬───────┘
            │                │                │
     ┌──────┴──┐      ┌─────┴──┐       ┌─────┴──────┐
     │ PG | ES │      │ Redis  │       │ PG | Redis │
     └─────────┘      └────────┘       └────────────┘
            │                │                │
            └────── Kafka ───┴────────────────┘

                    ┌──────────────┐
                    │   Eureka     │
                    │   :8761      │
                    └──────────────┘
```

### 2.2 데이터 흐름

```
1. Generator/k6 → Gateway → Transaction Service
   - 거래 데이터 수집
   - PostgreSQL에 거래 원본 저장
   - Elasticsearch에 검색용 인덱싱
   - Kafka [transaction-events] 토픽으로 이벤트 발행

2. Transaction Service → Kafka → Detection Service
   - [transaction-events] 컨슈밍
   - Redis에서 유저별 최근 거래 패턴 조회
   - 규칙 엔진으로 이상 여부 판정
   - 판정 결과를 Redis에 캐싱
   - Kafka [detection-results] 토픽으로 결과 발행

3. Detection Service → Kafka → Alert Service
   - [detection-results] 컨슈밍
   - 위험도(HIGH/CRITICAL)에 따라 알림 생성
   - Redis Sliding Window로 알림 중복 방지
   - PostgreSQL에 알림 이력 저장
```

### 2.3 포트 구성

| 서비스 | 포트 | 설명 |
|--------|------|------|
| fds-eureka-server | 8761 | Service Discovery |
| fds-gateway | 8080 | API Gateway |
| fds-transaction-service | 8081 | 거래 수집/저장 |
| fds-detection-service | 8082 | 이상 탐지 |
| fds-alert-service | 8083 | 알림 처리 |
| fds-generator | 8090 | 테스트 데이터 생성 |
| PostgreSQL | 5432 | RDB |
| Redis | 6379 | Cache |
| Kafka | 9092 | Message Broker |
| Elasticsearch | 9200 | 검색 엔진 |
| Kibana | 5601 | ES 대시보드 |
| Zipkin | 9411 | 분산 추적 |
| Kafka UI | 9090 | Kafka 모니터링 |

---

## 3. 모듈 구조

### 3.1 멀티모듈 의존성

```
fds/
├── buildSrc/                  # 버전/플러그인 중앙 관리
├── fds-common/                # 공통 DTO, Kafka 이벤트 스키마
├── fds-eureka-server/         # Eureka Server (독립)
├── fds-gateway/               # API Gateway (독립)
├── fds-transaction-service/   # → fds-common
├── fds-detection-service/     # → fds-common
├── fds-alert-service/         # → fds-common
├── fds-generator/             # 독립 (common 미사용, 자체 모델)
├── docker-compose.yml
└── infra/
```

### 3.2 모듈별 기술 의존성

| | JPA | Kafka | Redis | ES | Ktor | Flyway |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| transaction-service | O | Producer | - | O | O | O |
| detection-service | - | Consumer/Producer | O | - | O | - |
| alert-service | O | Consumer | O | - | O | O |
| generator | - | - | - | - | O | - |
| gateway | - | - | - | - | - | - |

---

## 4. 헥사고날 아키텍처 (Port & Adapter)

### 4.1 패키지 구조 (각 서비스 공통)

```
com.gijun.fds.{service}/
├── domain/                          # 순수 Kotlin (Spring/인프라 의존 없음)
│   ├── model/                       # 도메인 모델 (Entity, VO, Enum)
│   └── port/
│       ├── in/                      # Inbound Port (UseCase 인터페이스)
│       └── out/                     # Outbound Port (외부 시스템 인터페이스)
│
├── application/                     # UseCase 구현체
│   └── {Feature}Service.kt         # port.in 구현, port.out만 의존
│
├── infrastructure/
│   ├── adapter/
│   │   ├── in/
│   │   │   └── web/                 # Controller (@RestController)
│   │   └── out/
│   │       ├── persistence/         # JPA Repository, Entity 매핑
│   │       ├── messaging/           # Kafka Producer/Consumer
│   │       ├── cache/               # Redis 구현체
│   │       ├── search/              # Elasticsearch 구현체
│   │       └── client/              # Ktor HTTP Client
│   └── config/                      # Spring Bean 설정, Properties
│
└── {Service}Application.kt
```

### 4.2 의존 방향 규칙

```
infrastructure.adapter.in  ──▶  domain.port.in  ◀──  application
infrastructure.adapter.out ◀──  domain.port.out ◀──  application

    [Infrastructure]          [Domain]           [Application]
    Spring, JPA, Kafka        순수 Kotlin         UseCase 구현
    Redis, ES, Ktor           인터페이스만          Port만 의존
```

**절대 규칙:**
- `domain` 패키지는 Spring, JPA, Kafka 등 외부 프레임워크를 import하지 않는다
- `application`은 `domain.port.out` 인터페이스만 의존하고, 구현체(infrastructure)를 모른다
- `infrastructure.adapter.in.web`(Controller)은 `domain.port.in`(UseCase)만 의존한다

---

## 5. 서비스별 상세 설계

---

### 5.1 fds-common

공통 Kafka 이벤트 스키마. 모든 비즈니스 서비스가 의존한다.

#### Kafka 이벤트

```kotlin
// TransactionEvent — 거래 발생 이벤트
data class TransactionEvent(
    val transactionId: String,
    val userId: String,
    val cardNumber: String,       // 마스킹 처리 필요
    val amount: BigDecimal,
    val currency: String,         // KRW, USD
    val merchantName: String,
    val merchantCategory: String, // CAFE, GROCERY, ONLINE, ...
    val country: String,          // KR, US, JP, ...
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Instant,
)

// DetectionResultEvent — 탐지 결과 이벤트
data class DetectionResultEvent(
    val detectionId: String,
    val transactionId: String,
    val userId: String,
    val riskLevel: RiskLevel,     // LOW, MEDIUM, HIGH, CRITICAL
    val ruleNames: List<String>,  // 적용된 규칙 목록
    val riskScore: Int,           // 0~100
    val timestamp: Instant,
)
```

#### Kafka 토픽

| 토픽 | Producer | Consumer | 설명 |
|------|----------|----------|------|
| `transaction-events` | Transaction Service | Detection Service | 거래 발생 이벤트 |
| `detection-results` | Detection Service | Alert Service | 탐지 결과 이벤트 |

---

### 5.2 fds-transaction-service

거래 데이터를 수집, 저장, 검색하는 서비스.

#### 헥사고날 구조

```
domain/
├── model/
│   └── Transaction.kt              # 도메인 모델
├── port/
│   ├── in/
│   │   ├── CreateTransactionUseCase.kt
│   │   └── SearchTransactionUseCase.kt
│   └── out/
│       ├── SaveTransactionPort.kt         # DB 저장
│       ├── IndexTransactionPort.kt        # ES 인덱싱
│       └── PublishTransactionEventPort.kt # Kafka 발행
application/
├── CreateTransactionService.kt
└── SearchTransactionService.kt
infrastructure/
├── adapter/
│   ├── in/web/
│   │   └── TransactionController.kt
│   └── out/
│       ├── persistence/
│       │   ├── TransactionJpaEntity.kt
│       │   ├── TransactionJpaRepository.kt
│       │   └── TransactionPersistenceAdapter.kt
│       ├── search/
│       │   ├── TransactionDocument.kt
│       │   └── TransactionSearchAdapter.kt
│       └── messaging/
│           └── TransactionKafkaProducer.kt
└── config/
    ├── KafkaProducerConfig.kt
    └── ElasticsearchConfig.kt
```

#### 도메인 모델

```kotlin
// domain/model/Transaction.kt
data class Transaction(
    val id: String,
    val userId: String,
    val cardNumber: String,
    val amount: BigDecimal,
    val currency: String,
    val merchantName: String,
    val merchantCategory: String,
    val country: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val status: TransactionStatus,
    val createdAt: Instant,
)

enum class TransactionStatus {
    PENDING,    // 수집됨, 아직 탐지 전
    APPROVED,   // 정상 판정
    SUSPICIOUS, // 이상 의심
    BLOCKED,    // 차단
}
```

#### Inbound Port (UseCase)

```kotlin
// domain/port/in/CreateTransactionUseCase.kt
interface CreateTransactionUseCase {
    fun create(command: CreateTransactionCommand): Transaction
}

data class CreateTransactionCommand(
    val userId: String,
    val cardNumber: String,
    val amount: BigDecimal,
    val currency: String,
    val merchantName: String,
    val merchantCategory: String,
    val country: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
)

// domain/port/in/SearchTransactionUseCase.kt
interface SearchTransactionUseCase {
    fun searchByUserId(userId: String, page: Int, size: Int): Page<Transaction>
    fun searchByKeyword(keyword: String, page: Int, size: Int): Page<Transaction>
    fun searchByFilters(filters: TransactionSearchFilters): Page<Transaction>
}

data class TransactionSearchFilters(
    val userId: String? = null,
    val merchantCategory: String? = null,
    val country: String? = null,
    val minAmount: BigDecimal? = null,
    val maxAmount: BigDecimal? = null,
    val startDate: Instant? = null,
    val endDate: Instant? = null,
    val page: Int = 0,
    val size: Int = 20,
)
```

#### Outbound Port

```kotlin
// domain/port/out/SaveTransactionPort.kt
interface SaveTransactionPort {
    fun save(transaction: Transaction): Transaction
    fun findById(id: String): Transaction?
    fun findByUserId(userId: String, page: Int, size: Int): Page<Transaction>
}

// domain/port/out/IndexTransactionPort.kt
interface IndexTransactionPort {
    fun index(transaction: Transaction)
    fun search(keyword: String, page: Int, size: Int): Page<Transaction>
    fun searchByFilters(filters: TransactionSearchFilters): Page<Transaction>
}

// domain/port/out/PublishTransactionEventPort.kt
interface PublishTransactionEventPort {
    fun publish(transaction: Transaction)
}
```

#### Application (UseCase 구현)

```kotlin
// application/CreateTransactionService.kt
@Service
class CreateTransactionService(
    private val saveTransactionPort: SaveTransactionPort,
    private val indexTransactionPort: IndexTransactionPort,
    private val publishTransactionEventPort: PublishTransactionEventPort,
) : CreateTransactionUseCase {

    override fun create(command: CreateTransactionCommand): Transaction {
        // 1. 도메인 모델 생성
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            // ... command → Transaction 매핑
            status = TransactionStatus.PENDING,
            createdAt = Instant.now(),
        )

        // 2. DB 저장
        val saved = saveTransactionPort.save(transaction)

        // 3. ES 인덱싱 (비동기도 가능)
        indexTransactionPort.index(saved)

        // 4. Kafka 이벤트 발행
        publishTransactionEventPort.publish(saved)

        return saved
    }
}
```

#### DB 스키마 (Flyway)

```sql
-- V1__create_transactions.sql
CREATE TABLE transactions (
    id              VARCHAR(36)    PRIMARY KEY,
    user_id         VARCHAR(36)    NOT NULL,
    card_number     VARCHAR(20)    NOT NULL,
    amount          DECIMAL(18,2)  NOT NULL,
    currency        VARCHAR(3)     NOT NULL,
    merchant_name   VARCHAR(100)   NOT NULL,
    merchant_category VARCHAR(30)  NOT NULL,
    country         VARCHAR(5)     NOT NULL,
    city            VARCHAR(50)    NOT NULL,
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- 인덱스
    CONSTRAINT chk_status CHECK (status IN ('PENDING','APPROVED','SUSPICIOUS','BLOCKED'))
);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_country ON transactions(country);
```

#### ES 인덱스 매핑

```json
{
  "mappings": {
    "properties": {
      "transactionId":    { "type": "keyword" },
      "userId":           { "type": "keyword" },
      "amount":           { "type": "double" },
      "currency":         { "type": "keyword" },
      "merchantName":     { "type": "text", "fields": { "keyword": { "type": "keyword" } } },
      "merchantCategory": { "type": "keyword" },
      "country":          { "type": "keyword" },
      "city":             { "type": "keyword" },
      "location":         { "type": "geo_point" },
      "status":           { "type": "keyword" },
      "createdAt":        { "type": "date" }
    }
  }
}
```

#### API 스펙

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/v1/transactions` | 거래 생성 |
| GET | `/api/v1/transactions/{id}` | 거래 단건 조회 |
| GET | `/api/v1/transactions?userId=&page=&size=` | 유저별 거래 목록 |
| GET | `/api/v1/transactions/search?keyword=&page=&size=` | 키워드 검색 (ES) |
| GET | `/api/v1/transactions/search/filters` | 필터 검색 (ES) |

**POST /api/v1/transactions Request Body:**
```json
{
  "userId": "USER_00001",
  "cardNumber": "4123456789012345",
  "amount": 55000,
  "currency": "KRW",
  "merchantName": "스타벅스",
  "merchantCategory": "CAFE",
  "country": "KR",
  "city": "서울",
  "latitude": 37.5665,
  "longitude": 126.978
}
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "USER_00001",
  "cardNumber": "4123****2345",
  "amount": 55000,
  "currency": "KRW",
  "merchantName": "스타벅스",
  "merchantCategory": "CAFE",
  "country": "KR",
  "city": "서울",
  "status": "PENDING",
  "createdAt": "2026-04-11T10:30:00Z"
}
```

---

### 5.3 fds-detection-service

거래 이벤트를 소비하여 이상 여부를 판정하는 서비스.

#### 헥사고날 구조

```
domain/
├── model/
│   ├── DetectionResult.kt
│   ├── DetectionRule.kt             # 탐지 규칙 인터페이스
│   └── UserBehaviorProfile.kt      # 유저 행동 패턴
├── port/
│   ├── in/
│   │   ├── DetectTransactionUseCase.kt
│   │   └── GetDetectionResultUseCase.kt
│   └── out/
│       ├── LoadUserBehaviorPort.kt        # Redis에서 유저 패턴 조회
│       ├── SaveUserBehaviorPort.kt        # Redis에 유저 패턴 저장
│       ├── PublishDetectionResultPort.kt  # Kafka 발행
│       └── SaveDetectionResultPort.kt     # 결과 저장 (선택: Redis or DB)
application/
├── DetectTransactionService.kt
├── rules/                           # 탐지 규칙 구현체들
│   ├── HighAmountRule.kt
│   ├── RapidSuccessionRule.kt
│   ├── GeoImpossibleTravelRule.kt
│   └── MidnightTransactionRule.kt
└── GetDetectionResultService.kt
infrastructure/
├── adapter/
│   ├── in/
│   │   ├── web/DetectionController.kt
│   │   └── messaging/TransactionEventConsumer.kt  # Kafka Consumer
│   └── out/
│       ├── cache/
│       │   ├── UserBehaviorRedisAdapter.kt
│       │   └── DetectionResultRedisAdapter.kt
│       └── messaging/
│           └── DetectionResultKafkaProducer.kt
└── config/
    ├── KafkaConsumerConfig.kt
    └── RedisConfig.kt
```

#### 도메인 모델

```kotlin
// domain/model/DetectionResult.kt
data class DetectionResult(
    val id: String,
    val transactionId: String,
    val userId: String,
    val riskLevel: RiskLevel,
    val riskScore: Int,              // 0~100
    val triggeredRules: List<String>,
    val detectedAt: Instant,
)

// domain/model/UserBehaviorProfile.kt
data class UserBehaviorProfile(
    val userId: String,
    val recentTransactionCount: Int,     // 최근 5분 거래 건수
    val averageAmount: BigDecimal,       // 평균 거래 금액
    val lastTransactionCountry: String?, // 마지막 거래 국가
    val lastTransactionTime: Instant?,   // 마지막 거래 시간
    val lastLatitude: Double?,
    val lastLongitude: Double?,
)

// domain/model/DetectionRule.kt
interface DetectionRule {
    val name: String
    fun evaluate(transaction: TransactionEvent, profile: UserBehaviorProfile): RuleResult
}

data class RuleResult(
    val triggered: Boolean,
    val score: Int,        // 0~100, 기여 점수
    val reason: String,
)
```

#### 탐지 규칙 상세

| 규칙 | 클래스 | 조건 | 점수 |
|------|--------|------|------|
| 고액 거래 | HighAmountRule | 평균 금액의 5배 초과 | 40 |
| 연속 거래 | RapidSuccessionRule | 5분 내 5건 이상 | 30 |
| 불가능 이동 | GeoImpossibleTravelRule | 직전 거래와 물리적 이동 불가능 (시속 900km 초과) | 50 |
| 새벽 고액 | MidnightTransactionRule | 00~05시 + 50만원 이상 | 25 |

**리스크 레벨 산정:**
```
riskScore = sum(각 규칙의 score)
- 0~29:   LOW
- 30~59:  MEDIUM
- 60~79:  HIGH
- 80~100: CRITICAL
```

#### 탐지 규칙 구현 가이드

```kotlin
// application/rules/HighAmountRule.kt
class HighAmountRule : DetectionRule {
    override val name = "HIGH_AMOUNT"

    override fun evaluate(transaction: TransactionEvent, profile: UserBehaviorProfile): RuleResult {
        val threshold = profile.averageAmount.multiply(BigDecimal(5))
        val triggered = transaction.amount > threshold

        return RuleResult(
            triggered = triggered,
            score = if (triggered) 40 else 0,
            reason = if (triggered)
                "거래 금액(${transaction.amount})이 평균(${profile.averageAmount})의 5배 초과"
            else "",
        )
    }
}

// application/rules/GeoImpossibleTravelRule.kt
class GeoImpossibleTravelRule : DetectionRule {
    override val name = "GEO_IMPOSSIBLE_TRAVEL"

    override fun evaluate(transaction: TransactionEvent, profile: UserBehaviorProfile): RuleResult {
        if (profile.lastLatitude == null || profile.lastTransactionTime == null) {
            return RuleResult(false, 0, "")
        }

        val distanceKm = haversine(
            profile.lastLatitude, profile.lastLongitude!!,
            transaction.latitude, transaction.longitude,
        )
        val timeDiffHours = Duration.between(profile.lastTransactionTime, transaction.timestamp).toHours()
        val speedKmH = if (timeDiffHours > 0) distanceKm / timeDiffHours else Double.MAX_VALUE

        val triggered = speedKmH > 900 // 비행기 최대 속도

        return RuleResult(
            triggered = triggered,
            score = if (triggered) 50 else 0,
            reason = if (triggered)
                "이동 속도 ${speedKmH.toInt()}km/h (${distanceKm.toInt()}km in ${timeDiffHours}h)"
            else "",
        )
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        // Haversine 공식으로 두 좌표 간 거리(km) 계산
    }
}
```

#### Redis 데이터 구조

| Key Pattern | Type | TTL | 용도 |
|-------------|------|-----|------|
| `user:behavior:{userId}` | Hash | 30분 | 유저 행동 프로필 |
| `user:tx:count:{userId}` | String (INCR) | 5분 | 최근 5분 거래 건수 |
| `detection:result:{transactionId}` | String (JSON) | 1시간 | 탐지 결과 캐싱 |

```
# user:behavior:USER_00001
HSET user:behavior:USER_00001
    avgAmount       "45000"
    lastCountry     "KR"
    lastTime        "2026-04-11T10:30:00Z"
    lastLat         "37.5665"
    lastLon         "126.978"
    txCount5min     "3"
```

#### API 스펙

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/detections/{transactionId}` | 거래별 탐지 결과 조회 |
| GET | `/api/v1/detections?userId=&riskLevel=&page=&size=` | 탐지 결과 목록 |
| GET | `/api/v1/detections/stats` | 탐지 통계 (기간별 건수, 레벨별 분포) |

---

### 5.4 fds-alert-service

탐지 결과를 받아 알림을 생성/관리하는 서비스.

#### 헥사고날 구조

```
domain/
├── model/
│   ├── Alert.kt
│   └── AlertPolicy.kt
├── port/
│   ├── in/
│   │   ├── CreateAlertUseCase.kt
│   │   ├── GetAlertUseCase.kt
│   │   └── UpdateAlertUseCase.kt
│   └── out/
│       ├── SaveAlertPort.kt              # DB 저장
│       ├── CheckDuplicateAlertPort.kt    # Redis 중복 체크
│       └── SendNotificationPort.kt       # 알림 발송 (확장 포인트)
application/
├── CreateAlertService.kt
├── GetAlertService.kt
└── UpdateAlertService.kt
infrastructure/
├── adapter/
│   ├── in/
│   │   ├── web/AlertController.kt
│   │   └── messaging/DetectionResultConsumer.kt  # Kafka Consumer
│   └── out/
│       ├── persistence/
│       │   ├── AlertJpaEntity.kt
│       │   ├── AlertJpaRepository.kt
│       │   └── AlertPersistenceAdapter.kt
│       ├── cache/
│       │   └── AlertDuplicateRedisAdapter.kt
│       └── notification/
│           └── LogNotificationAdapter.kt  # 로그 출력 (나중에 교체 가능)
└── config/
    ├── KafkaConsumerConfig.kt
    └── RedisConfig.kt
```

#### 도메인 모델

```kotlin
// domain/model/Alert.kt
data class Alert(
    val id: String,
    val transactionId: String,
    val userId: String,
    val riskLevel: RiskLevel,
    val riskScore: Int,
    val triggeredRules: List<String>,
    val status: AlertStatus,
    val createdAt: Instant,
    val resolvedAt: Instant? = null,
    val resolvedBy: String? = null,
    val memo: String? = null,
)

enum class AlertStatus {
    OPEN,       // 미처리
    REVIEWING,  // 검토 중
    CONFIRMED,  // 이상거래 확인
    DISMISSED,  // 정상 판정
}
```

#### 알림 중복 방지 정책

```kotlin
// Redis Sliding Window
// 같은 userId에 대해 1분 내 중복 알림 방지
//
// Key: alert:dedup:{userId}
// Type: SET (SADD transactionId, EXPIRE 60s)
//
// 로직:
// 1. SISMEMBER alert:dedup:{userId} {transactionId}
// 2. 이미 존재하면 → 중복, 스킵
// 3. 없으면 → SADD + EXPIRE 60
```

#### DB 스키마 (Flyway)

```sql
-- V1__create_alerts.sql
CREATE TABLE alerts (
    id               VARCHAR(36)   PRIMARY KEY,
    transaction_id   VARCHAR(36)   NOT NULL,
    user_id          VARCHAR(36)   NOT NULL,
    risk_level       VARCHAR(10)   NOT NULL,
    risk_score       INTEGER       NOT NULL,
    triggered_rules  TEXT          NOT NULL,  -- JSON array
    status           VARCHAR(20)   NOT NULL DEFAULT 'OPEN',
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    resolved_at      TIMESTAMP WITH TIME ZONE,
    resolved_by      VARCHAR(50),
    memo             TEXT,

    CONSTRAINT chk_risk_level CHECK (risk_level IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    CONSTRAINT chk_alert_status CHECK (status IN ('OPEN','REVIEWING','CONFIRMED','DISMISSED'))
);

CREATE INDEX idx_alerts_user_id ON alerts(user_id);
CREATE INDEX idx_alerts_status ON alerts(status);
CREATE INDEX idx_alerts_risk_level ON alerts(risk_level);
CREATE INDEX idx_alerts_created_at ON alerts(created_at);
```

#### API 스펙

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/alerts` | 알림 목록 (필터: status, riskLevel, userId) |
| GET | `/api/v1/alerts/{id}` | 알림 상세 |
| PATCH | `/api/v1/alerts/{id}/status` | 알림 상태 변경 (REVIEWING, CONFIRMED, DISMISSED) |
| GET | `/api/v1/alerts/stats` | 알림 통계 |

**PATCH /api/v1/alerts/{id}/status Request Body:**
```json
{
  "status": "CONFIRMED",
  "resolvedBy": "admin",
  "memo": "해외 고액 거래 확인, 본인 아님"
}
```

---

### 5.5 fds-gateway

Spring Cloud Gateway Server WebMVC 기반 API Gateway.

#### 구조

```
infrastructure/
├── adapter/in/
│   ├── web/HealthController.kt
│   └── filter/
│       ├── LoggingFilter.kt       # 요청/응답 로깅
│       └── RateLimitFilter.kt     # IP별 토큰 버킷 Rate Limiting
└── config/
    └── RouteConfig.kt             # 서비스별 라우팅
```

#### 라우팅 규칙

| Path | Target |
|------|--------|
| `/api/v1/transactions/**` | Transaction Service (:8081) |
| `/api/v1/detections/**` | Detection Service (:8082) |
| `/api/v1/alerts/**` | Alert Service (:8083) |

#### Rate Limiting

- IP별 초당 1,000건 (TokenBucket 알고리즘)
- 초과 시 `429 Too Many Requests` 응답

---

### 5.6 fds-generator

테스트 데이터 생성기. Ktor Client로 Gateway에 거래 데이터를 전송한다.

#### 구조

```
domain/
├── model/
│   ├── TransactionData.kt
│   └── TransactionDataFactory.kt   # 거래 데이터 생성 로직
├── port/
│   ├── in/GeneratorUseCase.kt
│   └── out/TransactionSendPort.kt
application/
└── GeneratorServiceImpl.kt
infrastructure/
├── adapter/
│   ├── in/web/GeneratorController.kt
│   └── out/client/KtorTransactionSendAdapter.kt
└── config/
    └── KtorClientConfig.kt
```

#### API 스펙

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/v1/generator/start?rate=100&fraudRatio=0.05` | 생성 시작 (초당 rate건) |
| POST | `/api/v1/generator/stop` | 생성 중지 |
| POST | `/api/v1/generator/burst?count=10000&fraudRatio=0.3` | 스파이크 (count건 일괄) |
| GET | `/api/v1/generator/status` | 현재 상태 |

#### 거래 데이터 생성 규칙

**정상 거래 (95%):**
- 500명 유저 풀에서 랜덤 선택
- 15개 가맹점 (국내 10 + 해외 5)
- 카테고리별 현실적 금액 범위:
  - CAFE: 3,000~15,000원
  - GROCERY: 10,000~200,000원
  - ONLINE: 5,000~300,000원
  - DEPARTMENT: 30,000~500,000원
  - LUXURY: 500,000~5,000,000원

**이상 거래 (5%, fraudRatio로 조절):**

| 유형 | 패턴 | 탐지 규칙 매칭 |
|------|------|----------------|
| HIGH_AMOUNT | 300만~1000만원 | HighAmountRule |
| RAPID_SUCCESSION | 같은 유저 연속 발생 | RapidSuccessionRule |
| FOREIGN_AFTER_DOMESTIC | 국내 직후 해외 결제 | GeoImpossibleTravelRule |
| MIDNIGHT | 새벽 + 50만~300만원 | MidnightTransactionRule |

---

## 6. 인프라 (docker-compose)

### 6.1 실행

```bash
# 인프라 기동
docker compose up -d

# 서비스 기동 순서
1. EurekaServerApplication    (:8761)
2. TransactionServiceApplication (:8081)
   DetectionServiceApplication   (:8082)
   AlertServiceApplication       (:8083)
3. GatewayApplication           (:8080)
4. GeneratorApplication          (:8090)
```

### 6.2 인프라 구성

| 서비스 | 이미지 | 용도 |
|--------|--------|------|
| PostgreSQL 17 | postgres:17 | Transaction, Alert DB |
| Redis 7 | redis:7-alpine | Detection 캐싱, Alert 중복 방지 |
| Kafka 4.0 (KRaft) | apache/kafka:4.0.0 | 이벤트 스트리밍 |
| Elasticsearch 8.17 | elasticsearch:8.17.0 | 거래 검색/분석 |
| Kibana 8.17 | kibana:8.17.0 | ES 대시보드 |
| Zipkin | openzipkin/zipkin | 분산 추적 |
| Kafka UI | provectuslabs/kafka-ui | Kafka 토픽 모니터링 |

### 6.3 DB 초기화

PostgreSQL 컨테이너 기동 시 `infra/init-db.sql`로 데이터베이스 2개 생성:
- `fds_transaction` → Transaction Service
- `fds_alert` → Alert Service

Flyway 마이그레이션은 각 서비스 기동 시 자동 실행.

---

## 7. k6 부하 테스트 시나리오

### 7.1 시나리오 구성

```
k6/
├── scripts/
│   ├── smoke-test.js          # 소량 테스트 (정상 동작 확인)
│   ├── load-test.js           # 일반 부하 (초당 1,000건, 5분)
│   ├── stress-test.js         # 한계 테스트 (점진적 증가 → 초당 50,000건)
│   ├── spike-test.js          # 스파이크 (평소 100 → 순간 50,000)
│   └── search-test.js         # ES 검색 API 부하
└── config/
    └── thresholds.json        # 성능 기준
```

### 7.2 성능 목표

| 항목 | 기준 |
|------|------|
| 거래 수집 API (POST) | p99 < 100ms |
| 거래 검색 API (GET, ES) | p99 < 200ms |
| 처리량 (Throughput) | > 10,000 TPS |
| 에러율 | < 0.1% |
| Kafka Consumer Lag | < 1,000 |

### 7.3 k6 스크립트 예시

```javascript
// k6/scripts/load-test.js
import http from 'k6/http';
import { check } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 500 },   // ramp up
    { duration: '5m',  target: 1000 },  // 유지
    { duration: '30s', target: 0 },     // ramp down
  ],
  thresholds: {
    http_req_duration: ['p(99)<100'],
    http_req_failed: ['rate<0.001'],
  },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
  const payload = JSON.stringify({
    userId: `USER_${String(Math.floor(Math.random() * 500) + 1).padStart(5, '0')}`,
    cardNumber: `4${Math.random().toString().slice(2, 14)}`,
    amount: Math.floor(Math.random() * 500000) + 1000,
    currency: 'KRW',
    merchantName: '테스트가맹점',
    merchantCategory: 'ONLINE',
    country: 'KR',
    city: '서울',
    latitude: 37.5665 + (Math.random() - 0.5) * 0.1,
    longitude: 126.978 + (Math.random() - 0.5) * 0.1,
  });

  const res = http.post(`${BASE_URL}/api/v1/transactions`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, {
    'status is 201': (r) => r.status === 201,
    'has transactionId': (r) => JSON.parse(r.body).id !== undefined,
  });
}
```

---

## 8. 구현 순서 (권장)

### Phase 1: 기반 구축
1. `docker compose up -d`로 인프라 기동 확인
2. Eureka Server 기동 확인 (http://localhost:8761)
3. fds-common Kafka 이벤트 스키마 확정

### Phase 2: Transaction Service
4. Flyway 마이그레이션 작성 (V1__create_transactions.sql)
5. domain 레이어 (model, port) 작성
6. infrastructure/adapter/out/persistence (JPA Entity, Repository, Adapter) 구현
7. infrastructure/adapter/out/messaging (Kafka Producer) 구현
8. infrastructure/adapter/out/search (ES Document, Adapter) 구현
9. application 레이어 (CreateTransactionService, SearchTransactionService) 구현
10. infrastructure/adapter/in/web (TransactionController) 구현
11. Swagger UI로 API 동작 확인

### Phase 3: Detection Service
12. domain 레이어 (model, port) 작성
13. application/rules (4가지 탐지 규칙) 구현
14. infrastructure/adapter/out/cache (Redis Adapter) 구현
15. infrastructure/adapter/out/messaging (Kafka Producer) 구현
16. infrastructure/adapter/in/messaging (Kafka Consumer) 구현
17. application (DetectTransactionService) 구현
18. infrastructure/adapter/in/web (DetectionController) 구현

### Phase 4: Alert Service
19. Flyway 마이그레이션 작성 (V1__create_alerts.sql)
20. domain/application/infrastructure 구현 (Transaction과 유사한 구조)
21. Redis 중복 방지 로직 구현

### Phase 5: Gateway 연동
22. Gateway를 통한 전체 흐름 테스트
23. Generator → Gateway → Transaction → Kafka → Detection → Kafka → Alert 전체 플로우 확인

### Phase 6: 성능 테스트 및 최적화
24. k6 스크립트 작성
25. 부하 테스트 실행 및 병목 분석
26. Kafka 파티션 튜닝, JPA 배치 사이즈, ES 벌크 인덱싱 등 최적화
27. 결과 문서화 (README에 성능 수치 추가)

---

## 9. Swagger UI

각 서비스 기동 후 Swagger UI 접근:
- Transaction: http://localhost:8081/swagger-ui/index.html
- Detection: http://localhost:8082/swagger-ui/index.html
- Alert: http://localhost:8083/swagger-ui/index.html
- Generator: http://localhost:8090/swagger-ui/index.html

---

## 10. 모니터링

| 도구 | URL | 용도 |
|------|-----|------|
| Eureka Dashboard | http://localhost:8761 | 서비스 등록 현황 |
| Swagger UI | http://localhost:{port}/swagger-ui | API 문서 |
| Kafka UI | http://localhost:9090 | 토픽/컨슈머 그룹 모니터링 |
| Kibana | http://localhost:5601 | ES 데이터 시각화 |
| Zipkin | http://localhost:9411 | 분산 추적 |
| Prometheus | http://localhost:{port}/actuator/prometheus | 메트릭 수집 |
