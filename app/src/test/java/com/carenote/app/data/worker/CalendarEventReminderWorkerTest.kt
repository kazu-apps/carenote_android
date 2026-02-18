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
 * CalendarEventReminderWorker のユニットテスト
 *
 * WorkManager Worker のテストは複雑なため、ここでは主に:
 * 1. FakeNotificationHelper のカレンダーイベントリマインダー動作確認
 * 2. FakeSettingsRepository との連携確認
 * 3. おやすみ時間ロジックの検証
 * 4. InputData キー定数の確認
 * 5. Config 定数の確認
 * 6. 通知 ID の重複チェック
 *
 * を行う。実際の WorkManager 統合テストは androidTest で行う。
 */
class CalendarEventReminderWorkerTest {

    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var fakeNotificationHelper: FakeNotificationHelper

    @Before
    fun setUp() {
        fakeSettingsRepository = FakeSettingsRepository()
        fakeNotificationHelper = FakeNotificationHelper()
    }

    // ========== FakeNotificationHelper Calendar Event Reminder Tests ==========

    @Test
    fun `FakeNotificationHelper calendar event reminder initial state is correct`() {
        assertEquals(0, fakeNotificationHelper.showCalendarEventReminderCallCount)
        assertNull(fakeNotificationHelper.lastCalendarEventId)
        assertNull(fakeNotificationHelper.lastCalendarEventTitle)
        assertTrue(fakeNotificationHelper.calendarEventReminderHistory.isEmpty())
    }

    @Test
    fun `FakeNotificationHelper tracks showCalendarEventReminder calls`() {
        fakeNotificationHelper.showCalendarEventReminder(1L, "歯医者の予約")

        assertEquals(1, fakeNotificationHelper.showCalendarEventReminderCallCount)
        assertEquals(1L, fakeNotificationHelper.lastCalendarEventId)
        assertEquals("歯医者の予約", fakeNotificationHelper.lastCalendarEventTitle)
    }

    @Test
    fun `FakeNotificationHelper tracks multiple calendar event reminder calls`() {
        fakeNotificationHelper.showCalendarEventReminder(1L, "イベントA")
        fakeNotificationHelper.showCalendarEventReminder(2L, "イベントB")
        fakeNotificationHelper.showCalendarEventReminder(3L, "イベントC")

        assertEquals(3, fakeNotificationHelper.showCalendarEventReminderCallCount)
        assertEquals(3L, fakeNotificationHelper.lastCalendarEventId)
        assertEquals("イベントC", fakeNotificationHelper.lastCalendarEventTitle)
        assertEquals(3, fakeNotificationHelper.calendarEventReminderHistory.size)
    }

    @Test
    fun `FakeNotificationHelper calendarEventReminderHistory contains all calls`() {
        fakeNotificationHelper.showCalendarEventReminder(10L, "イベントX")
        fakeNotificationHelper.showCalendarEventReminder(20L, "イベントY")

        val history = fakeNotificationHelper.calendarEventReminderHistory
        assertEquals(2, history.size)
        assertEquals(10L, history[0].eventId)
        assertEquals("イベントX", history[0].eventTitle)
        assertEquals(20L, history[1].eventId)
        assertEquals("イベントY", history[1].eventTitle)
    }

    @Test
    fun `FakeNotificationHelper clear resets calendar event reminder state`() {
        fakeNotificationHelper.showCalendarEventReminder(1L, "Test")
        fakeNotificationHelper.showCalendarEventReminder(2L, "Test2")

        fakeNotificationHelper.clear()

        assertEquals(0, fakeNotificationHelper.showCalendarEventReminderCallCount)
        assertNull(fakeNotificationHelper.lastCalendarEventId)
        assertNull(fakeNotificationHelper.lastCalendarEventTitle)
        assertTrue(fakeNotificationHelper.calendarEventReminderHistory.isEmpty())
    }

