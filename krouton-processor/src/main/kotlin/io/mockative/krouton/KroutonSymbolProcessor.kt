package io.mockative.krouton

import com.google.devtools.ksp.processing.*
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

    private val moduleName: String = options["krouton.darwin.moduleName"] ?: "shared"
    private val swiftOutputDir: String? = options["krouton.swift.outputDir"]

    private fun addKroutonKitSwift() {
        debug("Writing KroutonKit.swift file")

        // TODO Investigate feasibility of generating Swift files from resulting Kotlin Module header file (e.g. `shared.h`), to deal with potential incremental compilation / cleaning issues.
        // TODO Consider using a top-level package/directory for ease-of-use in Xcode
        // TODO Consider outputting a single file instead of multiple files to improve the developer experience in Xcode

        javaClass.classLoader.getResourceAsStream("io/mockative/krouton/KroutonKit.swift")!!
            .use { source ->
                try {
                    codeGenerator.createNewFile(Dependencies(true), "krouton", "KroutonKit", "swift")
                        .use { destination ->
                            source.copyTo(destination)
                        }
                } catch (e: FileAlreadyExistsException) {
                    // Nothing
                }
            }
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        debug("Starting")

        addKroutonKitSwift()

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

        val kroutonWriter = KroutonWriter(codeGenerator, this)
        val swiftGenerator = KroutonSwiftGenerator(codeGenerator, this, moduleName, swiftOutputDir)

        annotatedSymbols
            .mapNotNull { symbol -> symbol as? KSClassDeclaration }
            .forEach { classDec ->
                kroutonWriter.writeKroutons(classDec)
                swiftGenerator.addExtensionFile(classDec)
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
