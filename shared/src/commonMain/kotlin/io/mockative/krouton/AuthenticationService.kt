package io.mockative.krouton

import kotlinx.coroutines.flow.Flow
import io.mockative.krouton.Krouton

@Krouton
interface AuthenticationService {

    var isLoggingIn: Flow<Boolean>

}