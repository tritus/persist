package com.tritus.test.factory

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.tritus.test.model.PersistentDataDefinition
import kotlinx.coroutines.flow.Flow

internal object DataExtensionsFactory {
    fun create(codeGenerator: CodeGenerator, definition: PersistentDataDefinition) {
        val fileName = definition.extensionsFileName
        val packageName = definition.packageName
        val fileSpec = FileSpec.builder(packageName, fileName)
            .addExtensionFunctions(definition)
            .addImport(PersistDatabaseProviderFactory.databasePackage, PersistDatabaseProviderFactory.classSimpleName)
            .build()
        codeGenerator.createNewFile(
            Dependencies(true, definition.containingFile),
            packageName,
            fileName
        ).use { dataHolderFile -> dataHolderFile.write(fileSpec.toString().toByteArray()) }
    }

    fun FileSpec.Builder.addExtensionFunctions(definition: PersistentDataDefinition) =
        addFunction(createAsFlowFunSpec(definition))

    private fun createAsFlowFunSpec(definition: PersistentDataDefinition) = FunSpec.builder("asFlow")
        .receiver(definition.className)
        .returns(ClassName(Flow::class))
        .build()
}