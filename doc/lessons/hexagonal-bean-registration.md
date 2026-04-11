# 헥사고날 아키텍처에서 Spring Bean 등록 패턴

## 배운 점
- application 계층의 UseCase 구현체(예: `GeneratorService`)에 `@Service` 어노테이션을 직접 붙이면 헥사고날 원칙 위반
- infrastructure/config에서 `@Configuration` + `@Bean`으로 수동 등록해야 application 계층이 Spring에 비의존
- `@Bean(destroyMethod = "shutdown")`으로 코루틴 scope 등 lifecycle 자원 정리 가능
- `@Bean` 반환 타입은 구체 클래스가 아닌 **인터페이스(UseCase)로 선언**해야 다른 계층에서 구체 클래스 직접 주입 방지

## 적용 방법
```kotlin
// infrastructure/config/SomeBeanConfig.kt
@Configuration
class SomeBeanConfig {
    @Bean(destroyMethod = "shutdown")
    fun someUseCase(outPort: SomeOutPort): SomeUseCase =  // 반환 타입: 인터페이스
        SomeService(outPort)  // 생성: 구체 클래스
}

// application/service/SomeService.kt
class SomeService(  // @Service 없음, 순수 클래스
    private val outPort: SomeOutPort,
) : SomeUseCase { ... }
```

## 관련 리뷰
- [2026-04-12 unstaged](../review/2026-04-12-unstaged.md)
