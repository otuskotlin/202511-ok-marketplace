plugins {
    id("build-jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(kotlin("stdlib"))
    implementation(projects.okMarketplaceApiV1Jackson)
    implementation(projects.okMarketplaceCommon)

    testImplementation(kotlin("test-junit"))
    testImplementation(projects.okMarketplaceStubs)
}
