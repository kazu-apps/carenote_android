package com.carenote.app.data.worker

import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.fakes.FakeNotificationHelper
import com.carenote.app.fakes.FakeSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * TaskReminderWorker のユニットテスト
 *
 * WorkManager Worker のテストは複雑なため、ここでは主に:
 * 1. FakeNotificationHelper のタスクリマインダー動作確認
 * 2. FakeSettingsRepository との連携確認
 * 3. おやすみ時間ロジックの検証
 * 4. InputData キー定数の確認
 *
 * を行う。実際の WorkManager 統合テストは androidTest で行う。
 */
class TaskReminderWorkerTest {

    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var fakeNotificationHelper: FakeNotificationHelper

    @Before
    fun setUp() {
        fakeSettingsRepository = FakeSettingsRepository()
        fakeNotificationHelper = FakeNotificationHelper()
    }

    // ========== FakeNotificationHelper Task Reminder Tests ==========

    @Test
    fun `FakeNotificationHelper task reminder initial state is correct`() {
        assertEquals(0, fakeNotificationHelper.showTaskReminderCallCount)
        assertNull(fakeNotificationHelper.lastTaskId)
        assertNull(fakeNotificationHelper.lastTaskTitle)
        assertTrue(fakeNotificationHelper.taskReminderHistory.isEmpty())
    }

    @Test
    fun `FakeNotificationHelper tracks showTaskReminder calls`() {
        fakeNotificationHelper.showTaskReminder(1L, "買い物に行く")

        assertEquals(1, fakeNotificationHelper.showTaskReminderCallCount)
        assertEquals(1L, fakeNotificationHelper.lastTaskId)
        assertEquals("買い物に行く", fakeNotificationHelper.lastTaskTitle)
    }

    @Test
    fun `FakeNotificationHelper tracks multiple task reminder calls`() {
        fakeNotificationHelper.showTaskReminder(1L, "タスクA")
        fakeNotificationHelper.showTaskReminder(2L, "タスクB")
        fakeNotificationHelper.showTaskReminder(3L, "タスクC")

        assertEquals(3, fakeNotificationHelper.showTaskReminderCallCount)
        assertEquals(3L, fakeNotificationHelper.lastTaskId)
        assertEquals("タスクC", fakeNotificationHelper.lastTaskTitle)
        assertEquals(3, fakeNotificationHelper.taskReminderHistory.size)
    }

    @Test
    fun `FakeNotificationHelper taskReminderHistory contains all calls`() {
        fakeNotificationHelper.showTaskReminder(10L, "タスクX")
        fakeNotificationHelper.showTaskReminder(20L, "タスクY")

        val history = fakeNotificationHelper.taskReminderHistory
        assertEquals(2, history.size)
        assertEquals(10L, history[0].taskId)
        assertEquals("タスクX", history[0].taskTitle)
        assertEquals(20L, history[1].taskId)
        assertEquals("タスクY", history[1].taskTitle)
    }

    @Test
    fun `FakeNotificationHelper clear resets task reminder state`() {
        fakeNotificationHelper.showTaskReminder(1L, "Test")
        fakeNotificationHelper.showTaskReminder(2L, "Test2")

        fakeNotificationHelper.clear()

        assertEquals(0, fakeNotificationHelper.showTaskReminderCallCount)
        assertNull(fakeNotificationHelper.lastTaskId)
        assertNull(fakeNotificationHelper.lastTaskTitle)
        assertTrue(fakeNotificationHelper.taskReminderHistory.isEmpty())
    }

    @Test
    fun `FakeNotificationHelper tracks medication and task reminders independently`() {
        fakeNotificationHelper.showMedicationReminder(1L, "薬A")
        fakeNotificationHelper.showTaskReminder(2L, "タスクA")

        assertEquals(1, fakeNotificationHelper.showMedicationReminderCallCount)
        assertEquals(1, fakeNotificationHelper.showTaskReminderCallCount)
        assertEquals(1L, fakeNotificationHelper.lastMedicationId)
        assertEquals(2L, fakeNotificationHelper.lastTaskId)
    }

    // ========== Settings Integration Tests ==========

    @Test
    fun `FakeSettingsRepository returns default settings with notifications enabled`() = runTest {
        val settings = fakeSettingsRepository.getSettings().first()

        assertTrue(settings.notificationsEnabled)
    }

    @Test
    fun `FakeSettingsRepository can disable notifications`() = runTest {
        fakeSettingsRepository.setSettings(UserSettings(notificationsEnabled = false))

        val settings = fakeSettingsRepository.getSettings().first()

        assertFalse(settings.notificationsEnabled)
    }

    @Test
    fun `FakeSettingsRepository can set quiet hours`() = runTest {
        fakeSettingsRepository.setSettings(
            UserSettings(
                quietHoursStart = 22,
                quietHoursEnd = 7
            )
        )

        val settings = fakeSettingsRepository.getSettings().first()

        assertEquals(22, settings.quietHoursStart)
        assertEquals(7, settings.quietHoursEnd)
    }

    // ========== Quiet Hours Logic Tests ==========

    @Test
    fun `isQuietHours returns true during quiet hours (same day)`() {
        val start = 10
        val end = 18
        val currentHour = 12

        val isQuiet = isQuietHoursLogic(currentHour, start, end)

        assertTrue(isQuiet)
    }

    @Test
    fun `isQuietHours returns false outside quiet hours (same day)`() {
        val start = 10
        val end = 18
        val currentHour = 8

        val isQuiet = isQuietHoursLogic(currentHour, start, end)

        assertFalse(isQuiet)
    }

