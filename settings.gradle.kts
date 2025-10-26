rootProject.name = "diet-app-backend"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

buildCache {
    local {
        isEnabled = false
    }
    remote<HttpBuildCache> {
        isEnabled = false
    }
}

include(":api")
include(":app")
include(":diet")
include(":persistence")
include(":scraper")
include(":translate")

project(":app").projectDir = file("Project/app")
project(":api").projectDir = file("Project/api")
project(":persistence").projectDir = file("Project/persistence")
project(":diet").projectDir = file("Project/diet")
project(":scraper").projectDir = file("Project/scraper")
project(":translate").projectDir = file("Project/translate")
include("diet-generator-backend")
