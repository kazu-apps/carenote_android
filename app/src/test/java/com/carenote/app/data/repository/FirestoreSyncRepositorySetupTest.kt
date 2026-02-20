package com.carenote.app.data.repository

import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.CareRecipient
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * FirestoreSyncRepositoryImpl.setupInitialCareRecipient のユニットテスト
 */
class FirestoreSyncRepositorySetupTest {

    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockCareRecipientsCollection: CollectionReference
    private lateinit var mockMembersCollection: CollectionReference
    private lateinit var mockCareRecipientDoc: DocumentReference
    private lateinit var mockMemberDoc: DocumentReference
    private lateinit var repository: FirestoreSyncRepositoryImpl

    @Before
    fun setUp() {
        mockFirestore = mockk(relaxed = true)
        mockCareRecipientsCollection = mockk(relaxed = true)
        mockMembersCollection = mockk(relaxed = true)
        mockCareRecipientDoc = mockk(relaxed = true)
        mockMemberDoc = mockk(relaxed = true)

        every { mockFirestore.collection("careRecipients") } returns mockCareRecipientsCollection
        every { mockFirestore.collection("careRecipientMembers") } returns mockMembersCollection
        every { mockCareRecipientsCollection.document() } returns mockCareRecipientDoc
        every { mockCareRecipientDoc.id } returns "generated-cr-id"
        every { mockCareRecipientsCollection.document("generated-cr-id") } returns mockCareRecipientDoc
        every { mockCareRecipientDoc.set(any()) } returns Tasks.forResult(null)
        every { mockMembersCollection.document(any()) } returns mockMemberDoc
        every { mockMemberDoc.set(any()) } returns Tasks.forResult(null)

        val lazyFirestore = dagger.Lazy { mockFirestore }

        repository = FirestoreSyncRepositoryImpl(
            firestore = lazyFirestore,
            settingsDataSource = mockk(relaxed = true),
            syncMappingDao = mockk(relaxed = true),
            medicationSyncer = mockk(relaxed = true),
            medicationLogSyncer = mockk(relaxed = true),
            noteSyncer = mockk(relaxed = true),
            healthRecordSyncer = mockk(relaxed = true),
            calendarEventSyncer = mockk(relaxed = true),
            noteCommentSyncer = mockk(relaxed = true)
        )
    }

    @Test
    fun `setupInitialCareRecipient creates membership doc then careRecipient doc`() = runTest {
        val careRecipient = CareRecipient(id = 1L, name = "Test Care Recipient")

        repository.setupInitialCareRecipient("test-user-id", careRecipient)

        verifyOrder {
            mockMembersCollection.document("test-user-id_generated-cr-id")
            mockMemberDoc.set(match<Map<String, Any>> {
                it["userId"] == "test-user-id" &&
                    it["careRecipientId"] == "generated-cr-id" &&
                    it["invitedBy"] == "test-user-id" &&
                    it["role"] == "owner"
            })
            mockCareRecipientsCollection.document("generated-cr-id")
            mockCareRecipientDoc.set(match<Map<String, Any>> {
                it["name"] == "Test Care Recipient" &&
                    it.containsKey("createdAt") &&
                    it.containsKey("updatedAt")
            })
        }
    }

    @Test
    fun `setupInitialCareRecipient returns careRecipientId on success`() = runTest {
        val careRecipient = CareRecipient(id = 1L, name = "Test")

        val result = repository.setupInitialCareRecipient("user-id", careRecipient)

        assertTrue(result is Result.Success)
        assertEquals("generated-cr-id", (result as Result.Success).value)
    }

    @Test
    fun `setupInitialCareRecipient returns NetworkError on exception`() = runTest {
        every { mockMemberDoc.set(any()) } returns Tasks.forException(RuntimeException("Firestore error"))

        val careRecipient = CareRecipient(id = 1L, name = "Test")

        val result = repository.setupInitialCareRecipient("user-id", careRecipient)

        assertTrue(result is Result.Failure)
        val error = (result as Result.Failure).error
        assertTrue(error is DomainError.NetworkError)
    }
}
