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
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")

    testImplementation(Dependencies.Test.SPRING_BOOT_TEST)
}
