package io.mockative.krouton.sample

import io.mockative.krouton.Krouton
import kotlinx.coroutines.flow.Flow

@Krouton
interface AuthenticationService {

    var isLoggingIn: Flow<Boolean>

    suspend fun login(request: AuthenticationRequest?): AuthenticationResponse

    /**
     * Doc string
     * @param request Param String
     */
    fun getFlows(request: AuthenticationRequest): Flow<AuthenticationResponse?>

}