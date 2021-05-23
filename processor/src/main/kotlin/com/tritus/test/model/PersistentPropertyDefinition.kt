package com.tritus.test.model

import com.squareup.kotlinpoet.ClassName

internal data class PersistentPropertyDefinition(
    val name: String,
    val className: ClassName,
    val sqlTypeName: String
)