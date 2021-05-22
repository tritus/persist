package com.tritus.persist.factory

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

internal object PersistDatabaseProviderFactory {
    private val classSimpleName = "PersistDatabaseProvider"

    fun create(codeGenerator: CodeGenerator) {
        val fileSpec = FileSpec.builder("", classSimpleName)
            .addType(createProviderClass())
            .build()
        codeGenerator.createNewFile(
            Dependencies(true),
            "",
            classSimpleName
        ).use { dataHolderFile -> dataHolderFile.write(fileSpec.toString().toByteArray()) }
        """PersistDatabaseProvider.getDatabase()"""
    }

    private fun createProviderClass() = TypeSpec.objectBuilder(classSimpleName)
        .addFunction(createGetDatabaseFunSpec())
        .build()

    private fun createGetDatabaseFunSpec() = FunSpec.builder("getDatabase")
        .returns(ClassName("", "PersistDatabase"))
        .addCode("""
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            PersistDatabase.Schema.create(driver)
            return PersistDatabase(driver)
        """.trimIndent())
        .build()
}
