plugins {
    id("fds-kotlin-base")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencyManagement {
    imports {
        mavenBom(Dependencies.SpringCloud.BOM)
    }
}

dependencies {
    implementation(Dependencies.Spring.WEB)
    implementation(Dependencies.Spring.ACTUATOR)
    implementation(Dependencies.Spring.VALIDATION)
    implementation(Dependencies.Spring.SECURITY)
    implementation(Dependencies.Jackson.KOTLIN_MODULE)
    implementation(Dependencies.SpringCloud.EUREKA_CLIENT)
    implementation(Dependencies.SpringCloud.RESILIENCE4J)
    implementation(Dependencies.Observability.MICROMETER_PROMETHEUS)

    testImplementation(Dependencies.Test.SPRING_BOOT_TEST)
    testImplementation(Dependencies.Test.SPRING_SECURITY_TEST)
    testImplementation(Dependencies.Test.KOTEST_EXTENSIONS_SPRING)
    testImplementation(Dependencies.Test.COROUTINES_TEST)
}
