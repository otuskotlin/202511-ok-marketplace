rootProject.name = "ok-marketplace-tests"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

pluginManagement {
    includeBuild("../build-plugin")
    plugins {
        id("build-jvm") apply false
        id("build-kmp") apply false
    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

//include(":ok-marketplace-api-v1-jackson")
//include(":ok-marketplace-api-v2-kmp")
include(":ok-marketplace-e2e-be")
