plugins {
    kotlin("jvm")
    id("org.openapi.generator") version "7.17.0"
    alias(libs.plugins.kotlin.noarg)
}

dependencies {
    api(project(":diet"))
    testImplementation(kotlin("test"))
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.context)
    implementation(libs.swagger.models)
    implementation(libs.jakarta.annotation)
    implementation(libs.jakarta.validation)
    implementation(libs.jakarta.servlet)
    compileOnly(libs.swagger.annotations)
    implementation(libs.modelmapper)
}

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/src/main/resources/openapi/api.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.path)
    apiPackage.set("pl.edu.pjwstk.s25236.diet_app.generated.api")
    modelPackage.set("pl.edu.pjwstk.s25236.diet_app.generated.model")

    configOptions.set(
        mapOf(
            "useSpringBoot3" to "true",
            "interfaceOnly" to "true",
            "useTags" to "true",
            "skipDefaultInterface" to "true",
            "dateLibrary" to "java21",
            "serializationLibrary" to "jackson",
            "generateSupportingFiles" to "false",
            "basePackage" to "pl.edu.pjwstk.s25236.diet_app.generated",
            "apiVisibility" to "public",
            "additionalModelTypeAnnotations" to "@pl.edu.pjwstk.s25236.diet_app.NoArg",
            "modelMutable" to "true"
        )
    )

    additionalProperties.set(
        mapOf(
            "modelNameSuffix" to "Dto",
            "generatedAnnotation" to "true",
            "generateMetadata" to "true"
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

noArg {
    annotation("pl.edu.pjwstk.s25236.diet_app.NoArg")
}

tasks.compileKotlin {
    dependsOn(tasks.openApiGenerate)
}
