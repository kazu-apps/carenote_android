package com.carenote.app.testing

import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.AppLanguage
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.CalendarEventType
import com.carenote.app.domain.model.CareRecipient
import com.carenote.app.domain.model.EmergencyContact
import com.carenote.app.domain.model.ExcretionType
import com.carenote.app.domain.model.Gender
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.Invitation
import com.carenote.app.domain.model.InvitationStatus
import com.carenote.app.domain.model.MealAmount
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.Member
import com.carenote.app.domain.model.MemberRole
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteComment
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.domain.model.RecurrenceFrequency
import com.carenote.app.domain.model.RelationshipType
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.model.TaskPriority
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.User
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.domain.util.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun aMedication(
    id: Long = 1L,
    careRecipientId: Long = TestDataFixtures.DEFAULT_CARE_RECIPIENT_ID,
    name: String = "テスト薬",
    dosage: String = "1錠",
    timings: List<MedicationTiming> = listOf(MedicationTiming.MORNING),
    times: Map<MedicationTiming, LocalTime> = emptyMap(),
    reminderEnabled: Boolean = true,
    currentStock: Int? = null,
    lowStockThreshold: Int? = null,
    clock: Clock = TestDataFixtures.DEFAULT_CLOCK
): Medication = Medication(
    id = id,
    careRecipientId = careRecipientId,
    name = name,
    dosage = dosage,
    timings = timings,
    times = times,
    reminderEnabled = reminderEnabled,
    createdAt = clock.now(),
    updatedAt = clock.now(),
    currentStock = currentStock,
    lowStockThreshold = lowStockThreshold
)

fun aNote(
    id: Long = 1L,
    careRecipientId: Long = TestDataFixtures.DEFAULT_CARE_RECIPIENT_ID,
    title: String = "テストメモ",
    content: String = "テスト内容",
    tag: NoteTag = NoteTag.OTHER,
    createdBy: String = TestDataFixtures.DEFAULT_USER,
    clock: Clock = TestDataFixtures.DEFAULT_CLOCK
): Note = Note(
    id = id,
    careRecipientId = careRecipientId,
    title = title,
    content = content,
    tag = tag,
    createdBy = createdBy,
    createdAt = clock.now(),
    updatedAt = clock.now()
)

fun aTask(
    id: Long = 1L,
    careRecipientId: Long = TestDataFixtures.DEFAULT_CARE_RECIPIENT_ID,
    title: String = "テストタスク",
    description: String = "",
    dueDate: LocalDate? = null,
    isCompleted: Boolean = false,
    priority: TaskPriority = TaskPriority.MEDIUM,
    recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
    recurrenceInterval: Int = 1,
    reminderEnabled: Boolean = false,
    reminderTime: LocalTime? = null,
    createdBy: String = TestDataFixtures.DEFAULT_USER,
    clock: Clock = TestDataFixtures.DEFAULT_CLOCK
): Task = Task(
    id = id,
    careRecipientId = careRecipientId,
    title = title,
    description = description,
    dueDate = dueDate,
    isCompleted = isCompleted,
    priority = priority,
    recurrenceFrequency = recurrenceFrequency,
    recurrenceInterval = recurrenceInterval,
    reminderEnabled = reminderEnabled,
    reminderTime = reminderTime,
    createdBy = createdBy,
    createdAt = clock.now(),
    updatedAt = clock.now()
)

fun aHealthRecord(
    id: Long = 1L,
    careRecipientId: Long = TestDataFixtures.DEFAULT_CARE_RECIPIENT_ID,
    temperature: Double? = 36.5,
    bloodPressureHigh: Int? = 120,
    bloodPressureLow: Int? = 80,
    pulse: Int? = 72,
    weight: Double? = null,
    meal: MealAmount? = null,
    excretion: ExcretionType? = null,
    conditionNote: String = "",
    createdBy: String = TestDataFixtures.DEFAULT_USER,
    clock: Clock = TestDataFixtures.DEFAULT_CLOCK
): HealthRecord = HealthRecord(
    id = id,
    careRecipientId = careRecipientId,
    temperature = temperature,
    bloodPressureHigh = bloodPressureHigh,
    bloodPressureLow = bloodPressureLow,
    pulse = pulse,
    weight = weight,
    meal = meal,
    excretion = excretion,
    conditionNote = conditionNote,
    createdBy = createdBy,
    recordedAt = clock.now(),
    createdAt = clock.now(),
    updatedAt = clock.now()
)

