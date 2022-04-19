package io.mockative.kontinuity.internal

actual typealias KontinuityError = Throwable

internal actual fun Throwable.asKontinuityError(): KontinuityError = this
