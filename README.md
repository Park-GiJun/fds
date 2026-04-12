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
│   ├── model/                       # 도메인 모델 (Entity, VO)
│   └── enums/                       # 도메인 enum
│
├── application/
│   ├── port/
│   │   ├── inbound/                 # Inbound Port (UseCase 인터페이스)
│   │   └── outbound/                # Outbound Port (외부 시스템 인터페이스)
│   └── handler/                     # UseCase 구현체 ({Resource}Handler)
│
├── infrastructure/
│   ├── adapter/
│   │   ├── inbound/
│   │   │   ├── web/                 # Controller (@RestController)
│   │   │   └── messaging/           # Kafka Consumer
│   │   └── outbound/
│   │       ├── persistence/         # JPA Repository, Entity 매핑
│   │       ├── messaging/           # Kafka Producer
│   │       ├── cache/               # Redis 구현체
│   │       ├── search/              # Elasticsearch 구현체
│   │       └── client/              # Ktor HTTP Client
│   └── config/                      # Spring Bean 설정, SecurityConfig
│
└── {Service}Application.kt
```

### 4.2 의존 방향 규칙

```
infrastructure.adapter.inbound  ──▶  application.port.inbound  ◀──  application.handler
infrastructure.adapter.outbound ◀──  application.port.outbound ◀──  application.handler

    [Infrastructure]              [Application]              [Domain]
    Spring, JPA, Kafka            Port + Handler             순수 Kotlin
    Redis, ES, Ktor               인터페이스 + 구현체          모델, enum
```

**절대 규칙:**
- `domain` 패키지는 Spring, JPA, Kafka 등 외부 프레임워크를 import하지 않는다
- `application.handler`는 `application.port.outbound` 인터페이스만 의존하고, 구현체(infrastructure)를 모른다
- `infrastructure.adapter.inbound.web`(Controller)은 `application.port.inbound`(UseCase)만 의존한다
- UseCase 구현체 네이밍: `{Resource}Handler` (Service, Impl 사용 금지)
- `application.handler`는 @Bean 수동 등록, `infrastructure.adapter`는 @Component 허용

---

## 5. 인프라 (docker-compose)

### 5.1 실행

```bash
# 인프라 기동
docker compose up -d

# 서비스 기동 순서 (Eureka/Config Server가 먼저 기동되어야 함)
1. EurekaServerApplication    (:8761) — Eureka + Config Server 겸임
   → 환경변수 필요: DB_HOST, DB_PORT, DB_USERNAME, DB_PASSWORD,
     REDIS_HOST, REDIS_PORT, REDIS_PASSWORD,
     KAFKA_BOOTSTRAP_SERVERS, ELASTICSEARCH_URIS,
     CONFIG_USERNAME, CONFIG_PASSWORD
2. TransactionServiceApplication (:8081)
   DetectionServiceApplication   (:8082)
   AlertServiceApplication       (:8083)
   → Config Server에서 설정 수신 (환경변수: CONFIG_PASSWORD, ADMIN_PASSWORD)
3. GatewayApplication           (:8080)
4. GeneratorApplication          (:8090)

# Eureka/Config Server 미기동 시 → 모든 서비스 기동 실패 (의도적 Fail-Fast)
```

### 5.2 인프라 구성

| 서비스 | 이미지 | 용도 |
|--------|--------|------|
| PostgreSQL 17 | postgres:17 | Transaction, Alert DB |
| Redis 7 | redis:7-alpine | Detection 캐싱, Alert 중복 방지 |
| Kafka 4.0 (KRaft) | apache/kafka:4.0.0 | 이벤트 스트리밍 |
| Elasticsearch 8.17 | elasticsearch:8.17.0 | 거래 검색/분석 |
| Kibana 8.17 | kibana:8.17.0 | ES 대시보드 |
| Zipkin | openzipkin/zipkin | 분산 추적 |
| Kafka UI | provectuslabs/kafka-ui | Kafka 토픽 모니터링 |

### 5.3 DB 초기화

PostgreSQL 컨테이너 기동 시 `infra/init-db.sql`로 데이터베이스 2개 생성:
- `fds_transaction` → Transaction Service
- `fds_alert` → Alert Service

Flyway 마이그레이션은 각 서비스 기동 시 자동 실행.

---

## 6. Swagger UI

각 서비스 기동 후 Swagger UI 접근:
- Transaction: http://localhost:8081/swagger-ui/index.html
- Detection: http://localhost:8082/swagger-ui/index.html
- Alert: http://localhost:8083/swagger-ui/index.html
- Generator: http://localhost:8090/swagger-ui/index.html

---

## 7. 모니터링

| 도구 | URL | 용도 |
|------|-----|------|
| Eureka Dashboard | http://localhost:8761 | 서비스 등록 현황 |
| Swagger UI | http://localhost:{port}/swagger-ui | API 문서 |
| Kafka UI | http://localhost:9090 | 토픽/컨슈머 그룹 모니터링 |
| Kibana | http://localhost:5601 | ES 데이터 시각화 |
| Zipkin | http://localhost:9411 | 분산 추적 |
| Prometheus | http://localhost:{port}/actuator/prometheus | 메트릭 수집 |
