plugins {
    id("build-jvm")
    id("maven-publish")
}

group = "ru.otus.otuskotlin.marketplace.tests"
version = "0.1.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}

tasks {
    register("buildInfra") {
        group = "build"
        dependsOn(project(":ok-marketplace-dcompose").getTasksByName("publish",false))
    }
}