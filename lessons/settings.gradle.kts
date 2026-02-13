pluginManagement {
    plugins {
        val kotlinVersion: String by settings
        val kotestVersion: String by settings
        kotlin("jvm") version kotlinVersion
        id("io.kotest") version kotestVersion
    }
}

rootProject.name = "lessons"

include("m1l1-first")
include("m1l2-basic")
include("m1l3-func")
include("m1l4-oop")
include("m2l1-dsl")
include("m2l2-coroutines")
include("m2l3-flows")
include("m2l4-kmp")
include("m2l5-1-interop")
include("m2l5-2-jni")
include("m2l6-gradle")
include("m4l3-testing")
