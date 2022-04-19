package io.mockative.kontinuity

import kotlin.reflect.KClass

@Suppress("unused")
fun KClass<*>.getKontinuityWrapperClass(): KClass<*> =
    throw UnsupportedOperationException(
        """
        |This function should not be called. Please verify whether the Kotlin Symbol Processor 
        |`kontinuity-processor` is running for the current target.
        """.trimMargin()
    )

@Suppress("unused")
fun createKontinuityWrapper(@Suppress("UNUSED_PARAMETER") wrapping: Any): Any =
    throw UnsupportedOperationException(
        """
        |This function should not be called. Please verify whether the Kotlin Symbol Processor 
        |`kontinuity-processor` is running for the current target.
        """.trimMargin()
    )
