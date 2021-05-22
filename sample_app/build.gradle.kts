plugins {
    id("com.google.devtools.ksp")
    kotlin("jvm")
    id("com.squareup.sqldelight")
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
    implementation("com.squareup.sqldelight:sqlite-driver:1.5.0")
}

sourceSets {
    main {
        java {
            srcDir("${buildDir.absolutePath}/generated/ksp/main/kotlin")
        }
    }
}

sqldelight {
    database("Database") {
        packageName = "com.tritus.persist"
    }
}

afterEvaluate {
    tasks.named("generateMainDatabaseInterface").get().dependsOn("kspKotlin")
}