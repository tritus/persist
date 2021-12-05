plugins {
    id("java-gradle-plugin")
    kotlin("jvm")
    id("maven-publish")
    `kotlin-dsl`
    id("com.google.devtools.ksp")
    id("com.squareup.sqldelight")
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
}

publishing {
    publications {
        create<MavenPublication>("persistPlugin") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "myRepo"
            url = uri(layout.projectDirectory.dir("../repo"))
        }
    }
}