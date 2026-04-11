# FDS Project — Claude Code 설정

## 프로젝트 정보
- Kotlin + Spring Boot 4.0 MSA 기반 이상거래 탐지 시스템
- 헥사고날 아키텍처: domain / application / infrastructure(adapter, config)
- 멀티모듈: buildSrc로 의존성 관리

## 코딩 규칙
- domain 패키지에 Spring/JPA/Kafka 등 프레임워크 의존 금지
- application 계층에 Spring 어노테이션(@Service, @Component 등) 사용 금지 → infrastructure/config에서 @Bean 등록
- application은 port 인터페이스만 의존 (port 위치: `application.port.in`, `application.port.out`)
- 도메인 모델은 HTTP 응답으로 직접 노출 금지 → infrastructure 계층에 Response DTO 분리
- UseCase 구현체 네이밍: `{Resource}Service` (Handler, Impl 사용 금지)
- non-const val은 camelCase, const val만 SCREAMING_SNAKE_CASE
- 한국어 커밋 메시지 사용

## 커스텀 커맨드

### `/init-review` — 초기화 (프로젝트 시작 시 1회)
프로젝트 전체를 스캔하여 Baseline을 수립한다.
- 6명 리뷰어가 전체 코드베이스를 분석
- 2명 리드가 기술 성숙도 판정 + 컨벤션 확정
- 산출물:
  - `doc/review/0000-00-00-baseline.md` — Baseline Report
  - `doc/memory/project-context.md` — 프로젝트 컨텍스트 갱신
  - `doc/memory/domain-glossary.md` — 도메인 용어 사전
  - `doc/memory/review-checklist.md` — 기술/품질 체크리스트

### `/review` — 커밋 단위 코드 리뷰
커밋 후 호출. 6 Reviewers + 2 Leads가 리뷰한다.
- 산출물:
  - `doc/review/YYYY-MM-DD-{hash}.md` — 리뷰 결과 + Action Items + Tech Debt
  - `doc/lessons/{주제}.md` — 학습 내용 (Quality Lead 추출)
  - `doc/memory/project-context.md` — 누적 업데이트 (Tech Lead)

### 리뷰 팀 구성

**Reviewers (병렬 실행 — 페르소나 기반)**
| # | 이름 | 역할 | 담당 |
|---|------|------|------|
| 1 | 강현수 | Architect | 헥사고날 준수, 의존 방향, SOLID, 모듈 경계 |
| 2 | 박서진 | Security | OWASP, 민감 데이터 마스킹, 인증/인가 |
| 3 | 이도윤 | Performance | N+1, Kafka/Redis/ES 설정, 동시성 |
| 4 | 정하은 | Code Quality | 클린 코드, Kotlin 관용구, 네이밍 |
| 5 | 김태현 | Testing | 테스트 존재 여부, 커버리지, 누락 시나리오 |
| 6 | 윤지아 | Domain Expert | FDS 비즈니스 로직 정확성, 도메인 모델링 |

**Leads (순차 실행 — 리뷰어 완료 후)**
| 이름 | 역할 | 담당 | 참조 문서 |
|------|------|------|-----------|
| 최민준 | Tech Lead | Reviewer 1~3 종합, 기술 심각도, 반복 실수 추적 | project-context, review-checklist |
| 한소율 | Quality Lead | Reviewer 4~6 종합, 품질 심각도, 학습 추출 | domain-glossary, review-checklist |

### 리뷰 흐름

```
/init-review (1회)
    │
    ├── Baseline Report 수립
    ├── 컨벤션 확정
    ├── 체크리스트 수립
    └── 용어 사전 생성
         │
         ▼
코딩 → 커밋 → /review (반복)
                │
                ├── 6명 리뷰 (병렬)
                ├── 2명 리드 종합 (순차)
                ├── doc/review/ 기록
                ├── doc/lessons/ 학습
                └── doc/memory/ 누적 업데이트
                     │
                     └── 다음 /review 시 참조 (피드백 루프)
```

## 문서 구조
```
doc/
├── review/                        # 코드 리뷰 결과 (커밋별)
│   └── 0000-00-00-baseline.md     # 초기 Baseline
├── lessons/                       # 학습 내용 (주제별)
└── memory/                        # 누적 컨텍스트
    ├── project-context.md         # 프로젝트 상황, 반복 실수, 기술 부채
    ├── domain-glossary.md         # 도메인 용어 사전
    └── review-checklist.md        # 기술/품질 체크리스트
```
