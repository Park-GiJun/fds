plugins {
    id("fds-spring-boot")
}

dependencies {
    // Coroutines (Generator 내부 동시 전송용)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    // Ktor Client (거래 API 호출용)
    implementation(Dependencies.Ktor.CLIENT_CORE)
    implementation(Dependencies.Ktor.CLIENT_CIO)
    implementation(Dependencies.Ktor.CLIENT_CONTENT_NEGOTIATION)
    implementation(Dependencies.Ktor.SERIALIZATION_JACKSON)
    implementation(Dependencies.Ktor.CLIENT_LOGGING)
}
