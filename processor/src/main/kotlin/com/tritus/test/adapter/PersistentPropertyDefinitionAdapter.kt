package com.tritus.test.adapter

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.tritus.test.model.PersistentPropertyDefinition

internal object PersistentPropertyDefinitionAdapter {
    fun KSPropertyDeclaration.toPersistentPropertyDefinition(): PersistentPropertyDefinition {
        val name = simpleName.asString()
        val parameterType = type.resolve()
        val parameterTypeDeclaration = parameterType.declaration
        val parameterTypeName = parameterTypeDeclaration.simpleName.asString()
        val typeName = ClassName(
            parameterTypeDeclaration.packageName.asString(),
            parameterTypeName
        ).copy(nullable = parameterType.nullability == Nullability.NULLABLE)
        val sqlRawTypeName = when (parameterTypeName) {
            "String" -> "TEXT"
            "Long" -> "INTEGER"
            else -> throw IllegalArgumentException("Cannot store $parameterTypeName")
        }
        val sqlNullabilitySuffix = if (!typeName.isNullable) " NOT NULL" else ""
        val capitalizedName = name.replaceFirstChar { it.titlecase() }
        return PersistentPropertyDefinition(
            name = name,
            typeName = typeName,
            sqlTypeName = "$sqlRawTypeName$sqlNullabilitySuffix",
            getterMethodName = "get$capitalizedName",
            setterMethodName = "set$capitalizedName",
            isMutable = isMutable
        )
    }
}