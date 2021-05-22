package com.tritus.persist.adapter

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.tritus.persist.model.PersistentDataDefinition
import com.tritus.persist.adapter.PersistentPropertyDefinitionAdapter.toPersistentPropertyDefinition
import com.tritus.persist.annotation.PersistentId

internal object PersistentDataDefinitionAdapter {
    fun KSClassDeclaration.toPersistentDataDefinition(): PersistentDataDefinition {
        val simpleNameString = simpleName.asString()
        val packageNameString = packageName.asString()
        val idProperty = getAllProperties().first { property ->
            property.annotations.any { annotationSymbol ->
                val annotation = annotationSymbol.annotationType.resolve().declaration.qualifiedName!!.asString()
                val persistentIdAnnotation = PersistentId::class.qualifiedName!!
                annotation == persistentIdAnnotation
            }
        }
        return PersistentDataDefinition(
            "${simpleNameString}_Data",
            "${simpleNameString}Provider",
            packageNameString,
            ClassName(packageNameString, simpleNameString),
            containingFile!!,
            idProperty.toPersistentPropertyDefinition(),
            getAllProperties().map { it.toPersistentPropertyDefinition() }.toList()
        )
    }
}