package io.mockative.krouton.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.UNIT

val FLOW = ClassName("kotlinx.coroutines.flow", "Flow")
val STATE_FLOW = ClassName("kotlinx.coroutines.flow", "StateFlow")
val SHARED_FLOW = ClassName("kotlinx.coroutines.flow", "SharedFlow")

val CANCELLATION = LambdaTypeName.get(returnType = UNIT)
