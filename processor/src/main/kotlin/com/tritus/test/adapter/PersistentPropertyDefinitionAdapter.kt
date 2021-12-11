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
        val typeName = ClassName(
            parameterTypeDeclaration.packageName.asString(),
            parameterTypeName
        ).copy(nullable = parameterType.nullability == Nullability.NULLABLE)
        val sqlRawTypeName = when (parameterTypeName) {
            "Long" -> "INTEGER"
            "Double" -> "REAL"
            "String" -> "TEXT"
            "ByteArray" -> "BLOB"
            "Int" -> "INTEGER AS Int"
            "Short" -> "INTEGER AS Short"
            "Float" -> "REAL AS Float"
            "Boolean" -> "INTEGER AS Boolean"
            else -> throw IllegalArgumentException("""
                Unsupported type $parameterTypeName.
                All supported types are : Long, Double, String, ByteArray, Int, Short, Float, Boolean.
                References to other @Persist annotated types are not yet supported.
            """.trimIndent())
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