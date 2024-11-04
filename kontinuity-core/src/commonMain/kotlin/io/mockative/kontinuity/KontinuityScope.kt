package io.mockative.kontinuity

import kotlinx.coroutines.CoroutineScope

/**
 * Designates a top-level property as the [CoroutineScope] of coroutines launched from within that
 * file, or as the default [CoroutineScope] used for all coroutines launched by Kontinuity, unless
 * otherwise overwritten in a file.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class KontinuityScope(
    /**
     * Specifies whether this is the default [CoroutineScope]. Only one property in a source set
     * may be the default scope. The default value is `false`.
     */
    val default: Boolean = false,
)
