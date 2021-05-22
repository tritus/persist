package com.tritus.persist.factory

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.*
import com.tritus.persist.model.PersistentDataDefinition

internal object DataHolderFactory {
    fun createDataHolder(codeGenerator: CodeGenerator, definition: PersistentDataDefinition) {
        val fileSpec = FileSpec.builder(definition.packageName, definition.dataHolderClassName)
            .addType(createDataHolderClass(definition))
            .build()
        codeGenerator.createNewFile(
            Dependencies(true, definition.containingFile),
            definition.packageName,
            definition.dataHolderClassName
        ).use { dataHolderFile ->
            val fileBytes = fileSpec.toString().toByteArray()
            dataHolderFile.write(fileBytes)
        }
    }


    private fun createDataHolderClass(definition: PersistentDataDefinition): TypeSpec {
        val className = definition.dataHolderClassName
        val classBuilder = TypeSpec.classBuilder(className)
        classBuilder.addSuperinterface(definition.className)
        val constructorBuilder = FunSpec.constructorBuilder()
        definition.allProperties.forEach { property ->
            constructorBuilder.addParameter(property.name, property.className)
            classBuilder.addProperty(
                PropertySpec.builder(property.name, property.className, KModifier.OVERRIDE)
                    .initializer(property.name)
                    .build()
            )
        }
        classBuilder.primaryConstructor(constructorBuilder.build())
        return classBuilder.build()
    }

}