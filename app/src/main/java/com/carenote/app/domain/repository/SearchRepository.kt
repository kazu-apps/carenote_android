package com.carenote.app.domain.repository

import com.carenote.app.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun searchAll(query: String): Flow<List<SearchResult>>
}
