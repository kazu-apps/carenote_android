package com.carenote.app.domain.repository

import com.carenote.app.domain.model.TimelineItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TimelineRepository {

    fun getTimelineItemsForDate(date: LocalDate): Flow<List<TimelineItem>>
}
