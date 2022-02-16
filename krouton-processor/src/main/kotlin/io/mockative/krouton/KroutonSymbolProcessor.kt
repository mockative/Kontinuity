package io.mockative.krouton

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import io.mockative.krouton.generator.KroutonWriter
import io.mockative.krouton.generator.Logger
import java.io.OutputStreamWriter

class KroutonSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    options: Map<String, String>
) : SymbolProcessor {

    private var processed = false

    private val isDebugLogEnabled: Boolean = options["krouton.logging"]?.lowercase() == "debug"
    private val isInfoLogEnabled: Boolean = isDebugLogEnabled || options["krouton.logging"]?.lowercase() == "info"

    override fun process(resolver: Resolver): List<KSAnnotated> {
        debug("Starting")

        if (processed) {
            debug("Skipped: Already Processed")
            return emptyList()
        }

        val annotatedSymbols = resolver.getSymbolsWithAnnotation(Krouton::class.qualifiedName!!).toList()
        if (annotatedSymbols.isEmpty()) {
            debug("Skipped: No annotated symbols returned")
            return emptyList()
        }

        debug("Processing")

        val kroutonClassDecs = annotatedSymbols
            .mapNotNull { symbol -> symbol as? KSClassDeclaration }

        if (kroutonClassDecs.isNotEmpty()) {
            debug("Writing Kroutons.kt file")

            // Create mock(KClass) functions
            val sources = resolver.getAllFiles().toList().toTypedArray()
            val kroutonsFile = codeGenerator.createNewFile(Dependencies(false, *sources), "io.mockative.krouton", "Kroutons")
            val kroutonsFileWriter = OutputStreamWriter(kroutonsFile)
            kroutonsFileWriter.appendLine("package io.mockative.krouton")
            kroutonsFileWriter.appendLine()

            val kroutonWriter = KroutonWriter(kroutonsFileWriter, object : Logger {
                override fun info(message: String) {
                    this@KroutonSymbolProcessor.info(message)
                }

                override fun debug(message: String) {
                    this@KroutonSymbolProcessor.debug(message)
                }
            })

            kroutonClassDecs.forEach { classDec ->
                kroutonWriter.writeKroutons(classDec)
            }

            kroutonsFileWriter.flush()

            info("Finished generating ${kroutonWriter.numberOfWrittenProperties} Krouton properties and ${kroutonWriter.numberOfWrittenFunctions} Krouton functions for ${kroutonClassDecs.size} annotated types")
        }

        processed = true

        return emptyList()
    }

    private fun info(message: String) {
        if (isInfoLogEnabled) {
            logger.info("[Krouton] $message")
        }
    }

    private fun debug(message: String) {
        if (isDebugLogEnabled) {
            logger.info("[Krouton] $message")
        }
    }
}
