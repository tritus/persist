package com.tritus.test.model

import com.squareup.kotlinpoet.ClassName
import java.io.File

internal sealed class PersistentPropertyDefinition(
    val name: String,
    val getterMethodName: String,
    val setterMethodName: String,
    val isMutable: Boolean,
)

internal class PrimitivePropertyDefinition(
    name: String,
    val typeDefinition: PropertyTypeDefinition,
    getterMethodName: String,
    setterMethodName: String,
    isMutable: Boolean,
) : PersistentPropertyDefinition(
    name,
    getterMethodName,
    setterMethodName,
    isMutable
)

internal class ListPropertyDefinition(
    name: String,
    getterMethodName: String,
    setterMethodName: String,
    val itemSetterMethodName: String,
    val deleteMethodName: String,
    isMutable: Boolean,
    val itemTypeDefinition: PropertyTypeDefinition,
    val dataholderClassName: String,
    val sqlDefinitionFile: File
) : PersistentPropertyDefinition(
    name,
    getterMethodName,
    setterMethodName,
    isMutable
)

internal class PropertyTypeDefinition(
    val typeName: ClassName,
    val isARelationship: Boolean,
    val sqlTypeName: String,
)