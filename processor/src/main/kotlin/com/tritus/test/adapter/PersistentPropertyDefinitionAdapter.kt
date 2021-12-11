package com.tritus.test.adapter

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.ClassName
import com.tritus.test.model.PersistentPropertyDefinition

internal object PersistentPropertyDefinitionAdapter {
    fun KSPropertyDeclaration.toPersistentPropertyDefinition(): PersistentPropertyDefinition {
        val name = simpleName.asString()
        val parameterType = type.resolve()
        val parameterTypeDeclaration = parameterType.declaration
        val parameterTypeName = parameterTypeDeclaration.simpleName.asString()
        val sqlRawTypeName = when (parameterTypeName) {
            "String" -> "TEXT"
            "Long" -> "INTEGER"
            else -> throw IllegalArgumentException("Cannot store $parameterTypeName")
        }
        val sqlNullabilitySuffix = if (parameterType.nullability == Nullability.NOT_NULL) " NOT NULL" else ""
        val capitalizedName = name.replaceFirstChar { it.titlecase() }
        return PersistentPropertyDefinition(
            name = name,
            className = ClassName(parameterTypeDeclaration.packageName.asString(), parameterTypeName),
            sqlTypeName = "$sqlRawTypeName$sqlNullabilitySuffix",
            getterMethodName = "get$capitalizedName",
            setterMethodName = "set$capitalizedName",
            isMutable = isMutable,
            isNullable = parameterType.nullability == Nullability.NULLABLE
        )
    }
}