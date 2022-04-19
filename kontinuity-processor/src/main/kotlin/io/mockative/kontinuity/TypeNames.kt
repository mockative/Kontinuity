package io.mockative.kontinuity

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.UNIT

val KOTLIN_THROWS = ClassName("kotlin", "Throws")
val KOTLIN_ANY = ClassName("kotlin", "Any")
val KCLASS = ClassName("kotlin.reflect", "KClass")

val KONTINUITY_ANNOTATION = ClassName("io.mockative.kontinuity", "Kontinuity")

val FLOW = ClassName("kotlinx.coroutines.flow", "Flow")
val STATE_FLOW = ClassName("kotlinx.coroutines.flow", "StateFlow")
val SHARED_FLOW = ClassName("kotlinx.coroutines.flow", "SharedFlow")

val KONTINUITY_SUSPEND = ClassName("io.mockative.kontinuity", "KontinuitySuspend")
val KONTINUITY_FLOW = ClassName("io.mockative.kontinuity", "KontinuityFlow")
val KONTINUITY_STATE_FLOW = ClassName("io.mockative.kontinuity", "KontinuityStateFlow")

val KONTINUITY_SUSPEND_FUNCTION = MemberName("io.mockative.kontinuity", "kontinuitySuspend")
val TO_KONTINUITY_FLOW_FUNCTION = MemberName("io.mockative.kontinuity", "toKontinuityFlow")
val TO_KONTINUITY_STATE_FLOW_FUNCTION = MemberName("io.mockative.kontinuity", "toKontinuityStateFlow")
