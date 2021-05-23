package com.tritus.test.model

import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName
import java.io.File

internal data class PersistentDataDefinition(
    val simpleName: String,
    val dataHolderClassName: String,
    val databaseQueriesMethodName: String,
    val providerClassName: String,
    val packageName: String,
    val className: ClassName,
    val containingFile: KSFile,
    val idProperty: PersistentPropertyDefinition,
    val allProperties: List<PersistentPropertyDefinition>,
    val dataProperties: List<PersistentPropertyDefinition>,
    val sqlDefinitionsFolder: File,
    val sqlDefinitionFile: File
)