    @Test
    fun `isQuietHours returns true during quiet hours (overnight)`() {
        val start = 22
        val end = 7

        assertTrue(isQuietHoursLogic(23, start, end))
        assertTrue(isQuietHoursLogic(3, start, end))
        assertTrue(isQuietHoursLogic(0, start, end))
    }

    @Test
    fun `isQuietHours returns false outside quiet hours (overnight)`() {
        val start = 22
        val end = 7

        assertFalse(isQuietHoursLogic(10, start, end))
        assertFalse(isQuietHoursLogic(15, start, end))
    }

    @Test
    fun `isQuietHours boundary case - start hour`() {
        val start = 22
        val end = 7

        assertTrue(isQuietHoursLogic(22, start, end))
    }

    @Test
    fun `isQuietHours boundary case - end hour`() {
        val start = 22
        val end = 7

        assertFalse(isQuietHoursLogic(7, start, end))
    }

    /**
     * TaskReminderWorker.isQuietHours() と同じロジック
     * Worker のプライベートメソッドをテストするための再実装
     */
    private fun isQuietHoursLogic(currentHour: Int, start: Int, end: Int): Boolean {
        return if (start < end) {
            currentHour in start until end
        } else {
            currentHour >= start || currentHour < end
        }
    }

    // ========== InputData Key Constants Tests ==========

    @Test
    fun `KEY_TASK_ID constant is correct`() {
        assertEquals("task_id", TaskReminderWorker.KEY_TASK_ID)
    }

    @Test
    fun `KEY_TASK_TITLE constant is correct`() {
        assertEquals("task_title", TaskReminderWorker.KEY_TASK_TITLE)
    }

    @Test
    fun `KEY_FOLLOW_UP_ATTEMPT constant is correct`() {
        assertEquals("follow_up_attempt", TaskReminderWorker.KEY_FOLLOW_UP_ATTEMPT)
    }

    // ========== Config Constants Tests ==========

    @Test
    fun `CHANNEL_ID_TASK_REMINDER config is accessible`() {
        assertEquals("task_reminder", AppConfig.Notification.CHANNEL_ID_TASK_REMINDER)
    }

    @Test
    fun `NOTIFICATION_ID_TASK_BASE config is accessible`() {
        assertEquals(3000, AppConfig.Notification.NOTIFICATION_ID_TASK_BASE)
    }

    @Test
    fun `TASK_REMINDER_WORK_TAG config is accessible`() {
        assertEquals("task_reminder_work", AppConfig.Notification.TASK_REMINDER_WORK_TAG)
    }

    @Test
    fun `TASK_FOLLOW_UP_WORK_TAG config is accessible`() {
        assertEquals("task_follow_up_work", AppConfig.Notification.TASK_FOLLOW_UP_WORK_TAG)
    }

    // ========== Notification ID Calculation Tests ==========

    @Test
    fun `Task notification ID calculation is correct`() {
        val baseId = AppConfig.Notification.NOTIFICATION_ID_TASK_BASE
        val taskId = 5L

        val notificationId = baseId + taskId.toInt()

        assertEquals(3005, notificationId)
    }

    @Test
    fun `Task notification IDs are unique for different tasks`() {
        val baseId = AppConfig.Notification.NOTIFICATION_ID_TASK_BASE

        val id1 = baseId + 1
        val id2 = baseId + 2
        val id3 = baseId + 100

        assertTrue(id1 != id2)
        assertTrue(id2 != id3)
        assertTrue(id1 != id3)
    }

    @Test
    fun `Task notification IDs do not overlap with medication notification IDs`() {
        val medicationBase = AppConfig.Notification.NOTIFICATION_ID_MEDICATION_BASE
        val taskBase = AppConfig.Notification.NOTIFICATION_ID_TASK_BASE

        assertTrue(taskBase > medicationBase)
        assertTrue(taskBase - medicationBase >= 1000)
    }

    // ========== Premium Feature Guard Tests ==========

    @Test
    fun `TASK_REMINDER_FREE_DAILY_LIMIT config is 3`() {
        assertEquals(3, AppConfig.Notification.TASK_REMINDER_FREE_DAILY_LIMIT)
    }

    @Test
    fun `FakePremiumFeatureGuard initial canSend is true`() = runTest {
        val guard = com.carenote.app.fakes.FakePremiumFeatureGuard()
        assertTrue(guard.canSendTaskReminder())
    }

    @Test
    fun `FakePremiumFeatureGuard blocks when canSend is false`() = runTest {
        val guard = com.carenote.app.fakes.FakePremiumFeatureGuard()
        guard.canSend = false
        assertFalse(guard.canSendTaskReminder())
    }

    @Test
    fun `FakePremiumFeatureGuard tracks recordTaskReminderSent calls`() = runTest {
        val guard = com.carenote.app.fakes.FakePremiumFeatureGuard()
        guard.recordTaskReminderSent()
        guard.recordTaskReminderSent()
        assertEquals(2, guard.recordedCount)
        assertEquals(2, guard.todayCount)
    }

    @Test
    fun `FakePremiumFeatureGuard clear resets state`() = runTest {
        val guard = com.carenote.app.fakes.FakePremiumFeatureGuard()
        guard.canSend = false
        guard.recordTaskReminderSent()
        guard.clear()
        assertTrue(guard.canSendTaskReminder())
        assertEquals(0, guard.recordedCount)
        assertEquals(0, guard.todayCount)
    }

    @Test
    fun `FakePremiumFeatureGuard daily limit matches config`() {
        val guard = com.carenote.app.fakes.FakePremiumFeatureGuard()
        assertEquals(
            AppConfig.Notification.TASK_REMINDER_FREE_DAILY_LIMIT,
            guard.getTaskReminderDailyLimit()
        )
    }
}
