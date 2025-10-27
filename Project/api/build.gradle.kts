plugins {
    kotlin("jvm")
    id("org.openapi.generator") version "7.0.0"
}

dependencies {
    implementation(project(":diet"))
    testImplementation(kotlin("test"))
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
            "useTags" to "true"
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

tasks.compileKotlin {
    dependsOn(tasks.openApiGenerate)
}
