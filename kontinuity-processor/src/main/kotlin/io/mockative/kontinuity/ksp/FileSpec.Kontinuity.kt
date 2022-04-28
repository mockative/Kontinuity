package io.mockative.kontinuity.ksp

import com.squareup.kotlinpoet.FileSpec
import io.mockative.kontinuity.ProcessableType
import io.mockative.kontinuity.kotlinpoet.buildWrapperTypeSpec

fun FileSpec.Builder.addWrapperTypes(types: List<ProcessableType>): FileSpec.Builder {
    return types.fold(this) { fileSpec, wrapperClass ->
        fileSpec.addWrapperType(wrapperClass)
    }
}

private fun FileSpec.Builder.addWrapperType(type: ProcessableType): FileSpec.Builder {
    return addType(type.buildWrapperTypeSpec())
}