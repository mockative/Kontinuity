package io.mockative.kontinuity.koin

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

internal lateinit var codeGenerator: CodeGenerator
    private set

class KontinuityKoinSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        codeGenerator = environment.codeGenerator

        Options.source = environment.options
        Log.logger = environment.logger

        return KontinuityKoinSymbolProcessor()
    }
}
