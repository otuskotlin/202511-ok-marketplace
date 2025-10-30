plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
}

group = "com.otus.otuskotlin.marketplace"
version = "0.0.1"

allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}

tasks {
    register("clean") {
        group = "build"
        gradle.includedBuilds.forEach {
            dependsOn(it.task(":clean"))
        }
    }
    val buildInfra by registering { ->
        dependsOn(
            gradle.includedBuild("ok-marketplace-other").task(":buildInfra")
        )
    }

//    val buildImages: Task by creating {
//        dependsOn(gradle.includedBuild("ok-marketplace-be").task(":buildImages"))
//        mustRunAfter(buildMigrations)
//    }
    val e2eTests by registering { ->
        dependsOn(
            gradle.includedBuild("ok-marketplace-tests").task(":e2eTests")
        )
    }

    register("check") {
        group = "verification"
        dependsOn(gradle.includedBuild("ok-marketplace-be").task(":check"))
        dependsOn(e2eTests)
    }
}

