package com.carenote.app.ui.screens.emergencycontact

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.model.EmergencyContact
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeEmergencyContactRepository
import com.carenote.app.testing.MainCoroutineRule
import com.carenote.app.testing.aEmergencyContact
import com.carenote.app.ui.util.SnackbarEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmergencyContactListViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(UnconfinedTestDispatcher())

    private lateinit var repository: FakeEmergencyContactRepository
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private lateinit var viewModel: EmergencyContactListViewModel

    @Before
    fun setUp() {
        repository = FakeEmergencyContactRepository()
        analyticsRepository = FakeAnalyticsRepository()
    }

    private fun createViewModel(): EmergencyContactListViewModel {
        return EmergencyContactListViewModel(repository, analyticsRepository)
    }

    @Test
    fun `contacts state initially empty`() = runTest {
        viewModel = createViewModel()
        assertEquals(emptyList<EmergencyContact>(), viewModel.contacts.value)
    }

    @Test
    fun `contacts state reflects repository data`() = runTest {
        val contactList = listOf(
            aEmergencyContact(id = 1L, name = "A"),
            aEmergencyContact(id = 2L, name = "B")
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
        val contact = aEmergencyContact(id = 1L, name = "削除対象")
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
        val contact = aEmergencyContact(1L)
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
        val contact = aEmergencyContact(1L)
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

            repository.setContacts(listOf(aEmergencyContact(id = 1L, name = "新規")))
            val updated = awaitItem()
            assertEquals(1, updated.size)
            assertEquals("新規", updated[0].name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteContact does not remove other contacts`() = runTest {
        val contacts = listOf(
            aEmergencyContact(id = 1L, name = "A"),
            aEmergencyContact(id = 2L, name = "B"),
            aEmergencyContact(id = 3L, name = "C")
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
            aEmergencyContact(id = 1L, name = "A"),
            aEmergencyContact(id = 2L, name = "B")
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
