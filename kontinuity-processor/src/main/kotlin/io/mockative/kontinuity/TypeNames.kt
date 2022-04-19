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

val KONTINUITY_SUSPEND = ClassName("io.mockative.kontinuity.internal", "KontinuitySuspend")
val KONTINUITY_FLOW = ClassName("io.mockative.kontinuity.internal", "KontinuityFlow")
val KONTINUITY_STATE_FLOW = ClassName("io.mockative.kontinuity.internal", "KontinuityStateFlow")

val KONTINUITY_SUSPEND_FUNCTION = MemberName("io.mockative.kontinuity.internal", "kontinuitySuspend")
val TO_KONTINUITY_FLOW_FUNCTION = MemberName("io.mockative.kontinuity.internal", "toKontinuityFlow")
val TO_KONTINUITY_STATE_FLOW_FUNCTION = MemberName("io.mockative.kontinuity.internal", "toKontinuityStateFlow")
