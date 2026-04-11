# FDS Project Context

## 프로젝트 개요
- 이상거래 탐지 시스템 (Fraud Detection System)
- Kotlin + Spring Boot 4.0 MSA
- 헥사고날 아키텍처 (domain / application / infrastructure)

## 아키텍처 원칙
- domain 패키지: 순수 Kotlin, Spring/JPA/Kafka 의존 금지
- infrastructure/adapter/in: Inbound (Controller, Kafka Consumer)
- infrastructure/adapter/out: Outbound (JPA, Redis, ES, Kafka Producer, Ktor)
- infrastructure/config: Spring Bean 설정
- application: UseCase 구현, port.out 인터페이스만 의존

## 구현 현황

### 완료
- [x] 멀티모듈 프로젝트 세팅 (buildSrc)
- [x] fds-common (Kafka 이벤트 스키마)
- [x] fds-eureka-server
- [x] fds-gateway (RouteConfig, LoggingFilter, RateLimitFilter)
- [x] fds-generator (헥사고날 구조, Ktor Client)
- [x] docker-compose.yml (인프라)

### 미구현
- [ ] fds-transaction-service (비즈니스 로직)
- [ ] fds-detection-service (비즈니스 로직)
- [ ] fds-alert-service (비즈니스 로직)
- [ ] k6 부하 테스트

## 반복 실수 패턴
(리뷰에서 2회 이상 지적된 항목을 여기에 누적)

## 확립된 컨벤션
- 패키지: domain / application / infrastructure(adapter, config)
- Kafka 이벤트: fds-common 모듈에 공통 정의
- HTTP Client: Ktor Client 사용 (OpenFeign 아님)
- DB: JPA + Flyway
- Build: buildSrc로 버전/의존성 중앙 관리

## 기술 부채
(리뷰에서 발견된 미해결 이슈를 여기에 누적)
