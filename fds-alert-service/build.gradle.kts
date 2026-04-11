plugins {
    id("fds-spring-boot-service")
}

dependencies {
    implementation(project(":fds-common"))

    // Kafka
    implementation(Dependencies.Spring.KAFKA)

    // Redis
    implementation(Dependencies.Spring.REDIS)

    // JPA + Flyway
    implementation(Dependencies.Spring.JPA)
    implementation(Dependencies.Spring.FLYWAY)
    implementation(Dependencies.Database.FLYWAY_POSTGRESQL)
    runtimeOnly(Dependencies.Database.POSTGRESQL)

    // Ktor Client
    implementation(Dependencies.Ktor.CLIENT_CORE)
    implementation(Dependencies.Ktor.CLIENT_CIO)
    implementation(Dependencies.Ktor.CLIENT_CONTENT_NEGOTIATION)
    implementation(Dependencies.Ktor.SERIALIZATION_JACKSON)
    implementation(Dependencies.Ktor.CLIENT_LOGGING)

    // Test — Testcontainers + Kafka
    testImplementation(platform(Dependencies.Test.TESTCONTAINERS_BOM))
    testImplementation(Dependencies.Test.TESTCONTAINERS_JUNIT)
    testImplementation(Dependencies.Test.TESTCONTAINERS_POSTGRESQL)
    testImplementation(Dependencies.Test.TESTCONTAINERS_KAFKA)
    testImplementation(Dependencies.Test.SPRING_KAFKA_TEST)
    testImplementation(Dependencies.Test.KTOR_CLIENT_MOCK)
}
