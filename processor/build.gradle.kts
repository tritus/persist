plugins {
    kotlin("jvm")
    id("maven-publish")
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.tritus.persist"
            artifactId = "processor"
            version = "0.1"

            from(components["kotlin"])
        }
    }
}