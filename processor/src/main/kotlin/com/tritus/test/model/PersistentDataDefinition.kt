package com.tritus.test.model

import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName
import java.io.File

internal data class PersistentDataDefinition(
    val simpleName: String,
    val dataholderClassName: String,
    val databaseQueriesMethodName: String,
    val extensionsFileName: String,
    val packageName: String,
    val className: ClassName,
    val containingFile: KSFile,
    val idProperty: PrimitivePropertyDefinition,
    val dataProperties: List<PersistentPropertyDefinition>,
    val sqlDelightFolder: File,
    val sqlDefinitionFile: File
)
