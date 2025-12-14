plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    testImplementation(platform(libs.spock.bom))
    implementation(libs.spring.boot.starter)
    implementation(libs.jackson.module.kotlin)
    testImplementation(kotlin("test"))
    implementation(libs.kotlin.reflect)
    api(libs.bundles.vavr)
}
