# 에이전트 프롬프트 명시성 — 의도는 말하고 기본값은 지정하라

> 리뷰 a388dba (2026-04-12) — #118 PassthroughCardEncryptor 에이전트 해석 불일치

## 배경

이슈 #118은 "fail-safe 강화 — 명시적 opt-in"을 요구했다. 에이전트에게 전달된 프롬프트는 `ConditionalOnProperty` + `FDS_CRYPTO_PASSTHROUGH_ENABLED:true`를 지시하고 "(note: default is true for dev convenience; prod deployments must explicitly set to false)"라고 덧붙였다.

에이전트는 지시대로 구현했다. **그런데 지시 자체가 틀렸다**. fail-safe opt-in의 의미는 "기본 OFF, 명시적으로 ON". 프롬프트에서 "기본 true"를 지시한 순간 fail-open으로 귀결.

## 교훈

**1. 에이전트는 이슈 본문을 읽지 않는다 — 프롬프트만 본다.**
프롬프트가 이슈와 모순되면 이슈 본문의 의도는 증발한다.

**2. 보안 기본값은 편의에 양보하지 않는다.**
- 기본 true + prod에서 false 설정 누락 → **평문 저장 (치명)**
- 기본 false + dev에서 true 설정 누락 → **기동 실패 (즉시 발견)**
후자가 압도적으로 안전. 모든 기본값은 **최악의 경우 손해가 가장 작은 방향**으로.

**3. 에이전트 프롬프트에 "왜"를 적어라.**
"default false"만 적으면 에이전트가 "왜?"를 추론하며 반대 선택을 할 수 있다. 의도를 명시해야 한다.

**4. 리뷰어(나) 자신의 의견 드리프트를 경계하라.**
이슈 작성 시 확신했던 방향이 프롬프트 작성 단계에서 "dev 편의" 같은 끼어듦으로 뒤집힐 수 있다. 에이전트 프롬프트 작성 전 원 이슈 본문을 한 번 더 읽는 단계를 강제화.

## 프롬프트 체크리스트

- [ ] 이슈 본문의 의도를 1문장 요약하여 프롬프트 맨 앞
- [ ] 보안/불변 관련 기본값은 fail-closed 고정
- [ ] 기본값 지시 시 **왜 그 값인지** 한 줄 주석
- [ ] dev 편의는 기본값이 아니라 **로컬 오버라이드 경로**로
- [ ] 이슈 본문 핵심 문구를 프롬프트에 그대로 인용

## 관련
- AF-1: 본 패턴의 remediation Action Item
- doc/lessons/remediation-review.md
