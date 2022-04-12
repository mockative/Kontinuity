package io.mockative.krouton

import platform.Foundation.NSException
import platform.Foundation.NSNumber
import platform.Foundation.NSString

/**
 * An implementation of [NSException] that wraps a [Throwable].
 */
internal class ThrowableNSException internal constructor(throwable: Throwable) : NSException(
    name = throwable::class.simpleName ?: "<unknown type>",
    reason = throwable.message ?: "<no message>",
    userInfo = null
) {
    /**
     * See also:
     *  - [Casting between mapped types](https://kotlinlang.org/docs/native-objc-interop.html#casting-between-mapped-types)
     *  - [KT-30959 - IDE incorrectly warns that cast from String to NSString can never succeed.](https://youtrack.jetbrains.com/issue/KT-30959)
     */
    private val _callStackReturnAddresses: List<NSNumber> = throwable.getStackTraceAddresses()
        .map {
            @Suppress("CAST_NEVER_SUCCEEDS")
            it as NSNumber
        }

    /**
     * See also:
     *  - [Casting between mapped types](https://kotlinlang.org/docs/native-objc-interop.html#casting-between-mapped-types)
     *  - [KT-30959 - IDE incorrectly warns that cast from String to NSString can never succeed.](https://youtrack.jetbrains.com/issue/KT-30959)
     */
    private val _callStackSymbols: List<NSString> = throwable.getStackTrace()
        .map {
            @Suppress("CAST_NEVER_SUCCEEDS")
            it as NSString
        }

    override fun callStackReturnAddresses(): List<*> {
        return _callStackReturnAddresses
    }

    override fun callStackSymbols(): List<*> {
        return _callStackSymbols
    }
}