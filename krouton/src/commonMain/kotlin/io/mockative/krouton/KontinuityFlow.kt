package io.mockative.krouton

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * A function that collects a [Flow] via callbacks.
 *
 * The function takes an `onItem` and `onComplete` callback
 * and returns a cancellable that can be used to cancel the collection.
 */
typealias KontinuityFlow<T> = (onItem: KontinuityCallback<T>, onComplete: KontinuityCallback<KontinuityError?>) -> KontinuityCancellable

/**
 * Creates a [KontinuityFlow] for this [Flow].
 *
 * @param scope the [CoroutineScope] to use for the collection, or `null` to use the [defaultCoroutineScope].
 * @receiver the [Flow] to collect.
 * @see Flow.collect
 */
fun <T> Flow<T>.toKontinuityFlow(scope: CoroutineScope? = null): KontinuityFlow<T> {
    val coroutineScope = scope ?: defaultCoroutineScope
    return (collect@{ onItem: KontinuityCallback<T>, onComplete: KontinuityCallback<KontinuityError?> ->
        val job = coroutineScope.launch {
            try {
                collect { onItem(it) }
                onComplete(null)
            } catch (e: CancellationException) {
                // CancellationExceptions are handled by the invokeOnCompletion
                // this is required since the job could be cancelled before it is started
                throw e
            }  catch (e: Throwable) {
                onComplete(e.asKontinuityError())
            }
        }
        job.invokeOnCompletion { cause ->
            // Only handle CancellationExceptions, all other exceptions should be handled inside the job
            if (cause !is CancellationException) return@invokeOnCompletion
            onComplete(cause.asKontinuityError())
        }
        return@collect job.asNativeCancellable()
    }).freeze()
}