package com.carenote.app.domain.repository

import java.time.LocalTime

/**
 * カレンダーイベントリマインダーのスケジューリング機能を定義するインターフェース
 *
 * テスト時に Fake 実装を注入できるようにするために導入。
 */
interface CalendarEventReminderSchedulerInterface {

    fun scheduleReminder(
        eventId: Long,
        eventTitle: String,
        time: LocalTime
    )

    fun cancelReminder(eventId: Long)

    fun cancelAllReminders()

    fun scheduleFollowUp(
        eventId: Long,
        eventTitle: String,
        attemptNumber: Int
    )

    fun cancelFollowUp(eventId: Long)
}
