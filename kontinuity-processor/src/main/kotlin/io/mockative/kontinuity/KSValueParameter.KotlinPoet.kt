package io.mockative.kontinuity

import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName

internal fun KSValueParameter.buildParameterSpec(
    typeParameterResolver: TypeParameterResolver
): ParameterSpec {
    val name = name!!.asString()
    val type = type.toTypeName(typeParameterResolver)
    return ParameterSpec.Companion.builder(name, type, modifiers)
        .build()
}

val KSValueParameter.modifiers: List<KModifier>
    get() = buildList {
        if (isCrossInline) add(KModifier.CROSSINLINE)
        if (isNoInline) add(KModifier.NOINLINE)
        if (isVararg) add(KModifier.VARARG)
    }