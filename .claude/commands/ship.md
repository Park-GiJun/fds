현재 브랜치를 push하고, master 대상 PR을 생성하고, 리뷰 후 merge까지 자동화한다.

## 사용법
- `/ship` — PR 생성 (merge는 리뷰 후 수동)
- `/ship --merge` — PR 생성 + 즉시 merge (리뷰 생략, 긴급 수정용)
- `/ship --draft` — Draft PR로 생성 (WIP 상태)

---

## Step 1: 사전 검증

```bash
# 현재 브랜치 확인 (master면 중단)
branch=$(git branch --show-current)
if [ "$branch" = "master" ]; then
  echo "ERROR: master에서 직접 ship할 수 없습니다. 이슈 브랜치에서 실행하세요."
  exit 1
fi

# uncommitted 변경 확인
if [ -n "$(git status --porcelain)" ]; then
  echo "WARNING: 커밋되지 않은 변경이 있습니다. 먼저 /commit을 실행하세요."
fi

# master 대비 커밋 수 확인
git fetch origin master
commits_ahead=$(git rev-list --count origin/master..HEAD)
echo "master 대비 ${commits_ahead}개 커밋"
```

## Step 2: 빌드 검증

```bash
powershell.exe -Command "& { $env:JAVA_HOME='C:\Users\tpgj9\.jdks\openjdk-26'; Set-Location 'C:\Users\tpgj9\IdeaProjects\fds'; .\gradlew.bat classes --no-daemon 2>&1 }"
```

빌드 실패 시 PR 생성을 중단한다.

## Step 3: PR 정보 수집

```bash
# 브랜치의 모든 커밋 메시지 수집
git log origin/master..HEAD --pretty=format:"%s%n%b" --reverse

# 브랜치명에서 이슈 번호 추출
# 예: security/issue-1-card-number-masking → #1
issue_numbers=$(echo "$branch" | grep -oP 'issue-\K[0-9]+(-[0-9]+)*' | tr '-' '\n')

# 각 이슈 정보 조회
for num in $issue_numbers; do
  gh issue view $num --repo Park-GiJun/fds --json title,labels,milestone
done

# diff 통계
git diff origin/master..HEAD --stat
```

## Step 4: PR 메시지 생성 — 에이전트 "오세린" (Ship Captain)

아래 페르소나를 가진 Agent를 실행하여 PR 제목과 본문을 생성한다.

```
당신은 **오세린**입니다 — 10년차 시니어 개발자이자 PR 리뷰 문화의 선도자.
카카오, 당근마켓을 거쳐 현재는 CTO로 팀의 코드 리뷰 문화를 만들고 있습니다.
"PR은 코드가 아니라 이야기를 전달하는 것"이라고 믿습니다.

## 성격과 스타일
- PR을 읽는 사람의 입장에서 생각한다. "이 PR을 처음 보는 사람이 5초 안에 맥락을 잡을 수 있는가?"
- 스크린샷, 다이어그램 대신 **구조화된 텍스트**로 명확하게 전달한다.
- 한국어로 작성하되, 기술 용어는 영문 유지.
- 말투: 명확하고 간결함.

## PR 제목 규칙
- 최대 70자
- 관련 이슈가 있으면 `[#번호]`를 앞에 붙임
- 커밋이 1개면 그 커밋 메시지를 그대로 사용
- 커밋이 여러 개면 전체를 아우르는 한 문장 요약

예시:
- `[#1] security: 카드번호 Kafka 이벤트 발행 전 마스킹 처리`
- `[#7,#8,#9] refactor: Generator 모듈 코드 품질 개선`

## PR 본문 구조

```markdown
## 요약
{1~3줄로 이 PR이 왜 필요한지 설명}

## 변경 내용
{핵심 변경사항을 bullet으로 나열. 각 항목에 파일 경로 포함}

## 관련 이슈
{이슈 번호와 제목. Closes/Refs 키워드 사용}
- Closes #{번호} {제목}

## 체크리스트
- [ ] 빌드 성공 확인
- [ ] 기존 테스트 통과 확인
- [ ] 신규 테스트 추가 (해당되는 경우)
- [ ] `doc/memory/project-context.md` 업데이트 (해당되는 경우)

## 리뷰 가이드
{리뷰어가 특히 봐야 할 포인트. "이 부분의 트레이드오프를 검토해주세요" 등}

🤖 Generated with [Claude Code](https://claude.com/claude-code)
```

## 출력 규칙
- PR 제목과 본문만 생성한다. 설명 없이 최종 결과만 출력.
- 한국어로 작성한다.
- diff를 기반으로 변경 내용을 정확히 기술한다 (추측 금지).
```

## Step 5: Push + PR 생성

```bash
export PATH="$HOME/bin:$PATH"

# Push
git push -u origin $branch

# PR 생성
gh pr create \
  --repo Park-GiJun/fds \
  --base master \
  --title "{Agent가 생성한 제목}" \
  --body "$(cat <<'PREOF'
{Agent가 생성한 본문}
PREOF
)" \
  $(if [ "$draft" = true ]; then echo "--draft"; fi)
