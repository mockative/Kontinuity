package io.mockative.krouton

import platform.Foundation.NSException

actual typealias KontinuityError = NSException

internal actual fun Throwable.asKontinuityError(): KontinuityError = ThrowableNSException(this)
