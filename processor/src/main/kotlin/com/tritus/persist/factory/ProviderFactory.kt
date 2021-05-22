package com.tritus.persist.factory

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.tritus.persist.annotation.PersistentId
import com.tritus.persist.extension.getDataHolderClassName
import com.tritus.persist.extension.getProviderClassName

internal object ProviderFactory {
    fun createProvider(codeGenerator: CodeGenerator, persistDefinition: KSClassDeclaration) {
        val fileName = persistDefinition.getProviderClassName()
        val packageName = persistDefinition.packageName.asString()
        val fileSpec = FileSpec.builder(packageName, fileName)
            .addType(createProviderClass(persistDefinition))
            .build()
        codeGenerator.createNewFile(
            Dependencies(true, persistDefinition.containingFile!!),
            packageName,
            fileName
        ).use { dataHolderFile ->
            dataHolderFile.write(fileSpec.toString().toByteArray())
        }
    }

    private fun createProviderClass(persistDefinition: KSClassDeclaration): TypeSpec {
        val className = persistDefinition.getProviderClassName()
        val classBuilder = TypeSpec.Companion.objectBuilder(className)
        val newBuilder = FunSpec.builder("new")
        val dataProperties = persistDefinition.getAllProperties()
        val idProperty = dataProperties.first { property ->
            property.annotations.any { annotationSymbol ->
                val annotation = annotationSymbol.annotationType.resolve().declaration.qualifiedName!!.asString()
                val persistentIdAnnotation = PersistentId::class.qualifiedName!!
                annotation == persistentIdAnnotation
            }
        }
        (dataProperties - idProperty).forEach { property ->
            val propertyName = property.simpleName.asString()
            val parameterTypeDeclaration = property.type.resolve().declaration
            val propertyClassName = ClassName(
                parameterTypeDeclaration.packageName.asString(),
                parameterTypeDeclaration.simpleName.asString()
            )
            newBuilder.addParameter(propertyName, propertyClassName)
        }
        newBuilder.addCode(
            """
            return ${persistDefinition.getDataHolderClassName()}(${
                persistDefinition.getAllProperties().map { it.simpleName.asString() }.joinToString(", ")
            })
        """.trimIndent()
        )
        newBuilder.returns(ClassName(persistDefinition.packageName.asString(), persistDefinition.simpleName.asString()))
        classBuilder.addFunction(newBuilder.build())
        return classBuilder.build()
    }
}