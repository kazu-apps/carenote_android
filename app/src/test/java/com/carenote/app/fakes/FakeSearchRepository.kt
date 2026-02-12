package com.carenote.app.fakes

import com.carenote.app.domain.model.SearchResult
import com.carenote.app.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class FakeSearchRepository : SearchRepository {

    private val results = MutableStateFlow<List<SearchResult>>(emptyList())
    var shouldFail = false

    fun setResults(list: List<SearchResult>) {
        results.value = list
    }

    fun clear() {
        results.value = emptyList()
        shouldFail = false
    }

    override fun searchAll(query: String): Flow<List<SearchResult>> {
        if (shouldFail) {
            return flow { throw RuntimeException("Fake search error") }
        }
        return results
    }
}
