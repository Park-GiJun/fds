plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "com.gijun"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(Versions.JAVA)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(Dependencies.Kotlin.REFLECT)

    testImplementation(Dependencies.Test.KOTLIN_TEST)
    testImplementation(Dependencies.Test.MOCKK)
    testImplementation(Dependencies.Test.KOTEST_RUNNER)
    testImplementation(Dependencies.Test.KOTEST_ASSERTIONS)
    testImplementation(Dependencies.Test.KOTEST_PROPERTY)
    testRuntimeOnly(Dependencies.Test.JUNIT_LAUNCHER)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform {
        excludeEngines("kotest")
    }
}
