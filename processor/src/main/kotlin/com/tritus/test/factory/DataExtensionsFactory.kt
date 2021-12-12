package com.tritus.test.factory

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.tritus.test.model.PersistentDataDefinition
import com.tritus.test.model.PersistentPropertyDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal object DataExtensionsFactory {
    fun create(codeGenerator: CodeGenerator, definition: PersistentDataDefinition) {
        val fileName = definition.extensionsFileName
        val packageName = definition.packageName
        val fileSpec = FileSpec.builder(packageName, fileName)
            .addExtensionFunctions(definition)
            .addImport(PersistDatabaseProviderFactory.databasePackage, PersistDatabaseProviderFactory.classSimpleName)
            .addImport("com.squareup.sqldelight.runtime.coroutines", "asFlow")
            .addImport("kotlinx.coroutines.flow", "map")
            .build()
        codeGenerator.createNewFile(
            Dependencies(true, definition.containingFile),
            packageName,
            fileName
        ).use { dataHolderFile -> dataHolderFile.write(fileSpec.toString().toByteArray()) }
    }

    private fun FileSpec.Builder.addExtensionFunctions(definition: PersistentDataDefinition) =
        addFunction(createAsFlowFunSpec(definition))
            .let { builder ->
                definition.dataProperties
                    .filter { it.isMutable }
                    .fold(builder) { funSpec, property ->
                        funSpec.addFunction(createPropertyAsFlowFunSpec(property, definition))
                    }
            }

    private fun createPropertyAsFlowFunSpec(
        property: PersistentPropertyDefinition,
        definition: PersistentDataDefinition
    ) = FunSpec.builder("${property.name}AsFlow")
        .receiver(definition.className)
        .returns(ClassName("kotlinx.coroutines.flow", "Flow").parameterizedBy(property.typeName))
        .addCode(
            """
                return ${PersistDatabaseProviderFactory.classSimpleName}.getDatabase().${definition.databaseQueriesMethodName}.${property.getterMethodName}(id).asFlow().map { it.executeAsOne() }
            """.trimIndent()
        )
        .build()

    private fun createAsFlowFunSpec(definition: PersistentDataDefinition) = FunSpec.builder("asFlow")
        .receiver(definition.className)
        .returns(ClassName("kotlinx.coroutines.flow", "Flow").parameterizedBy(definition.className))
        .addCode(
            """
                return ${PersistDatabaseProviderFactory.classSimpleName}.getDatabase().${definition.databaseQueriesMethodName}.getRecord(id).asFlow().map { ${definition.providerClassName}.retrieve(id) }
            """.trimIndent()
        )
        .build()
}