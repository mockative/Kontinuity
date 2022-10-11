package io.mockative.kontinuity.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect

/**
 * A function that collects a [Flow] via callbacks.
 *
 * The function takes an `onItem` and `onComplete` callback
 * and returns a cancellable that can be used to cancel the collection.
 */
typealias KontinuityStateFlow<T> = (mode: String, setValue: KontinuityCallback<T>, onItem: KontinuityCallback<T>, onComplete: KontinuityCallback<KontinuityError?>) -> KontinuityCancellable

/**
 * Creates a [KontinuityStateFlow] for this [Flow].
 *
 * @param scope the [CoroutineScope] to use for the collection, or `null` to use the [defaultCoroutineScope].
 * @receiver the [Flow] to collect.
 * @see Flow.collect
 */
fun <T> StateFlow<T>.toKontinuityStateFlow(scope: CoroutineScope? = null): KontinuityStateFlow<T> {
    val stateFlow: KontinuityStateFlow<T> = stateFlow@{ mode, setValue, onItem, onComplete ->
        if (mode == "receive") {
            setValue(value)
            return@stateFlow ({})
        } else {
            return@stateFlow toKontinuityFlow(scope)(onItem, onComplete)
        }
    }

    return stateFlow
}