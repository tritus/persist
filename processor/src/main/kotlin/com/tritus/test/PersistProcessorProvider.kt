package com.tritus.test

import com.google.devtools.ksp.processing.*

internal class PersistProcessorProvider: SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor = PersistProcessor(environment.codeGenerator, environment.logger)
}
