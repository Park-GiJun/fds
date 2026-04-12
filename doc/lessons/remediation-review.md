# Remediation Review — 일괄 처리 후 follow-up 필수

> 리뷰 489688d (2026-04-12) — 14개 이슈 병렬 remediation 후 검증

## 배경
2026-04-12 리뷰 8cfdaae에서 14개 Action Items/Tech Debt를 등록하고, 같은 날 병렬 에이전트로 14개 브랜치에 일괄 구현 후 master에 merge했다. 이후 follow-up 리뷰에서 다음 문제를 발견:

1. **기존 기능 regression**: `logback-spring.xml`을 간소화하는 과정에서 기존 FILE appender / framework logger level / springProfile 블록 손실 의심
2. **학습의 미반영**: `doc/lessons/card-number-handling.md`가 권장한 "Transaction 도메인 모델에서 cardNumber 원문 필드 제거"가 remediation에 포함되지 않음
3. **부분 적용**: `ApiResponse` 분리가 transaction-service에만 적용, 다른 서비스 및 같은 서비스 내 `GlobalExceptionHandler`에도 미적용
4. **Dead code 생성**: `CountryCode`/`CurrencyCode` VO를 만들었지만 Transaction/Request에서 미사용
5. **원칙 슬립**: `@Transactional`을 application 계층에 선언했으나 CLAUDE.md 예외 규정 미명시

## 규칙

### 규칙 1: Remediation은 코드 변경이다 → 리뷰 대상이다
일괄 이슈 처리 후 "완료"로 종결하지 말고 **follow-up 리뷰**를 수행한다. 원본 리뷰가 지적한 방향이 실제로 적용됐는지, 새 regression이 없는지 체크한다.

### 규칙 2: 설정 파일 재작성은 원본 우선
`logback-spring.xml`, `application.yml` 같은 설정 파일은 **patch 모드**(일부만 수정)로 접근. 전체 재작성은 이전 설정을 실수로 삭제하기 쉽다. 병렬 에이전트에게 "기존 파일 replace"가 아닌 "특정 섹션만 add/update" 지시.

### 규칙 3: Dead code 금지 — VO/포트 생성 시 즉시 적용
`CountryCode`/`CurrencyCode`처럼 타입을 만들고 사용처 전환을 "후속 작업"으로 미루면 미적용 상태로 남는다. **생성과 적용을 한 커밋에** 묶거나, 적용 대상이 너무 큰 경우 아예 생성을 뒤로 미룬다.

### 규칙 4: 예외 규정은 코드와 함께 문서에 반영
코딩 규칙(예: "application 계층에 Spring 어노테이션 금지")에 예외가 필요하면(예: `@Transactional`), 코드 커밋과 **동일한 커밋**에서 CLAUDE.md에 예외 규정을 추가한다. "예외라서 괜찮다"고 말로만 합의하면 다음 리뷰에서 재지적된다.

### 규칙 5: 학습(lessons)은 코드 변경을 동반해야 산다
리뷰에서 lessons 문서를 쓰고 "권장사항"만 적고 끝내면 잊힌다. 권장사항의 실행 가능한 항목은 **즉시 이슈로 등록**하여 다음 sprint에 편입한다. 본 리뷰의 RF-6(`Transaction.cardNumber` 원문 제거)가 그 예시.

### 규칙 6: 분리 작업은 전부 or 일관 선언
`ApiResponse`를 transaction-service에만 분리하고 다른 서비스는 미이행하면 "부분 분리"라는 어정쩡한 상태가 남아 일관성이 없어진다. 분리한다면 (a) 모든 서비스에 한 스프린트 안에 적용하거나, (b) 시작하지 말고 Tech Debt로만 남긴다.

## 프로세스 제안

- `/review` 커맨드: 이전 리뷰에 대한 follow-up 모드 추가 — `git log`에서 이전 리뷰의 Action Items가 해결됐는지 자동 확인
- `/ship --merge` Gate Keeper: "설정 파일(*.yml, *.xml) 커밋은 diff patch만 허용, 전체 재작성 금지" 체크
- remediation 일괄 처리 시 **원본 리뷰 lessons.md 필수 참조** — 에이전트 프롬프트에 포함

## 관련 이슈
- 본 follow-up 리뷰의 RF-1 (logback 복원), RF-6 (cardNumber 원문 제거), RTD-3/4/5 (부분 적용 정리)
