package io.mockative.kontinuity.internal

expect class KontinuityError

internal expect fun Throwable.asKontinuityError(): KontinuityError
