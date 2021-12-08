plugins {
    kotlin("jvm")
    id("com.tritus.persist") version("0.1")
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}