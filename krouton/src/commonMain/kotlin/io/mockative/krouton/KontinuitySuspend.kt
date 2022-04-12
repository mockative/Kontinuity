package io.mockative.krouton

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A function that awaits a suspend function via callbacks.
 *
 * The function takes an `onResult` and `onError` callback
 * and returns a cancellable that can be used to cancel the suspend function.
 */
typealias KontinuitySuspend<T> = (onResult: KontinuityCallback<T>, onError: KontinuityCallback<KontinuityError>) -> KontinuityCancellable

/**
 * Creates a [kontinuitySuspend] for the provided suspend [block].
 *
 * @param scope the [CoroutineScope] to run the [block] in, or `null` to use the [defaultCoroutineScope].
 * @param block the suspend block to await.
 */
fun <T> kontinuitySuspend(scope: CoroutineScope? = null, block: suspend () -> T): KontinuitySuspend<T> {
    val coroutineScope = scope ?: defaultCoroutineScope
    return (collect@{ onResult: KontinuityCallback<T>, onError: KontinuityCallback<KontinuityError> ->
        val job = coroutineScope.launch {
            try {
                onResult(block())
            } catch (e: CancellationException) {
                // CancellationExceptions are handled by the invokeOnCompletion
                // this is required since the job could be cancelled before it is started
                throw e
            } catch (e: Throwable) {
                onError(e.asKontinuityError())
            }
        }
        job.invokeOnCompletion { cause ->
            // Only handle CancellationExceptions, all other exceptions should be handled inside the job
            if (cause !is CancellationException) return@invokeOnCompletion
            onError(cause.asKontinuityError())
        }
        return@collect job.asNativeCancellable()
    }).freeze()
}