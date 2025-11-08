rootProject.name = "diet-app-backend"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
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
include("diet-app-backend")
