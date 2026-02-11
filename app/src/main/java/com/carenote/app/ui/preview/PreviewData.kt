package com.carenote.app.ui.preview

import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.ExcretionType
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.MealAmount
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.domain.model.RecurrenceFrequency
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.model.TaskPriority
import com.carenote.app.ui.screens.auth.ForgotPasswordFormState
import com.carenote.app.ui.screens.auth.LoginFormState
import com.carenote.app.ui.screens.auth.RegisterFormState
import com.carenote.app.ui.screens.calendar.AddEditCalendarEventFormState
import com.carenote.app.ui.screens.healthrecords.AddEditHealthRecordFormState
import com.carenote.app.ui.screens.medication.AddEditMedicationFormState
import com.carenote.app.ui.screens.notes.AddEditNoteFormState
import com.carenote.app.ui.screens.tasks.AddEditTaskFormState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object PreviewData {

    private val fixedDateTime = LocalDateTime.of(2025, 1, 15, 9, 0)
    private val fixedDate = fixedDateTime.toLocalDate()
    private val fixedTime = fixedDateTime.toLocalTime()

    // --- Domain Models ---

    val medication1 = Medication(
        id = 1,
        name = "アムロジピン",
        dosage = "5mg",
        timings = listOf(MedicationTiming.MORNING),
        times = mapOf(MedicationTiming.MORNING to LocalTime.of(8, 0)),
        reminderEnabled = true,
        createdAt = fixedDateTime,
        updatedAt = fixedDateTime,
        currentStock = 28,
        lowStockThreshold = 5
    )

    val medication2 = Medication(
        id = 2,
        name = "メトホルミン",
        dosage = "500mg",
        timings = listOf(MedicationTiming.MORNING, MedicationTiming.EVENING),
        times = mapOf(
            MedicationTiming.MORNING to LocalTime.of(8, 0),
            MedicationTiming.EVENING to LocalTime.of(20, 0)
        ),
        reminderEnabled = true,
        createdAt = fixedDateTime,
        updatedAt = fixedDateTime,
        currentStock = 3,
        lowStockThreshold = 5
    )

    val medication3 = Medication(
        id = 3,
        name = "ロキソプロフェン",
        dosage = "60mg",
        timings = listOf(MedicationTiming.NOON),
        times = mapOf(MedicationTiming.NOON to LocalTime.of(12, 0)),
        reminderEnabled = false,
        createdAt = fixedDateTime,
        updatedAt = fixedDateTime
    )

    val medications = listOf(medication1, medication2, medication3)

    val todayLogs: Map<Pair<Long, String?>, MedicationLogStatus> = mapOf(
        (1L to MedicationTiming.MORNING.name) to MedicationLogStatus.TAKEN,
        (2L to MedicationTiming.MORNING.name) to MedicationLogStatus.TAKEN,
        (2L to MedicationTiming.EVENING.name) to MedicationLogStatus.SKIPPED
    )

    val task1 = Task(
        id = 1,
        title = "通院予約の確認",
        description = "来週の内科通院の予約時間を確認する",
        dueDate = fixedDate.plusDays(3),
        isCompleted = false,
        priority = TaskPriority.HIGH,
        createdAt = fixedDateTime,
        updatedAt = fixedDateTime
    )

    val task2 = Task(
        id = 2,
        title = "介護保険の更新手続き",
        description = "市役所で介護保険の更新申請を行う",
        dueDate = fixedDate.plusDays(7),
        isCompleted = false,
        priority = TaskPriority.MEDIUM,
        recurrenceFrequency = RecurrenceFrequency.MONTHLY,
        reminderEnabled = true,
        reminderTime = LocalTime.of(9, 0),
        createdAt = fixedDateTime,
        updatedAt = fixedDateTime
    )

    val task3 = Task(
        id = 3,
        title = "買い物リスト作成",
        description = "",
        dueDate = null,
        isCompleted = true,
        priority = TaskPriority.LOW,
        createdAt = fixedDateTime,
        updatedAt = fixedDateTime
    )

    val tasks = listOf(task1, task2, task3)

    val note1 = Note(
        id = 1,
        title = "今日の体調メモ",
        content = "朝から食欲があり、散歩も30分できた。顔色も良い。",
        tag = NoteTag.CONDITION,
        authorId = "user1",
        createdAt = fixedDateTime,
        updatedAt = fixedDateTime
    )

    val note2 = Note(
        id = 2,
        title = "昼食の記録",
        content = "おかゆと煮物を完食。水分も十分に摂取。",
        tag = NoteTag.MEAL,
        authorId = "user1",
        createdAt = fixedDateTime,
        updatedAt = fixedDateTime
    )

    val note3 = Note(
        id = 3,
        title = "夜勤への申し送り",
        content = "15時に微熱あり（37.2度）。解熱剤は未服用。経過観察中。",
        tag = NoteTag.REPORT,
        authorId = "user1",
        createdAt = fixedDateTime,
        updatedAt = fixedDateTime
    )

    val notes = listOf(note1, note2, note3)

    val healthRecord1 = HealthRecord(
        id = 1,
        temperature = 36.5,
        bloodPressureHigh = 130,
        bloodPressureLow = 85,
        pulse = 72,
        weight = 58.5,
        meal = MealAmount.MOSTLY,
        excretion = ExcretionType.NORMAL,
        conditionNote = "朝の体調は良好",
        recordedAt = fixedDateTime,
        createdAt = fixedDateTime,
        updatedAt = fixedDateTime
    )

    val healthRecord2 = HealthRecord(
        id = 2,
        temperature = 37.2,
        bloodPressureHigh = 145,
        bloodPressureLow = 92,
        pulse = 80,
        weight = 58.3,
        meal = MealAmount.HALF,
        excretion = ExcretionType.SOFT,
        conditionNote = "微熱あり。経過観察",
        recordedAt = fixedDateTime.minusHours(12),
        createdAt = fixedDateTime.minusHours(12),
        updatedAt = fixedDateTime.minusHours(12)
    )

    val healthRecords = listOf(healthRecord1, healthRecord2)

    val calendarEvent1 = CalendarEvent(
        id = 1,
        title = "内科通院",
        description = "定期検診。血液検査あり",
        date = fixedDate,
        startTime = LocalTime.of(10, 0),
        endTime = LocalTime.of(11, 30),
        isAllDay = false,
        createdAt = fixedDateTime,
        updatedAt = fixedDateTime
    )

    val calendarEvent2 = CalendarEvent(
        id = 2,
        title = "デイサービス",
        description = "",
        date = fixedDate.plusDays(1),
        isAllDay = true,
        createdAt = fixedDateTime,
        updatedAt = fixedDateTime
    )

    val calendarEvents = listOf(calendarEvent1, calendarEvent2)

    // --- Form States (populated) ---

    val loginFormState = LoginFormState(
        email = "caregiver@example.com",
        password = "password123"
    )

    val registerFormState = RegisterFormState(
        email = "caregiver@example.com",
        password = "password123",
        displayName = "田中太郎"
    )

    val forgotPasswordFormState = ForgotPasswordFormState(
        email = "caregiver@example.com"
    )

    val addEditMedicationFormState = AddEditMedicationFormState(
        name = "アムロジピン",
        dosage = "5mg",
        timings = listOf(MedicationTiming.MORNING, MedicationTiming.EVENING),
        times = mapOf(
            MedicationTiming.MORNING to LocalTime.of(8, 0),
            MedicationTiming.EVENING to LocalTime.of(20, 0)
        ),
        reminderEnabled = true
    )

    val addEditTaskFormState = AddEditTaskFormState(
        title = "通院予約の確認",
        description = "来週の内科通院の予約時間を確認する",
        dueDate = fixedDate.plusDays(3),
        priority = TaskPriority.HIGH,
        reminderEnabled = true,
        reminderTime = LocalTime.of(9, 0)
    )

    val addEditCalendarEventFormState = AddEditCalendarEventFormState(
        title = "内科通院",
        description = "定期検診。血液検査あり",
        date = fixedDate,
        startTime = LocalTime.of(10, 0),
        endTime = LocalTime.of(11, 30),
        isAllDay = false
    )

    val addEditHealthRecordFormState = AddEditHealthRecordFormState(
        temperature = "36.5",
        bloodPressureHigh = "130",
        bloodPressureLow = "85",
        pulse = "72",
        weight = "58.5",
        meal = MealAmount.MOSTLY,
        excretion = ExcretionType.NORMAL,
        conditionNote = "朝の体調は良好",
        recordedAt = fixedDateTime
    )

    val addEditNoteFormState = AddEditNoteFormState(
        title = "今日の体調メモ",
        content = "朝から食欲があり、散歩も30分できた。",
        tag = NoteTag.CONDITION
    )
}
