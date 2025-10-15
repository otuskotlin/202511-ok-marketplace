plugins {
    id("build-jvm")
}

repositories {
    maven {
        name = "LocalRepo"
        url = uri("${rootProject.projectDir}/../ok-marketplace-other/build/repo")
    }
}

val resourcesFromLib by configurations.creating

dependencies {
    resourcesFromLib("ru.otus.otuskotlin.marketplace:dcompose:1.0:resources@zip")
}

tasks.register<Copy>("extractLibResources") {
    from(zipTree(resourcesFromLib.singleFile))
    into(layout.buildDirectory.dir("dcompose"))
}

tasks["build"].dependsOn("extractLibResources")



