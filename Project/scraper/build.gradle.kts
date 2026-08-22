plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

application {
    mainClass.set("scraper.MainKt")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":diet"))
    implementation(project(":persistence"))
    implementation(libs.playwright)
    implementation(libs.okhttp)
    implementation(libs.deepl)
    implementation(libs.coroutines.core)
    implementation(libs.jackson.kotlin)
    implementation(libs.apache.poi)
    implementation(libs.jackson.databind)
    implementation(libs.postgresql)
    implementation(libs.jooq)
    implementation(libs.hikari)
}
