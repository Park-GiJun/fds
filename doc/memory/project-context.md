# FDS Project Context

## 프로젝트 개요
- 이상거래 탐지 시스템 (Fraud Detection System)
- Kotlin + Spring Boot 4.0 MSA
- 헥사고날 아키텍처 (domain / application / infrastructure)
- 기술 성숙도: **INITIAL** (2026-04-11 Baseline 판정)

## 아키텍처 원칙
- domain 패키지: 순수 Kotlin, Spring/JPA/Kafka 의존 금지
- application 계층: port 인터페이스만 의존, Spring 어노테이션 사용 금지 (infrastructure config에서 @Bean 등록)
- infrastructure/adapter/in: Inbound (Controller, Kafka Consumer)
- infrastructure/adapter/out: Outbound (JPA, Redis, ES, Kafka Producer, Ktor)
- infrastructure/config: Spring Bean 설정
- 도메인 모델은 HTTP 응답으로 직접 노출 금지 (별도 Response DTO 사용)
- @Bean 반환 타입은 인터페이스(UseCase)로 선언 (구체 클래스 노출 금지)
- fds-common은 Kafka 이벤트 스키마 전용 — HTTP 응답 래퍼, 인프라 유틸은 넣지 않음

## 구현 현황

### 완료
- [x] 멀티모듈 프로젝트 세팅 (buildSrc)
- [x] fds-common (Kafka 이벤트 스키마: TransactionEvent, DetectionResultEvent, KafkaTopics)
- [x] fds-eureka-server
- [x] fds-gateway (RouteConfig, LoggingFilter, RateLimitFilter, HealthWebAdapter)
- [x] fds-generator (헥사고날 구조, Ktor Client, TransactionDataFactory)
- [x] docker-compose.yml (PostgreSQL, Redis, Kafka, ES, Kibana, Zipkin, Kafka UI)
- [x] 빌드 플러그인 분리: fds-spring-boot (core) / fds-spring-boot-service (+ SpringDoc)
- [x] GeneratorService — @Service 제거, @Bean 수동 등록 패턴 적용
- [x] GeneratorStatusResponse / TransactionSendRequest DTO 분리

### 미구현
- [ ] fds-transaction-service (비즈니스 로직 — Application.kt stub만 존재)
- [ ] fds-detection-service (비즈니스 로직 — Application.kt stub만 존재)
- [ ] fds-alert-service (비즈니스 로직 — Application.kt stub만 존재)
- [ ] k6 부하 테스트
- [ ] 테스트 코드 (전체 0개)

## 확립된 컨벤션 (Quality Lead 확정, 2026-04-11)
- 패키지: `com.gijun.fds.{module}.{layer}.{sublayer}`
- UseCase 구현체: `{Resource}Service` (Handler/Impl 사용 금지)
- in-port: `{Resource}UseCase` at `application.port.in`
- out-port: `{Action}{Resource}Port` at `application.port.out`
- 웹 어댑터: `{Name}WebAdapter`
- 상수: `const val` → SCREAMING_SNAKE_CASE / 일반 `val` → camelCase
- 함수 스타일: 단일 표현식 → expression body, named argument + trailing comma 필수
- Logger: `LoggerFactory.getLogger(javaClass)` 인스턴스 패턴
- HTTP Client: Ktor Client 사용 (OpenFeign 아님)
- DB: JPA + Flyway
- Build: buildSrc로 버전/의존성 중앙 관리
- 커밋 메시지: 한국어

## 기술 부채

### Critical
- [ ] 카드번호 평문 전송 — TransactionEvent/TransactionData/TransactionSendRequest에 마스킹 없음 (PCI-DSS)
- [ ] 인증/인가 전무 — Spring Security 의존성 자체 없음
- [ ] Gateway 라우트 localhost 하드코딩 — Eureka 무력화
- [x] ~~application 계층 @Service 침투 (GeneratorHandler)~~ → 2026-04-12 수정됨

### High
- [ ] DB 자격증명 application.yml 하드코딩
- [ ] HikariCP 기본 10 pool (목표 10K TPS에 1/10 수준)
- [ ] Kafka 단일 파티션 (consumer 병렬성 불가)
- [ ] ES 512MB 힙 (10K TPS 불가)
- [ ] Redis maxmemory 미설정
- [ ] RateLimitFilter ConcurrentHashMap 메모리 누수 → Caffeine Cache 교체 필요
- [ ] GeneratorBeanConfig 반환 타입이 구체 클래스 (→ UseCase 인터페이스로 변경)
- [ ] Generator start() 코루틴 Semaphore 미적용
- [ ] CommonApiResponse/DomainExceptions — fds-common 모듈 책임 범위 초과 (재배치 필요)

### Medium
- [x] ~~Generator burst() 비구조적 코루틴~~ → 2026-04-12 Semaphore(200) 적용
- [x] ~~currentRate var 멀티스레드 비가시성~~ → 2026-04-12 AtomicInteger 전환
- [ ] Kafka Producer 배치 미최적화
- [ ] FraudType ↔ DetectionRule 이름 불일치
- [ ] 제너레이터 Midnight/RapidSuccession 탐지 시나리오 불완전
- [ ] Generator API 입력 검증 없음 (rate, fraudRatio 범위)
- [ ] burst() 전송 결과 totalSent/totalFailed 미반영

## 반복 실수 패턴
(리뷰에서 2회 이상 지적된 항목)

- **카드번호 평문 전송**: Baseline SEC-001 + 2026-04-12 리뷰에서 재지적. 신규 TransactionSendRequest에도 평문 포함. **즉시 수정 필요.**
- **ConcurrentHashMap 무한 증가**: Baseline PERF-001 + 2026-04-12 리뷰에서 재지적. **Caffeine Cache 전환 필요.**

## 미확정 사항 (팀 합의 완료/필요)
- [x] in-port 패키지 위치: `application.port.in` 확정 (2026-04-12)
- [ ] 인터페이스 suspend 사용 기준
- [ ] 코루틴 Scope 관리 정책 (lifecycle 통합 방식)
- [ ] Exposed ORM 사용 여부 (Dependencies.kt에 정의되어 있으나 미사용)
- [ ] DomainExceptions/CommonApiResponse 모듈 배치 (fds-common vs 별도 모듈)
