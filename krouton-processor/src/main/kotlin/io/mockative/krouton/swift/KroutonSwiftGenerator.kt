package io.mockative.krouton.swift

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.toClassName
import io.mockative.krouton.Krouton
import io.mockative.krouton.generator.Logger

class KroutonSwiftGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: Logger?,
    private val moduleName: String,
) {
    fun addExtensionFile(classDec: KSClassDeclaration) {
        val annotation = classDec.getAnnotationsByType(Krouton::class).first()
        if (annotation.generate) {
            val classGenerator = KroutonSwiftClassExtensionGenerator(codeGenerator, logger, classDec, moduleName)
            classGenerator.addExtensionFile(annotation.outputPackage)
        } else {
            logger?.debug("Skipping Krouton generation for the type `${classDec.toClassName()}` because generation was disabled using the Krouton annotation.")
        }
    }
}

