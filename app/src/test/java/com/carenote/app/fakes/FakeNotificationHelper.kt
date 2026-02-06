package com.carenote.app.fakes

/**
 * NotificationHelper の Fake 実装
 *
 * テストで通知表示の呼び出しを追跡するために使用。
 * 実際の通知は表示せず、呼び出し履歴のみ記録する。
 */
class FakeNotificationHelper {

    /** showMedicationReminder の呼び出し回数 */
    var showMedicationReminderCallCount = 0
        private set

    /** 最後に呼び出された medicationId */
    var lastMedicationId: Long? = null
        private set

    /** 最後に呼び出された medicationName */
    var lastMedicationName: String? = null
        private set

    /** 全呼び出し履歴 */
    private val _reminderHistory = mutableListOf<ReminderCall>()
    val reminderHistory: List<ReminderCall> get() = _reminderHistory.toList()

    /** showTaskReminder の呼び出し回数 */
    var showTaskReminderCallCount = 0
        private set

    /** 最後に呼び出された taskId */
    var lastTaskId: Long? = null
        private set

    /** 最後に呼び出された taskTitle */
    var lastTaskTitle: String? = null
        private set

    /** タスクリマインダー全呼び出し履歴 */
    private val _taskReminderHistory = mutableListOf<TaskReminderCall>()
    val taskReminderHistory: List<TaskReminderCall> get() = _taskReminderHistory.toList()

    /**
     * 服薬リマインダー通知を「表示」（実際には呼び出しを記録）
     */
    fun showMedicationReminder(medicationId: Long, medicationName: String) {
        showMedicationReminderCallCount++
        lastMedicationId = medicationId
        lastMedicationName = medicationName
        _reminderHistory.add(ReminderCall(medicationId, medicationName))
    }

    /**
     * タスクリマインダー通知を「表示」（実際には呼び出しを記録）
     */
    fun showTaskReminder(taskId: Long, taskTitle: String) {
        showTaskReminderCallCount++
        lastTaskId = taskId
        lastTaskTitle = taskTitle
        _taskReminderHistory.add(TaskReminderCall(taskId, taskTitle))
    }

    /**
     * 状態をリセット
     */
    fun clear() {
        showMedicationReminderCallCount = 0
        lastMedicationId = null
        lastMedicationName = null
        _reminderHistory.clear()
        showTaskReminderCallCount = 0
        lastTaskId = null
        lastTaskTitle = null
        _taskReminderHistory.clear()
    }

    /**
     * 服薬リマインダー呼び出しの記録
     */
    data class ReminderCall(
        val medicationId: Long,
        val medicationName: String
    )

    /**
     * タスクリマインダー呼び出しの記録
     */
    data class TaskReminderCall(
        val taskId: Long,
        val taskTitle: String
    )
}
