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
 * MedicationReminderWorker のユニットテスト
 *
 * WorkManager Worker のテストは複雑なため、ここでは主に:
 * 1. FakeNotificationHelper の動作確認
 * 2. FakeSettingsRepository との連携確認
 * 3. おやすみ時間ロジックの検証
 * 4. InputData キー定数の確認
 *
 * を行う。実際の WorkManager 統合テストは androidTest で行う。
 */
class MedicationReminderWorkerTest {

    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var fakeNotificationHelper: FakeNotificationHelper

    @Before
    fun setUp() {
        fakeSettingsRepository = FakeSettingsRepository()
        fakeNotificationHelper = FakeNotificationHelper()
    }

    // ========== FakeNotificationHelper Tests ==========

    @Test
    fun `FakeNotificationHelper initial state is correct`() {
        assertEquals(0, fakeNotificationHelper.showMedicationReminderCallCount)
        assertNull(fakeNotificationHelper.lastMedicationId)
        assertNull(fakeNotificationHelper.lastMedicationName)
        assertTrue(fakeNotificationHelper.reminderHistory.isEmpty())
    }

    @Test
    fun `FakeNotificationHelper tracks showMedicationReminder calls`() {
        fakeNotificationHelper.showMedicationReminder(1L, "ロキソニン")

        assertEquals(1, fakeNotificationHelper.showMedicationReminderCallCount)
        assertEquals(1L, fakeNotificationHelper.lastMedicationId)
        assertEquals("ロキソニン", fakeNotificationHelper.lastMedicationName)
    }

    @Test
    fun `FakeNotificationHelper tracks multiple calls`() {
        fakeNotificationHelper.showMedicationReminder(1L, "薬A")
        fakeNotificationHelper.showMedicationReminder(2L, "薬B")
        fakeNotificationHelper.showMedicationReminder(3L, "薬C")

        assertEquals(3, fakeNotificationHelper.showMedicationReminderCallCount)
        assertEquals(3L, fakeNotificationHelper.lastMedicationId)
        assertEquals("薬C", fakeNotificationHelper.lastMedicationName)
        assertEquals(3, fakeNotificationHelper.reminderHistory.size)
    }

    @Test
    fun `FakeNotificationHelper reminderHistory contains all calls`() {
        fakeNotificationHelper.showMedicationReminder(10L, "薬X")
        fakeNotificationHelper.showMedicationReminder(20L, "薬Y")

        val history = fakeNotificationHelper.reminderHistory
        assertEquals(2, history.size)
        assertEquals(10L, history[0].medicationId)
        assertEquals("薬X", history[0].medicationName)
        assertEquals(20L, history[1].medicationId)
        assertEquals("薬Y", history[1].medicationName)
    }

    @Test
    fun `FakeNotificationHelper clear resets all state`() {
        fakeNotificationHelper.showMedicationReminder(1L, "Test")
        fakeNotificationHelper.showMedicationReminder(2L, "Test2")

        fakeNotificationHelper.clear()

        assertEquals(0, fakeNotificationHelper.showMedicationReminderCallCount)
        assertNull(fakeNotificationHelper.lastMedicationId)
        assertNull(fakeNotificationHelper.lastMedicationName)
        assertTrue(fakeNotificationHelper.reminderHistory.isEmpty())
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
        // 10:00 〜 18:00 のおやすみ時間
        val start = 10
        val end = 18

        // 12:00 はおやすみ時間内
        val currentHour = 12
        val isQuiet = isQuietHoursLogic(currentHour, start, end)

        assertTrue(isQuiet)
    }

    @Test
    fun `isQuietHours returns false outside quiet hours (same day)`() {
        // 10:00 〜 18:00 のおやすみ時間
        val start = 10
        val end = 18

        // 8:00 はおやすみ時間外
        val currentHour = 8
        val isQuiet = isQuietHoursLogic(currentHour, start, end)

        assertFalse(isQuiet)
    }

