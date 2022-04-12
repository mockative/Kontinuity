package io.mockative.kontinuity.generator

import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.KModifier

val KSValueParameter.modifiers: List<KModifier>
    get() = buildList {
        if (isCrossInline) add(KModifier.CROSSINLINE)
        if (isNoInline) add(KModifier.NOINLINE)
        if (isVararg) add(KModifier.VARARG)
    }
