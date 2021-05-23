package com.tritus.persist.factory

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.*

internal object PersistDatabaseProviderFactory {
    fun create(codeGenerator: CodeGenerator) {
        val fileSpec = FileSpec.builder(getDatabasePackage(), getClassSimpleName())
            .addImport("com.squareup.sqldelight.sqlite.driver", "JdbcSqliteDriver")
            .addType(createProviderClass())
            .build()
        codeGenerator.createNewFile(
            Dependencies(true),
            getDatabasePackage(),
            getClassSimpleName()
        ).use { dataHolderFile -> dataHolderFile.write(fileSpec.toString().toByteArray()) }
    }

    private fun createProviderClass() = TypeSpec.objectBuilder(getClassSimpleName())
        .addProperty(
            PropertySpec
                .builder("cachedDatabase", ClassName("", "Database").copy(nullable = true))
                .mutable()
                .addModifiers(KModifier.PRIVATE)
                .initializer("null")
                .build()
        )
        .addFunction(createGetDatabaseFunSpec())
        .addFunction(createCreateDatabaseFunSpec())
        .build()

    private fun createCreateDatabaseFunSpec() = FunSpec.builder("createDatabase")
        .addModifiers(KModifier.PRIVATE)
        .returns(ClassName("", "Database"))
        .addCode(
            """
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            Database.Schema.create(driver)
            val newDatabase = Database(driver)
            cachedDatabase = newDatabase
            return newDatabase
        """.trimIndent()
        )
        .build()

    private fun createGetDatabaseFunSpec() = FunSpec.builder("getDatabase")
        .returns(ClassName(getDatabasePackage(), "Database"))
        .addCode(
            """
            return cachedDatabase ?: createDatabase()
        """.trimIndent()
        )
        .build()

    private fun getClassSimpleName() = "PersistDatabaseProvider"

    private fun getDatabasePackage() = "com.tritus.persist"
}
