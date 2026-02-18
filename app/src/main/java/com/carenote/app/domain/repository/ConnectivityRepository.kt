package com.carenote.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface ConnectivityRepository {
    val isOnline: Flow<Boolean>
}
