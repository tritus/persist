plugins {
    kotlin("jvm")
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup:kotlinpoet:1.8.0")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.5.31-1.0.0")
}
