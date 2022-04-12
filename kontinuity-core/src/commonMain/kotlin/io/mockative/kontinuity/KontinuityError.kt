package io.mockative.kontinuity

expect class KontinuityError

internal expect fun Throwable.asKontinuityError(): KontinuityError
