package ru.otus.otuskotlin.marketplace.plugin

import org.gradle.api.provider.MapProperty

open class DockerExtension {
    var buildContext = "./"
    var dockerFile = "Dockerfile"
    var imageTag = "latest"
    var buildArgs: MapProperty<String, String>? = null
    var noCache = false
    var removeIntermediateContainers = false
    var imageName = ""

    val images = DockerImagesExtension()
}
