package io.mockative.krouton

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun <R> invokeSuspend(block: suspend () -> R, onSuccess: (R) -> Unit, onFailure: (Throwable) -> Unit): Cancellable {
    val mainScope = CoroutineScope(Dispatchers.Main.immediate)

    val job = mainScope.launch {
        try {
            onSuccess(block())
        } catch (error: Throwable) {
            onFailure(error)
        }
    }

    return Cancellable { job.cancel() }
}
