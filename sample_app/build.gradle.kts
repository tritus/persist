plugins {
    id("com.google.devtools.ksp")
    kotlin("jvm")
    id("com.squareup.sqldelight")
    //id("com.tritus.persist") version("0.1")
}

class PersistPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.dependencies {
            implementation(project(":processor"))
            ksp(project(":processor"))
            implementation("com.squareup.sqldelight:sqlite-driver:1.5.0")
        }
        //project.sourceSets {
        //    main {
        //        java {
        //            srcDir("${buildDir.absolutePath}/generated/ksp/main/kotlin")
        //        }
        //    }
        //}
        project.sqldelight {
            database("PersistDatabase") {
                packageName = "com.tritus.persist"
            }
        }
        project.afterEvaluate {
            tasks.named("generateMainPersistDatabaseInterface").get().dependsOn("kspKotlin")
        }
    }
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

apply<PersistPlugin>()

dependencies {
    implementation(kotlin("stdlib"))
}