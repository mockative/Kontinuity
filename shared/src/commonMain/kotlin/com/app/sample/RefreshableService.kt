package com.app.sample

interface RefreshableService {
    suspend fun refresh(force: Boolean = false)
}