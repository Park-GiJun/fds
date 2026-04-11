커밋 단위 코드 리뷰를 수행한다. 6명의 전문 리뷰어 + 2명의 리드가 팀을 구성한다.

## 리뷰 절차

### Step 1: 최신 커밋 분석
`git log --oneline -5`로 최근 커밋을 확인하고, `git diff HEAD~1..HEAD`로 변경 내용을 파악한다.
인자가 주어지면 해당 커밋 범위를 사용한다. (예: `/review HEAD~3..HEAD`)

### Step 2: 리뷰팀 구성 (6 Reviewers + 2 Leads)

**병렬 실행 — 6명의 전문 리뷰어를 Agent로 동시에 실행한다.**

---

#### Reviewer 1 — 아키텍처 리뷰어 (Architect)
헥사고날 아키텍처 전문가. 구조적 결함을 찾는다.
- 헥사고날 아키텍처 준수 여부 (domain → port → application → infrastructure 의존 방향)
- domain 패키지에 Spring/JPA/Kafka 등 인프라 의존이 침투했는지
- 패키지 구조, 책임 분리, SOLID 원칙
- 서비스 간 결합도, 모듈 경계 침범
- UseCase(Port) 인터페이스 설계 적절성

#### Reviewer 2 — 보안 리뷰어 (Security)
보안 전문가. 취약점과 민감 데이터 노출을 찾는다.
- SQL Injection, XSS 등 OWASP Top 10 취약점
- 민감 데이터 노출 (카드번호 마스킹, 로그에 개인정보 등)
- 인증/인가 누락
- 의존성 보안 (알려진 CVE)
- 시크릿/크레덴셜 하드코딩

#### Reviewer 3 — 성능 리뷰어 (Performance)
성능 엔지니어. 병목과 확장성 문제를 찾는다.
- N+1 쿼리, 불필요한 DB 호출
- Kafka Producer/Consumer 설정 (배치 사이즈, ack, 파티션)
- Redis 키 설계, TTL, 메모리 효율
- ES 벌크 인덱싱, 쿼리 최적화
- 동시성 이슈 (Race Condition, Thread Safety)
- 커넥션 풀, 스레드 풀 사이징

#### Reviewer 4 — 코드 품질 리뷰어 (Code Quality)
클린 코드 전문가. 가독성과 유지보수성을 본다.
- 코드 중복, 불필요한 복잡도
- 네이밍 컨벤션 (클래스, 함수, 변수)
- Kotlin 관용구 활용 (data class, sealed class, extension, scope function)
- 함수 길이, 파라미터 개수
- 매직 넘버, 하드코딩 상수
- 주석 필요성 (없어야 할 주석, 있어야 할 주석)

#### Reviewer 5 — 테스트 리뷰어 (Testing)
테스트 전문가. 테스트 전략과 커버리지를 본다.
- 변경된 코드에 대한 테스트 존재 여부
- 테스트 케이스 충분성 (정상, 에러, 경계값)
- 테스트 격리성 (다른 테스트에 영향 주지 않는지)
- mock 사용 적절성 (과도한 mock, 실제 동작과 괴리)
- 테스트 네이밍, 가독성
- 누락된 테스트 시나리오 제안

#### Reviewer 6 — 도메인/비즈니스 리뷰어 (Domain Expert)
FDS 도메인 전문가. 비즈니스 로직의 정확성을 본다.
- 이상거래 탐지 규칙의 정확성, 누락된 케이스
- 도메인 모델링 적절성 (Entity, VO 구분)
- 비즈니스 용어와 코드 네이밍 일관성 (Ubiquitous Language)
- 엣지 케이스 (금액 0, 동일 시각 거래, 국가 코드 오류 등)
- 도메인 이벤트 설계 적절성

---

### Step 3: 리드 리뷰 (순차 실행 — 리뷰어 6명 완료 후)

6명의 리뷰 결과를 종합하여 2명의 리드가 최종 판정한다.
리드는 반드시 아래 문서를 먼저 읽고 리뷰에 반영한다:
- `doc/memory/project-context.md` (프로젝트 컨텍스트, 반복 실수)
- `doc/memory/review-checklist.md` (기술/품질 체크리스트)
- `doc/memory/domain-glossary.md` (도메인 용어 사전)

#### Lead 1 — 기술 리드 (Tech Lead)
Reviewer 1~3 (아키텍처, 보안, 성능) 결과를 종합한다.
- 6명의 리뷰 결과 간 **충돌/모순** 확인 (예: 성능을 위해 보안 희생하는 제안)
- 심각도 조정 (과도하게 높거나 낮은 판정 보정)
- `doc/memory/project-context.md`를 참조하여:
  - 이전 리뷰에서 지적된 것이 수정되었는지 확인
  - 반복되는 실수 패턴 감지 (2회 이상 → 패턴 등록)
