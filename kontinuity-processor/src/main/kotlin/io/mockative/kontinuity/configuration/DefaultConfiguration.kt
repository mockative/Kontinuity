package io.mockative.kontinuity.configuration

import io.mockative.kontinuity.KontinuityGeneration

@Suppress("MayBeConstant")
data class DefaultConfiguration(
    val wrappers: String = "K%T",
    val generation: KontinuityGeneration = KontinuityGeneration.OPT_OUT,
    val suspend: String = "%MK",
    val suspendFlow: String = "%MK",
    val flow: String = "%MK",
    val members: String = "%M",
)