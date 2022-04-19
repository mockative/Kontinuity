package io.mockative.kontinuity

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo
import io.mockative.kontinuity.generator.*
import io.mockative.kontinuity.generator.getSourceClass
import kotlin.time.measureTime

data class SourceFile(val file: KSFile, val fileName: String, val packageName: String)

fun KSDeclaration.getContainingFile(): SourceFile {
    val file = containingFile!!
    val fileName = file.fileName.removeSuffix(".kt")
    val packageName = file.packageName.asString()
    return SourceFile(file, fileName, packageName)
}

data class WrapperFile(val fileName: String)

fun SourceFile.getWrapperFile(): WrapperFile {
    return WrapperFile("${fileName}.Kontinuity")
}

fun List<KSClassDeclaration>.getAllDependentFiles(): List<KSFile> {
    return flatMap { it.getAllDependentFiles() }
}

fun KSClassDeclaration.getAllDependentFiles(): List<KSFile> {
    return listOfNotNull(containingFile) + getAllSuperTypesContainingFiles()
}

private fun KSClassDeclaration.getAllSuperTypesContainingFiles() =
    getAllSuperTypes()
        .mapNotNull { type -> type.declaration as? KSClassDeclaration }
        .mapNotNull { classDec -> classDec.containingFile }

class KontinuitySymbolProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {

    private var processed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (processed) {
            Log.debug("Skipped: Already Processed")
            return emptyList()
        }

        val duration = measureTime {
            Log.info("Starting with options:\n$Options")

            val annotatedTypes = resolver.getSymbolsWithAnnotation(KONTINUITY_ANNOTATION.canonicalName)
                .mapNotNull { classDec -> classDec as? KSClassDeclaration }
                .toList()

            if (annotatedTypes.isEmpty()) {
                Log.debug("Skipped: No annotated types found")
                return emptyList()
            }

            annotatedTypes
                .groupBy { classDec -> classDec.getContainingFile() }
                .forEach { (sourceFile, classDecs) ->
                    val wrapperFile = sourceFile.getWrapperFile()
                    val dependentFiles = classDecs.getAllDependentFiles()

                    FileSpec.builder(sourceFile.packageName, wrapperFile.fileName)
                        .addWrapperTypes(classDecs)
                        .build()
                        .writeTo(codeGenerator, false, dependentFiles)

                    FileSpec.builder("io.mockative.kontinuity", "${sourceFile.packageName}.${sourceFile.fileName}.Kontinuity")
                        .addGetKontinuityWrapperClassFunctions(classDecs)
                        .addCreateKontinuityWrapperFunctions(classDecs)
                        .build()
                        .writeTo(codeGenerator, false, listOf(sourceFile.file))
                }

            processed = true
        }

        Log.info("Processing finished after $duration")

        return emptyList()
    }
}

internal fun FileSpec.Builder.addGetKontinuityWrapperClassFunctions(classDecs: List<KSClassDeclaration>): FileSpec.Builder {
    return classDecs.fold(this) { fileSpec, classDec ->
        fileSpec.addGetKontinuityWrapperClassFunction(classDec)
    }
}

internal fun FileSpec.Builder.addGetKontinuityWrapperClassFunction(classDec: KSClassDeclaration): FileSpec.Builder {
    val source = classDec.getSourceClass()
    val wrapper = classDec.getWrapperClass()

    return addFunction(
        FunSpec.builder("getKontinuityWrapperClass")
            .receiver(KCLASS.parameterizedBy(source.className))
            .returns(KCLASS.parameterizedBy(wrapper.className))
            .addStatement("return %T::class", wrapper.className)
            .build()
    )
}

internal fun FileSpec.Builder.addCreateKontinuityWrapperFunctions(classDecs: List<KSClassDeclaration>): FileSpec.Builder {
    return classDecs.fold(this) { fileSpec, classDec ->
        fileSpec.addCreateKontinuityWrapperFunction(classDec)
    }
}

internal fun FileSpec.Builder.addCreateKontinuityWrapperFunction(classDec: KSClassDeclaration): FileSpec.Builder {
    val source = classDec.getSourceClass()
    val wrapper = classDec.getWrapperClass()

    val parameterSpec = ParameterSpec.builder("wrapping", source.className)
        .build()

    return addFunction(
        FunSpec.builder("createKontinuityWrapper")
            .addParameter(parameterSpec)
            .returns(wrapper.className)
            .addStatement("return %T(%N)", wrapper.className, parameterSpec)
            .build()
    )
}
