package ru.otus.otuskotlin.marketplace.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.provider.Property
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.StopExecutionException
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.collections.forEach

abstract class DockerBuildTask : DefaultTask() {

    init {
        super.setGroup("docker")
    }

    @get:Input
    abstract val dockerFile: Property<String>

    @get:Input
    abstract val imageName: Property<String>

    @get:Input
    abstract val imageTag: Property<String>

    @get:Input
    abstract val buildContext: Property<String>

    @get:Input
    @get:Optional
    abstract val buildArgs: MapProperty<String, String>

    @get:Input
    @get:Optional
    abstract val noCache: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val removeIntermediateContainers: Property<Boolean>

    interface InjectedExecOps {
        @get:Inject
        val execOps: ExecOperations
    }

    @TaskAction
    fun build() {
        val dockerfilePath = "${buildContext.get()}/${dockerFile.get()}"
        val fullImageName = "${imageName.get()}:${imageTag.get()}"

        logger.lifecycle("Building Docker image: $fullImageName")

        val command = mutableListOf<String>("docker", "build")
        command.add("-t")
        command.add(fullImageName)
        command.add("-f")
        command.add(dockerfilePath)

        if (noCache.get()) {
            command.add("--no-cache")
        }

        if (removeIntermediateContainers.get()) {
            command.add("--rm")
        }

        buildArgs.get().forEach { (key, value) ->
            command.add("--build-arg")
            command.add("$key=$value")
        }

        command.add(buildContext.get())

        val outputStream = ByteArrayOutputStream()
        val execResult = project.exec {
            commandLine(command)
            standardOutput = outputStream
            errorOutput = outputStream
            isIgnoreExitValue = true
            workingDir = project.rootProject.layout.projectDirectory.asFile
        }

        if (execResult.exitValue != 0) {
            logger.error("Docker build failed:\n${outputStream}")
            throw StopExecutionException("Docker build failed with exit code ${execResult.exitValue}")
        }

        logger.lifecycle("Docker image built successfully: $fullImageName")
        logger.quiet(outputStream.toString())
    }
}