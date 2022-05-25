package io.mockative.kontinuity

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo
import io.mockative.kontinuity.configuration.DefaultConfiguration
import io.mockative.kontinuity.configuration.KSPArgumentConfiguration
import io.mockative.kontinuity.configuration.SourceConfiguration
import io.mockative.kontinuity.ksp.addWrapperTypes
import kotlin.time.measureTime

class KontinuitySymbolProcessor(
    private val log: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val options: Map<String, String>,
) : SymbolProcessor {

    private var isProcessed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (isProcessed) {
            return emptyList()
        }

        val startTime = System.currentTimeMillis()

        // Default Configuration
        val defaultConfiguration = DefaultConfiguration()
        log.info("Default Configuration: $defaultConfiguration")

        // KSP Argument Configuration
        val kspArgumentConfiguration = KSPArgumentConfiguration
            .fromOptions(options, defaultConfiguration)

        log.info("KSP Argument Configuration: $kspArgumentConfiguration")

        // Source Configuration
        val sourceConfiguration = SourceConfiguration
            .fromResolver(resolver, log, kspArgumentConfiguration) ?: return emptyList()

        log.info("Source Configuration: $sourceConfiguration")

        // Default Scope Declaration
        val defaultScopeDeclaration = DefaultKontinuityScopeDeclaration.fromResolver(resolver)
        log.warn("Default Scope: $defaultScopeDeclaration")

        // Annotated Types
        val processableFiles = ProcessableFile.fromResolver(resolver, sourceConfiguration, defaultScopeDeclaration)
        processableFiles.forEach { file ->
            FileSpec.builder(file.packageName, "${file.fileName.removeSuffix(".kt")}.Kontinuity")
                .addWrapperTypes(file.types)
                .build()
                .writeTo(codeGenerator, aggregating = false)
        }

        val endTime = System.currentTimeMillis()

        val duration = (endTime - startTime).toDouble() / 1000.0
        log.info("Processing finished after ${String.format("%.2f", duration)}")

        isProcessed = true

        return emptyList()
    }
}
