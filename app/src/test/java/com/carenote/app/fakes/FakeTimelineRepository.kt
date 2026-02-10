package com.carenote.app.fakes

import com.carenote.app.domain.model.TimelineItem
import com.carenote.app.domain.repository.TimelineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FakeTimelineRepository : TimelineRepository {

    private val items = MutableStateFlow<List<TimelineItem>>(emptyList())
    var shouldFail = false

    fun setItems(list: List<TimelineItem>) {
        items.value = list
    }

    fun clear() {
        items.value = emptyList()
        shouldFail = false
    }

    override fun getTimelineItemsForDate(date: LocalDate): Flow<List<TimelineItem>> {
        if (shouldFail) {
            return kotlinx.coroutines.flow.flow {
                throw RuntimeException("Fake timeline error")
            }
        }
        return items.map { list ->
            list.filter { it.timestamp.toLocalDate() == date }
                .sortedBy { it.timestamp }
        }
    }
}
