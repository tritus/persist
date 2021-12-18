package com.tritus.test.model

import com.squareup.kotlinpoet.ClassName

internal class PropertyTypeDefinition(
    val typeName: ClassName,
    val isARelationship: Boolean,
    val sqlTypeName: String,
)
