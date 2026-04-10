plugins {
    id("build-jvm")
}
repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(projects.okMarketplaceCommon)
    api(projects.okMarketplaceRepoCommon)

    implementation(libs.coroutines.core)
    implementation(libs.uuid)

    implementation(libs.db.postgres)
//  implementation(libs.db.hikari)
    implementation(libs.bundles.exposed)

    testImplementation(kotlin("test-junit"))
    testImplementation(projects.okMarketplaceRepoTests)
    testImplementation(libs.testcontainers.core)
    testImplementation(libs.logback)

}
