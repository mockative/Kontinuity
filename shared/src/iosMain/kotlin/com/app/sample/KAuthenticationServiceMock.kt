package com.app.sample

import io.mockative.kontinuity.internal.KontinuityFlow
import io.mockative.kontinuity.internal.KontinuityStateFlow
import io.mockative.kontinuity.internal.KontinuitySuspend

class KAuthenticationServiceMock : KAuthenticationService() {
    fun expectGet(times: Int) = GetterExpectations(times)
    fun expectSet(times: Int) = SetterExpectations(times)

    fun expectInvocation(times: Int) = FunctionExpectations(times)

    class SetterExpectations(private val times: Int) {
        fun unrelatedProperty(block: (String, Unit) -> Unit): Any? = TODO()
    }

    class GetterExpectations(private val times: Int) {
        fun unrelatedProperty(block: (Unit) -> String): Any? = TODO()

        fun stateFlowK(block: KontinuityStateFlow<String?>): Any? = TODO()
    }

    class FunctionExpectations(private val times: Int) {
        fun loginK(request: AuthenticationRequest?, block: KontinuitySuspend<AuthenticationResponse>): Any? = TODO()

        fun getFlowsAsyncK(request: AuthenticationRequest, implementation: KontinuitySuspend<KontinuityFlow<List<AuthenticationResponse>>>): Any? = TODO()
    }
}

fun foo() {
    val mock = KAuthenticationServiceMock()
    mock.expectGet(times = 1).stateFlowK { _, _, _, _ -> TODO() }
    mock.expectSet(times = 1).unrelatedProperty { value, unit -> TODO() }

    mock.expectInvocation(times = 1).loginK(AuthenticationRequest(code = "code")) { _, _ -> TODO() }
}
