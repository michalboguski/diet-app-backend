plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
kotlin("plugin.spring")
groovy
}

dependencies {
    implementation(project(":api"))
    implementation(project(":diet"))
    implementation(project(":persistence"))
    implementation(project(":scraper"))
    implementation(project(":translate"))

    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.25")


    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    implementation("org.modelmapper:modelmapper:3.2.0")

    implementation("io.vavr:vavr:0.10.2")
    implementation("io.vavr:vavr-kotlin:0.10.2")
    implementation("io.vavr:vavr-jackson:0.10.2")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")


    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.spockframework:spock-bom:2.3-groovy-4.0"))
    testImplementation("org.spockframework:spock-core")
    testImplementation("org.spockframework:spock-spring")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

repositories {
    mavenCentral()
}

springBoot {
    mainClass.set("pl.edu.pjwstk.s25236.diet_app.DietAppBackendKt")
}


tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    jvmArgs("-Xms1g","-Xmx2g","-XX:MaxMetaspaceSize=4g")
}

tasks.named("bootRun") {
    dependsOn(
        ":api:openApiGenerate",
        ":persistence:flywayMigrate",
        ":persistence:jooqCodegen"
    )
}
