package com.gijun.fds.transaction.infrastructure.adapter.outbound.persistence.transaction.adapter

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * TransactionPersistenceAdapter Testcontainers 통합 테스트 — 스캐폴드.
 *
 * 현재 비활성 사유: fds-transaction-service 테스트 classpath에 spring-boot-starter-data-jpa-test
 * 의존성이 없어 `@DataJpaTest`/`@AutoConfigureTestDatabase` import 불가.
 *
 * 활성화 방법:
 * 1. build.gradle.kts에 testImplementation("org.springframework.boot:spring-boot-starter-test") 추가
 * 2. 본 파일의 @Disabled 제거 + 실제 테스트 구현체(아래 주석 참고)로 복원
 * 3. Docker 환경에서 Testcontainers 활성 필요 (WSL Docker Desktop 통합)
 *
 * 원본 설계 (주석):
 * ```
 * @DataJpaTest
 * @AutoConfigureTestDatabase(replace = Replace.NONE)
 * @Testcontainers
 * class TransactionPersistenceAdapterIntegrationTest {
 *     @Autowired private lateinit var jpaRepo: TransactionJpaRepository
 *     private val encryptor = object : CardEncryptor {
 *         override fun encrypt(plain: String) = "enc:$plain"
 *     }
 *     // save/find/exists/duplicate 시나리오 4건
 *     // @Container postgres = PostgreSQLContainer("postgres:16-alpine")
 * }
 * ```
 *
 * 관련: RF-2, 후속 이슈로 의존성 추가 필요.
 */
@Disabled("의존성 추가 필요 — spring-boot-starter-test를 테스트 classpath에 추가 후 활성화")
class TransactionPersistenceAdapterIntegrationTest {

    @Test
    fun placeholder() {
        // Disabled until @DataJpaTest dependency is available
    }
}
