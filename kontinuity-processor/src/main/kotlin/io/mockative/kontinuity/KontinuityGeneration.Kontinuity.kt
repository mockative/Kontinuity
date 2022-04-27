package io.mockative.kontinuity

import io.mockative.kontinuity.KontinuityGeneration

inline fun KontinuityGeneration.ifUnspecified(block: () -> KontinuityGeneration): KontinuityGeneration {
    return when (this) {
        KontinuityGeneration.UNSPECIFIED -> block()
        else -> this
    }
}