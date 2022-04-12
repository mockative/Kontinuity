package io.mockative.krouton

import kotlinx.coroutines.flow.Flow
import io.mockative.krouton.Krouton

/**
 * The authentication service
 */
@Krouton
interface AuthenticationService : RefreshableService {

    var unrelatedProperty: String

    var isLoggingIn: Flow<Boolean>

    var intFlow: Flow<Int>
    var doubleFlow: Flow<Double>

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