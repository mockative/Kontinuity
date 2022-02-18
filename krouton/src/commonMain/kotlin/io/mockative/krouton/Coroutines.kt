package io.mockative.krouton

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun <R> invokeSuspend(block: suspend () -> R, onSuccess: (R) -> Unit, onFailure: (Throwable) -> Unit): Cancellation {
    val mainScope = CoroutineScope(Dispatchers.Main.immediate)

    val job = mainScope.launch {
        try {
            onSuccess(block())
        } catch (error: Throwable) {
            onFailure(error)
        }
    }

    return { job.cancel() }
}

fun invokeSuspend(block: suspend () -> Unit, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit): Cancellation {
    val mainScope = CoroutineScope(Dispatchers.Main.immediate)

    val job = mainScope.launch {
        try {
            block()
            onSuccess()
        } catch (error: Throwable) {
            onFailure(error)
        }
    }

    return { job.cancel() }
}
