object Dependencies {

    object Spring {
        const val WEB = "org.springframework.boot:spring-boot-starter-web"
        const val ACTUATOR = "org.springframework.boot:spring-boot-starter-actuator"
        const val VALIDATION = "org.springframework.boot:spring-boot-starter-validation"
        const val JPA = "org.springframework.boot:spring-boot-starter-data-jpa"
        const val KAFKA = "org.springframework.boot:spring-boot-starter-kafka"
        const val REDIS = "org.springframework.boot:spring-boot-starter-data-redis"
        const val ELASTICSEARCH = "org.springframework.boot:spring-boot-starter-data-elasticsearch"
        const val FLYWAY = "org.springframework.boot:spring-boot-starter-flyway"
    }

    object SpringCloud {
        const val BOM = "org.springframework.cloud:spring-cloud-dependencies:${Versions.SPRING_CLOUD}"
        const val GATEWAY_MVC = "org.springframework.cloud:spring-cloud-gateway-server-webmvc"
        const val EUREKA_CLIENT = "org.springframework.cloud:spring-cloud-starter-netflix-eureka-client"
        const val RESILIENCE4J = "org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j"
    }

    object Kotlin {
        const val REFLECT = "org.jetbrains.kotlin:kotlin-reflect"
    }

    object Ktor {
        const val CLIENT_CORE = "io.ktor:ktor-client-core:${Versions.KTOR}"
        const val CLIENT_CIO = "io.ktor:ktor-client-cio:${Versions.KTOR}"
        const val CLIENT_CONTENT_NEGOTIATION = "io.ktor:ktor-client-content-negotiation:${Versions.KTOR}"
        const val SERIALIZATION_JACKSON = "io.ktor:ktor-serialization-jackson:${Versions.KTOR}"
        const val CLIENT_LOGGING = "io.ktor:ktor-client-logging:${Versions.KTOR}"
    }

    object Exposed {
        const val CORE = "org.jetbrains.exposed:exposed-core:${Versions.EXPOSED}"
        const val JDBC = "org.jetbrains.exposed:exposed-jdbc:${Versions.EXPOSED}"
        const val KOTLIN_DATETIME = "org.jetbrains.exposed:exposed-kotlin-datetime:${Versions.EXPOSED}"
        const val JSON = "org.jetbrains.exposed:exposed-json:${Versions.EXPOSED}"
    }

    object Database {
        const val POSTGRESQL = "org.postgresql:postgresql"
        const val FLYWAY_POSTGRESQL = "org.flywaydb:flyway-database-postgresql"
    }

    object Observability {
        const val MICROMETER_PROMETHEUS = "io.micrometer:micrometer-registry-prometheus"
    }

    object Docs {
        const val SPRINGDOC = "org.springdoc:springdoc-openapi-starter-webmvc-ui:${Versions.SPRINGDOC}"
    }

    object Jackson {
        const val KOTLIN_MODULE = "tools.jackson.module:jackson-module-kotlin"
    }

    object Test {
        const val SPRING_BOOT_TEST = "org.springframework.boot:spring-boot-starter-test"
        const val KOTLIN_TEST = "org.jetbrains.kotlin:kotlin-test-junit5"
        const val JUNIT_LAUNCHER = "org.junit.platform:junit-platform-launcher"
    }
}
