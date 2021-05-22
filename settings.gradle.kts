pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { setUrl("https://www.jetbrains.com/intellij-repository/releases") }
        maven { setUrl("https://jetbrains.bintray.com/intellij-third-party-dependencies") }
    }
    plugins {
        id("com.google.devtools.ksp") version kspVersion
        kotlin("jvm") version kotlinVersion
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
