package com.app.sample

import io.mockative.kontinuity.internal.KontinuityFlow
import io.mockative.kontinuity.internal.KontinuityStateFlow
import io.mockative.kontinuity.internal.KontinuitySuspend

class KAuthenticationServiceMock : KAuthenticationService() {
    fun expect(times: Int) = Expectations()

    class Expectations {
        val stateFlowK_get: StateFlowStub.Builder<String?>
            get() = StateFlowStub.Builder()

        fun loginK(request: AuthenticationRequest?): SuspendStub.Builder<AuthenticationResponse> {
            return SuspendStub.Builder()
        }

        fun getFlowsAsyncK(request: AuthenticationRequest): Suspend
    }
}

class StateFlowStub<R> {
    class Builder<R> {
        fun andReturn(value: KontinuityStateFlow<R>) {
            TODO()
        }
    }
}

class SuspendStub<R> {
    class Builder<R> {
        fun andReturn(value: R) {
            TODO()
        }

        fun andInvoke(block: KontinuitySuspend<R>) {
            TODO()
        }
    }
}

class SuspendFlowStub<R> {
    class Builder<R> {
        fun andReturn(value: R)
    }
}

fun <T> createSuspend(value: T): KontinuitySuspend<T> {
    TODO()
}

fun foo() {
    val mock = KAuthenticationServiceMock()
    mock.expect(times = 1).stateFlowK_get.andReturn(value = { _, _, _, _ -> TODO() })
    mock.expect(times = 1).stateFlowK_get.andInvoke { createStateFlow("foo") }

    mock.expect(times = 1).loginK(AuthenticationRequest(code = "code")).andInvoke { { _, _ -> TODO() } }
}
