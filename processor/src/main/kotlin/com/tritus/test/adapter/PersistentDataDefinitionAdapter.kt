package com.tritus.test.adapter

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.tritus.test.model.PersistentDataDefinition
import com.tritus.test.adapter.PersistentPropertyDefinitionAdapter.toPersistentPropertyDefinition
import com.tritus.test.annotation.PersistentId
import java.io.File

internal object PersistentDataDefinitionAdapter {
    fun KSClassDeclaration.toPersistentDataDefinition(): PersistentDataDefinition {
        val simpleNameString = simpleName.asString()
        val dataHolderClassName = "${simpleNameString}_Data"
        val packageNameString = packageName.asString()
        val idProperty = getAllProperties().first { property ->
            property.annotations.any { annotationSymbol ->
                val annotation = annotationSymbol.annotationType.resolve().declaration.qualifiedName!!.asString()
                val persistentIdAnnotation = PersistentId::class.qualifiedName!!
                annotation == persistentIdAnnotation
            }
        }.toPersistentPropertyDefinition()
        val pathInSource = packageNameString.replace(".", "/")
        val containingClassFile = containingFile!!
        val sqlDefinitionFileName = "$dataHolderClassName.sq"
        val sqlDefinitionsPath = containingClassFile.filePath
            .replace(pathInSource, "")
            .split("/")
            .dropLast(3)
            .plus("sqldelight")
            .joinToString("/")
        val allProperties = getAllProperties().map { it.toPersistentPropertyDefinition() }.toList()
        return PersistentDataDefinition(
            simpleName = simpleNameString,
            dataHolderClassName = dataHolderClassName,
            databaseQueriesMethodName = "${dataHolderClassName.replace(Regex("^.")) { it.value.toLowerCase() }}Queries",
            providerClassName = "${simpleNameString}Provider",
            packageName = packageNameString,
            className = ClassName(packageNameString, simpleNameString),
            containingFile = containingClassFile,
            idProperty = idProperty,
            allProperties = allProperties,
            dataProperties = allProperties - idProperty,
            sqlDefinitionsFolder = File(sqlDefinitionsPath),
            sqlDefinitionFile = File("$sqlDefinitionsPath/$pathInSource/$sqlDefinitionFileName")
        )
    }
}