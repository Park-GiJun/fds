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
- [x] fds-gateway (RouteConfig lb://, LoggingFilter, RateLimitFilter+Caffeine, HealthWebAdapter, SecurityConfig)
- [x] fds-generator (헥사고날 구조, Ktor Client, TransactionDataFactory, SecurityConfig)
- [x] docker-compose.yml (PostgreSQL, Redis, Kafka, ES, Kibana, Zipkin, Kafka UI)
- [x] 빌드 플러그인 분리: fds-spring-boot (core+Security) / fds-spring-boot-service (+ SpringDoc)
- [x] GeneratorService — @Bean 수동 등록, 반환타입 GeneratorUseCase 인터페이스
- [x] GeneratorStatusResponse / TransactionSendRequest DTO 분리
- [x] 카드번호 마스킹 (CardMasking, TransactionEvent.maskedCardNumber, TransactionData.toString())
- [x] Spring Security 기본 구조 (Gateway, Generator)
- [x] DB/Kafka/Redis 자격증명 환경변수화
- [x] Gateway RouteConfig lb:// 서비스 디스커버리
- [x] RateLimitFilter Caffeine Cache 전환 (100K entries, 10min TTL)
- [x] GeneratorService 입력 검증 (rate, fraudRatio, count)
- [x] burst() 전송 결과 totalSent/totalFailed 반영
- [x] DomainExceptions 리네이밍 (AccessDenied, AuthenticationRequired)
- [x] CommonApiResponse → web 패키지 이동
- [x] 테스트 코드 48개 (fds-common, fds-generator, fds-gateway)

### 미구현
- [ ] fds-transaction-service (비즈니스 로직 — Application.kt stub만 존재)
- [ ] fds-detection-service (비즈니스 로직 — Application.kt stub만 존재)
- [ ] fds-alert-service (비즈니스 로직 — Application.kt stub만 존재)
- [ ] k6 부하 테스트

## 확립된 컨벤션 (Quality Lead 확정, 2026-04-11)
- 패키지: `com.gijun.fds.{module}.{layer}.{sublayer}`
- UseCase 구현체: `{Resource}Handler` (Service/Impl 사용 금지) — 2026-04-12 컨벤션 변경
- in-port: `{Resource}UseCase` at `application.port.inbound`
- out-port: `{Action}{Resource}Port` at `application.port.outbound`
- 웹 어댑터: `{Name}WebAdapter` at `infrastructure.adapter.inbound.web`
- 상수: `const val` → SCREAMING_SNAKE_CASE / 일반 `val` → camelCase
- 함수 스타일: 단일 표현식 → expression body, named argument + trailing comma 필수
- Logger: `LoggerFactory.getLogger(javaClass)` 인스턴스 패턴
- HTTP Client: Ktor Client 사용 (OpenFeign 아님)
- DB: JPA + Flyway
- Build: buildSrc로 버전/의존성 중앙 관리
- 커밋 메시지: 한국어

## 기술 부채

### Critical — 모두 해결됨 ✅
- [x] ~~{noop} 비밀번호 인코더~~ → 2026-04-12 BCryptPasswordEncoder 교체
- [x] ~~Gateway /api/** permitAll~~ → 2026-04-12 엔드포인트별 authenticated + denyAll
- [x] ~~TokenBucket @Synchronized 경합~~ → 2026-04-12 AtomicInteger CAS 교체
- [x] ~~start() Semaphore 미적용~~ → 2026-04-12 Semaphore(200) 추가
- [x] ~~CardMasking PCI-DSS 미준수~~ → 2026-04-12 앞6+뒤4 형식 수정
- [x] ~~TransactionData cardNumber 원본 잔존~~ → 2026-04-12 CardNumber VO 캡슐화
- [x] ~~카드번호 평문 전송~~ → 2026-04-12 마스킹 처리 완료
- [x] ~~인증/인가 전무~~ → 2026-04-12 Spring Security + BCrypt + 엔드포인트별 인증
- [x] ~~Gateway 라우트 localhost 하드코딩~~ → 2026-04-12 lb:// 전환 완료
- [x] ~~application 계층 @Service 침투~~ → 2026-04-12 수정됨

### High (2026-04-12 2차 리뷰 신규)
- [ ] Generator SecurityConfig anyRequest denyAll 누락 — 미등록 경로 열림
- [ ] Generator /actuator/** 전체 공개 → health/info만 축소 필요
- [ ] SecurityConfig 통합 테스트 부재 — 보안 회귀 위험

### High (2026-04-12 Config fallback 제거 리뷰 신규)
- [ ] Config Server + Eureka SPOF — fds-eureka-server 단일 노드 장애 시 전 서비스 기동 불가, 운영 전환 전 분리 필수
- [ ] 테스트 프로파일 Config Server 의존 — `@SpringBootTest` 실행 시 `spring.cloud.config.enabled=false` 미설정 시 CI 기동 실패
- [ ] CONFIG_PASSWORD 기본값 `config-secret` 운영 프로파일 미제거 (2회 반복 지적)

### High (기존 — 미해결)
- [ ] HikariCP 기본 10 pool (목표 10K TPS에 1/10 수준)
- [ ] Kafka 단일 파티션 (consumer 병렬성 불가)
- [ ] ES 512MB 힙 (10K TPS 불가)
- [ ] Redis maxmemory 미설정
- [ ] Caffeine expireAfterWrite 병행 적용 필요
- [ ] Kafka trusted.packages 와일드카드 범위 축소 필요
- [x] ~~DB 자격증명 application.yml 하드코딩~~ → 2026-04-12 환경변수화
- [x] ~~RateLimitFilter ConcurrentHashMap 메모리 누수~~ → 2026-04-12 Caffeine Cache 전환
- [x] ~~GeneratorBeanConfig 반환 타입이 구체 클래스~~ → 2026-04-12 UseCase 인터페이스로 변경
- [x] ~~Generator start() 코루틴 Semaphore 미적용~~ → 2026-04-12 Semaphore(200) 추가
- [x] ~~SecurityConfig adminPassword 기본값 "admin"~~ → 2026-04-12 환경변수 필수화
- [ ] CommonApiResponse — fds-common/web 잔존 → 각 서비스 infrastructure로 분리

### Medium
- [ ] CAS 리필 tokens.set() over-refill race → compareAndSet 루프 개선
- [ ] Semaphore 루프 내 재생성 → 필드 이동
- [ ] CardNumber.toString() vs CardMasking.mask() 마스킹 로직 통일
- [ ] CardNumber.raw → internal 접근 제한
- [ ] Kafka Producer 배치 미최적화
- [ ] FraudType ↔ DetectionRule 이름 불일치
- [ ] 제너레이터 Midnight/RapidSuccession 탐지 시나리오 불완전
- [ ] TransactionEvent의 CardMasking import → 마스킹 책임 infrastructure adapter로 이동
- [ ] fromRaw() 파라미터 12개 → 도메인 객체 수신 팩토리로 리팩토링
- [ ] MAX_CONCURRENT_SEND/BURST 중복 상수 통합
- [ ] httpBasic → JWT 전환 계획
- [x] ~~Generator burst() 비구조적 코루틴~~ → 2026-04-12 Semaphore(200) 적용
- [x] ~~currentRate var 멀티스레드 비가시성~~ → 2026-04-12 AtomicInteger 전환
- [x] ~~Generator API 입력 검증 없음~~ → 2026-04-12 require() 추가
- [x] ~~burst() 전송 결과 totalSent/totalFailed 미반영~~ → 2026-04-12 수정

## 반복 실수 패턴
(리뷰에서 2회 이상 지적된 항목)

- **카드번호 평문 전송**: Baseline SEC-001 + 3회 리뷰 → 2026-04-12 마스킹 + PCI-DSS 형식 + CardNumber VO. ✅ **완전 해결.**
- **ConcurrentHashMap 무한 증가**: Baseline PERF-001 + 2회 리뷰 → Caffeine + CAS. ✅ **해결.** (over-refill race는 Medium Tech Debt)
- **인증/보안 설계 후순위화**: 3회 반복(Baseline → 1차 리뷰 → 2차 리뷰). BCrypt/엔드포인트 인증으로 대폭 개선. ⚠️ 단, Generator denyAll 누락 잔존.
- **보안 컴포넌트 무테스트**: SecurityConfig 2개 작성 후 테스트 0개. 2차 리뷰에서 지적. ⚠️ 신규 패턴.
- **CONFIG_PASSWORD 기본값 미제거**: 2차 리뷰 + Config fallback 제거 리뷰 2회 지적. `config-secret` 기본값이 운영에 노출될 위험. ⚠️ 반복 실수.

## 미확정 사항 (팀 합의 완료/필요)
- [x] in-port 패키지 위치: `application.port.in` 확정 (2026-04-12)
- [ ] 인터페이스 suspend 사용 기준
- [ ] 코루틴 Scope 관리 정책 (lifecycle 통합 방식)
- [ ] Exposed ORM 사용 여부 (Dependencies.kt에 정의되어 있으나 미사용)
- [x] ~~DomainExceptions/CommonApiResponse 모듈 배치~~ → 2026-04-12 web 패키지 이동 + 예외 리네이밍 (추가 분리는 Tech Debt)
