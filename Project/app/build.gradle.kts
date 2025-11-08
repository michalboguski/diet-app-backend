plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    groovy
}

dependencies {
    implementation(project(":api"))
    implementation(project(":diet"))
    implementation(project(":persistence"))
    implementation(project(":scraper"))
    implementation(project(":translate"))

    implementation(libs.spring.boot.starter.web )
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.logging.jvm)
    implementation(libs.modelmapper)
    implementation(libs.bundles.vavr)
    implementation(libs.springdoc.openapi.ui)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(platform(libs.spock.bom))
    testImplementation(libs.bundles.spock)
    testRuntimeOnly(libs.junit.platform.launcher)
}

springBoot {
    mainClass.set("pl.edu.pjwstk.s25236.diet_app.DietAppBackendKt")
}


tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    jvmArgs("-Xms1g", "-Xmx2g", "-XX:MaxMetaspaceSize=4g")
}

tasks.named("bootRun") {
    dependsOn(
        ":api:openApiGenerate",
        ":persistence:flywayMigrate",
        ":persistence:jooqCodegen"
    )
}
