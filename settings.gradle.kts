pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        jcenter()
        maven { setUrl("https://www.jetbrains.com/intellij-repository/releases") }
        maven { setUrl("https://jetbrains.bintray.com/intellij-third-party-dependencies") }
        maven { url = uri(rootProject.projectDir.absolutePath + "/repo") }
    }
    plugins {
        id("com.google.devtools.ksp") version ("1.5.0-1.0.0-alpha10")
        kotlin("jvm") version ("1.5.0")
        id("com.squareup.sqldelight")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.squareup.sqldelight") {
                useModule("com.squareup.sqldelight:gradle-plugin:1.5.0")
            }
        }
    }
}

rootProject.name = "persist"

include("sample_app")
include("processor")
include("persist_plugin")
