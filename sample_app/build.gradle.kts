plugins {
    id("com.google.devtools.ksp")
    kotlin("jvm")
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":processor"))
    ksp(project(":processor"))
}

sourceSets {
    main {
        java {
            srcDir("${buildDir.absolutePath}/generated/ksp/main/kotlin")
        }
    }
}
