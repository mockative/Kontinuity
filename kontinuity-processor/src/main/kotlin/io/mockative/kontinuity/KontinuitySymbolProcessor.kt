package io.mockative.kontinuity

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo
import io.mockative.kontinuity.configuration.DefaultConfiguration
import io.mockative.kontinuity.configuration.KSPArgumentConfiguration
import io.mockative.kontinuity.configuration.SourceConfiguration
import io.mockative.kontinuity.kotlinpoet.toMemberName
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

        val duration = measureTime {
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

            val defaultScopeDeclaration = resolver.getSymbolsWithAnnotation(KONTINUITY_SCOPE_ANNOTATION.canonicalName)
                .filter { it.getAnnotationsByType(KontinuityScope::class).first().default }
                .joinToString { it.toString() }
            log.warn("Default Scope: ${defaultScopeDeclaration}")

            // Annotated Types
            val processableFiles = ProcessableFile.fromResolver(resolver, sourceConfiguration)
            processableFiles.forEach { file ->
                FileSpec.builder(file.packageName, "${file.fileName.removeSuffix(".kt")}.Kontinuity")
                    .addWrapperTypes(file.types)
                    .build()
                    .writeTo(codeGenerator, aggregating = false)
            }
        }

        log.info("Processing finished after $duration")

        isProcessed = true

        return emptyList()
    }
}
