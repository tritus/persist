package com.tritus.test

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import com.tritus.test.factory.DataExtensionsFactory
import com.tritus.test.factory.SQLDeclarationFactory
import com.tritus.test.model.PersistentDataDefinition

internal data class DataVisitorParam(val definition: PersistentDataDefinition, val allDefinitions: Sequence<PersistentDataDefinition>)

internal class PersistentDataVisitor(private val codeGenerator: CodeGenerator) :
    KSEmptyVisitor<DataVisitorParam, Unit>() {
    override fun defaultHandler(node: KSNode, data: DataVisitorParam) {
        DataExtensionsFactory.create(codeGenerator, data.definition, data.allDefinitions)
        SQLDeclarationFactory.create(data.definition)
    }
}