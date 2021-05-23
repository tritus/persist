package com.tritus.test

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import com.tritus.test.factory.PersistDatabaseProviderFactory
import com.tritus.test.factory.DataProviderFactory
import com.tritus.test.factory.SQLDeclarationFactory
import com.tritus.test.model.PersistentDataDefinition

internal class PersistVisitor(private val codeGenerator: CodeGenerator) :
    KSEmptyVisitor<PersistentDataDefinition, Unit>() {
    override fun defaultHandler(node: KSNode, data: PersistentDataDefinition) {
        PersistDatabaseProviderFactory.create(codeGenerator)
        DataProviderFactory.create(codeGenerator, data)
        SQLDeclarationFactory.create(data)
    }
}