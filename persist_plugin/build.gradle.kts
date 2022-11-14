plugins {
    id("java-gradle-plugin")
    kotlin("jvm")
    id("maven-publish")
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        create("persistPlugin") {
            id = "com.tritus.persist"
            implementationClass = "com.tritus.PersistPlugin"
        }
    }
}

version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.squareup.sqldelight:gradle-plugin:1.5.0")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.5.31-1.0.0")
}

publishing {
    repositories {
        maven {
            name = "myRepo"
            url = uri(layout.projectDirectory.dir("../repo"))
        }
    }
}