package io.mockative.krouton.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*

class KroutonWriter(
    private val codeGenerator: CodeGenerator,
    private val logger: Logger? = null
) {
    var numberOfWrittenProperties = 0
        private set

    var numberOfWrittenFunctions = 0
        private set

    fun writeKroutons(classDec: KSClassDeclaration) {
        val kroutonClassWriter = KroutonClassWriter(codeGenerator, logger, classDec)
        kroutonClassWriter.writeKroutons()

        numberOfWrittenProperties += kroutonClassWriter.numberOfWrittenProperties
        numberOfWrittenFunctions += kroutonClassWriter.numberOfWrittenFunctions
    }
}


