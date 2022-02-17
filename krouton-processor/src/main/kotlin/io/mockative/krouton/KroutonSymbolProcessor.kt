package io.mockative.krouton

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.mockative.krouton.generator.KroutonWriter
import io.mockative.krouton.generator.Logger
import io.mockative.krouton.swift.KroutonSwiftGenerator

class KroutonSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    options: Map<String, String>
) : SymbolProcessor, Logger {

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

            val kroutonWriter = KroutonWriter(codeGenerator, this)

            kroutonClassDecs.forEach { classDec ->
                kroutonWriter.writeKroutons(classDec)
            }

            info("Finished generating ${kroutonWriter.numberOfWrittenProperties} Krouton properties and ${kroutonWriter.numberOfWrittenFunctions} Krouton functions for ${kroutonClassDecs.size} annotated types")

            val swiftGenerator = KroutonSwiftGenerator(codeGenerator, this, "shared")
            kroutonClassDecs.forEach { classDec ->
                swiftGenerator.addExtensionFile(classDec)
            }
        }

        processed = true

        return emptyList()
    }

    override fun info(message: String) {
        if (isInfoLogEnabled) {
            logger.info("[Krouton] $message")
        }
    }

    override fun debug(message: String) {
        if (isDebugLogEnabled) {
            logger.info("[Krouton] $message")
        }
    }
}
