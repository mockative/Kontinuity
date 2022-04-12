package io.mockative.krouton

import kotlinx.cinterop.UnsafeNumber
import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey

actual typealias KontinuityError = NSError

@OptIn(UnsafeNumber::class)
internal actual fun Throwable.asKontinuityError(): KontinuityError {
    val userInfo = mutableMapOf<Any?, Any>()
    userInfo["KotlinException"] = this.freeze()

    val message = message
    if (message != null) {
        userInfo[NSLocalizedDescriptionKey] = message
    }

    return NSError.errorWithDomain("KotlinException", 0, userInfo)
}
