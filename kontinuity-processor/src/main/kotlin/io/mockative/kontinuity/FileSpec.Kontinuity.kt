package io.mockative.kontinuity

import com.squareup.kotlinpoet.FileSpec

fun FileSpec.Builder.addWrapperTypes(types: List<ProcessableType>): FileSpec.Builder {
    return types.fold(this) { fileSpec, wrapperClass ->
        fileSpec.addWrapperType(wrapperClass)
    }
}

private fun FileSpec.Builder.addWrapperType(type: ProcessableType): FileSpec.Builder {
    return addType(type.buildWrapperTypeSpec())
}