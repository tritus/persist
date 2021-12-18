package com.tritus.test.factory

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal object PersistDatabaseProviderFactory {
    const val classSimpleName = "PersistDatabaseProvider"
    const val databaseName = "PersistDatabase"
    const val databasePackage = "com.tritus.persist"
    val databaseClassname = ClassName(databasePackage, databaseName)

    fun create(environment: SymbolProcessorEnvironment) {
        val fileSpec = FileSpec.builder(databasePackage, classSimpleName)
            .addImport("com.squareup.sqldelight.sqlite.driver", "JdbcSqliteDriver")
            .addType(createProviderClass())
            .build()
        environment.codeGenerator.createNewFile(
            Dependencies(true),
            databasePackage,
            classSimpleName
        ).use { dataHolderFile -> dataHolderFile.write(fileSpec.toString().toByteArray()) }
    }

    private fun createProviderClass() = TypeSpec.objectBuilder(classSimpleName)
        .addProperty(
            PropertySpec
                .builder("cachedDatabase", databaseClassname.copy(nullable = true))
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
        .returns(databaseClassname)
        .addCode(
            """
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            $databaseName.Schema.create(driver)
            val newDatabase = $databaseName(driver)
            cachedDatabase = newDatabase
            return newDatabase
            """.trimIndent()
        )
        .build()

    private fun createGetDatabaseFunSpec() = FunSpec.builder("getDatabase")
        .returns(databaseClassname)
        .addCode("return cachedDatabase ?: createDatabase()")
        .build()
}
