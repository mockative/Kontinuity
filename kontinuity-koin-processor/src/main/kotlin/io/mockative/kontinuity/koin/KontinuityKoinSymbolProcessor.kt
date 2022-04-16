package io.mockative.kontinuity.koin

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

fun KSDeclaration.getContainingFileName(): String {
    return containingFile!!.fileName.removeSuffix(".kt")
}

fun KSClassDeclaration.getAllDependentFiles(): List<KSFile> {
    return listOfNotNull(containingFile) + getAllSuperTypes()
        .mapNotNull { type -> type.declaration as? KSClassDeclaration }
        .mapNotNull { classDec -> classDec.containingFile }
}

fun ClassName.toWrapperClassName(): ClassName {
    return ClassName(packageName, getWrapperSimpleName())
}

fun ClassName.getWrapperSimpleName(): String {
    return simpleNames.dropLast(1)
        .plus("K$simpleName")
        .joinToString(".")
}

class KontinuityKoinSymbolProcessor : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(KONTINUITY_ANNOTATION.canonicalName)
            .mapNotNull { classDec -> classDec as? KSClassDeclaration }
            .forEach { classDec ->
                val className = classDec.toClassName()
                val wrapperClassName = className.toWrapperClassName()

                val packageName = "io.mockative.kontinuity.koin"
                val fileName = "${classDec.getContainingFileName()}.Kontinuity.Koin"

                val originatingKSFiles = classDec.getAllDependentFiles()

                FileSpec.builder(packageName, fileName)
                    .addFunction(
                        FunSpec.builder("createKontinuityWrapper")
                            .receiver(KOIN_SCOPE)
                            .addParameter(
                                ParameterSpec.builder("type", KCLASS.parameterizedBy(className))
                                    .addAnnotation(
                                        AnnotationSpec.builder(SUPPRESS_ANNOTATION)
                                            .addMember("%S", "UNUSED_PARAMETER")
                                            .build()
                                    )
                                    .build()
                            )
                            .returns(wrapperClassName)
                            .addStatement("return %T(get())", wrapperClassName)
                            .build()
                    )
                    .build()
                    .writeTo(codeGenerator, false, originatingKSFiles)
            }

        return emptyList()
    }
}
