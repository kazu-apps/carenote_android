package com.carenote.app.testing

import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.AppLanguage
import com.carenote.app.domain.model.Gender
import com.carenote.app.domain.model.MealAmount
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.domain.model.RecurrenceFrequency
import com.carenote.app.domain.model.RelationshipType
import com.carenote.app.domain.model.TaskPriority
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.fakes.FakeClock
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class TestBuildersTest {

    // --- Medication ---

    @Test
    fun `aMedication - default values are correct`() {
        val med = aMedication()
        assertEquals(1L, med.id)
        assertEquals(TestDataFixtures.DEFAULT_CARE_RECIPIENT_ID, med.careRecipientId)
        assertEquals("テスト薬", med.name)
        assertEquals("1錠", med.dosage)
        assertEquals(listOf(MedicationTiming.MORNING), med.timings)
        assertEquals(true, med.reminderEnabled)
        assertEquals(TestDataFixtures.NOW, med.createdAt)
        assertEquals(TestDataFixtures.NOW, med.updatedAt)
    }

    @Test
    fun `aMedication - custom values override defaults`() {
        val med = aMedication(
            id = 99L,
            name = "カスタム薬",
            dosage = "2錠",
            timings = listOf(MedicationTiming.EVENING),
            reminderEnabled = false,
            currentStock = 30,
            lowStockThreshold = 5
        )
        assertEquals(99L, med.id)
        assertEquals("カスタム薬", med.name)
        assertEquals("2錠", med.dosage)
        assertEquals(listOf(MedicationTiming.EVENING), med.timings)
        assertEquals(false, med.reminderEnabled)
        assertEquals(30, med.currentStock)
        assertEquals(5, med.lowStockThreshold)
    }

    @Test
    fun `aMedication - FakeClock integration`() {
        val customClock = FakeClock(LocalDateTime.of(2025, 6, 1, 12, 0, 0))
        val med = aMedication(clock = customClock)
        assertEquals(LocalDateTime.of(2025, 6, 1, 12, 0, 0), med.createdAt)
        assertEquals(LocalDateTime.of(2025, 6, 1, 12, 0, 0), med.updatedAt)
    }

    // --- Note ---

    @Test
    fun `aNote - default values are correct`() {
        val note = aNote()
        assertEquals(1L, note.id)
        assertEquals(TestDataFixtures.DEFAULT_CARE_RECIPIENT_ID, note.careRecipientId)
        assertEquals("テストメモ", note.title)
        assertEquals("テスト内容", note.content)
        assertEquals(NoteTag.OTHER, note.tag)
        assertEquals(TestDataFixtures.DEFAULT_USER, note.createdBy)
        assertEquals(TestDataFixtures.NOW, note.createdAt)
    }

    @Test
    fun `aNote - custom values override defaults`() {
        val note = aNote(
            id = 50L,
            title = "カスタムメモ",
            content = "カスタム内容",
            tag = NoteTag.CONDITION,
            createdBy = "customUser"
        )
        assertEquals(50L, note.id)
        assertEquals("カスタムメモ", note.title)
        assertEquals("カスタム内容", note.content)
        assertEquals(NoteTag.CONDITION, note.tag)
        assertEquals("customUser", note.createdBy)
    }

    @Test
    fun `aNote - FakeClock integration`() {
        val customClock = FakeClock(LocalDateTime.of(2025, 3, 15, 8, 30, 0))
        val note = aNote(clock = customClock)
        assertEquals(LocalDateTime.of(2025, 3, 15, 8, 30, 0), note.createdAt)
        assertEquals(LocalDateTime.of(2025, 3, 15, 8, 30, 0), note.updatedAt)
    }

    // --- Task ---

    @Test
    fun `aTask - default values are correct`() {
        val task = aTask()
        assertEquals(1L, task.id)
        assertEquals(TestDataFixtures.DEFAULT_CARE_RECIPIENT_ID, task.careRecipientId)
        assertEquals("テストタスク", task.title)
        assertEquals("", task.description)
        assertEquals(null, task.dueDate)
        assertEquals(false, task.isCompleted)
        assertEquals(TaskPriority.MEDIUM, task.priority)
        assertEquals(RecurrenceFrequency.NONE, task.recurrenceFrequency)
        assertEquals(TestDataFixtures.DEFAULT_USER, task.createdBy)
        assertEquals(TestDataFixtures.NOW, task.createdAt)
    }

    @Test
    fun `aTask - custom values override defaults`() {
        val task = aTask(
            id = 77L,
            title = "カスタムタスク",
            description = "重要なタスク",
            isCompleted = true,
            priority = TaskPriority.HIGH
        )
        assertEquals(77L, task.id)
        assertEquals("カスタムタスク", task.title)
        assertEquals("重要なタスク", task.description)
        assertEquals(true, task.isCompleted)
        assertEquals(TaskPriority.HIGH, task.priority)
    }

    @Test
    fun `aTask - FakeClock integration`() {
        val customClock = FakeClock(LocalDateTime.of(2025, 12, 25, 0, 0, 0))
        val task = aTask(clock = customClock)
        assertEquals(LocalDateTime.of(2025, 12, 25, 0, 0, 0), task.createdAt)
        assertEquals(LocalDateTime.of(2025, 12, 25, 0, 0, 0), task.updatedAt)
    }

    // --- HealthRecord ---

    @Test
    fun `aHealthRecord - default values are correct`() {
        val record = aHealthRecord()
        assertEquals(1L, record.id)
        assertEquals(TestDataFixtures.DEFAULT_CARE_RECIPIENT_ID, record.careRecipientId)
        assertEquals(36.5, record.temperature)
        assertEquals(120, record.bloodPressureHigh)
        assertEquals(80, record.bloodPressureLow)
        assertEquals(72, record.pulse)
        assertEquals(null, record.weight)
        assertEquals("", record.conditionNote)
        assertEquals(TestDataFixtures.DEFAULT_USER, record.createdBy)
        assertEquals(TestDataFixtures.NOW, record.recordedAt)
        assertEquals(TestDataFixtures.NOW, record.createdAt)
    }

    @Test
    fun `aHealthRecord - custom values override defaults`() {
        val record = aHealthRecord(
            id = 42L,
            temperature = 38.2,
            bloodPressureHigh = 140,
            bloodPressureLow = 90,
            pulse = 88,
            weight = 65.0,
            meal = MealAmount.HALF,
            conditionNote = "発熱あり"
        )
        assertEquals(42L, record.id)
        assertEquals(38.2, record.temperature)
        assertEquals(140, record.bloodPressureHigh)
        assertEquals(90, record.bloodPressureLow)
        assertEquals(88, record.pulse)
        assertEquals(65.0, record.weight)
        assertEquals(MealAmount.HALF, record.meal)
        assertEquals("発熱あり", record.conditionNote)
    }

    @Test
    fun `aHealthRecord - FakeClock integration`() {
        val customClock = FakeClock(LocalDateTime.of(2025, 7, 4, 14, 30, 0))
        val record = aHealthRecord(clock = customClock)
        assertEquals(LocalDateTime.of(2025, 7, 4, 14, 30, 0), record.recordedAt)
        assertEquals(LocalDateTime.of(2025, 7, 4, 14, 30, 0), record.createdAt)
        assertEquals(LocalDateTime.of(2025, 7, 4, 14, 30, 0), record.updatedAt)
    }

    // --- User ---

    @Test
    fun `aUser - default values are correct`() {
        val user = aUser()
        assertEquals("testUid", user.uid)
        assertEquals(TestDataFixtures.DEFAULT_USER, user.name)
        assertEquals("test@example.com", user.email)
        assertEquals(false, user.isPremium)
        assertEquals(false, user.isEmailVerified)
        assertEquals(TestDataFixtures.NOW, user.createdAt)
    }

    @Test
    fun `aUser - custom values override defaults`() {
        val user = aUser(
            uid = "customUid",
            name = "カスタムユーザー",
            email = "custom@test.com",
            isPremium = true,
            isEmailVerified = true
        )
        assertEquals("customUid", user.uid)
        assertEquals("カスタムユーザー", user.name)
        assertEquals("custom@test.com", user.email)
        assertEquals(true, user.isPremium)
        assertEquals(true, user.isEmailVerified)
    }

    @Test
    fun `aUser - FakeClock integration`() {
        val customClock = FakeClock(LocalDateTime.of(2025, 9, 1, 15, 0, 0))
        val user = aUser(clock = customClock)
        assertEquals(LocalDateTime.of(2025, 9, 1, 15, 0, 0), user.createdAt)
    }

    // --- CareRecipient ---

    @Test
    fun `aCareRecipient - default values are correct`() {
        val recipient = aCareRecipient()
        assertEquals(1L, recipient.id)
        assertEquals("テスト太郎", recipient.name)
        assertEquals(null, recipient.birthDate)
        assertEquals(Gender.UNSPECIFIED, recipient.gender)
        assertEquals("", recipient.nickname)
        assertEquals("", recipient.careLevel)
        assertEquals("", recipient.medicalHistory)
        assertEquals("", recipient.allergies)
        assertEquals("", recipient.memo)
        assertEquals(TestDataFixtures.NOW, recipient.createdAt)
        assertEquals(TestDataFixtures.NOW, recipient.updatedAt)
    }

    @Test
    fun `aCareRecipient - custom values override defaults`() {
        val recipient = aCareRecipient(
            id = 5L,
            name = "カスタム太郎",
            birthDate = java.time.LocalDate.of(1950, 1, 1),
            gender = Gender.MALE,
            nickname = "たろう",
            careLevel = "要介護3"
        )
        assertEquals(5L, recipient.id)
        assertEquals("カスタム太郎", recipient.name)
        assertEquals(java.time.LocalDate.of(1950, 1, 1), recipient.birthDate)
        assertEquals(Gender.MALE, recipient.gender)
        assertEquals("たろう", recipient.nickname)
        assertEquals("要介護3", recipient.careLevel)
    }

    @Test
    fun `aCareRecipient - FakeClock integration`() {
        val customClock = FakeClock(LocalDateTime.of(2025, 4, 10, 9, 0, 0))
        val recipient = aCareRecipient(clock = customClock)
        assertEquals(LocalDateTime.of(2025, 4, 10, 9, 0, 0), recipient.createdAt)
        assertEquals(LocalDateTime.of(2025, 4, 10, 9, 0, 0), recipient.updatedAt)
    }

    // --- EmergencyContact ---

    @Test
    fun `aEmergencyContact - default values are correct`() {
        val contact = aEmergencyContact()
        assertEquals(1L, contact.id)
        assertEquals(TestDataFixtures.DEFAULT_CARE_RECIPIENT_ID, contact.careRecipientId)
        assertEquals("テスト連絡先", contact.name)
        assertEquals("090-1234-5678", contact.phoneNumber)
        assertEquals(RelationshipType.FAMILY, contact.relationship)
        assertEquals("", contact.memo)
        assertEquals(TestDataFixtures.NOW, contact.createdAt)
        assertEquals(TestDataFixtures.NOW, contact.updatedAt)
    }

    @Test
    fun `aEmergencyContact - custom values override defaults`() {
        val contact = aEmergencyContact(
            id = 10L,
            careRecipientId = 5L,
            name = "田中医師",
            phoneNumber = "03-1234-5678",
            relationship = RelationshipType.DOCTOR,
            memo = "主治医"
        )
        assertEquals(10L, contact.id)
        assertEquals(5L, contact.careRecipientId)
        assertEquals("田中医師", contact.name)
        assertEquals("03-1234-5678", contact.phoneNumber)
        assertEquals(RelationshipType.DOCTOR, contact.relationship)
        assertEquals("主治医", contact.memo)
    }

    @Test
    fun `aEmergencyContact - FakeClock integration`() {
        val customClock = FakeClock(LocalDateTime.of(2025, 11, 20, 16, 30, 0))
        val contact = aEmergencyContact(clock = customClock)
        assertEquals(LocalDateTime.of(2025, 11, 20, 16, 30, 0), contact.createdAt)
        assertEquals(LocalDateTime.of(2025, 11, 20, 16, 30, 0), contact.updatedAt)
    }

    // --- UserSettings ---

    @Test
    fun `aUserSettings - default values match AppConfig`() {
        val settings = aUserSettings()
        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertEquals(AppLanguage.SYSTEM, settings.appLanguage)
        assertEquals(true, settings.notificationsEnabled)
        assertEquals(AppConfig.Notification.DEFAULT_QUIET_HOURS_START, settings.quietHoursStart)
        assertEquals(AppConfig.Notification.DEFAULT_QUIET_HOURS_END, settings.quietHoursEnd)
        assertEquals(AppConfig.HealthThresholds.TEMPERATURE_HIGH, settings.temperatureHigh, 0.01)
        assertEquals(AppConfig.HealthThresholds.BLOOD_PRESSURE_HIGH_UPPER, settings.bloodPressureHighUpper)
        assertEquals(AppConfig.HealthThresholds.BLOOD_PRESSURE_HIGH_LOWER, settings.bloodPressureHighLower)
        assertEquals(AppConfig.HealthThresholds.PULSE_HIGH, settings.pulseHigh)
        assertEquals(AppConfig.HealthThresholds.PULSE_LOW, settings.pulseLow)
        assertEquals(AppConfig.Medication.DEFAULT_MORNING_HOUR, settings.morningHour)
        assertEquals(AppConfig.Medication.DEFAULT_MORNING_MINUTE, settings.morningMinute)
        assertEquals(AppConfig.Medication.DEFAULT_NOON_HOUR, settings.noonHour)
        assertEquals(AppConfig.Medication.DEFAULT_NOON_MINUTE, settings.noonMinute)
        assertEquals(AppConfig.Medication.DEFAULT_EVENING_HOUR, settings.eveningHour)
        assertEquals(AppConfig.Medication.DEFAULT_EVENING_MINUTE, settings.eveningMinute)
        assertEquals(true, settings.syncEnabled)
        assertEquals(null, settings.lastSyncTime)
        assertEquals(false, settings.biometricEnabled)
        assertEquals(AppConfig.Session.DEFAULT_TIMEOUT_MINUTES, settings.sessionTimeoutMinutes)
        assertEquals(false, settings.useDynamicColor)
    }

    @Test
    fun `aUserSettings - custom values override defaults`() {
        val settings = aUserSettings(
            themeMode = ThemeMode.DARK,
            appLanguage = AppLanguage.JAPANESE,
            notificationsEnabled = false,
            quietHoursStart = 20,
            quietHoursEnd = 8,
            syncEnabled = false,
            biometricEnabled = true,
            useDynamicColor = true
        )
        assertEquals(ThemeMode.DARK, settings.themeMode)
        assertEquals(AppLanguage.JAPANESE, settings.appLanguage)
        assertEquals(false, settings.notificationsEnabled)
        assertEquals(20, settings.quietHoursStart)
        assertEquals(8, settings.quietHoursEnd)
        assertEquals(false, settings.syncEnabled)
        assertEquals(true, settings.biometricEnabled)
        assertEquals(true, settings.useDynamicColor)
    }

    @Test
    fun `aUserSettings - lastSyncTime can be set`() {
        val syncTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)
        val settings = aUserSettings(lastSyncTime = syncTime)
        assertEquals(syncTime, settings.lastSyncTime)
    }
}
