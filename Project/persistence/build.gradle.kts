plugins {
    alias(libs.plugins.flyway)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.jooq)
    groovy
}

dependencies {
    implementation(project(":diet"))
    implementation(libs.bundles.db)
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.boot.starter)
    implementation(libs.jackson.module.kotlin)
    jooqCodegen(libs.postgresql)
    jooqCodegen(libs.jooq.meta.extensions)
    runtimeOnly(libs.flyway.database.postgresql)
    runtimeOnly(libs.postgresql)
    implementation(libs.vavr)
    implementation(libs.vavr.kotlin)
    implementation(libs.vavr.jackson)

    testImplementation(kotlin("test"))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(platform(libs.spock.bom))
    testImplementation(libs.spock.core)
    testImplementation(libs.spock.spring)
    testRuntimeOnly(libs.junit.platform.launcher)
}

buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath(libs.flyway.database.postgresql)
        classpath(libs.postgresql)
    }
}

val dbUrl = providers.gradleProperty("db.url")
    .orElse(System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/diet_db")

val dbUser = providers.gradleProperty("db.user")
    .orElse(System.getenv("DB_USER") ?: "diet-generator-agent")

val dbPassword = providers.gradleProperty("db.password")
    .orElse(System.getenv("DB_PASSWORD") ?: "postgres")

val dbSchema = providers.gradleProperty("db.schema")
    .orElse("diet_app")

jooq {
    configuration {
        logging = org.jooq.meta.jaxb.Logging.WARN

        jdbc {
            driver = "org.postgresql.Driver"
            url = dbUrl.get()
            user = dbUser.get()
            password = dbPassword.get()
        }

        generator {
            name = "org.jooq.codegen.KotlinGenerator"

            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = dbSchema.get()
                includes = ".*"
                excludes = "flyway_schema_history|pg_.*"
            }

            target {
                packageName = "pl.edu.pjwstk.s25236.dietgenerator.jooq"
                directory = layout.buildDirectory.dir("generated/jooq").get().asFile.path
            }
        }
    }
}

flyway {
    url = dbUrl.get()
    user = dbUser.get()
    password = dbPassword.get()
    schemas = arrayOf(dbSchema.get())
    locations = arrayOf("filesystem:src/main/resources/db/migration")
}

sourceSets {
    named("main") {
        kotlin.srcDir(layout.buildDirectory.dir("generated/jooq"))

    }
}

tasks.named("jooqCodegen") {
    dependsOn("flywayMigrate")
    inputs.files(fileTree("src/main/resources/db/migration"))
}

tasks.named("compileKotlin") {
    dependsOn(tasks.named("jooqCodegen"))
}
