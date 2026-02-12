package com.carenote.app.ui.screens.carerecipient

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.model.CareRecipient
import com.carenote.app.domain.model.Gender
import com.carenote.app.fakes.FakeCareRecipientRepository
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeClock
import com.carenote.app.ui.util.SnackbarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class CareRecipientViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeClock = FakeClock()
    private lateinit var repository: FakeCareRecipientRepository
    private lateinit var analyticsRepository: FakeAnalyticsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeCareRecipientRepository()
        analyticsRepository = FakeAnalyticsRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): CareRecipientViewModel {
        return CareRecipientViewModel(repository, analyticsRepository, fakeClock)
    }

    @Test
    fun `initial state when no care recipient exists`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("", state.name)
        assertNull(state.birthDate)
        assertEquals(Gender.UNSPECIFIED, state.gender)
        assertEquals("", state.memo)
    }

    @Test
    fun `loads existing care recipient`() = runTest {
        val existing = CareRecipient(
            id = 1L,
            name = "田中太郎",
            birthDate = LocalDate.of(1940, 5, 15),
            gender = Gender.MALE,
            memo = "アレルギー：花粉症",
            createdAt = LocalDateTime.of(2025, 1, 1, 0, 0),
            updatedAt = LocalDateTime.of(2025, 6, 1, 0, 0)
        )
        repository.setCareRecipient(existing)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("田中太郎", state.name)
        assertEquals(LocalDate.of(1940, 5, 15), state.birthDate)
        assertEquals(Gender.MALE, state.gender)
        assertEquals("アレルギー：花粉症", state.memo)
    }

    @Test
    fun `updateName changes name in state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateName("佐藤花子")
        assertEquals("佐藤花子", viewModel.uiState.value.name)
    }

    @Test
    fun `updateBirthDate changes birth date in state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val date = LocalDate.of(1935, 3, 10)
        viewModel.updateBirthDate(date)
        assertEquals(date, viewModel.uiState.value.birthDate)
    }

    @Test
    fun `updateGender changes gender in state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateGender(Gender.FEMALE)
        assertEquals(Gender.FEMALE, viewModel.uiState.value.gender)
    }

    @Test
    fun `updateMemo changes memo in state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateMemo("糖尿病あり")
        assertEquals("糖尿病あり", viewModel.uiState.value.memo)
    }

    @Test
    fun `save success shows snackbar`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateName("山田太郎")

        viewModel.snackbarController.events.test {
            viewModel.save()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.care_recipient_save_success,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }

        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `save failure shows error snackbar`() = runTest {
        repository.shouldFail = true
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateName("山田太郎")

        viewModel.snackbarController.events.test {
            viewModel.save()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.care_recipient_save_error,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }

        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `loads existing care recipient with extended fields`() = runTest {
        val existing = CareRecipient(
            id = 1L,
            name = "田中太郎",
            birthDate = LocalDate.of(1940, 5, 15),
            gender = Gender.MALE,
            nickname = "たろさん",
            careLevel = "要介護2",
            medicalHistory = "高血圧、糖尿病",
            allergies = "花粉症、ペニシリン",
            memo = "メモ",
            createdAt = LocalDateTime.of(2025, 1, 1, 0, 0),
            updatedAt = LocalDateTime.of(2025, 6, 1, 0, 0)
        )
        repository.setCareRecipient(existing)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("たろさん", state.nickname)
        assertEquals("要介護2", state.careLevel)
        assertEquals("高血圧、糖尿病", state.medicalHistory)
        assertEquals("花粉症、ペニシリン", state.allergies)
    }

    @Test
    fun `updateNickname changes nickname in state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateNickname("たろさん")
        assertEquals("たろさん", viewModel.uiState.value.nickname)
    }

    @Test
    fun `updateCareLevel changes careLevel in state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateCareLevel("要介護3")
        assertEquals("要介護3", viewModel.uiState.value.careLevel)
    }

    @Test
    fun `updateMedicalHistory changes medicalHistory in state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateMedicalHistory("高血圧")
        assertEquals("高血圧", viewModel.uiState.value.medicalHistory)
    }

    @Test
    fun `updateAllergies changes allergies in state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateAllergies("花粉症")
        assertEquals("花粉症", viewModel.uiState.value.allergies)
    }

    @Test
    fun `save includes extended fields`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateName("山田太郎")
        viewModel.updateNickname("たろう")
        viewModel.updateCareLevel("要介護1")
        viewModel.updateMedicalHistory("なし")
        viewModel.updateAllergies("卵アレルギー")

        viewModel.snackbarController.events.test {
            viewModel.save()
            advanceUntilIdle()
            awaitItem()
        }

        val saved = repository.getCareRecipient().first()
        assertNotNull(saved)
        assertEquals("たろう", saved!!.nickname)
        assertEquals("要介護1", saved.careLevel)
        assertEquals("なし", saved.medicalHistory)
        assertEquals("卵アレルギー", saved.allergies)
    }

    @Test
    fun `initial state has empty extended fields`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.nickname)
        assertEquals("", state.careLevel)
        assertEquals("", state.medicalHistory)
        assertEquals("", state.allergies)
    }

    @Test
    fun `save trims extended fields`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateName("山田太郎")
        viewModel.updateNickname("  たろう  ")
        viewModel.updateCareLevel("  要介護2  ")

        viewModel.snackbarController.events.test {
            viewModel.save()
            advanceUntilIdle()
            awaitItem()
        }

        val saved = repository.getCareRecipient().first()
        assertEquals("たろう", saved!!.nickname)
        assertEquals("要介護2", saved.careLevel)
    }
}