    @Test
    fun `FakeNotificationHelper tracks medication task and calendar reminders independently`() {
        fakeNotificationHelper.showMedicationReminder(1L, "薬A")
        fakeNotificationHelper.showTaskReminder(2L, "タスクA")
        fakeNotificationHelper.showCalendarEventReminder(3L, "イベントA")

        assertEquals(1, fakeNotificationHelper.showMedicationReminderCallCount)
        assertEquals(1, fakeNotificationHelper.showTaskReminderCallCount)
        assertEquals(1, fakeNotificationHelper.showCalendarEventReminderCallCount)
        assertEquals(1L, fakeNotificationHelper.lastMedicationId)
        assertEquals(2L, fakeNotificationHelper.lastTaskId)
        assertEquals(3L, fakeNotificationHelper.lastCalendarEventId)
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
     * CalendarEventReminderWorker.isQuietHours() と同じロジック
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
    fun `KEY_EVENT_ID constant is correct`() {
        assertEquals("event_id", CalendarEventReminderWorker.KEY_EVENT_ID)
    }

    @Test
    fun `KEY_EVENT_TITLE constant is correct`() {
        assertEquals("event_title", CalendarEventReminderWorker.KEY_EVENT_TITLE)
    }

    @Test
    fun `KEY_FOLLOW_UP_ATTEMPT constant is correct`() {
        assertEquals("follow_up_attempt", CalendarEventReminderWorker.KEY_FOLLOW_UP_ATTEMPT)
    }

    // ========== Config Constants Tests ==========

    @Test
    fun `CHANNEL_ID_CALENDAR_REMINDER config is accessible`() {
        assertEquals("calendar_reminder", AppConfig.Notification.CHANNEL_ID_CALENDAR_REMINDER)
    }

    @Test
    fun `NOTIFICATION_ID_CALENDAR_BASE config is accessible`() {
        assertEquals(4000, AppConfig.Notification.NOTIFICATION_ID_CALENDAR_BASE)
    }

    @Test
    fun `CALENDAR_REMINDER_WORK_TAG config is accessible`() {
        assertEquals("calendar_reminder_work", AppConfig.Notification.CALENDAR_REMINDER_WORK_TAG)
    }

    @Test
    fun `CALENDAR_FOLLOW_UP_WORK_TAG config is accessible`() {
        assertEquals("calendar_follow_up_work", AppConfig.Notification.CALENDAR_FOLLOW_UP_WORK_TAG)
    }

    // ========== Notification ID Calculation Tests ==========

    @Test
    fun `Calendar event notification ID calculation is correct`() {
        val baseId = AppConfig.Notification.NOTIFICATION_ID_CALENDAR_BASE
        val eventId = 5L

        val notificationId = baseId + eventId.toInt()

        assertEquals(4005, notificationId)
    }

    @Test
    fun `Calendar event notification IDs are unique for different events`() {
        val baseId = AppConfig.Notification.NOTIFICATION_ID_CALENDAR_BASE

        val id1 = baseId + 1
        val id2 = baseId + 2
        val id3 = baseId + 100

        assertTrue(id1 != id2)
        assertTrue(id2 != id3)
        assertTrue(id1 != id3)
    }

    @Test
    fun `Calendar event notification IDs do not overlap with medication notification IDs`() {
        val medicationBase = AppConfig.Notification.NOTIFICATION_ID_MEDICATION_BASE
        val calendarBase = AppConfig.Notification.NOTIFICATION_ID_CALENDAR_BASE

        assertTrue(calendarBase > medicationBase)
        assertTrue(calendarBase - medicationBase >= 1000)
    }

    @Test
    fun `Calendar event notification IDs do not overlap with sync notification IDs`() {
        val syncId = AppConfig.Notification.NOTIFICATION_ID_SYNC
        val calendarBase = AppConfig.Notification.NOTIFICATION_ID_CALENDAR_BASE

        assertTrue(calendarBase > syncId)
        assertTrue(calendarBase - syncId >= 1000)
    }

    @Test
    fun `Calendar event notification IDs do not overlap with task notification IDs`() {
        val taskBase = AppConfig.Notification.NOTIFICATION_ID_TASK_BASE
        val calendarBase = AppConfig.Notification.NOTIFICATION_ID_CALENDAR_BASE

        assertTrue(calendarBase > taskBase)
        assertTrue(calendarBase - taskBase >= 1000)
    }
}
