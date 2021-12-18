package com.tritus.test.adapter

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.ClassName
import com.tritus.test.adapter.PersistentDataDefinitionAdapter.sqlDefinitionsFolderPath
import com.tritus.test.model.PersistentPropertyDefinition
import com.tritus.test.model.PropertyTypeDefinition
import java.io.File

internal object PersistentPropertyDefinitionAdapter {
    fun KSPropertyDeclaration.toPersistentPropertyDefinition(
        parentDeclaration: KSClassDeclaration,
        persistAnnotatedSymbols: Sequence<KSClassDeclaration>
    ): PersistentPropertyDefinition {
        val isAList = type.resolve().declaration.qualifiedName?.asString() == List::class.qualifiedName
        return if (isAList) {
            toListPropertyDefinition(parentDeclaration, persistAnnotatedSymbols)
        } else {
            toPrimitivePropertyDefinition(persistAnnotatedSymbols)
        }
    }

    fun KSPropertyDeclaration.toPrimitivePropertyDefinition(persistAnnotatedSymbols: Sequence<KSClassDeclaration>): PersistentPropertyDefinition.Primitive {
        val name = simpleName.asString()
        val parameterType = type.resolve()
        val capitalizedName = name.replaceFirstChar { it.titlecase() }
        return PersistentPropertyDefinition.Primitive(
            name = name,
            typeDefinition = parameterType.toTypeDefinition(parent?.location, persistAnnotatedSymbols),
            getterMethodName = "get$capitalizedName",
            setterMethodName = "set$capitalizedName",
            isMutable = isMutable,
        )
    }

    private fun KSType.toTypeDefinition(
        declarationLocation: Location?,
        persistAnnotatedSymbols: Sequence<KSClassDeclaration>
    ): PropertyTypeDefinition {
        val parameterTypeDeclaration = declaration
        val parameterTypeName = parameterTypeDeclaration.simpleName.asString()
        val parameterTypePackageName = parameterTypeDeclaration.packageName.asString()
        val isARelationShip = parameterTypeName in persistAnnotatedSymbols.map { it.simpleName.asString() }
        val typeName = ClassName(
            parameterTypePackageName,
            parameterTypeName
        ).copy(nullable = nullability == Nullability.NULLABLE) as ClassName
        val sqlRawTypeName = extractSqlTypeName(declarationLocation, parameterTypeName, isARelationShip)
        val sqlNullabilitySuffix = if (!typeName.isNullable) " NOT NULL" else ""
        return PropertyTypeDefinition(
            typeName = typeName,
            isARelationship = isARelationShip,
            sqlTypeName = "$sqlRawTypeName$sqlNullabilitySuffix"
        )
    }

    private fun KSPropertyDeclaration.toListPropertyDefinition(
        parentDeclaration: KSClassDeclaration,
        persistAnnotatedSymbols: Sequence<KSClassDeclaration>
    ): PersistentPropertyDefinition.List {
        val name = simpleName.asString()
        val parameterType = type.resolve()
        val parameterTypeDeclaration = parameterType.declaration
        val parameterTypeName = parameterTypeDeclaration.simpleName.asString()
        val capitalizedName = name.replaceFirstChar { it.titlecase() }
        val typeArgument = parameterType.arguments.firstOrNull()
        require(typeArgument != null) {
            """
                No type argument found in List $parameterTypeName in ${parent?.location}.
            """.trimIndent()
        }
        val itemType = parameterType.arguments.first().type
        require(itemType != null) {
            """
                Argument of List $parameterTypeName in ${parent?.location} has no type.
            """.trimIndent()
        }
        val itemTypeDefinition = itemType.resolve().toTypeDefinition(parent?.location, persistAnnotatedSymbols)
        val dataholderClassName = "${parentDeclaration.simpleName.asString()}_${name}_Data"
        return PersistentPropertyDefinition.List(
            name = name,
            getterMethodName = "get$capitalizedName",
            setterMethodName = "set$capitalizedName",
            itemSetterMethodName = "setItem$capitalizedName",
            deleteMethodName = "deleteAll$capitalizedName",
            isMutable = isMutable,
            itemTypeDefinition = itemTypeDefinition,
            dataholderClassName = dataholderClassName,
            sqlDefinitionFile = File(parentDeclaration.sqlDefinitionsFolderPath(), "$dataholderClassName.sq")
        )
    }

    private fun extractSqlTypeName(
        parentLocation: Location?,
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
                    Unsupported type $parameterTypeName in $parentLocation
                    All supported types are : Long, Double, String, ByteArray, Int, Short, Float, Boolean
                    and types annotated with @Persist.
                    Also List<T> of the previous types are also supported.
                """.trimIndent()
            )
        }
    }
}
