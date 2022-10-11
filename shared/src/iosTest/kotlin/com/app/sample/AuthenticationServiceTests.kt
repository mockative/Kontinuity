package com.app.sample

import io.mockative.Mock
import io.mockative.classOf
import io.mockative.given
import io.mockative.mock
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

class AuthenticationServiceTests {
    @Mock
    val authenticationService = mock(classOf<AuthenticationService>())

    @Test
    fun foo() = runBlocking {
        // Given
        val request = AuthenticationRequest(code = "abc")
        val response = AuthenticationResponse(accessToken = "access-token", refreshToken = "refresh-token")
        given(authenticationService).coroutine { login(request) }
            .thenReturn(response)

        val kAuthenticationService = KAuthenticationService(wrapping = authenticationService)

        var resultOrNull: Result<AuthenticationResponse>? by atomic(null)

        // When
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        withContext(scope.coroutineContext) {
            val mutex = Mutex()
            mutex.lock()

            val kSuspend = kAuthenticationService.loginK(request)
            kSuspend.invoke(
                { value, _ ->
                    resultOrNull = Result.success(value)
                    mutex.unlock()
                },
                { error, _ ->
                    resultOrNull = Result.failure(AssertionError(error.localizedDescription))
                    mutex.unlock()
                }
            )

            withTimeout(5.seconds) {
                mutex.lock()
            }
        }

        // Then
        val result = assertNotNull(resultOrNull)
        val actualResponse = result.getOrThrow()
        assertEquals(response, actualResponse)
    }
}
