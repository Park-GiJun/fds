plugins {
    id("fds-spring-boot")
}

dependencies {
    implementation(Dependencies.SpringCloud.GATEWAY_MVC)
    implementation(Dependencies.Cache.CAFFEINE)
}
