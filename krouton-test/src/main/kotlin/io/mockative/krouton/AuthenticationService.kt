package io.mockative.krouton

import kotlinx.coroutines.flow.Flow

@Krouton
interface AuthenticationService {

    var isLoggingIn: Flow<Boolean>

    suspend fun login(request: AuthenticationRequest?): AuthenticationResponse

    fun getFlows(request: AuthenticationRequest): Flow<AuthenticationResponse?>

}