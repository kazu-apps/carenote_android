package com.carenote.app.ui.screens.emergencycontact

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.model.EmergencyContact
import com.carenote.app.domain.model.RelationshipType
import com.carenote.app.fakes.FakeEmergencyContactRepository
import com.carenote.app.ui.util.SnackbarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditEmergencyContactViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeEmergencyContactRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeEmergencyContactRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        contactId: Long? = null
    ): AddEditEmergencyContactViewModel {
        val savedStateHandle = SavedStateHandle().apply {
            if (contactId != null) {
                set("contactId", contactId)
            }
        }
        return AddEditEmergencyContactViewModel(savedStateHandle, repository)
    }

    private fun createContact(
        id: Long = 1L,
        name: String = "テスト太郎",
        phoneNumber: String = "090-1234-5678",
        relationship: RelationshipType = RelationshipType.FAMILY,
        memo: String = ""
    ) = EmergencyContact(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        relationship = relationship,
        memo = memo,
        createdAt = LocalDateTime.of(2026, 1, 1, 10, 0),
        updatedAt = LocalDateTime.of(2026, 1, 1, 10, 0)
    )

    @Test
    fun `initial state is add mode with empty fields`() {
        val viewModel = createViewModel()
        val state = viewModel.formState.value

        assertFalse(state.isEditMode)
        assertEquals("", state.name)
        assertEquals("", state.phoneNumber)
        assertEquals(RelationshipType.FAMILY, state.relationship)
        assertEquals("", state.memo)
        assertNull(state.nameError)
        assertNull(state.phoneNumberError)
        assertFalse(state.isSaving)
    }

    @Test
    fun `edit mode loads existing contact`() = runTest {
        val contact = createContact(1L, "既存太郎", "080-9999-0000", RelationshipType.DOCTOR, "メモ")
        repository.setContacts(listOf(contact))

        val viewModel = createViewModel(contactId = 1L)

        viewModel.formState.test {
            val state = awaitItem()
            assertTrue(state.isEditMode)
            assertEquals("既存太郎", state.name)
            assertEquals("080-9999-0000", state.phoneNumber)
            assertEquals(RelationshipType.DOCTOR, state.relationship)
            assertEquals("メモ", state.memo)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateName updates form state`() {
        val viewModel = createViewModel()
        viewModel.updateName("新しい名前")
        assertEquals("新しい名前", viewModel.formState.value.name)
    }

    @Test
    fun `updateName clears name error`() {
        val viewModel = createViewModel()
        viewModel.save() // triggers error
        assertNotNull(viewModel.formState.value.nameError)

        viewModel.updateName("A")
        assertNull(viewModel.formState.value.nameError)
    }

    @Test
    fun `updatePhoneNumber updates form state`() {
        val viewModel = createViewModel()
        viewModel.updatePhoneNumber("090-0000-0000")
        assertEquals("090-0000-0000", viewModel.formState.value.phoneNumber)
    }

    @Test
    fun `updatePhoneNumber clears phone error`() {
        val viewModel = createViewModel()
        viewModel.updateName("名前")
        viewModel.save() // triggers phone error
        assertNotNull(viewModel.formState.value.phoneNumberError)

        viewModel.updatePhoneNumber("090")
        assertNull(viewModel.formState.value.phoneNumberError)
    }

    @Test
    fun `updateRelationship updates form state`() {
        val viewModel = createViewModel()
        viewModel.updateRelationship(RelationshipType.HOSPITAL)
        assertEquals(RelationshipType.HOSPITAL, viewModel.formState.value.relationship)
    }

    @Test
    fun `updateMemo updates form state`() {
        val viewModel = createViewModel()
        viewModel.updateMemo("テストメモ")
        assertEquals("テストメモ", viewModel.formState.value.memo)
    }

    @Test
    fun `save with blank name shows error`() {
        val viewModel = createViewModel()
        viewModel.updatePhoneNumber("090-0000-0000")
        viewModel.save()

        assertNotNull(viewModel.formState.value.nameError)
        assertFalse(viewModel.formState.value.isSaving)
    }

    @Test
    fun `save with blank phone shows error`() {
        val viewModel = createViewModel()
        viewModel.updateName("テスト")
        viewModel.save()

        assertNotNull(viewModel.formState.value.phoneNumberError)
        assertFalse(viewModel.formState.value.isSaving)
    }

    @Test
    fun `save with name too long shows error`() {
        val viewModel = createViewModel()
        viewModel.updateName("A".repeat(101))
        viewModel.updatePhoneNumber("090-0000-0000")
        viewModel.save()

        assertNotNull(viewModel.formState.value.nameError)
    }

    @Test
    fun `save with phone too long shows error`() {
        val viewModel = createViewModel()
        viewModel.updateName("テスト")
        viewModel.updatePhoneNumber("0".repeat(21))
        viewModel.save()

        assertNotNull(viewModel.formState.value.phoneNumberError)
    }

    @Test
    fun `save success emits savedEvent for new contact`() = runTest {
        val viewModel = createViewModel()
        viewModel.updateName("新規連絡先")
        viewModel.updatePhoneNumber("090-1111-2222")

        viewModel.savedEvent.test {
            viewModel.save()
            val result = awaitItem()
            assertTrue(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save success emits savedEvent for edit`() = runTest {
        val contact = createContact(1L, "既存", "090-0000-0000")
        repository.setContacts(listOf(contact))

        val viewModel = createViewModel(contactId = 1L)
        viewModel.updateName("更新後")

        viewModel.savedEvent.test {
            viewModel.save()
            val result = awaitItem()
            assertTrue(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save failure shows error snackbar`() = runTest {
        repository.shouldFail = true
        val viewModel = createViewModel()
        viewModel.updateName("テスト")
        viewModel.updatePhoneNumber("090-0000-0000")

        viewModel.snackbarController.events.test {
            viewModel.save()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.emergency_contact_save_failed, (event as SnackbarEvent.WithResId).messageResId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save failure resets isSaving`() = runTest {
        repository.shouldFail = true
        val viewModel = createViewModel()
        viewModel.updateName("テスト")
        viewModel.updatePhoneNumber("090-0000-0000")
        viewModel.save()

        assertFalse(viewModel.formState.value.isSaving)
    }

    @Test
    fun `isDirty is false initially`() {
        val viewModel = createViewModel()
        assertFalse(viewModel.isDirty)
    }

    @Test
    fun `isDirty is true after name change`() {
        val viewModel = createViewModel()
        viewModel.updateName("変更")
        assertTrue(viewModel.isDirty)
    }

    @Test
    fun `isDirty is true after phone change`() {
        val viewModel = createViewModel()
        viewModel.updatePhoneNumber("090-0000-0000")
        assertTrue(viewModel.isDirty)
    }

    @Test
    fun `isDirty is true after relationship change`() {
        val viewModel = createViewModel()
        viewModel.updateRelationship(RelationshipType.DOCTOR)
        assertTrue(viewModel.isDirty)
    }

    @Test
    fun `isDirty is true after memo change`() {
        val viewModel = createViewModel()
        viewModel.updateMemo("メモ")
        assertTrue(viewModel.isDirty)
    }

    @Test
    fun `isDirty is false after reverting to initial state`() {
        val viewModel = createViewModel()
        viewModel.updateName("変更")
        viewModel.updateName("")
        assertFalse(viewModel.isDirty)
    }

    @Test
    fun `save trims whitespace from fields`() = runTest {
        val viewModel = createViewModel()
        viewModel.updateName("  テスト  ")
        viewModel.updatePhoneNumber("  090-1234  ")
        viewModel.updateMemo("  メモ  ")

        viewModel.savedEvent.test {
            viewModel.save()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        // Verify trimmed data was saved
        val saved = repository.getAllContacts()
        saved.test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("テスト", list[0].name)
            assertEquals("090-1234", list[0].phoneNumber)
            assertEquals("メモ", list[0].memo)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
