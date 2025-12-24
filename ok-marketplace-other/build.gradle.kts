plugins {
    id("build-docker") apply false
    id("build-jvm")
    id("maven-publish")
}

//group = "ru.otus.otuskotlin.marketplace.tests"
//version = "0.1.0"

group = "ru.otus.otuskotlin.marketplace"
version = "1.0"

base {
    archivesName.set("dcompose")
}

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
        dependsOn(project(":ok-marketplace-migration-cs").getTasksByName("buildImages",false))
        dependsOn(project(":ok-marketplace-migration-pg").getTasksByName("buildImages",false))
    }
}
