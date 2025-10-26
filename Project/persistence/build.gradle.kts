plugins {
    id("org.flywaydb.flyway") version "11.0.1"
    kotlin("jvm")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
    id("org.jooq.jooq-codegen-gradle") version "3.20.0"
    groovy
}

buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:10.0.1")
        classpath("org.postgresql:postgresql:42.7.4")
    }
}

dependencies {
    implementation(project(":diet"))
    implementation("org.flywaydb:flyway-core:10.0.1")
    implementation("org.flywaydb:flyway-database-postgresql:10.0.1")
    testImplementation(kotlin("test"))
    implementation("org.jooq:jooq:3.20.0")
    implementation("org.postgresql:postgresql:42.7.4")
    jooqCodegen("org.postgresql:postgresql:42.7.4")
    jooqCodegen("org.jooq:jooq-meta-extensions:3.20.0")
    implementation("io.vavr:vavr:0.10.2")
    implementation("io.vavr:vavr-kotlin:0.10.2")
    implementation("io.vavr:vavr-jackson:0.10.2")
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.5"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.vavr:vavr-jackson:0.10.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.spockframework:spock-bom:2.3-groovy-4.0"))
    testImplementation("org.spockframework:spock-core")
    testImplementation("org.spockframework:spock-spring")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
