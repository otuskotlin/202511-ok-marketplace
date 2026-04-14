package ru.otus.otuskotlin.marketplace.plugin

class DockerImagesExtension {
    private val _images = mutableMapOf<String, DockerImageExtension>()

    val images: Map<String, DockerImageExtension> get() = _images

    fun register(name: String, configure: DockerImageExtension.() -> Unit): DockerImageExtension {
        val ext = DockerImageExtension()
        println("Registering image: $name")
        configure(ext)
        println("Registered image: $name with ext: ${ext.imageName}")
        _images[name] = ext
        println("Added to map: $name - total: ${_images.size}")
        return ext
    }

    val registeredNames: Set<String> get() = _images.keys
}
