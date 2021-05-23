package com.tritus.persist

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import com.tritus.persist.factory.PersistDatabaseProviderFactory
import com.tritus.persist.factory.ProviderFactory
import com.tritus.persist.factory.SQLDeclarationFactory
import com.tritus.persist.model.PersistentDataDefinition

internal class PersistVisitor(private val codeGenerator: CodeGenerator) :
    KSEmptyVisitor<PersistentDataDefinition, Unit>() {
    override fun defaultHandler(node: KSNode, data: PersistentDataDefinition) {
        PersistDatabaseProviderFactory.create(codeGenerator)
        ProviderFactory.create(codeGenerator, data)
        SQLDeclarationFactory.create(data)
    }
}