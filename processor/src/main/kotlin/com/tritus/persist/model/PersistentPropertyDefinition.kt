package com.tritus.persist.model

import com.squareup.kotlinpoet.ClassName

internal data class PersistentPropertyDefinition(
    val name: String,
    val className: ClassName
)