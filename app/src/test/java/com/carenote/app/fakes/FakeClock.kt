package com.carenote.app.fakes

import com.carenote.app.domain.util.Clock
import java.time.LocalDate
import java.time.LocalDateTime

class FakeClock(
    private var currentTime: LocalDateTime = LocalDateTime.of(2026, 1, 15, 10, 0, 0)
) : Clock {
    override fun now(): LocalDateTime = currentTime
    override fun today(): LocalDate = currentTime.toLocalDate()
    fun setTime(time: LocalDateTime) { currentTime = time }
    fun advanceMinutes(minutes: Long) { currentTime = currentTime.plusMinutes(minutes) }
}
