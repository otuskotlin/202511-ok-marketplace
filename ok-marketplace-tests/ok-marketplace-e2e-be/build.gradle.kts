plugins {
    id("build-jvm")
    alias(libs.plugins.kotlinx.serialization)
}

repositories {
    maven {
        name = "LocalRepo"
        url = uri("${rootProject.projectDir}/../ok-marketplace-other/build/repo")
    }
}

val resourcesFromLib by configurations.creating

dependencies {
    implementation(kotlin("stdlib"))

    resourcesFromLib("ru.otus.otuskotlin.marketplace:dcompose:1.0:resources@zip")

    implementation("ru.otus.otuskotlin.marketplace:ok-marketplace-api-v1-jackson")
    implementation("ru.otus.otuskotlin.marketplace:ok-marketplace-api-v1-mappers")
    implementation("ru.otus.otuskotlin.marketplace:ok-marketplace-api-v2-kmp")
    implementation("ru.otus.otuskotlin.marketplace:ok-marketplace-stubs")

    testImplementation(kotlin("test-junit5"))

    testImplementation(libs.logback)
    testImplementation(libs.kermit)

    testImplementation(libs.bundles.kotest)

    testImplementation(libs.testcontainers.core)
    testImplementation(libs.coroutines.core)

    testImplementation(libs.ktor.client.core)
    testImplementation(libs.ktor.client.okhttp)
    testImplementation(libs.kotlinx.serialization.core)
    testImplementation(libs.kotlinx.serialization.json)
}

var severity: String = "MINOR"

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
        dependsOn("extractLibResources")
    }
    register<Copy>("extractLibResources") {
        from(zipTree(resourcesFromLib.singleFile))
        into(layout.buildDirectory.dir("dcompose"))
    }
}
