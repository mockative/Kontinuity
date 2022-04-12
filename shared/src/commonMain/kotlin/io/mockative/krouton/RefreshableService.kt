package io.mockative.krouton

interface RefreshableService {
    suspend fun refresh(force: Boolean = false)
}