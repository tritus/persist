package com.tritus

import com.squareup.sqldelight.gradle.SqlDelightExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.*

class PersistPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            apply(plugin = "com.google.devtools.ksp")
            apply(plugin = "com.squareup.sqldelight")
            dependencies {
                "implementation"(project(":processor"))
                "ksp"(project(":processor"))
                "implementation"("com.squareup.sqldelight:sqlite-driver:1.5.0")
            }
            configure<SourceSetContainer> {
                named("main") {
                    java {
                        srcDir("${buildDir.absolutePath}/generated/ksp/main/kotlin")
                    }
                }
            }
            configure<SqlDelightExtension> {
                database("PersistDatabase") {
                    packageName = "com.tritus.persist"
                }
            }
            afterEvaluate {
                tasks.named("generateMainPersistDatabaseInterface").get().dependsOn("kspKotlin")
            }
        }
    }
}
