plugins {
    kotlin("jvm")
    id("org.openapi.generator") version "7.14.0"
}

dependencies {
    implementation(project(":diet"))
    testImplementation(kotlin("test"))

    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.web)
    implementation(libs.spring.context)
    implementation(libs.swagger.models)
    implementation(libs.jakarta.annotation)
    implementation(libs.jakarta.validation)
    implementation(libs.jakarta.servlet)
    compileOnly(libs.swagger.annotations)
}

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/src/main/resources/openapi/api.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.path)
    apiPackage.set("pl.edu.pjwstk.s25236.diet_app.generated.api")
    modelPackage.set("pl.edu.pjwstk.s25236.diet_app.generated.model")

    additionalProperties.set(
        mapOf(
            "modelNameSuffix" to "Dto",
            "interfaceOnly" to "true",
            "apiVisibility" to "public",
            "useTags" to "true",
        )
    )

    configOptions.set(
        mapOf(
            "useSpringBoot3" to "true",
            "dateLibrary" to "java21",
            "serializationLibrary" to "jackson"
        )
    )
}

kotlin {
    sourceSets {
        main {
            kotlin.srcDir(layout.buildDirectory.dir("generated/openapi/src/main/kotlin"))
        }
    }
}

//tasks.openApiGenerate {
//    doLast {
//        val springDocConfig = layout.buildDirectory
//            .dir("generated/openapi/src/main/kotlin/org/openapitools")
//            .get()
//            .file("SpringDocConfiguration.kt")
//            .asFile
//        if (springDocConfig.exists()) {
//            springDocConfig.delete()
//        }
//    }
//}

tasks.compileKotlin {
    dependsOn(tasks.openApiGenerate)
}
