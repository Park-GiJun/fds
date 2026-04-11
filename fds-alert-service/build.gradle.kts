plugins {
    id("fds-spring-boot")
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
}
