package com.tritus.persist

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import com.tritus.persist.factory.DataHolderFactory
import com.tritus.persist.factory.ProviderFactory

internal class PersistVisitor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) :
    KSEmptyVisitor<Unit, Unit>() {
    override fun defaultHandler(node: KSNode, data: Unit) {
        val persistDefinition = node as KSClassDeclaration
        DataHolderFactory.createDataHolder(codeGenerator, persistDefinition)
        ProviderFactory.createProvider(codeGenerator, persistDefinition)
    }
}