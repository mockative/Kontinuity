package io.mockative.krouton

expect class KontinuityError

internal expect fun Throwable.asKontinuityError(): KontinuityError
