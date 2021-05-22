package com.tritus.persist

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import com.tritus.persist.adapter.PersistentDataDefinitionAdapter.toPersistentDataDefinition
import com.tritus.persist.factory.DataHolderFactory
import com.tritus.persist.factory.ProviderFactory
import com.tritus.persist.factory.SQLDeclarationFactory

internal class PersistVisitor(private val codeGenerator: CodeGenerator) :
    KSEmptyVisitor<Unit, Unit>() {
    override fun defaultHandler(node: KSNode, data: Unit) {
        val persistentDataDefinition = (node as KSClassDeclaration).toPersistentDataDefinition()
        //DataHolderFactory.create(codeGenerator, persistentDataDefinition)
        //ProviderFactory.create(codeGenerator, persistentDataDefinition)
        SQLDeclarationFactory.create(codeGenerator, persistentDataDefinition)
    }
}