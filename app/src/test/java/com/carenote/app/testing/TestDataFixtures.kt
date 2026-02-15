package com.carenote.app.testing

import com.carenote.app.fakes.FakeClock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TestDataFixtures {
    val DEFAULT_CLOCK = FakeClock() // 2026-01-15 10:00:00
    val NOW: LocalDateTime get() = DEFAULT_CLOCK.now()
    val TODAY: LocalDate get() = DEFAULT_CLOCK.today()
    val YESTERDAY: LocalDate get() = TODAY.minusDays(1)
    val TOMORROW: LocalDate get() = TODAY.plusDays(1)
    val NOW_STRING: String get() = NOW.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    val TODAY_STRING: String get() = TODAY.format(DateTimeFormatter.ISO_DATE)
    const val DEFAULT_CARE_RECIPIENT_ID = 1L
    const val DEFAULT_USER = "testUser"
}
