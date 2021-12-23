package com.tritus.test.model

import java.io.File

internal sealed class PersistentPropertyDefinition(
    val name: String,
    val getterMethodName: String,
    val setterMethodName: String,
    val isMutable: Boolean,
) {

    class Primitive(
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

    class List(
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
}
