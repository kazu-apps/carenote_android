package com.carenote.app.ui.screens.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsDialogStateTest {

    @Test
    fun dialogState_initialStateIsNone() {
        val state: SettingsDialogState = SettingsDialogState.None
        assertEquals(SettingsDialogState.None, state)
    }

    @Test
    fun dialogState_hasAllExpectedStates() {
        val allStates = listOf(
            SettingsDialogState.None,
            SettingsDialogState.QuietHoursStart,
            SettingsDialogState.QuietHoursEnd,
            SettingsDialogState.Temperature,
            SettingsDialogState.BpUpper,
            SettingsDialogState.BpLower,
            SettingsDialogState.PulseHigh,
            SettingsDialogState.PulseLow,
            SettingsDialogState.MorningTime,
            SettingsDialogState.NoonTime,
            SettingsDialogState.EveningTime,
            SettingsDialogState.ResetConfirm,
        )
        assertEquals(12, allStates.size)
    }

    @Test
    fun dialogState_quietHoursStartIsDistinctFromEnd() {
        val start: SettingsDialogState = SettingsDialogState.QuietHoursStart
        val end: SettingsDialogState = SettingsDialogState.QuietHoursEnd
        assertNotEquals(start, end)
    }

    @Test
    fun dialogState_allStatesAreDistinct() {
        val allStates = listOf(
            SettingsDialogState.None,
            SettingsDialogState.QuietHoursStart,
            SettingsDialogState.QuietHoursEnd,
            SettingsDialogState.Temperature,
            SettingsDialogState.BpUpper,
            SettingsDialogState.BpLower,
            SettingsDialogState.PulseHigh,
            SettingsDialogState.PulseLow,
            SettingsDialogState.MorningTime,
            SettingsDialogState.NoonTime,
            SettingsDialogState.EveningTime,
            SettingsDialogState.ResetConfirm,
        )
        val distinctStates = allStates.toSet()
        assertEquals(allStates.size, distinctStates.size)
    }

    @Test
    fun dialogState_canTransitionFromNoneToAnyState() {
        var state: SettingsDialogState = SettingsDialogState.None

        state = SettingsDialogState.QuietHoursStart
        assertEquals(SettingsDialogState.QuietHoursStart, state)

        state = SettingsDialogState.None
        state = SettingsDialogState.Temperature
        assertEquals(SettingsDialogState.Temperature, state)

        state = SettingsDialogState.None
        state = SettingsDialogState.ResetConfirm
        assertEquals(SettingsDialogState.ResetConfirm, state)
    }

    @Test
    fun dialogState_canTransitionFromAnyStateToNone() {
        var state: SettingsDialogState = SettingsDialogState.QuietHoursStart
        state = SettingsDialogState.None
        assertEquals(SettingsDialogState.None, state)

        state = SettingsDialogState.BpUpper
        state = SettingsDialogState.None
        assertEquals(SettingsDialogState.None, state)

        state = SettingsDialogState.ResetConfirm
        state = SettingsDialogState.None
        assertEquals(SettingsDialogState.None, state)
    }

    @Test
    fun dialogState_quietHoursCascade() {
        var state: SettingsDialogState = SettingsDialogState.None

        state = SettingsDialogState.QuietHoursStart
        assertEquals(SettingsDialogState.QuietHoursStart, state)

        state = SettingsDialogState.QuietHoursEnd
        assertEquals(SettingsDialogState.QuietHoursEnd, state)

        state = SettingsDialogState.None
        assertEquals(SettingsDialogState.None, state)
    }

    @Test
    fun dialogState_isTimePickerDialog() {
        assertTrue(SettingsDialogState.QuietHoursStart.isTimePicker)
        assertTrue(SettingsDialogState.QuietHoursEnd.isTimePicker)
        assertTrue(SettingsDialogState.MorningTime.isTimePicker)
        assertTrue(SettingsDialogState.NoonTime.isTimePicker)
        assertTrue(SettingsDialogState.EveningTime.isTimePicker)

        assertFalse(SettingsDialogState.None.isTimePicker)
        assertFalse(SettingsDialogState.Temperature.isTimePicker)
        assertFalse(SettingsDialogState.BpUpper.isTimePicker)
        assertFalse(SettingsDialogState.ResetConfirm.isTimePicker)
    }

    @Test
    fun dialogState_isNumberInputDialog() {
        assertTrue(SettingsDialogState.Temperature.isNumberInput)
        assertTrue(SettingsDialogState.BpUpper.isNumberInput)
        assertTrue(SettingsDialogState.BpLower.isNumberInput)
        assertTrue(SettingsDialogState.PulseHigh.isNumberInput)
        assertTrue(SettingsDialogState.PulseLow.isNumberInput)

        assertFalse(SettingsDialogState.None.isNumberInput)
        assertFalse(SettingsDialogState.QuietHoursStart.isNumberInput)
        assertFalse(SettingsDialogState.MorningTime.isNumberInput)
        assertFalse(SettingsDialogState.ResetConfirm.isNumberInput)
    }

    @Test
    fun dialogState_isConfirmDialog() {
        assertTrue(SettingsDialogState.ResetConfirm.isConfirm)

        assertFalse(SettingsDialogState.None.isConfirm)
        assertFalse(SettingsDialogState.QuietHoursStart.isConfirm)
        assertFalse(SettingsDialogState.Temperature.isConfirm)
        assertFalse(SettingsDialogState.MorningTime.isConfirm)
    }
}
