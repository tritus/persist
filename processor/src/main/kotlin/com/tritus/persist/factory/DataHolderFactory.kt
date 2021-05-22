package com.tritus.persist.factory

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.tritus.persist.extension.getDataHolderClassName

internal object DataHolderFactory {
    fun createDataHolder(codeGenerator: CodeGenerator, persistDefinition: KSClassDeclaration) {
        val fileName = persistDefinition.getDataHolderClassName()
        val packageName = persistDefinition.packageName.asString()
        val fileSpec = FileSpec.builder(packageName, fileName)
            .addType(createDataHolderClass(persistDefinition))
            .build()
        codeGenerator.createNewFile(
            Dependencies(true, persistDefinition.containingFile!!),
            packageName,
            fileName
        ).use { dataHolderFile ->
            val fileBytes = fileSpec.toString().toByteArray()
            dataHolderFile.write(fileBytes)
        }
    }


    private fun createDataHolderClass(persistDefinition: KSClassDeclaration): TypeSpec {
        val className = persistDefinition.getDataHolderClassName()
        val classBuilder = TypeSpec.classBuilder(className)
        classBuilder.addSuperinterface(
            ClassName(
                persistDefinition.packageName.asString(),
                persistDefinition.simpleName.asString()
            )
        )
        val constructorBuilder = FunSpec.constructorBuilder()
        persistDefinition.getAllProperties().forEach { property ->
            val propertyName = property.simpleName.asString()
            val parameterTypeDeclaration = property.type.resolve().declaration
            val propertyClassName = ClassName(
                parameterTypeDeclaration.packageName.asString(),
                parameterTypeDeclaration.simpleName.asString()
            )
            constructorBuilder.addParameter(propertyName, propertyClassName)
            classBuilder.addProperty(
                PropertySpec.builder(propertyName, propertyClassName, KModifier.OVERRIDE)
                    .initializer(propertyName)
                    .build()
            )
        }
        classBuilder.primaryConstructor(constructorBuilder.build())
        return classBuilder.build()
    }

}