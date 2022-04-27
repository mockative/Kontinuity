package io.mockative.kontinuity

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.time.measureTime

class KontinuitySymbolProcessor(
    private val log: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val options: Map<String, String>,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
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

        return emptyList()
    }
}

//internal fun CodeGenerator.addWrapperFile(sourceFile: SourceFile, classDecs: List<KSClassDeclaration>) {
//    val wrapperFile = sourceFile.getWrapperFile()
//    val dependentFiles = classDecs.getAllDependentFiles()
//
//    val packageName = sourceFile.packageName
//    val fileName = wrapperFile.fileName
//
//    FileSpec.builder(packageName, fileName)
//        .addWrapperTypes(classDecs)
//        .build()
//        .writeTo(this, false, dependentFiles)
//}
//
//internal fun CodeGenerator.addFunctionsFile(sourceFile: SourceFile, classDecs: List<KSClassDeclaration>) {
//    val packageName = "io.mockative.kontinuity"
//    val fileName = "${sourceFile.packageName}.${sourceFile.fileName}.Kontinuity"
//
//    FileSpec.builder(packageName, fileName)
//        .addGetKontinuityWrapperClassFunctions(classDecs)
//        .addCreateKontinuityWrapperFunctions(classDecs)
//        .build()
//        .writeTo(this, false, listOf(sourceFile.file))
//}
//
//internal fun FileSpec.Builder.addGetKontinuityWrapperClassFunctions(classDecs: List<KSClassDeclaration>): FileSpec.Builder {
//    return classDecs
//        .mapNotNull { classDec -> classDec.getWrapperClass() }
//        .fold(this) { fileSpec, wrapperClass ->
//            fileSpec.addGetKontinuityWrapperClassFunction(wrapperClass.source, wrapperClass)
//        }
//}
//
//internal fun FileSpec.Builder.addGetKontinuityWrapperClassFunction(source: SourceClass, wrapper: WrapperClass): FileSpec.Builder {
//    return addFunction(
//        FunSpec.builder("getKontinuityWrapperClass")
//            .receiver(KCLASS.parameterizedBy(source.className))
//            .returns(KCLASS.parameterizedBy(wrapper.className))
//            .addStatement("return %T::class", wrapper.className)
//            .build()
//    )
//}
//
//internal fun FileSpec.Builder.addCreateKontinuityWrapperFunctions(classDecs: List<KSClassDeclaration>): FileSpec.Builder {
//    return classDecs
//        .mapNotNull { classDec -> classDec.getWrapperClass() }
//        .fold(this) { fileSpec, wrapperClass ->
//            fileSpec.addCreateKontinuityWrapperFunction(wrapperClass.source, wrapperClass)
//        }
//}
//
//internal fun FileSpec.Builder.addCreateKontinuityWrapperFunction(source: SourceClass, wrapper: WrapperClass): FileSpec.Builder {
//    val parameterSpec = ParameterSpec.builder("wrapping", source.className)
//        .build()
//
//    return addFunction(
//        FunSpec.builder("createKontinuityWrapper")
//            .addParameter(parameterSpec)
//            .returns(wrapper.className)
//            .addStatement("return %T(%N)", wrapper.className, parameterSpec)
//            .build()
//    )
//}
