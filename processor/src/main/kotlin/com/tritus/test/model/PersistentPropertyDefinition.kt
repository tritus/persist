package com.tritus.test.model

import com.squareup.kotlinpoet.TypeName

internal data class PersistentPropertyDefinition(
    val name: String,
    val typeName: TypeName,
    val sqlTypeName: String,
    val getterMethodName: String,
    val setterMethodName: String,
    val isMutable: Boolean,
    val isARelationShip: Boolean
)