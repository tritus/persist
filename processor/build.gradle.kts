plugins {
    kotlin("jvm")
}

version = "0.1"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup:kotlinpoet:1.8.0")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.5.31-1.0.0")
}
