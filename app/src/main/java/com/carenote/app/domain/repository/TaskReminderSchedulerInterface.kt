package com.carenote.app.domain.repository

import java.time.LocalTime

/**
 * タスクリマインダーのスケジューリング機能を定義するインターフェース
 *
 * テスト時に Fake 実装を注入できるようにするために導入。
 */
interface TaskReminderSchedulerInterface {

    fun scheduleReminder(
        taskId: Long,
        taskTitle: String,
        time: LocalTime
    )

    fun cancelReminder(taskId: Long)

    fun cancelAllReminders()

    fun scheduleFollowUp(
        taskId: Long,
        taskTitle: String,
        attemptNumber: Int
    )

    fun cancelFollowUp(taskId: Long)
}
