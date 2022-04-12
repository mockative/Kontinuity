package io.mockative.krouton.sample

import io.mockative.krouton.Kontinuity
import kotlinx.coroutines.flow.Flow

@Kontinuity
interface AuthenticationService {

    var isLoggingIn: Flow<Boolean?>

    suspend fun login(request: AuthenticationRequest?): AuthenticationResponse

    suspend fun foo(args: List<String?>)

    /**
     * Doc string
     * @param request Param String
     */
    fun getFlows(request: AuthenticationRequest): Flow<List<AuthenticationResponse?>>

}