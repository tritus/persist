package com.tritus.test

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import com.tritus.test.factory.DataExtensionsFactory
import com.tritus.test.factory.DataProviderFactory
import com.tritus.test.factory.SQLDeclarationFactory
import com.tritus.test.model.PersistentDataDefinition

internal class PersistentDataVisitor(private val codeGenerator: CodeGenerator) :
    KSEmptyVisitor<PersistentDataDefinition, Unit>() {
    override fun defaultHandler(node: KSNode, data: PersistentDataDefinition) {
        DataProviderFactory.create(codeGenerator, data)
        DataExtensionsFactory.create(codeGenerator, data)
        SQLDeclarationFactory.create(data)
    }
}