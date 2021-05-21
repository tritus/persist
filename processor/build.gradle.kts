plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.5.0-1.0.0-alpha10")
    implementation("com.squareup:kotlinpoet:1.8.0")
}
