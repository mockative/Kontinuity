package com.app.sample

import io.mockative.kontinuity.Kontinuity
import io.mockative.kontinuity.KontinuityScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
@KontinuityScope
internal val authenticationServiceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

/**
 * The authentication service
 */
@Kontinuity
interface AuthenticationService : RefreshableService {

    var unrelatedProperty: String

    val isLoggingIn: Flow<Boolean>

    val intFlow: Flow<Int>
    val doubleFlow: Flow<Double>

    val stateFlow: StateFlow<String?>

    // KMRoutines
    // Kallback
    // Kontinuity
    //

    @Throws(Throwable::class, Exception::class)
    fun isBiometricAuthAvailable(): Boolean

    suspend fun login(request: AuthenticationRequest?): AuthenticationResponse

    suspend fun foo(args: List<String>)

    /**
     * Doc string
     * @param request Param String
     * @return All the flows of type [AuthenticationResponse]
     */
    fun getFlows(request: AuthenticationRequest): Flow<List<AuthenticationResponse>>

    suspend fun getFlowsAsync(request: AuthenticationRequest): Flow<List<AuthenticationResponse>>

}