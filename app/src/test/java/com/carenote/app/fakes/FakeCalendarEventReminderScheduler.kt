package com.carenote.app.fakes

import com.carenote.app.domain.repository.CalendarEventReminderSchedulerInterface
import java.time.LocalTime

class FakeCalendarEventReminderScheduler : CalendarEventReminderSchedulerInterface {

    data class ScheduleReminderCall(
        val eventId: Long,
        val eventTitle: String,
        val time: LocalTime
    )

    data class CancelReminderCall(val eventId: Long)

    data class ScheduleFollowUpCall(
        val eventId: Long,
        val eventTitle: String,
        val attemptNumber: Int
    )

    data class CancelFollowUpCall(val eventId: Long)

    val scheduleReminderCalls = mutableListOf<ScheduleReminderCall>()
    val cancelReminderCalls = mutableListOf<CancelReminderCall>()
    var cancelAllRemindersCalled = false
    val scheduleFollowUpCalls = mutableListOf<ScheduleFollowUpCall>()
    val cancelFollowUpCalls = mutableListOf<CancelFollowUpCall>()

    fun clear() {
        scheduleReminderCalls.clear()
        cancelReminderCalls.clear()
        cancelAllRemindersCalled = false
        scheduleFollowUpCalls.clear()
        cancelFollowUpCalls.clear()
    }

    override fun scheduleReminder(eventId: Long, eventTitle: String, time: LocalTime) {
        scheduleReminderCalls.add(ScheduleReminderCall(eventId, eventTitle, time))
    }

    override fun cancelReminder(eventId: Long) {
        cancelReminderCalls.add(CancelReminderCall(eventId))
    }

    override fun cancelAllReminders() {
        cancelAllRemindersCalled = true
    }

    override fun scheduleFollowUp(eventId: Long, eventTitle: String, attemptNumber: Int) {
        scheduleFollowUpCalls.add(ScheduleFollowUpCall(eventId, eventTitle, attemptNumber))
    }

    override fun cancelFollowUp(eventId: Long) {
        cancelFollowUpCalls.add(CancelFollowUpCall(eventId))
    }
}
