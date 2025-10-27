plugins {
    alias(libs.plugins.flyway)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.jooq)
    groovy
}

dependencies {
    implementation(project(":diet"))

    implementation(libs.jooq)
    implementation(libs.flyway.core)
    implementation(libs.postgresql)
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

jooq {
    configuration {
        logging = org.jooq.meta.jaxb.Logging.WARN

        jdbc {
            driver = "org.postgresql.Driver"
            url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/postgres"
            user = System.getenv("DB_USER") ?: "postgres"
            password = System.getenv("DB_PASS") ?: "postgres"
        }

        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "public"
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

flyway {
    url = "jdbc:postgresql://localhost:5432/postgres"
    user = "postgres"
    password = "postgres"
    locations = arrayOf("filesystem:src/main/resources/db/migration")
}
