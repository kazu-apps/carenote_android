package com.carenote.app.data.repository

import com.carenote.app.config.AppConfig
import com.carenote.app.data.local.NotificationCountDataSource
import com.carenote.app.domain.util.Clock
import com.carenote.app.fakes.FakeBillingRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class PremiumFeatureGuardImplTest {

    private lateinit var billingRepository: FakeBillingRepository
    private lateinit var notificationCountDataSource: NotificationCountDataSource
    private lateinit var clock: Clock
    private lateinit var guard: PremiumFeatureGuardImpl

    @Before
    fun setUp() {
        billingRepository = FakeBillingRepository()
        notificationCountDataSource = mockk(relaxed = true)
        clock = mockk()
        every { clock.today() } returns LocalDate.of(2026, 2, 15)
        every { clock.now() } returns LocalDateTime.of(2026, 2, 15, 10, 0)
        every { notificationCountDataSource.getTaskReminderCountToday(clock) } returns 0
        guard = PremiumFeatureGuardImpl(billingRepository, notificationCountDataSource, clock)
    }

    @Test
    fun `premium active always allows task reminder`() = runTest {
        billingRepository.setPremiumActive()

        assertTrue(guard.canSendTaskReminder())
    }

    @Test
    fun `free user allows first reminder`() = runTest {
        every { notificationCountDataSource.getTaskReminderCountToday(clock) } returns 0

        assertTrue(guard.canSendTaskReminder())
    }

    @Test
    fun `free user allows up to limit minus one`() = runTest {
        every { notificationCountDataSource.getTaskReminderCountToday(clock) } returns 2

        assertTrue(guard.canSendTaskReminder())
    }

    @Test
    fun `free user blocks at limit`() = runTest {
        every { notificationCountDataSource.getTaskReminderCountToday(clock) } returns 3

        assertFalse(guard.canSendTaskReminder())
    }

    @Test
    fun `free user blocks over limit`() = runTest {
        every { notificationCountDataSource.getTaskReminderCountToday(clock) } returns 5

        assertFalse(guard.canSendTaskReminder())
    }

    @Test
    fun `expired premium applies limit`() = runTest {
        billingRepository.setPremiumExpired()
        every { notificationCountDataSource.getTaskReminderCountToday(clock) } returns 3

        assertFalse(guard.canSendTaskReminder())
    }

    @Test
    fun `pending premium applies limit`() = runTest {
        billingRepository.setPremiumPending()
        every { notificationCountDataSource.getTaskReminderCountToday(clock) } returns 3

        assertFalse(guard.canSendTaskReminder())
    }

    @Test
    fun `record increments count`() = runTest {
        guard.recordTaskReminderSent()

        verify(exactly = 1) { notificationCountDataSource.incrementTaskReminderCount(clock) }
    }

    @Test
    fun `getTaskReminderCountToday delegates to data source`() {
        every { notificationCountDataSource.getTaskReminderCountToday(clock) } returns 2

        assertEquals(2, guard.getTaskReminderCountToday())
    }

    @Test
    fun `getTaskReminderDailyLimit returns config value`() {
        assertEquals(
            AppConfig.Notification.TASK_REMINDER_FREE_DAILY_LIMIT,
            guard.getTaskReminderDailyLimit()
        )
    }

    @Test
    fun `limit matches expected config value of 3`() {
        assertEquals(3, AppConfig.Notification.TASK_REMINDER_FREE_DAILY_LIMIT)
    }

    @Test
    fun `premium user record still increments count`() = runTest {
        billingRepository.setPremiumActive()

        guard.recordTaskReminderSent()

        verify(exactly = 1) { notificationCountDataSource.incrementTaskReminderCount(clock) }
    }
}
