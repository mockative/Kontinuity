package io.mockative.krouton

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.toClassName
import io.mockative.krouton.generator.KONTINUITY_ANNOTATION
import io.mockative.krouton.generator.KontinuityWriter
import io.mockative.krouton.generator.Log
import io.mockative.krouton.generator.Options

class KontinuitySymbolProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {

    private var processed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (processed) {
            Log.debug("Skipped: Already Processed")
            return emptyList()
        }

        Log.info("Starting with options:\n$Options")

        val annotatedSymbols = resolver.getSymbolsWithAnnotation(KONTINUITY_ANNOTATION).toList()
        if (annotatedSymbols.isEmpty()) {
            Log.debug("Skipped: No annotated types found 12")
            return emptyList()
        }

        val declarationsToGenerate = annotatedSymbols
            .mapNotNull { symbol -> symbol as? KSClassDeclaration }
            .flatMap { symbol ->
                symbol.getAllSuperTypes()
                    .mapNotNull { it.declaration as? KSClassDeclaration } + symbol
            }
            .distinctBy { it.toClassName() }

        Log.info("Processing ${declarationsToGenerate.size} types from ${annotatedSymbols.size} annotated types")

        val kontinuityWriter = KontinuityWriter()

        declarationsToGenerate
            .forEach { classDec ->
                val className = classDec.toClassName()

                if (classDec.isAnnotationPresent(Kontinuity::class)) {
                    Log.debug("Processing annotated type $className")
                } else {
                    Log.debug("Processing inherited type $className")
                }

                kontinuityWriter.writeClassKontinuity(codeGenerator, classDec)
            }

        processed = true

        return emptyList()
    }
}
