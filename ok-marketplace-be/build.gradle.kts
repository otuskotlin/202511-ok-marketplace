plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
}

group = "ru.otus.otuskotlin.marketplace"
version = "0.0.1"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}

ext {
    val specDir = layout.projectDirectory.dir("../specs")
    set("spec-v1", specDir.file("specs-ad-v1.yaml").toString())
    set("spec-v2", specDir.file("specs-ad-v2.yaml").toString())
    set("spec-log1", specDir.file("specs-ad-log1.yaml").toString())
}

tasks {
    register("build" ) {
        group = "build"
    }
    register("clean" ) {
        group = "build"
        subprojects.forEach { proj ->
            println("PROJ $proj")
            proj.getTasksByName("clean", false).also {
                this@register.dependsOn(it)
            }
        }
    }
    register("check" ) {
        group = "verification"
        subprojects.forEach { proj ->
            println("PROJ $proj")
            proj.getTasksByName("check", false).also {
                this@register.dependsOn(it)
            }
        }
    }
    register("buildImages") {
        dependsOn(project("ok-marketplace-app-spring").tasks.getByName("bootBuildImage"))
        dependsOn(project("ok-marketplace-app-ktor").tasks.getByName("publishImageToLocalRegistry"))
        dependsOn(project("ok-marketplace-app-ktor").tasks.getByName("dockerBuild"))
    }
}
