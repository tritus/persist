package com.tritus.persist.factory

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

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
        .addFunction(createGetDatabaseFunSpec())
        .build()

    private fun createGetDatabaseFunSpec() = FunSpec.builder("getDatabase")
        .returns(ClassName(getDatabasePackage(), "Database"))
        .addCode("""
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            Database.Schema.create(driver)
            return Database(driver)
        """.trimIndent())
        .build()

    private fun getClassSimpleName() = "PersistDatabaseProvider"

    private fun getDatabasePackage() = "com.tritus.persist"
}
