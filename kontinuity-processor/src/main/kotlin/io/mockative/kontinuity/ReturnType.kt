package io.mockative.kontinuity

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName
import io.mockative.kontinuity.generator.FLOW
import io.mockative.kontinuity.generator.SHARED_FLOW
import io.mockative.kontinuity.generator.STATE_FLOW

sealed interface ReturnType {
    data class Value(val type: TypeName) : ReturnType
    data class Flow(val elementType: TypeName) : ReturnType
    data class StateFlow(val elementType: TypeName) : ReturnType
}

internal fun KSTypeReference.getReturnType(typeParameterResolver: TypeParameterResolver): ReturnType {
    val typeName = toTypeName(typeParameterResolver)

    if (typeName is ParameterizedTypeName) {
        when (typeName.rawType) {
            STATE_FLOW -> {
                return ReturnType.StateFlow(typeName.typeArguments[0])
            }
            FLOW, SHARED_FLOW -> {
                return ReturnType.Flow(typeName.typeArguments[0])
            }
        }
    }

    return ReturnType.Value(typeName)
}

internal fun KSFunctionDeclaration.getReturnType(typeParameterResolver: TypeParameterResolver): ReturnType {
    return returnType!!.getReturnType(typeParameterResolver)
}