package com.carenote.app.fakes

import com.carenote.app.domain.repository.MedicationReminderSchedulerInterface
import com.carenote.app.domain.model.MedicationTiming
import java.time.LocalTime

class FakeMedicationReminderScheduler : MedicationReminderSchedulerInterface {

    data class ScheduleReminderCall(
        val medicationId: Long,
        val medicationName: String,
        val timing: MedicationTiming,
        val time: LocalTime
    )

    data class ScheduleAllCall(
        val medicationId: Long,
        val medicationName: String,
        val times: Map<MedicationTiming, LocalTime>
    )

    data class CancelRemindersCall(val medicationId: Long)

    data class ScheduleFollowUpCall(
        val medicationId: Long,
        val medicationName: String,
        val timing: MedicationTiming?,
        val attemptNumber: Int
    )

    data class CancelFollowUpCall(
        val medicationId: Long,
        val timing: MedicationTiming?
    )

    val scheduleReminderCalls = mutableListOf<ScheduleReminderCall>()
    val scheduleAllCalls = mutableListOf<ScheduleAllCall>()
    val cancelRemindersCalls = mutableListOf<CancelRemindersCall>()
    var cancelAllRemindersCalled = false
    val scheduleFollowUpCalls = mutableListOf<ScheduleFollowUpCall>()
    val cancelFollowUpCalls = mutableListOf<CancelFollowUpCall>()

    fun clear() {
        scheduleReminderCalls.clear()
        scheduleAllCalls.clear()
        cancelRemindersCalls.clear()
        cancelAllRemindersCalled = false
        scheduleFollowUpCalls.clear()
        cancelFollowUpCalls.clear()
    }

    override fun scheduleReminder(
        medicationId: Long,
        medicationName: String,
        timing: MedicationTiming,
        time: LocalTime
    ) {
        scheduleReminderCalls.add(
            ScheduleReminderCall(medicationId, medicationName, timing, time)
        )
    }

    override fun scheduleAllReminders(
        medicationId: Long,
        medicationName: String,
        times: Map<MedicationTiming, LocalTime>
    ) {
        scheduleAllCalls.add(ScheduleAllCall(medicationId, medicationName, times))
    }

    override fun cancelReminders(medicationId: Long) {
        cancelRemindersCalls.add(CancelRemindersCall(medicationId))
    }

    override fun cancelAllReminders() {
        cancelAllRemindersCalled = true
    }

    override fun scheduleFollowUp(
        medicationId: Long,
        medicationName: String,
        timing: MedicationTiming?,
        attemptNumber: Int
    ) {
        scheduleFollowUpCalls.add(
            ScheduleFollowUpCall(medicationId, medicationName, timing, attemptNumber)
        )
    }

    override fun cancelFollowUp(medicationId: Long, timing: MedicationTiming?) {
        cancelFollowUpCalls.add(CancelFollowUpCall(medicationId, timing))
    }
}
