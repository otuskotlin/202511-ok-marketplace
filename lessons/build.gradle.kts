plugins {
    kotlin("jvm") apply false
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
}

group = "ru.otus.otuskotlin.marketplace"
version = "0.0.1"

repositories {
    mavenCentral()
}

subprojects {
    repositories {
        mavenCentral()
    }
    group = rootProject.group
    version = rootProject.version
}