fun aMedicationLog(
    id: Long = 1L,
    careRecipientId: Long = TestDataFixtures.DEFAULT_CARE_RECIPIENT_ID,
    medicationId: Long = 1L,
    status: MedicationLogStatus = MedicationLogStatus.TAKEN,
    scheduledAt: LocalDateTime? = null,
    memo: String = "",
    timing: MedicationTiming? = null,
    clock: Clock = TestDataFixtures.DEFAULT_CLOCK
): MedicationLog = MedicationLog(
    id = id,
    careRecipientId = careRecipientId,
    medicationId = medicationId,
    status = status,
    scheduledAt = scheduledAt ?: clock.now(),
    recordedAt = clock.now(),
    memo = memo,
    timing = timing
)

fun aNoteComment(
    id: Long = 1L,
    careRecipientId: Long = TestDataFixtures.DEFAULT_CARE_RECIPIENT_ID,
    noteId: Long = 1L,
    content: String = "テストコメント",
    createdBy: String = TestDataFixtures.DEFAULT_USER,
    clock: Clock = TestDataFixtures.DEFAULT_CLOCK
): NoteComment = NoteComment(
    id = id,
    careRecipientId = careRecipientId,
    noteId = noteId,
    content = content,
    createdBy = createdBy,
    createdAt = clock.now(),
    updatedAt = clock.now()
)

fun aCalendarEvent(
    id: Long = 1L,
    careRecipientId: Long = TestDataFixtures.DEFAULT_CARE_RECIPIENT_ID,
    title: String = "テストイベント",
    description: String = "",
    date: LocalDate? = null,
    startTime: LocalTime? = null,
    endTime: LocalTime? = null,
    isAllDay: Boolean = true,
    type: CalendarEventType = CalendarEventType.OTHER,
    completed: Boolean = false,
    recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
    recurrenceInterval: Int = 1,
    clock: Clock = TestDataFixtures.DEFAULT_CLOCK
): CalendarEvent = CalendarEvent(
    id = id,
    careRecipientId = careRecipientId,
    title = title,
    description = description,
    date = date ?: clock.today(),
    startTime = startTime,
    endTime = endTime,
    isAllDay = isAllDay,
    type = type,
    completed = completed,
    recurrenceFrequency = recurrenceFrequency,
    recurrenceInterval = recurrenceInterval,
    createdAt = clock.now(),
    updatedAt = clock.now()
)

fun aUser(
    uid: String = "testUid",
    name: String = TestDataFixtures.DEFAULT_USER,
    email: String = "test@example.com",
    isPremium: Boolean = false,
    isEmailVerified: Boolean = false,
    clock: Clock = TestDataFixtures.DEFAULT_CLOCK
): User = User(
    uid = uid,
    name = name,
    email = email,
    createdAt = clock.now(),
    isPremium = isPremium,
    isEmailVerified = isEmailVerified
)

fun aCareRecipient(
    id: Long = 1L,
    name: String = "テスト太郎",
    birthDate: LocalDate? = null,
    gender: Gender = Gender.UNSPECIFIED,
    nickname: String = "",
    careLevel: String = "",
    medicalHistory: String = "",
    allergies: String = "",
    memo: String = "",
    firestoreId: String? = null,
    clock: Clock = TestDataFixtures.DEFAULT_CLOCK
): CareRecipient = CareRecipient(
    id = id,
    name = name,
    birthDate = birthDate,
    gender = gender,
    nickname = nickname,
    careLevel = careLevel,
    medicalHistory = medicalHistory,
    allergies = allergies,
    memo = memo,
    createdAt = clock.now(),
    updatedAt = clock.now(),
    firestoreId = firestoreId
)

fun aEmergencyContact(
    id: Long = 1L,
    careRecipientId: Long = TestDataFixtures.DEFAULT_CARE_RECIPIENT_ID,
    name: String = "テスト連絡先",
    phoneNumber: String = "090-1234-5678",
    relationship: RelationshipType = RelationshipType.FAMILY,
    memo: String = "",
    clock: Clock = TestDataFixtures.DEFAULT_CLOCK
): EmergencyContact = EmergencyContact(
    id = id,
    careRecipientId = careRecipientId,
    name = name,
    phoneNumber = phoneNumber,
    relationship = relationship,
    memo = memo,
    createdAt = clock.now(),
    updatedAt = clock.now()
)

