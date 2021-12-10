package com.tritus.test

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import com.tritus.test.factory.PersistDatabaseProviderFactory
import com.tritus.test.model.PersistentDataDefinition

internal class DatabaseCreationVisitor(private val codeGenerator: CodeGenerator): KSEmptyVisitor<PersistentDataDefinition, Unit>() {
    override fun defaultHandler(node: KSNode, data: PersistentDataDefinition) {
        data.sqlDefinitionsFolder.deleteRecursively()
        PersistDatabaseProviderFactory.create(codeGenerator)
    }
}