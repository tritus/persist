plugins {
    id("com.google.devtools.ksp") version "1.5.0-1.0.0-alpha10"
    kotlin("jvm")
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":processor"))
    ksp(project(":processor"))
}

