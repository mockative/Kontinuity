package io.mockative.krouton.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.UNIT

val FLOW = ClassName("kotlinx.coroutines.flow", "Flow")
val STATE_FLOW = ClassName("kotlinx.coroutines.flow", "StateFlow")
val SHARED_FLOW = ClassName("kotlinx.coroutines.flow", "SharedFlow")

val CANCELLATION = LambdaTypeName.get(returnType = UNIT)

val KONTINUITY_SUSPEND = ClassName("io.mockative.krouton", "KontinuitySuspend")
val KONTINUITY_FLOW = ClassName("io.mockative.krouton", "KontinuityFlow")
val KONTINUITY_STATE_FLOW = ClassName("io.mockative.krouton", "KontinuityStateFlow")

val KONTINUITY_SUSPEND_FUNCTION = MemberName("io.mockative.krouton", "kontinuitySuspend")
val TO_KONTINUITY_FLOW_FUNCTION = MemberName("io.mockative.krouton", "toKontinuityFlow")
val TO_KONTINUITY_STATE_FLOW_FUNCTION = MemberName("io.mockative.krouton", "toKontinuityStateFlow")
