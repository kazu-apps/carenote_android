package com.carenote.app.fakes

import com.carenote.app.data.worker.TaskReminderSchedulerInterface
import java.time.LocalTime

class FakeTaskReminderScheduler : TaskReminderSchedulerInterface {

    data class ScheduleReminderCall(
        val taskId: Long,
        val taskTitle: String,
        val time: LocalTime
    )

    data class CancelReminderCall(val taskId: Long)

    data class ScheduleFollowUpCall(
        val taskId: Long,
        val taskTitle: String,
        val attemptNumber: Int
    )

    data class CancelFollowUpCall(val taskId: Long)

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

    override fun scheduleReminder(
        taskId: Long,
        taskTitle: String,
        time: LocalTime
    ) {
        scheduleReminderCalls.add(
            ScheduleReminderCall(taskId, taskTitle, time)
        )
    }

    override fun cancelReminder(taskId: Long) {
        cancelReminderCalls.add(CancelReminderCall(taskId))
    }

    override fun cancelAllReminders() {
        cancelAllRemindersCalled = true
    }

    override fun scheduleFollowUp(
        taskId: Long,
        taskTitle: String,
        attemptNumber: Int
    ) {
        scheduleFollowUpCalls.add(
            ScheduleFollowUpCall(taskId, taskTitle, attemptNumber)
        )
    }

    override fun cancelFollowUp(taskId: Long) {
        cancelFollowUpCalls.add(CancelFollowUpCall(taskId))
    }
}
