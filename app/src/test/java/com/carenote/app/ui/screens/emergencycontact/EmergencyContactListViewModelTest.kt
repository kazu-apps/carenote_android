package com.carenote.app.ui.screens.emergencycontact

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class EmergencyContactListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeEmergencyContactRepository
    private lateinit var viewModel: EmergencyContactListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeEmergencyContactRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): EmergencyContactListViewModel {
        return EmergencyContactListViewModel(repository)
    }

    private fun createContact(
        id: Long = 1L,
        name: String = "テスト太郎",
        phoneNumber: String = "090-1234-5678",
        relationship: RelationshipType = RelationshipType.FAMILY
    ) = EmergencyContact(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        relationship = relationship,
        memo = "",
        createdAt = LocalDateTime.of(2026, 1, 1, 10, 0),
        updatedAt = LocalDateTime.of(2026, 1, 1, 10, 0)
    )

    @Test
    fun `contacts state initially empty`() = runTest {
        viewModel = createViewModel()
        assertEquals(emptyList<EmergencyContact>(), viewModel.contacts.value)
    }

    @Test
    fun `contacts state reflects repository data`() = runTest {
        val contactList = listOf(
            createContact(1L, "A"),
            createContact(2L, "B")
        )
        repository.setContacts(contactList)
        viewModel = createViewModel()

        viewModel.contacts.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("A", result[0].name)
            assertEquals("B", result[1].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteContact removes contact from list`() = runTest {
        val contact = createContact(1L, "削除対象")
        repository.setContacts(listOf(contact))
        viewModel = createViewModel()

        viewModel.deleteContact(1L)

        viewModel.contacts.test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteContact shows success snackbar`() = runTest {
        val contact = createContact(1L)
        repository.setContacts(listOf(contact))
        viewModel = createViewModel()

        viewModel.snackbarController.events.test {
            viewModel.deleteContact(1L)
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.emergency_contact_deleted, (event as SnackbarEvent.WithResId).messageResId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteContact shows error snackbar on failure`() = runTest {
        val contact = createContact(1L)
        repository.setContacts(listOf(contact))
        repository.shouldFail = true
        viewModel = createViewModel()

        viewModel.snackbarController.events.test {
            viewModel.deleteContact(1L)
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.emergency_contact_delete_failed, (event as SnackbarEvent.WithResId).messageResId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `contacts updates when repository changes`() = runTest {
        viewModel = createViewModel()

        viewModel.contacts.test {
            assertEquals(emptyList<EmergencyContact>(), awaitItem())

            repository.setContacts(listOf(createContact(1L, "新規")))
            val updated = awaitItem()
            assertEquals(1, updated.size)
            assertEquals("新規", updated[0].name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteContact does not remove other contacts`() = runTest {
        val contacts = listOf(
            createContact(1L, "A"),
            createContact(2L, "B"),
            createContact(3L, "C")
        )
        repository.setContacts(contacts)
        viewModel = createViewModel()

        viewModel.deleteContact(2L)

        viewModel.contacts.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("A", result[0].name)
            assertEquals("C", result[1].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `multiple deletes work correctly`() = runTest {
        val contacts = listOf(
            createContact(1L, "A"),
            createContact(2L, "B")
        )
        repository.setContacts(contacts)
        viewModel = createViewModel()

        viewModel.deleteContact(1L)
        viewModel.deleteContact(2L)

        viewModel.contacts.test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
