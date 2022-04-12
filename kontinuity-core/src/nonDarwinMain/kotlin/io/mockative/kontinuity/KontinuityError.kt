package io.mockative.kontinuity

actual typealias KontinuityError = Throwable

internal actual fun Throwable.asKontinuityError(): KontinuityError = this
