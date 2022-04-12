package io.mockative.krouton

actual typealias KontinuityError = Throwable

internal actual fun Throwable.asKontinuityError(): KontinuityError = this
