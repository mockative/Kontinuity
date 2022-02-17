package io.mockative.krouton.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import io.mockative.krouton.Cancellable

val FLOW = ClassName("kotlinx.coroutines.flow", "Flow")
val STATE_FLOW = ClassName("kotlinx.coroutines.flow", "StateFlow")
val SHARED_FLOW = ClassName("kotlinx.coroutines.flow", "SharedFlow")

val CANCELLABLE = Cancellable::class.asClassName()
