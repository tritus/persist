package com.tritus

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class PersistPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            dependencies {
                "implementation"(project(":processor"))
                "ksp"(project(":processor"))
                "implementation"("com.squareup.sqldelight:sqlite-driver:1.5.0")
            }
            //project.sourceSets {
            //    main {
            //        java {
            //            srcDir("${buildDir.absolutePath}/generated/ksp/main/kotlin")
            //        }
            //    }
            //}
            configure<> {"sqldelight" {
                "database"("PersistDatabase") {
                    packageName = "com.tritus.persist"
                }
            }
            afterEvaluate {
                tasks.named("generateMainPersistDatabaseInterface").get().dependsOn("kspKotlin")
            }
        }
    }
}
