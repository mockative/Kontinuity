package io.mockative.kontinuity.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.*

class KontinuityWriter {
    fun writeClassKontinuity(codeGenerator: CodeGenerator, classDec: KSClassDeclaration) {
        val nativeClassName = classDec.toNativeInterfaceClassName()
        val nativeWrapperClassName = classDec.toNativeWrapperClassName()

        val fileName = classDec.containingFile!!.fileName.removeSuffix(".kt") + ".Kontinuity"

        val fileSpec = FileSpec.builder(classDec.packageName.asString(), fileName)
            .addType(classDec.buildNativeTypeSpec(nativeClassName))
            .addType(classDec.buildNativeWrapperTypeSpec(nativeWrapperClassName, nativeClassName))
            .build()

        fileSpec.writeTo(codeGenerator, Dependencies(true, classDec.containingFile!!))
    }
}