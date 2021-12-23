package com.tritus.test

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import com.tritus.test.factory.DataExtensionsFactory
import com.tritus.test.factory.SQLDeclarationFactory
import com.tritus.test.factory.SQLJoinDeclarationFactory
import com.tritus.test.model.PersistentDataDefinition
import com.tritus.test.model.PersistentPropertyDefinition

internal class PersistentDataVisitor(private val environment: SymbolProcessorEnvironment) :
    KSEmptyVisitor<PersistentDataVisitor.Params, Unit>() {
    override fun defaultHandler(node: KSNode, data: Params) {
        DataExtensionsFactory.create(environment, data.definition, data.allDefinitions)
        data.definition.dataProperties
            .filterIsInstance<PersistentPropertyDefinition.List>()
            .forEach { SQLJoinDeclarationFactory.create(it) }
        SQLDeclarationFactory.create(data.definition)
    }

    data class Params(val definition: PersistentDataDefinition, val allDefinitions: Sequence<PersistentDataDefinition>)
}
