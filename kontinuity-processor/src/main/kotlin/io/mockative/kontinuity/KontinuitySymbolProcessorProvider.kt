package io.mockative.kontinuity

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class KontinuitySymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        Log.logger = environment.logger
        Options.source = environment.options
        return KontinuitySymbolProcessor(environment.codeGenerator, environment.options)
    }

}