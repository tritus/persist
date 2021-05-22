package com.tritus.persist.model

import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName

internal data class PersistentDataDefinition(
    val dataHolderClassName: String,
    val providerClassName: String,
    val packageName: String,
    val className: ClassName,
    val containingFile: KSFile,
    val idProperty: PersistentPropertyDefinition,
    val allProperties: List<PersistentPropertyDefinition>
)
