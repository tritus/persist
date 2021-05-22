package com.tritus.persist.factory

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.tritus.persist.model.PersistentDataDefinition

internal object ProviderFactory {
    fun createProvider(codeGenerator: CodeGenerator, definition: PersistentDataDefinition) {
        val fileName = definition.providerClassName
        val packageName = definition.packageName
        val fileSpec = FileSpec.builder(packageName, fileName)
            .addType(createProviderClass(definition))
            .build()
        codeGenerator.createNewFile(
            Dependencies(true, definition.containingFile),
            packageName,
            fileName
        ).use { dataHolderFile -> dataHolderFile.write(fileSpec.toString().toByteArray()) }
    }

    private fun createProviderClass(definition: PersistentDataDefinition): TypeSpec {
        val classBuilder = TypeSpec.Companion.objectBuilder(definition.providerClassName)
        val newFunSpec = createNewFunSpec(definition)
        classBuilder.addFunction(newFunSpec)
        return classBuilder.build()
    }

    private fun createNewFunSpec(definition: PersistentDataDefinition): FunSpec {
        val newBuilder = FunSpec.builder("new")
        (definition.allProperties - definition.idProperty).forEach { property ->
            newBuilder.addParameter(property.name, property.className)
        }
        newBuilder.returns(definition.className)
        newBuilder.addCode("return ${definition.dataHolderClassName}(${definition.allProperties.joinToString { it.name }})")
        return newBuilder.build()
    }
}