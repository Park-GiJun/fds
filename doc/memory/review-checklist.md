# Review Checklist

> `/review` 실행 시 리뷰어와 리드가 참조하는 체크리스트.
> `/init-review` (2026-04-11)에서 초기 수립, 이후 리뷰에서 누적 업데이트.

## 기술 체크리스트 (Tech Lead)

### Architecture
- [ ] domain 패키지에 Spring/JPA/Kafka 등 프레임워크 import 없음
- [ ] application 계층에 `@Service`, `@Component` 등 Spring 어노테이션 사용 금지 (infrastructure config에서 `@Bean` 등록)
- [ ] infrastructure.adapter.in.web은 domain.port.in(UseCase)만 의존
- [ ] 도메인 모델이 HTTP 응답 DTO로 직접 노출되지 않음 (별도 Response DTO 사용)
- [ ] Gateway 라우트가 `lb://` 서비스 디스커버리 URI 사용 (localhost 하드코딩 금지)
- [ ] 환경별 프로파일 분리 (application-docker.yml, application-prod.yml)

### Security
- [ ] 카드번호(PAN) Kafka 이벤트 발행 전 마스킹 처리
- [ ] application.yml에 민감 정보 하드코딩 없음 (환경변수 `${...}` 사용)
- [ ] Spring Security 인증/인가 적용 여부 확인
- [ ] API 입력 검증 (`@Validated`, `@Min`, `@Max` 등) 적용
- [ ] Kafka trusted.packages에 와일드카드 사용 금지 (구체적 FQCN 지정)
- [ ] Actuator 엔드포인트 인증 적용

### Performance
- [ ] HikariCP `maximum-pool-size` 명시 설정 (기본값 10 사용 금지)
- [ ] Kafka 토픽 파티션 수 12+ 설정
- [ ] `spring.kafka.listener.concurrency` 명시 설정
- [ ] ES 힙 메모리 최소 4GB 설정
- [ ] Redis maxmemory + eviction 정책 설정
- [ ] ConcurrentHashMap 기반 캐시에 TTL/정리 로직 존재
- [ ] 코루틴 동시 생성 수 제한 (Semaphore/chunked)
- [ ] Kafka Producer batch 최적화 (linger.ms, batch.size)

## 품질 체크리스트 (Quality Lead)

### Code Quality
- [ ] UseCase 구현체 클래스명: `{Resource}Handler` (Service/Impl 사용 금지 — 2026-04-12 컨벤션 전환)
- [ ] in-port 인터페이스: `{Resource}UseCase`
- [ ] out-port 인터페이스: `{Action}{Resource}Port`
- [ ] 웹 어댑터: `{Name}WebAdapter`
- [ ] 일반 `val`에 SCREAMING_SNAKE_CASE 사용 금지 (`const val`만 허용)
- [ ] 멀티스레드 접근 변수에 `@Volatile` 또는 Atomic 타입 사용
- [ ] 코루틴 scope lifecycle 관리 (비구조적 fire-and-forget 금지)
- [ ] named argument + trailing comma 일관 적용

### Testing
- [ ] 순수 도메인 로직: 단위 테스트 존재
- [ ] 탐지 규칙: 규칙별 단위 테스트 존재 (TDD 선행)
- [ ] Kafka producer/consumer: 계약 테스트 존재
- [ ] HTTP 어댑터: mock 기반 테스트 존재
- [ ] 동시성 관련 코드: 멀티스레드 테스트 존재

### Domain
- [ ] FraudType 열거값과 DetectionRule.name 일관성
- [ ] 제너레이터가 각 FraudType에 대해 실제 탐지 가능한 데이터 생성
- [ ] 시간 기반 규칙의 타임존 기준 명시 (KST)
- [ ] RiskScore 합산 cap(100) 처리
- [ ] Redis 슬라이딩 윈도우 정확성 (INCR+TTL 대신 Sorted Set)
- [ ] Alert 중복 방지 원자성 (Lua 스크립트 또는 SET NX EX)
- [ ] DetectionResultEvent에 알림 표시용 컨텍스트 포함