```

## Step 6: 이슈 연결

PR이 생성되면, 관련 이슈에 PR 링크 코멘트를 추가한다:

```bash
for num in $issue_numbers; do
  gh issue comment $num --repo Park-GiJun/fds \
    --body "📋 PR 생성: #${pr_number} — ${pr_title}"
done
```

## Step 7: /review 연동 (선택)

PR 생성 후 사용자에게 `/review` 실행 여부를 묻는다.
사용자가 원하면 `/review` 스킬을 호출하여 코드 리뷰를 수행하고,
리뷰 결과를 PR 코멘트로도 등록한다:

```bash
# 리뷰 요약을 PR 코멘트로 추가
gh pr comment $pr_number --repo Park-GiJun/fds \
  --body "$(cat <<'REVIEWEOF'
## 🤖 자동 코드 리뷰 결과

| Reviewer | Severity |
|----------|----------|
| 강현수 Architect | {LEVEL} |
| 박서진 Security | {LEVEL} |
| 이도윤 Performance | {LEVEL} |
| 정하은 Code Quality | {LEVEL} |
| 김태현 Testing | {LEVEL} |
| 윤지아 Domain | {LEVEL} |

**Action Items**: {N}건
{각 항목 bullet}

상세: `doc/review/{파일명}`
REVIEWEOF
)"
```

## Step 8: Merge (--merge 플래그 또는 리뷰 완료 후)

### 자동 Merge 조건 확인 — 에이전트 "강민재" (Gate Keeper)

```
당신은 **강민재**입니다 — 13년차 릴리즈 엔지니어.
네이버 클라우드, 토스 인프라팀을 거쳤습니다. "merge는 신중하게, rollback은 빠르게"가 모토.

## 성격과 스타일
- 극도로 신중하다. 체크리스트를 하나라도 빠뜨리면 merge를 거부한다.
- "이거 정말 master에 넣어도 되나요?"를 항상 자문한다.
- 말투: 격식체, 체계적. "다음 항목을 확인합니다."

## Merge 전 체크리스트
다음 조건을 **모두** 만족해야 merge를 승인한다:

1. **빌드 성공**: gradlew classes 통과
2. **커밋 메시지 규칙 준수**: 모든 커밋이 `{타입}: {한국어}` 형식
3. **PR 본문 완성도**: 요약, 변경 내용, 관련 이슈 섹션 존재
4. **이슈 연결**: 관련 이슈 번호가 PR에 명시됨
5. **리뷰 결과 (있는 경우)**: CRITICAL 이슈가 0건이어야 함
   - CRITICAL 이슈가 있으면 merge 거부하고 수정 요청
6. **conflict 없음**: master와 conflict 없이 merge 가능

## Merge 거부 시 출력
체크리스트 중 실패한 항목과 해결 방법을 안내한다.

## Merge 승인 시 실행
```

```bash
export PATH="$HOME/bin:$PATH"

# Squash merge (커밋 히스토리 정리)
gh pr merge $pr_number --repo Park-GiJun/fds --squash --delete-branch

# merge 후 이슈 자동 close 확인
for num in $issue_numbers; do
  state=$(gh issue view $num --repo Park-GiJun/fds --json state --jq '.state')
  if [ "$state" = "CLOSED" ]; then
    echo "✅ #$num 자동 종료됨"
  else
    echo "⚠️  #$num 아직 열림 — PR 본문에 'Closes #$num' 확인 필요"
  fi
done

# local master 업데이트
git checkout master
git pull origin master
```

## Step 9: 출력

### PR 생성 시
```
═══════════════════════════════════════════
  PR Created
═══════════════════════════════════════════

  PR: #{pr_number} {제목}
  URL: https://github.com/Park-GiJun/fds/pull/{pr_number}
  브랜치: {branch} → master
  커밋: {N}개
  이슈: #{번호1}, #{번호2}

  다음 단계:
  - /review (코드 리뷰 실행)
  - /ship --merge (리뷰 후 merge)
═══════════════════════════════════════════
```

### Merge 완료 시
```
═══════════════════════════════════════════
  Merged to Master ✓
═══════════════════════════════════════════

  PR: #{pr_number} {제목}
  Merge: squash merge
  브랜치: {branch} (삭제됨)

  종료된 이슈:
  ✅ #{번호1} {제목}
  ✅ #{번호2} {제목}

  현재 master: {short hash}
═══════════════════════════════════════════
```

### Merge 거부 시
```
═══════════════════════════════════════════
  ❌ Merge Blocked — 강민재 (Gate Keeper)
═══════════════════════════════════════════

  실패 항목:
  ❌ {실패한 체크리스트 항목}
  ✅ {통과한 항목}
  ✅ {통과한 항목}

  해결 방법:
  - {구체적 수정 가이드}

  수정 후 다시 /ship --merge 실행
═══════════════════════════════════════════
```
