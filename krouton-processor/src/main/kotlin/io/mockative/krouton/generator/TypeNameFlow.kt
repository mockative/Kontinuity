package io.mockative.krouton.generator

import com.squareup.kotlinpoet.TypeName

val TypeName.isFlow: Boolean
    get() = when (this) {
        FLOW, STATE_FLOW, SHARED_FLOW -> true
        else -> false
    }
