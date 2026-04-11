이슈 번호(들)를 받아서 적절한 브랜치를 생성하고 체크아웃한다.

## 사용법
- `/create-issue-branch 1` — 단일 이슈
- `/create-issue-branch 7 8 9` — 여러 이슈를 하나의 브랜치에 묶기
- `/create-issue-branch all` — 열린 이슈 전체를 의존 관계에 따라 브랜치 일괄 생성 (체크아웃은 하지 않음)

## 실행 절차

### Step 1: 이슈 정보 조회

```bash
export PATH="$HOME/bin:$PATH"
gh issue view {번호} --repo Park-GiJun/fds --json number,title,labels,milestone
```

### Step 2: 브랜치 prefix 결정

이슈의 **라벨**을 기준으로 브랜치 prefix를 결정한다:

| 라벨 | prefix | 설명 |
|------|--------|------|
| `security` | `security/` | 보안 수정 |
| `architecture` | `refactor/` | 아키텍처 리팩터링 |
| `performance` | `perf/` | 성능 최적화 |
| `code-quality` | `refactor/` | 코드 품질 개선 |
| `testing` | `test/` | 테스트 추가 |
| `domain` | `feat/` | 도메인 로직 |
| `tech-debt` | `chore/` | 기술 부채 정리 |
| `review-action` | (다른 라벨 우선 적용) | 리뷰 액션 아이템 |

**우선순위**: security > architecture > performance > domain > testing > code-quality > tech-debt
(라벨이 여러 개면 가장 높은 우선순위의 prefix를 사용)

### Step 3: 브랜치명 생성

**형식**: `{prefix}/issue-{번호}-{slug}`

- `slug`: 이슈 제목에서 `[카테고리]` 부분을 제거하고, 한국어를 영문 키워드로 변환하여 kebab-case로 생성
- 여러 이슈를 묶는 경우: `{prefix}/issue-{번호1}-{번호2}-{slug}`

**slug 변환 규칙**:
- 이슈 제목의 핵심 키워드만 추출 (최대 5단어)
- 한국어 → 영문 축약 (예: "카드번호 마스킹 처리" → "card-number-masking")
- 특수문자, 괄호 제거
- 소문자 + 하이픈

**예시**:
| 이슈 | 생성되는 브랜치명 |
|------|------------------|
| #1 [Security] 카드번호(PAN) 평문 전송 마스킹 처리 | `security/issue-1-card-number-masking` |
| #4 [Architecture] Gateway RouteConfig localhost → 서비스 디스커버리 | `refactor/issue-4-gateway-service-discovery` |
| #5 [Performance] RateLimitFilter ConcurrentHashMap → Caffeine Cache | `perf/issue-5-ratelimit-caffeine-cache` |
| #6 [Testing] GeneratorService + RateLimitFilter 테스트 작성 | `test/issue-6-generator-ratelimit-tests` |
| #7,#8,#9 묶음 | `refactor/issue-7-8-9-generator-cleanup` |

### Step 4: 의존 관계 분석 (all 모드)

`/create-issue-branch all` 실행 시, 이슈 간 의존 관계를 분석하여 순서대로 브랜치를 생성한다.

**의존 관계 규칙**:
1. `severity: critical` 이슈가 먼저
2. 같은 모듈을 수정하는 이슈는 의존 관계가 있을 수 있음
3. 보안 이슈(#1,#2,#3) → 아키텍처(#4,#7) → 성능(#5) → 품질(#8,#9) → 테스트(#6) → 기술부채(#10)

**의존 관계를 자동 판단하는 기준**:
- 이슈 본문의 "관련 파일" 섹션이 겹치면 → 의존 가능성 있음
- 같은 마일스톤 내에서 severity가 높은 것이 먼저
- 테스트 이슈(#6)는 다른 코드 변경 이슈 이후에 생성 (변경된 코드를 테스트해야 하므로)

**all 모드 실행 시**:
1. 모든 open 이슈를 조회
2. 의존 관계 순서대로 정렬
3. 각 이슈에 대해 브랜치 생성 (이미 존재하면 스킵)
4. 생성된 브랜치 목록을 출력

### Step 5: 브랜치 생성 및 체크아웃

```bash
# 최신 master에서 분기
git fetch origin
git checkout master
git pull origin master

# 브랜치 생성
git checkout -b {브랜치명}

# 이슈에 브랜치 연결 정보 코멘트 추가
gh issue comment {번호} --repo Park-GiJun/fds --body "🔀 브랜치 생성: \`{브랜치명}\`"
```

여러 이슈를 묶는 경우, 각 이슈에 모두 코멘트를 남긴다.

### Step 6: 출력

```
═══════════════════════════════════════════
  Branch Created
═══════════════════════════════════════════

  이슈: #{번호} {제목}
  브랜치: {브랜치명}
  Base: master ({해시})

  관련 이슈: #{번호1}, #{번호2} ...
  의존 브랜치: {있으면 표시}

  다음 단계: 코드 수정 → 커밋 → /review
═══════════════════════════════════════════
```

### all 모드 출력

```
═══════════════════════════════════════════
  Issue Branches — 일괄 생성 완료
═══════════════════════════════════════════

  ┌─────┬──────────────────────────────────────┬──────────┐
  │ #   │ 브랜치                                │ 상태     │
  ├─────┼──────────────────────────────────────┼──────────┤
  │ #1  │ security/issue-1-card-number-masking  │ 생성완료 │
  │ #2  │ security/issue-2-spring-security      │ 생성완료 │
  │ ... │ ...                                   │ ...      │
  └─────┴──────────────────────────────────────┴──────────┘

  총 {N}개 브랜치 생성, {M}개 스킵 (이미 존재)

  권장 작업 순서:
  1. security/issue-1-... (Critical)
  2. security/issue-2-... (Critical)
  3. ...
═══════════════════════════════════════════
```
