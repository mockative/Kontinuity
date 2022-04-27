package io.mockative.kontinuity

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class KontinuitySymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val logger = PrefixedKSPLogger(environment.logger)
        val codeGenerator = environment.codeGenerator
        val options = environment.options
        return KontinuitySymbolProcessor(logger, codeGenerator, options)
    }
}