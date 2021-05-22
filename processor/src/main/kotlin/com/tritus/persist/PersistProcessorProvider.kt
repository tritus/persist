package com.tritus.persist

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorProvider

internal class PersistProcessorProvider: SymbolProcessorProvider {
    override fun create(
        options: Map<String, String>,
        kotlinVersion: KotlinVersion,
        codeGenerator: CodeGenerator,
        logger: KSPLogger
    ): SymbolProcessor = PersistProcessor(codeGenerator, logger)
}