    @Test
    fun `isQuietHours returns true during quiet hours (overnight)`() {
        // 22:00 〜 7:00 のおやすみ時間（日をまたぐ）
        val start = 22
        val end = 7

        // 23:00 はおやすみ時間内
        val isQuiet23 = isQuietHoursLogic(23, start, end)
        assertTrue(isQuiet23)

        // 3:00 はおやすみ時間内
        val isQuiet3 = isQuietHoursLogic(3, start, end)
        assertTrue(isQuiet3)

        // 0:00 はおやすみ時間内
        val isQuiet0 = isQuietHoursLogic(0, start, end)
        assertTrue(isQuiet0)
    }

    @Test
    fun `isQuietHours returns false outside quiet hours (overnight)`() {
        // 22:00 〜 7:00 のおやすみ時間（日をまたぐ）
        val start = 22
        val end = 7

        // 10:00 はおやすみ時間外
        val isQuiet10 = isQuietHoursLogic(10, start, end)
        assertFalse(isQuiet10)

        // 15:00 はおやすみ時間外
        val isQuiet15 = isQuietHoursLogic(15, start, end)
        assertFalse(isQuiet15)
    }

    @Test
    fun `isQuietHours boundary case - start hour`() {
        // 22:00 〜 7:00
        val start = 22
        val end = 7

        // 22:00 ちょうどはおやすみ時間内
        val isQuiet = isQuietHoursLogic(22, start, end)
        assertTrue(isQuiet)
    }

    @Test
    fun `isQuietHours boundary case - end hour`() {
        // 22:00 〜 7:00
        val start = 22
        val end = 7

        // 7:00 ちょうどはおやすみ時間外
        val isQuiet = isQuietHoursLogic(7, start, end)
        assertFalse(isQuiet)
    }

    /**
     * MedicationReminderWorker.isQuietHours() と同じロジック
     * Worker のプライベートメソッドをテストするための再実装
     */
    private fun isQuietHoursLogic(currentHour: Int, start: Int, end: Int): Boolean {
        return if (start < end) {
            // 例: 10:00 〜 18:00
            currentHour in start until end
        } else {
            // 例: 22:00 〜 7:00（日をまたぐケース）
            currentHour >= start || currentHour < end
        }
    }

    // ========== InputData Key Constants Tests ==========

    @Test
    fun `KEY_MEDICATION_ID constant is correct`() {
        assertEquals("medication_id", MedicationReminderWorker.KEY_MEDICATION_ID)
    }

    @Test
    fun `KEY_MEDICATION_NAME constant is correct`() {
        assertEquals("medication_name", MedicationReminderWorker.KEY_MEDICATION_NAME)
    }

    // ========== Config Constants Tests ==========

    @Test
    fun `CHANNEL_ID_MEDICATION_REMINDER config is accessible`() {
        assertEquals("medication_reminder", AppConfig.Notification.CHANNEL_ID_MEDICATION_REMINDER)
    }

    @Test
    fun `NOTIFICATION_ID_MEDICATION_BASE config is accessible`() {
        assertEquals(1000, AppConfig.Notification.NOTIFICATION_ID_MEDICATION_BASE)
    }

    @Test
    fun `REMINDER_WORK_TAG config is accessible`() {
        assertEquals("medication_reminder_work", AppConfig.Notification.REMINDER_WORK_TAG)
    }

    @Test
    fun `DEFAULT_QUIET_HOURS_START config is accessible`() {
        assertEquals(22, AppConfig.Notification.DEFAULT_QUIET_HOURS_START)
    }

    @Test
    fun `DEFAULT_QUIET_HOURS_END config is accessible`() {
        assertEquals(7, AppConfig.Notification.DEFAULT_QUIET_HOURS_END)
    }

    // ========== Notification ID Calculation Tests ==========

    @Test
    fun `Notification ID calculation is correct`() {
        val baseId = AppConfig.Notification.NOTIFICATION_ID_MEDICATION_BASE
        val medicationId = 5L

        val notificationId = baseId + medicationId.toInt()

        assertEquals(1005, notificationId)
    }

    @Test
    fun `Notification IDs are unique for different medications`() {
        val baseId = AppConfig.Notification.NOTIFICATION_ID_MEDICATION_BASE

        val id1 = baseId + 1
        val id2 = baseId + 2
        val id3 = baseId + 100

        assertTrue(id1 != id2)
        assertTrue(id2 != id3)
        assertTrue(id1 != id3)
    }
}
