package io.mockative.kontinuity

import kotlin.reflect.KClass

/**
 * Annotation applied to every generated Kontinuity Wrapper class
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KontinuityGenerated(
    val source: KClass<*>,
)
