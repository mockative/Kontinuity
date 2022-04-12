package io.mockative.krouton

import kotlinx.coroutines.flow.Flow
import io.mockative.krouton.Krouton
import kotlinx.coroutines.flow.StateFlow

/**
 * The authentication service
 */
@Krouton
interface AuthenticationService : RefreshableService {

    var unrelatedProperty: String

    val isLoggingIn: Flow<Boolean>

    val intFlow: Flow<Int>
    val doubleFlow: Flow<Double>

    val stateFlow: StateFlow<String?>

    // KMRoutines
    // Kallback
    //

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