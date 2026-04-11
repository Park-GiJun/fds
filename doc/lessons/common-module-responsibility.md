# 공통 모듈(fds-common) 책임 원칙

## 배운 점
- `fds-common`은 Kafka 이벤트 스키마 공유 목적으로 설계됨 (TransactionEvent, DetectionResultEvent, KafkaTopics)
- `CommonApiResponse`(HTTP 응답 래퍼)는 Web 어댑터(인프라 계층)의 관심사 → fds-common에 두면 모든 서비스가 HTTP 응답 구조에 의존
- 도메인 예외(`DomainExceptions`)도 마찬가지 — `Forbidden`, `Unauthorized`는 HTTP 인프라 개념이므로 도메인 예외로 부적절
- 공통 모듈에 추가할 때는 "이것이 모든 서비스의 도메인 공통 관심사인가?"를 기준으로 판단

## 적용 방법
- **fds-common에 넣을 것**: Kafka 이벤트 스키마, 도메인 공통 Value Object, 도메인 공통 예외 (비즈니스 규칙 위반)
- **fds-common에 넣지 말 것**: HTTP 응답 래퍼, CORS 설정, 인증/인가 관련 예외, 인프라 유틸리티
- HTTP 응답 래퍼가 필요하면 `fds-api-support` 별도 모듈 또는 각 서비스의 `infrastructure.web` 패키지에 배치

## 관련 리뷰
- [2026-04-12 unstaged](../review/2026-04-12-unstaged.md)
