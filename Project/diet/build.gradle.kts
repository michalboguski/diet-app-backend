plugins {
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

repositories {
    mavenCentral()
}

dependencies {

    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.5"))

    testImplementation(kotlin("test"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.25")
    implementation("io.vavr:vavr:0.10.2")
    implementation("io.vavr:vavr-kotlin:0.10.2")
    implementation("io.vavr:vavr-jackson:0.10.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}
