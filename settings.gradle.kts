pluginManagement {
    repositories {
        maven { url = uri(rootProject.projectDir.absolutePath + "/repo") }
        gradlePluginPortal()
    }
    plugins {
        kotlin("jvm") version ("1.5.31")
    }
}

rootProject.name = "persist"

include("sample_app")
include("processor")
include("persist_plugin")
