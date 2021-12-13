package com.tritus.test

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.tritus.test.adapter.PersistentDataDefinitionAdapter.toPersistentDataDefinition
import com.tritus.test.annotation.persistQualifiedName

internal class PersistProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val persistAnnotatedSymbols = extractPersistAnnotatedSymbols(resolver)
        val definitions = persistAnnotatedSymbols.map { it.toPersistentDataDefinition(persistAnnotatedSymbols) }
        persistAnnotatedSymbols.zip(definitions).forEachIndexed { index, (declaration, definition) ->
            if (index == 0) declaration.accept(DatabaseCreationVisitor(environment), definition)
            declaration.accept(PersistentDataVisitor(environment), DataVisitorParam(definition, definitions))
        }
        return emptyList()
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
