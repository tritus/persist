package com.tritus.test.adapter

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.ClassName
import com.tritus.test.model.PersistentPropertyDefinition

internal object PersistentPropertyDefinitionAdapter {
    fun KSPropertyDeclaration.toPersistentPropertyDefinition(persistAnnotatedSymbols: Sequence<KSClassDeclaration>): PersistentPropertyDefinition {
        val name = simpleName.asString()
        val parameterType = type.resolve()
        val parameterTypeDeclaration = parameterType.declaration
        val parameterTypeName = parameterTypeDeclaration.simpleName.asString()
        val typeName = ClassName(
            parameterTypeDeclaration.packageName.asString(),
            parameterTypeName
        ).copy(nullable = parameterType.nullability == Nullability.NULLABLE)
        val isARelationShip = parameterTypeName in persistAnnotatedSymbols.map { it.simpleName.asString() }
        val sqlRawTypeName = extractSqlTypeName(parameterTypeName, isARelationShip)
        val sqlNullabilitySuffix = if (!typeName.isNullable) " NOT NULL" else ""
        val capitalizedName = name.replaceFirstChar { it.titlecase() }
        return PersistentPropertyDefinition(
            name = name,
            typeName = typeName,
            sqlTypeName = "$sqlRawTypeName$sqlNullabilitySuffix",
            getterMethodName = "get$capitalizedName",
            setterMethodName = "set$capitalizedName",
            isMutable = isMutable,
            isARelationShip = isARelationShip
        )
    }

    private fun KSPropertyDeclaration.extractSqlTypeName(
        parameterTypeName: String,
        isARelationShip: Boolean
    ) = when (parameterTypeName) {
        "Long" -> "INTEGER"
        "Double" -> "REAL"
        "String" -> "TEXT"
        "ByteArray" -> "BLOB"
        "Int" -> "INTEGER AS Int"
        "Short" -> "INTEGER AS Short"
        "Float" -> "REAL AS Float"
        "Boolean" -> "INTEGER AS Boolean"
        else -> if (isARelationShip) {
            "INTEGER"
        } else {
            throw IllegalArgumentException(
                """
                    Unsupported type $parameterTypeName in ${parent?.location}
                    All supported types are : Long, Double, String, ByteArray, Int, Short, Float, Boolean
                    and types annotated with @Persist.
                """.trimIndent()
            )
        }
    }
}