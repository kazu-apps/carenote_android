package com.carenote.app.data.worker

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.carenote.app.fakes.FakeClock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * TaskReminderScheduler のユニットテスト
 *
 * Layer 1: calculateDelay の直接テスト（internal メソッド）
 * Layer 2: scheduleReminder の統合テスト（WorkManager 連携）
 */
class TaskReminderSchedulerTest {

    private lateinit var workManager: WorkManager
    private lateinit var clock: FakeClock
    private lateinit var scheduler: TaskReminderScheduler

    @Before
    fun setUp() {
        workManager = mockk(relaxed = true)
        every {
            workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
        } returns mockk(relaxed = true)
        clock = FakeClock(LocalDateTime.of(2026, 2, 17, 14, 0, 0))
        scheduler = TaskReminderScheduler(workManager, clock)
    }

    // ========== Layer 1: calculateDelay direct tests ==========

    @Test
    fun `calculateDelay future 1h returns 3600000ms`() {
        val delay = scheduler.calculateDelay(LocalTime.of(15, 0))

        assertEquals(3_600_000L, delay)
        assertTrue("delay=$delay", delay > 0 && delay <= 86_400_000)
    }

    @Test
    fun `calculateDelay future end of day returns 35940000ms`() {
        val delay = scheduler.calculateDelay(LocalTime.of(23, 59))

        assertEquals(35_940_000L, delay)
        assertTrue("delay=$delay", delay > 0 && delay <= 86_400_000)
    }

    @Test
    fun `calculateDelay past 1h ago wraps to next day`() {
        val delay = scheduler.calculateDelay(LocalTime.of(13, 0))

        assertEquals(82_800_000L, delay)
        assertTrue("delay=$delay", delay > 0 && delay <= 86_400_000)
    }

    @Test
    fun `calculateDelay past 1min ago wraps to next day`() {
        val delay = scheduler.calculateDelay(LocalTime.of(13, 59))

        assertEquals(86_340_000L, delay)
        assertTrue("delay=$delay", delay > 0 && delay <= 86_400_000)
    }

    @Test
    fun `calculateDelay exact same time wraps to next day 24h`() {
        val delay = scheduler.calculateDelay(LocalTime.of(14, 0))

        assertEquals(86_400_000L, delay)
        assertTrue("delay=$delay", delay > 0 && delay <= 86_400_000)
    }

    @Test
    fun `calculateDelay midnight wraps to next day`() {
        val delay = scheduler.calculateDelay(LocalTime.of(0, 0))

        assertEquals(36_000_000L, delay)
        assertTrue("delay=$delay", delay > 0 && delay <= 86_400_000)
    }

    @Test
    fun `calculateDelay 1 second after now returns 1000ms`() {
        clock.setTime(LocalDateTime.of(2026, 2, 17, 13, 59, 59))

        val delay = scheduler.calculateDelay(LocalTime.of(14, 0))

        assertEquals(1_000L, delay)
        assertTrue("delay=$delay", delay > 0 && delay <= 86_400_000)
    }

    @Test
    fun `calculateDelay 1 second before now wraps to next day`() {
        clock.setTime(LocalDateTime.of(2026, 2, 17, 14, 0, 1))

        val delay = scheduler.calculateDelay(LocalTime.of(14, 0))

        assertEquals(86_399_000L, delay)
        assertTrue("delay=$delay", delay > 0 && delay <= 86_400_000)
    }

    // ========== Layer 2: scheduleReminder integration tests ==========

    @Test
    fun `scheduleReminder with future time enqueues work`() {
        scheduler.scheduleReminder(
            taskId = 1L,
            taskTitle = "買い物に行く",
            time = LocalTime.of(15, 0)
        )

        verify(exactly = 1) {
            workManager.enqueueUniqueWork(
                any(),
                eq(ExistingWorkPolicy.REPLACE),
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `scheduleReminder with past time also enqueues work for next day`() {
        scheduler.scheduleReminder(
            taskId = 2L,
            taskTitle = "薬を飲む",
            time = LocalTime.of(13, 0)
        )

        verify(exactly = 1) {
            workManager.enqueueUniqueWork(
                any(),
                eq(ExistingWorkPolicy.REPLACE),
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `scheduleReminder uses correct unique work name`() {
        scheduler.scheduleReminder(
            taskId = 42L,
            taskTitle = "テストタスク",
            time = LocalTime.of(15, 0)
        )

        verify(exactly = 1) {
            workManager.enqueueUniqueWork(
                eq("task_reminder_42"),
                any(),
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `cancelReminder cancels work by tag`() {
        every { workManager.cancelAllWorkByTag(any()) } returns mockk(relaxed = true)

        scheduler.cancelReminder(taskId = 5L)

        verify(exactly = 1) {
            workManager.cancelAllWorkByTag(eq("task_reminder_5"))
        }
    }

    @Test
    fun `cancelAllReminders cancels by reminder work tag`() {
        every { workManager.cancelAllWorkByTag(any()) } returns mockk(relaxed = true)

        scheduler.cancelAllReminders()

        verify(exactly = 1) {
            workManager.cancelAllWorkByTag(eq("task_reminder_work"))
        }
    }
}
