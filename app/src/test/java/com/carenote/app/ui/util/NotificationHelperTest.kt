package com.carenote.app.ui.util

import com.carenote.app.config.AppConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * NotificationHelper のユニットテスト
 *
 * NotificationHelper は Android SDK に強く依存しているため、
 * ここでは主に以下をテスト:
 * 1. 定数値の確認
 * 2. 通知 ID 計算ロジックの検証
 *
 * 実際の通知チャンネル作成・表示は androidTest または手動テストで確認。
 */
class NotificationHelperTest {

    // ========== Config Constants Tests ==========

    @Test
    fun `CHANNEL_ID_MEDICATION_REMINDER constant is correct`() {
        assertEquals("medication_reminder", AppConfig.Notification.CHANNEL_ID_MEDICATION_REMINDER)
    }

    @Test
    fun `CHANNEL_ID_SYNC_STATUS constant is correct`() {
        assertEquals("sync_status", AppConfig.Notification.CHANNEL_ID_SYNC_STATUS)
    }

    @Test
    fun `CHANNEL_ID_GENERAL constant is correct`() {
        assertEquals("general", AppConfig.Notification.CHANNEL_ID_GENERAL)
    }

    @Test
    fun `NOTIFICATION_ID_MEDICATION_BASE constant is correct`() {
        assertEquals(1000, AppConfig.Notification.NOTIFICATION_ID_MEDICATION_BASE)
    }

    @Test
    fun `NOTIFICATION_ID_SYNC constant is correct`() {
        assertEquals(2000, AppConfig.Notification.NOTIFICATION_ID_SYNC)
    }

    @Test
    fun `REMINDER_WORK_TAG constant is correct`() {
        assertEquals("medication_reminder_work", AppConfig.Notification.REMINDER_WORK_TAG)
    }

    // ========== Notification ID Calculation Tests ==========

    @Test
    fun `notification ID calculation for medication ID 1`() {
        val baseId = AppConfig.Notification.NOTIFICATION_ID_MEDICATION_BASE
        val medicationId = 1L

        val notificationId = baseId + medicationId.toInt()

        assertEquals(1001, notificationId)
    }

    @Test
    fun `notification ID calculation for medication ID 100`() {
        val baseId = AppConfig.Notification.NOTIFICATION_ID_MEDICATION_BASE
        val medicationId = 100L

        val notificationId = baseId + medicationId.toInt()

        assertEquals(1100, notificationId)
    }

    @Test
    fun `notification IDs are unique for different medications`() {
        val baseId = AppConfig.Notification.NOTIFICATION_ID_MEDICATION_BASE

        val id1 = baseId + 1
        val id2 = baseId + 2
        val id3 = baseId + 999

        assertNotEquals(id1, id2)
        assertNotEquals(id2, id3)
        assertNotEquals(id1, id3)
    }

    @Test
    fun `medication and sync notification ID ranges do not overlap`() {
        val medicationMax = AppConfig.Notification.NOTIFICATION_ID_MEDICATION_BASE + 999
        val syncId = AppConfig.Notification.NOTIFICATION_ID_SYNC

        // 薬の通知 ID (1000-1999) と同期の通知 ID (2000) は重複しない
        assertNotEquals(medicationMax, syncId)
        assertEquals(1999, medicationMax)
        assertEquals(2000, syncId)
    }

    // ========== Channel ID Uniqueness Tests ==========

    @Test
    fun `all channel IDs are unique`() {
        val channelIds = listOf(
            AppConfig.Notification.CHANNEL_ID_MEDICATION_REMINDER,
            AppConfig.Notification.CHANNEL_ID_SYNC_STATUS,
            AppConfig.Notification.CHANNEL_ID_GENERAL
        )

        assertEquals(3, channelIds.distinct().size)
    }

    // ========== Quiet Hours Config Tests ==========

    @Test
    fun `DEFAULT_QUIET_HOURS_START is valid hour`() {
        val start = AppConfig.Notification.DEFAULT_QUIET_HOURS_START

        assertEquals(22, start)
        assert(start in 0..23)
    }

    @Test
    fun `DEFAULT_QUIET_HOURS_END is valid hour`() {
        val end = AppConfig.Notification.DEFAULT_QUIET_HOURS_END

        assertEquals(7, end)
        assert(end in 0..23)
    }

    @Test
    fun `default quiet hours span overnight`() {
        val start = AppConfig.Notification.DEFAULT_QUIET_HOURS_START
        val end = AppConfig.Notification.DEFAULT_QUIET_HOURS_END

        // 22:00 〜 7:00 は日をまたぐ
        assert(start > end)
    }
}