- **기술 관점 최종 Action Items** 확정
- **전체 기술 심각도** 판정: LOW / MEDIUM / HIGH / CRITICAL

#### Lead 2 — 품질 리드 (Quality Lead)
Reviewer 4~6 (코드 품질, 테스트, 도메인) 결과를 종합한다.
- 코드 품질 이슈 중 **당장 수정할 것 vs 기술 부채로 기록할 것** 분류
- 테스트 리뷰어가 제안한 시나리오의 **우선순위** 매기기
- 도메인 리뷰어 피드백을 바탕으로 **학습 내용(lesson)** 추출
- **품질 관점 최종 Action Items** 확정
- **전체 품질 심각도** 판정: LOW / MEDIUM / HIGH / CRITICAL

---

### Step 4: 리뷰 결과 기록
`doc/review/YYYY-MM-DD-{커밋해시앞7자리}.md`에 저장:

```markdown
# Code Review — {커밋 메시지}

- **커밋**: {hash}
- **날짜**: {date}
- **변경 파일**: {files}
- **기술 심각도**: {Tech Lead 판정}
- **품질 심각도**: {Quality Lead 판정}

---

## 1. Architect Review
### 잘한 점
### 개선 필요
### 심각도: {LOW | MEDIUM | HIGH | CRITICAL}

## 2. Security Review
### 취약점
### 권고사항
### 심각도: {LOW | MEDIUM | HIGH | CRITICAL}

## 3. Performance Review
### 성능 이슈
### 최적화 제안
### 심각도: {LOW | MEDIUM | HIGH | CRITICAL}

## 4. Code Quality Review
### 잘한 점
### 개선 필요
### 심각도: {LOW | MEDIUM | HIGH | CRITICAL}

## 5. Testing Review
### 테스트 현황
### 누락된 테스트 시나리오
### 심각도: {LOW | MEDIUM | HIGH | CRITICAL}

## 6. Domain Review
### 비즈니스 로직 정확성
### 도메인 모델링 피드백
### 심각도: {LOW | MEDIUM | HIGH | CRITICAL}

---

## Tech Lead 종합
### 기술 판정
### 이전 리뷰 대비 개선/퇴보
### 반복 실수 감지

## Quality Lead 종합
### 품질 판정
### 기술 부채 등록 항목
### 학습 포인트

---

## Action Items (즉시 수정)
- [ ] {수정 사항} — 출처: {Reviewer}
- [ ] {수정 사항} — 출처: {Reviewer}

## Tech Debt (기록만)
- {항목} — 이유: {왜 지금 안 하는지}
```

### Step 5: 학습 내용 기록
Quality Lead가 추출한 학습 포인트를 `doc/lessons/`에 기록한다.
이미 같은 주제의 파일이 있으면 업데이트한다.

파일명: `doc/lessons/{주제}.md`
```markdown
# {주제}

## 배운 점
## 적용 방법
## 관련 리뷰
- [{날짜} {커밋}](../review/{리뷰파일})
```

### Step 6: 메모리 업데이트
`doc/memory/project-context.md`에 Tech Lead의 분석을 반영하여 누적 업데이트한다:
- 현재까지 구현된 기능
- 반복되는 실수 패턴 (리뷰에서 2회 이상 지적된 것)
- 확립된 코드 컨벤션
- 미해결 기술 부채 (Quality Lead가 분류한 것)

이 메모리는 다음 리뷰 시 Tech Lead가 참조하여:
- **같은 지적을 반복하지 않는다**
- **이전 지적이 수정되었는지 확인한다**
- **반복 실수가 패턴화되면 경고 레벨을 올린다**

### Step 7: 요약 출력
리뷰 완료 후 터미널에 간결한 요약을 출력한다:

```
═══════════════════════════════════════════
  FDS Code Review Report — {커밋 해시}
═══════════════════════════════════════════

  기술 심각도: {LEVEL}  |  품질 심각도: {LEVEL}

  ┌─────────────────┬──────────┐
  │ Reviewer        │ Severity │
  ├─────────────────┼──────────┤
  │ Architect       │ {LEVEL}  │
  │ Security        │ {LEVEL}  │
  │ Performance     │ {LEVEL}  │
  │ Code Quality    │ {LEVEL}  │
  │ Testing         │ {LEVEL}  │
  │ Domain          │ {LEVEL}  │
  └─────────────────┴──────────┘

  Action Items: {N}건 (즉시 수정)
  Tech Debt: {N}건 (기록)
  Lessons: {N}건 (신규 학습)
  반복 실수: {있으면 경고 표시}

  리뷰 파일: doc/review/{파일명}
═══════════════════════════════════════════
```
