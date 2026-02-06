package com.carenote.app.data.worker

import com.carenote.app.domain.model.MedicationTiming
import java.time.LocalTime

/**
 * 服薬リマインダーのスケジューリング機能を定義するインターフェース
 *
 * テスト時に Fake 実装を注入できるようにするために導入。
 */
interface MedicationReminderSchedulerInterface {

    fun scheduleReminder(
        medicationId: Long,
        medicationName: String,
        timing: MedicationTiming,
        time: LocalTime
    )

    fun scheduleAllReminders(
        medicationId: Long,
        medicationName: String,
        times: Map<MedicationTiming, LocalTime>
    )

    fun cancelReminders(medicationId: Long)

    fun cancelAllReminders()

    fun scheduleFollowUp(
        medicationId: Long,
        medicationName: String,
        timing: MedicationTiming?,
        attemptNumber: Int
    )

    fun cancelFollowUp(medicationId: Long, timing: MedicationTiming? = null)
}
