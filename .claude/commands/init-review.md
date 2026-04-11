FDS 프로젝트 코드 리뷰 시스템을 초기화한다. 프로젝트 전체를 분석하여 기준선(Baseline)을 수립한다.

**이 커맨드는 프로젝트 시작 시 1회만 실행한다. 이후 `/review`로 커밋 단위 리뷰를 수행한다.**

---

## Step 1: 프로젝트 전체 스캔

현재 프로젝트의 전체 소스 코드를 분석한다.
- `find . -name "*.kt" -not -path "*/build/*"` 로 전체 Kotlin 파일 목록
- `find . -name "*.yml" -o -name "*.kts" -o -name "*.sql" -not -path "*/build/*"` 로 설정 파일 목록
- `git log --oneline -20` 으로 최근 커밋 히스토리
- `README.md` 읽어서 프로젝트 설계 의도 파악

---

## Step 2: 6인 리뷰어 Baseline 분석 (병렬 Agent)

각 리뷰어가 **현재 코드베이스 전체**를 자신의 관점에서 분석한다.

#### Reviewer 1 — Architect
- 전체 모듈 구조와 의존 관계 맵핑
- 헥사고날 아키텍처 준수 현황 (모듈별 점검)
- 현재 위반 사항 목록
- 구조적 기술 부채 식별

#### Reviewer 2 — Security
- 전체 코드의 보안 취약점 스캔
- application.yml 내 민감 정보 노출 확인
- 의존성 보안 점검 (build.gradle.kts)
- 보안 관련 설정 누락 항목 (CORS, CSRF, Rate Limit 등)

#### Reviewer 3 — Performance
- 현재 인프라 설정 분석 (docker-compose.yml, application.yml)
- Kafka/Redis/ES 설정값 적절성
- 커넥션 풀 / 스레드 풀 기본값 확인
- 잠재적 병목 지점 식별

#### Reviewer 4 — Code Quality
- 전체 코드의 네이밍 컨벤션 현황 분석
- Kotlin 관용구 사용 수준
- 코드 중복 패턴 탐지
- 현재 코드 스타일 기준선 수립 (이후 리뷰의 비교 기준)

#### Reviewer 5 — Testing
- 현재 테스트 코드 존재 여부 및 현황
- 테스트 전략 수립 (어떤 테스트가 필요한지)
- 테스트 환경 설정 상태 (testcontainers, embedded kafka 등)
- 각 모듈별 테스트 우선순위 제안

#### Reviewer 6 — Domain Expert
- README.md 기반 도메인 설계 검증
- fds-common 이벤트 스키마 적절성
- 탐지 규칙 설계 검증 (누락된 패턴, 오탐 가능성)
- 도메인 용어 일관성 체크 (코드 네이밍 vs README 설계)

---

## Step 3: 리드 종합 (순차 실행)

#### Tech Lead
- Reviewer 1~3 결과를 종합하여 **기술 Baseline Report** 작성
- 프로젝트의 현재 기술 성숙도 판정: INITIAL / DEVELOPING / ESTABLISHED / MATURE
- 즉시 수정이 필요한 **Critical Issues** 식별
- 향후 `/review`에서 추적할 **기술 체크리스트** 확정

#### Quality Lead
- Reviewer 4~6 결과를 종합하여 **품질 Baseline Report** 작성
- 현재 코드 컨벤션 확정 (이후 이 기준으로 리뷰)
- 테스트 전략 및 우선순위 확정
- 도메인 용어 사전 초안 작성
- 향후 `/review`에서 추적할 **품질 체크리스트** 확정

---

## Step 4: 산출물 생성

### 4.1 Baseline Report
`doc/review/0000-00-00-baseline.md` 에 저장:

