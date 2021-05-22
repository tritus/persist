package com.tritus.persist

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import com.tritus.persist.adapter.PersistentDataDefinitionAdapter.toPersistentDataDefinition
import com.tritus.persist.factory.DataHolderFactory
import com.tritus.persist.factory.ProviderFactory

internal class PersistVisitor(private val codeGenerator: CodeGenerator) :
    KSEmptyVisitor<Unit, Unit>() {
    override fun defaultHandler(node: KSNode, data: Unit) {
        val persistentDataDefinition = (node as KSClassDeclaration).toPersistentDataDefinition()
        DataHolderFactory.createDataHolder(codeGenerator, persistentDataDefinition)
        ProviderFactory.createProvider(codeGenerator, persistentDataDefinition)
    }
}