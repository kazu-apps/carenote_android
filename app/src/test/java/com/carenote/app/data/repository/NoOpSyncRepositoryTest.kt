package com.carenote.app.data.repository

import app.cash.turbine.test
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.SyncResult
import com.carenote.app.domain.common.SyncState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NoOpSyncRepositoryTest {

    private lateinit var repository: NoOpSyncRepository

    @Before
    fun setup() {
        repository = NoOpSyncRepository()
    }

    @Test
    fun `syncState emits Idle`() = runTest {
        repository.syncState.test {
            assertTrue(awaitItem() is SyncState.Idle)
        }
    }

    @Test
    fun `syncAll returns Failure with NetworkError`() = runTest {
        val result = repository.syncAll("careRecipient1")
        assertTrue(result is SyncResult.Failure)
        assertTrue((result as SyncResult.Failure).error is DomainError.NetworkError)
    }

    @Test
    fun `syncMedications returns Failure`() = runTest {
        val result = repository.syncMedications("careRecipient1")
        assertTrue(result is SyncResult.Failure)
    }

    @Test
    fun `syncMedicationLogs returns Failure`() = runTest {
        val result = repository.syncMedicationLogs("careRecipient1", 1L)
        assertTrue(result is SyncResult.Failure)
    }

    @Test
    fun `syncNotes returns Failure`() = runTest {
        val result = repository.syncNotes("careRecipient1")
        assertTrue(result is SyncResult.Failure)
    }

    @Test
    fun `syncHealthRecords returns Failure`() = runTest {
        val result = repository.syncHealthRecords("careRecipient1")
        assertTrue(result is SyncResult.Failure)
    }

    @Test
    fun `syncCalendarEvents returns Failure`() = runTest {
        val result = repository.syncCalendarEvents("careRecipient1")
        assertTrue(result is SyncResult.Failure)
    }

    @Test
    fun `syncTasks returns Failure`() = runTest {
        val result = repository.syncTasks("careRecipient1")
        assertTrue(result is SyncResult.Failure)
    }

    @Test
    fun `getLastSyncTime returns null`() = runTest {
        assertNull(repository.getLastSyncTime())
    }

    @Test
    fun `pushLocalChanges returns Failure`() = runTest {
        val result = repository.pushLocalChanges("careRecipient1")
        assertTrue(result is SyncResult.Failure)
    }

    @Test
    fun `pullRemoteChanges returns Failure`() = runTest {
        val result = repository.pullRemoteChanges("careRecipient1")
        assertTrue(result is SyncResult.Failure)
    }
}
