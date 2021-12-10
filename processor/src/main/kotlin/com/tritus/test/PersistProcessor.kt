package com.tritus.test

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.tritus.test.adapter.PersistentDataDefinitionAdapter.toPersistentDataDefinition
import com.tritus.test.annotation.Persist
import com.tritus.test.factory.PersistDatabaseProviderFactory
import com.tritus.test.annotation.persistQualifiedName

internal class PersistProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val persistAnnotatedSymbols = extractPersistAnnotatedSymbols(resolver)
        return persistAnnotatedSymbols.foldIndexed(emptyList()) { index, acc, declaration ->
            val definition = declaration.toPersistentDataDefinition()
            if (index == 0) declaration.accept(DatabaseCreationVisitor(environment.codeGenerator), definition)
            declaration.accept(PersistentDataVisitor(environment.codeGenerator), definition)
            acc
        }
    }

    private fun extractPersistAnnotatedSymbols(resolver: Resolver): Sequence<KSClassDeclaration> = resolver
        .getSymbolsWithAnnotation(persistQualifiedName)
        .map { symbol ->
            require(symbol is KSClassDeclaration) {
                """
                    The @Persist annotation must be used only on interfaces.
                    Annotated symbol located at ${symbol.location} cannot be persisted.
                """.trimIndent()
            }
            require(symbol.classKind == ClassKind.INTERFACE) {
                """
                    Data annotated with @Persist must be interfaces. Classes and objects are not supported.
                    ${symbol.qualifiedName?.asString()} must be an interface.
                """.trimIndent()
            }
            require(symbol.validate()) {
                """
                    Invalidity detected for @Persist annotated data : ${symbol.qualifiedName?.asString()}
                    All interested types in the symbols enclosed scope may not be resolvable.
                """.trimIndent()
            }
            symbol
        }
}
