package com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.transaction.adapter

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * TransactionPersistenceAdapter Testcontainers 통합 테스트 — 스캐폴드.
 *
 * 현재 비활성 사유: Spring Boot 4.0.x `spring-boot-test-autoconfigure` 아티팩트가 shim 상태로
 * `@DataJpaTest` / `@AutoConfigureTestDatabase` 클래스를 포함하지 않음. Spring Boot 4 test 슬라이스
 * 재구성 대응이 필요.
 *
 * 활성화 방법 (재도전):
 * 1. Spring Boot 4 전용 test 슬라이스 아티팩트 확인 — spring-boot-data-jpa-autoconfigure 등
 * 2. 또는 `@SpringBootTest` + Testcontainers + 직접 JPA 설정으로 우회
 * 3. Docker Desktop WSL 통합 활성
 * 4. 본 파일의 @Disabled 제거
 *
 * 관련: RF-2 / #158 재검토 필요
 */
@Disabled("Spring Boot 4.0 test 슬라이스 재구성 — @DataJpaTest 부재. 후속 이슈 필요")
class TransactionPersistenceAdapterIntegrationTest {

    @Test
    fun placeholder() {
        // Disabled until Spring Boot 4 test slice strategy decided
    }
}