fun aUserSettings(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    appLanguage: AppLanguage = AppLanguage.SYSTEM,
    notificationsEnabled: Boolean = true,
    quietHoursStart: Int = AppConfig.Notification.DEFAULT_QUIET_HOURS_START,
    quietHoursEnd: Int = AppConfig.Notification.DEFAULT_QUIET_HOURS_END,
    temperatureHigh: Double = AppConfig.HealthThresholds.TEMPERATURE_HIGH,
    bloodPressureHighUpper: Int = AppConfig.HealthThresholds.BLOOD_PRESSURE_HIGH_UPPER,
    bloodPressureHighLower: Int = AppConfig.HealthThresholds.BLOOD_PRESSURE_HIGH_LOWER,
    pulseHigh: Int = AppConfig.HealthThresholds.PULSE_HIGH,
    pulseLow: Int = AppConfig.HealthThresholds.PULSE_LOW,
    morningHour: Int = AppConfig.Medication.DEFAULT_MORNING_HOUR,
    morningMinute: Int = AppConfig.Medication.DEFAULT_MORNING_MINUTE,
    noonHour: Int = AppConfig.Medication.DEFAULT_NOON_HOUR,
    noonMinute: Int = AppConfig.Medication.DEFAULT_NOON_MINUTE,
    eveningHour: Int = AppConfig.Medication.DEFAULT_EVENING_HOUR,
    eveningMinute: Int = AppConfig.Medication.DEFAULT_EVENING_MINUTE,
    syncEnabled: Boolean = true,
    lastSyncTime: LocalDateTime? = null,
    biometricEnabled: Boolean = false,
    sessionTimeoutMinutes: Int = AppConfig.Session.DEFAULT_TIMEOUT_MINUTES,
    useDynamicColor: Boolean = false
): UserSettings = UserSettings(
    themeMode = themeMode,
    appLanguage = appLanguage,
    notificationsEnabled = notificationsEnabled,
    quietHoursStart = quietHoursStart,
    quietHoursEnd = quietHoursEnd,
    temperatureHigh = temperatureHigh,
    bloodPressureHighUpper = bloodPressureHighUpper,
    bloodPressureHighLower = bloodPressureHighLower,
    pulseHigh = pulseHigh,
    pulseLow = pulseLow,
    morningHour = morningHour,
    morningMinute = morningMinute,
    noonHour = noonHour,
    noonMinute = noonMinute,
    eveningHour = eveningHour,
    eveningMinute = eveningMinute,
    syncEnabled = syncEnabled,
    lastSyncTime = lastSyncTime,
    biometricEnabled = biometricEnabled,
    sessionTimeoutMinutes = sessionTimeoutMinutes,
    useDynamicColor = useDynamicColor
)

fun aMember(
    id: Long = 1L,
    careRecipientId: Long = TestDataFixtures.DEFAULT_CARE_RECIPIENT_ID,
    uid: String = "testUid",
    role: MemberRole = MemberRole.MEMBER,
    clock: Clock = TestDataFixtures.DEFAULT_CLOCK
): Member = Member(
    id = id,
    careRecipientId = careRecipientId,
    uid = uid,
    role = role,
    joinedAt = clock.now()
)

fun aInvitation(
    id: Long = 1L,
    careRecipientId: Long = TestDataFixtures.DEFAULT_CARE_RECIPIENT_ID,
    inviterUid: String = "inviterUid",
    inviteeEmail: String = "invitee@example.com",
    status: InvitationStatus = InvitationStatus.PENDING,
    token: String = "test-token-001",
    expiresAt: LocalDateTime? = null,
    clock: Clock = TestDataFixtures.DEFAULT_CLOCK
): Invitation = Invitation(
    id = id,
    careRecipientId = careRecipientId,
    inviterUid = inviterUid,
    inviteeEmail = inviteeEmail,
    status = status,
    token = token,
    expiresAt = expiresAt ?: clock.now().plusDays(7),
    createdAt = clock.now()
)
