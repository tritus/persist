package com.tritus.persist

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.tritus.persist.annotation.Persist

internal class PersistProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotatedSymbols = resolver.getSymbolsWithAnnotation(Persist::class.qualifiedName!!).toList()
        val processableSymbols = annotatedSymbols
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.INTERFACE && it.validate() }
        processableSymbols.forEach { it.accept(PersistVisitor(codeGenerator), Unit) }
        return annotatedSymbols - processableSymbols
    }
}
