# FDS Project — Claude Code 설정

## 프로젝트 정보
- Kotlin + Spring Boot 4.0 MSA 기반 이상거래 탐지 시스템
- 헥사고날 아키텍처: domain / application / infrastructure(adapter, config)
- 멀티모듈: buildSrc로 의존성 관리

## 코딩 규칙
- domain 패키지에 Spring/JPA/Kafka 등 프레임워크 의존 금지
- application 계층에 Spring 어노테이션(@Service, @Component 등) 사용 금지 → infrastructure/config에서 @Bean 등록
- application은 port 인터페이스만 의존 (port 위치: `application.port.inbound`, `application.port.outbound`)
- 도메인 모델은 HTTP 응답으로 직접 노출 금지 → infrastructure 계층에 Response DTO 분리
- UseCase 구현체 네이밍: `{Resource}Handler` (Service, Impl 사용 금지)
- 아웃바운드 포트 네이밍: `{Domain}{Infra}Port` — `Infra`는 인프라 유형별 고정 접미어
  - Kafka/이벤트 → `Message` (e.g., `TransactionMessagePort`)
  - DB/JPA → `Persistence` (e.g., `TransactionPersistencePort`)
  - Redis → `Cache` (e.g., `RateLimitCachePort`)
  - Elasticsearch → `Search` (e.g., `TransactionSearchPort`)
- inbound adapter 위치: `infrastructure.adapter.inbound` (web, filter 등)
- Spring 어노테이션 정책:
  - `application/handler`: @Bean 수동 등록만 (infrastructure/config에서 등록). **예외**: `@Transactional`은 유스케이스 경계(트랜잭션 경계)를 명확히 하기 위해 handler 메서드에 선언 허용. 타 Spring 어노테이션은 금지.
  - `infrastructure/adapter`: @Component 허용 (infrastructure 계층이므로 Spring 의존 허용)
  - `infrastructure/config`: @Configuration + @Bean
- non-const val은 camelCase, const val만 SCREAMING_SNAKE_CASE
- 한국어 커밋 메시지 사용

## Git 브랜치 규칙
- **코드 변경 후 commit/push 시 반드시 prefix가 적용된 브랜치에서 작업해야 한다. master 직접 push 금지.**
- 브랜치 prefix: `feat/`, `fix/`, `refactor/`, `perf/`, `test/`, `security/`, `chore/`, `docs/`
- 브랜치 생성: `/create-branch` 또는 `/create-issue-branch` 사용
- 워크플로우: 브랜치 생성 → 코딩 → `/commit` → `/ship` (PR → merge)
- 문서(doc/)만 변경하는 경우도 `docs/` prefix 브랜치 사용

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

### `/commit` — 커밋 메시지 자동 생성 + 커밋
변경 사항을 분석하여 한국어 커밋 메시지를 자동 생성하고 커밋한다.
- 에이전트 "송준호" (Commit Craft) — 커밋 메시지 전문가
- 형식: `{타입}: {한국어 요약}` (feat/fix/refactor/perf/test/security/chore/docs/build)
- 브랜치명에서 이슈 번호 자동 추출 → `Refs #번호` 자동 추가
- 빌드 검증 후 커밋 (빌드 실패 시 중단)

### `/ship` — PR 생성 → merge 자동화
현재 브랜치를 push하고 master 대상 PR을 생성한다.
- `/ship` — PR 생성
- `/ship --merge` — PR 생성 + 즉시 squash merge
- `/ship --draft` — Draft PR 생성
- 에이전트 "오세린" (Ship Captain) — PR 메시지 전문가
- 에이전트 "강민재" (Gate Keeper) — merge 전 체크리스트 검증
- 이슈 자동 연결 + PR 코멘트 + merge 후 브랜치 삭제

### `/test` — 테스트 자동 생성 파이프라인
브랜치 생성 → 테스트 작성 → 실행 → 커밋 → PR → master merge 전체 자동화.
- `/test` — 최근 커밋 변경 파일 기반
- `/test {파일경로}` — 특정 파일 대상
- `/test --module {모듈명}` — 모듈 전체 대상
- `/test --setup` — 인프라 세팅만 (의존성, 설정 파일)
- 파이프라인: `test/` 브랜치 생성 → 테스트 작성 → 빌드+실행 → 커밋+push → PR → squash merge
- 프로덕션 코드 변경 없으면 자동 merge, 변경 있으면 수동 리뷰 요청
- 에이전트: 김태현(전략) → 이수빈(단위) → 박준영(통합)
- 테스트 프레임워크: Kotest 6 + MockK + Testcontainers

### `/create-issue-branch` — 이슈 기반 브랜치 생성
이슈 번호를 받아 라벨 기반 prefix + slug로 브랜치를 자동 생성한다.
- `/create-issue-branch 1` — 단일 이슈 브랜치
- `/create-issue-branch 7 8 9` — 여러 이슈 묶음 브랜치
- `/create-issue-branch all` — 열린 이슈 전체를 의존 순서대로 일괄 생성
- prefix 규칙: security/ > refactor/ > perf/ > feat/ > test/ > chore/
- 브랜치명: `{prefix}/issue-{번호}-{slug}`

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

**테스트 에이전트**
| 이름 | 역할 | 담당 커맨드 |
|------|------|------------|
| 김태현 | Test Strategist | `/test` — 전략 수립, 케이스 설계 (리뷰 겸임) |
| 이수빈 | Test Engineer | `/test` — 단위 테스트 작성 (Kotest + MockK) |
| 박준영 | Integration Specialist | `/test` — 통합 테스트 (Testcontainers) |

**Git 워크플로우 에이전트**
| 이름 | 역할 | 담당 커맨드 |
|------|------|------------|
| 송준호 | Commit Craft | `/commit` — 커밋 메시지 자동 생성 |
| 오세린 | Ship Captain | `/ship` — PR 제목/본문 생성 |
| 강민재 | Gate Keeper | `/ship --merge` — merge 전 체크리스트 검증 |

### 전체 워크플로우

```
/create-issue-branch {번호}     ← 이슈 브랜치 생성
         │
         ▼
    코딩 작업
         │
         ▼
    /test                        ← 김태현+이수빈+박준영: 테스트 자동 생성
         │
         ▼
    /commit                      ← 송준호: 커밋 메시지 생성 + 빌드 검증
         │
         ▼
    /ship                        ← 오세린: PR 생성
         │
         ▼
    /review                      ← 6인 리뷰 + 2인 리드
         │
         ▼
    /ship --merge                ← 강민재: 체크리스트 → squash merge
         │
         ▼
    이슈 자동 close + 브랜치 삭제
```

### 리뷰 흐름 (상세)

```
/init-review (1회)
    │
    ├── Baseline Report 수립
    ├── 컨벤션 확정
    ├── 체크리스트 수립
    └── 용어 사전 생성
         │
         ▼
코딩 → /commit → /ship → /review (반복)
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