```markdown
# Baseline Review — 프로젝트 초기 분석

- **날짜**: {date}
- **분석 범위**: 전체 코드베이스
- **기술 성숙도**: {INITIAL | DEVELOPING | ESTABLISHED | MATURE}

## 프로젝트 현황 요약
- 전체 모듈 수: {N}
- 전체 Kotlin 파일 수: {N}
- 구현 완료 모듈: {목록}
- 미구현 모듈: {목록}

---

## 1. Architect Baseline
### 구조 현황
### 위반 사항
### 기술 부채

## 2. Security Baseline
### 취약점 현황
### 설정 누락
### 보안 권고

## 3. Performance Baseline
### 인프라 설정 현황
### 잠재적 병목
### 최적화 권고

## 4. Code Quality Baseline
### 컨벤션 현황
### 코드 스타일 기준선
### 개선 권고

## 5. Testing Baseline
### 테스트 현황
### 테스트 전략
### 우선순위

## 6. Domain Baseline
### 도메인 설계 검증
### 용어 일관성
### 누락된 도메인 규칙

---

## Tech Lead 종합
### 기술 성숙도 판정
### Critical Issues (즉시 수정)
### 기술 체크리스트 (향후 /review 추적 항목)

## Quality Lead 종합
### 품질 판정
### 확정된 코드 컨벤션
### 테스트 전략 확정
### 품질 체크리스트 (향후 /review 추적 항목)

---

## 확정된 컨벤션
{Quality Lead가 확정한 코드 컨벤션 목록}

## 도메인 용어 사전
| 한국어 | 영어 (코드) | 설명 |
|--------|------------|------|
| 거래 | Transaction | ... |
| 탐지 | Detection | ... |
| ... | ... | ... |
```

### 4.2 메모리 초기화
`doc/memory/project-context.md`를 분석 결과로 갱신:
- 구현 현황 업데이트
- 확립된 코드 컨벤션 (Quality Lead 확정본)
- 기술 부채 초기 목록 (Tech Lead 식별)
- 반복 실수 패턴: (초기이므로 비어있음)
- 기술/품질 체크리스트 등록

### 4.3 도메인 용어 사전
`doc/memory/domain-glossary.md` 생성:
```markdown
# FDS 도메인 용어 사전

> 코드 네이밍과 커뮤니케이션에서 사용하는 공식 용어 목록.
> Domain Expert Reviewer가 관리하며, `/review` 시 네이밍 일관성 검사에 사용.

| 한국어 | 영어 (코드) | 설명 | 사용 위치 |
|--------|------------|------|----------|
```

### 4.4 체크리스트
`doc/memory/review-checklist.md` 생성:
```markdown
# Review Checklist

> `/review` 실행 시 리뷰어와 리드가 참조하는 체크리스트.
> `/init-review`에서 초기 수립, 이후 리뷰에서 누적 업데이트.

## 기술 체크리스트 (Tech Lead)
- [ ] {항목}

## 품질 체크리스트 (Quality Lead)
- [ ] {항목}
```

---

## Step 5: 요약 출력

```
═══════════════════════════════════════════════════════
  FDS Code Review System — Initialization Complete
═══════════════════════════════════════════════════════

  기술 성숙도: {LEVEL}

  ┌─────────────────┬──────────┬───────────────────┐
  │ Reviewer        │ Issues   │ Critical          │
  ├─────────────────┼──────────┼───────────────────┤
  │ Architect       │ {N}건    │ {N}건             │
  │ Security        │ {N}건    │ {N}건             │
  │ Performance     │ {N}건    │ {N}건             │
  │ Code Quality    │ {N}건    │ {N}건             │
  │ Testing         │ {N}건    │ {N}건             │
  │ Domain          │ {N}건    │ {N}건             │
  └─────────────────┴──────────┴───────────────────┘

  산출물:
  ├── doc/review/0000-00-00-baseline.md
  ├── doc/memory/project-context.md (갱신)
  ├── doc/memory/domain-glossary.md (신규)
  └── doc/memory/review-checklist.md (신규)

  다음 단계: 코드 작성 후 커밋 → `/review` 실행
═══════════════════════════════════════════════════════
```
