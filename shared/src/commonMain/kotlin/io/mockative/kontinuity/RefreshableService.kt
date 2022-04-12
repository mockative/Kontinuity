package io.mockative.kontinuity

interface RefreshableService {
    suspend fun refresh(force: Boolean = false)
}