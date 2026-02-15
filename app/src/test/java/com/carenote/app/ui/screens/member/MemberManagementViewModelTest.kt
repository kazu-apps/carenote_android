package com.carenote.app.ui.screens.member

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.model.Invitation
import com.carenote.app.domain.model.InvitationStatus
import com.carenote.app.domain.model.Member
import com.carenote.app.domain.model.MemberRole
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeAuthRepository
import com.carenote.app.fakes.FakeInvitationRepository
import com.carenote.app.fakes.FakeMemberRepository
import com.carenote.app.testing.MainCoroutineRule
import com.carenote.app.testing.aInvitation
import com.carenote.app.testing.aMember
import com.carenote.app.testing.aUser
import com.carenote.app.ui.util.SnackbarEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MemberManagementViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(UnconfinedTestDispatcher())

    private lateinit var memberRepository: FakeMemberRepository
    private lateinit var invitationRepository: FakeInvitationRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private lateinit var viewModel: MemberManagementViewModel

    @Before
    fun setUp() {
        memberRepository = FakeMemberRepository()
        invitationRepository = FakeInvitationRepository()
        authRepository = FakeAuthRepository()
        analyticsRepository = FakeAnalyticsRepository()
    }

    private fun createViewModel(): MemberManagementViewModel {
        return MemberManagementViewModel(
            memberRepository,
            invitationRepository,
            authRepository,
            analyticsRepository
        )
    }

    @Test
    fun `members state initially empty`() = runTest {
        viewModel = createViewModel()
        assertEquals(emptyList<Member>(), viewModel.members.value)
    }

    @Test
    fun `members state reflects repository data`() = runTest {
        val memberList = listOf(
            aMember(id = 1L, uid = "user1", role = MemberRole.OWNER),
            aMember(id = 2L, uid = "user2", role = MemberRole.MEMBER)
        )
        memberRepository.setMembers(memberList)
        viewModel = createViewModel()

        viewModel.members.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("user1", result[0].uid)
            assertEquals("user2", result[1].uid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pendingInvitations filters only PENDING`() = runTest {
        val invitations = listOf(
            aInvitation(id = 1L, status = InvitationStatus.PENDING, inviteeEmail = "a@example.com"),
            aInvitation(id = 2L, status = InvitationStatus.ACCEPTED, inviteeEmail = "b@example.com"),
            aInvitation(id = 3L, status = InvitationStatus.PENDING, inviteeEmail = "c@example.com"),
            aInvitation(id = 4L, status = InvitationStatus.REJECTED, inviteeEmail = "d@example.com")
        )
        invitationRepository.setInvitations(invitations)
        viewModel = createViewModel()

        viewModel.pendingInvitations.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertTrue(result.all { it.status == InvitationStatus.PENDING })
            assertEquals("a@example.com", result[0].inviteeEmail)
            assertEquals("c@example.com", result[1].inviteeEmail)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isOwner true when current user is OWNER`() = runTest {
        val user = aUser(uid = "ownerUid")
        authRepository.setCurrentUser(user)
        memberRepository.setMembers(
            listOf(aMember(id = 1L, uid = "ownerUid", role = MemberRole.OWNER))
        )
        viewModel = createViewModel()

        viewModel.isOwner.test {
            val result = awaitItem()
            assertTrue(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isOwner false when current user is MEMBER`() = runTest {
        val user = aUser(uid = "memberUid")
        authRepository.setCurrentUser(user)
        memberRepository.setMembers(
            listOf(aMember(id = 1L, uid = "memberUid", role = MemberRole.MEMBER))
        )
        viewModel = createViewModel()

        viewModel.isOwner.test {
            val result = awaitItem()
            assertFalse(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteMember removes member from list`() = runTest {
        val user = aUser(uid = "ownerUid")
        authRepository.setCurrentUser(user)
        val members = listOf(
            aMember(id = 1L, uid = "ownerUid", role = MemberRole.OWNER),
            aMember(id = 2L, uid = "targetUid", role = MemberRole.MEMBER)
        )
        memberRepository.setMembers(members)
        viewModel = createViewModel()

        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.members.collect {} }
        viewModel.members.first { it.isNotEmpty() }

        viewModel.deleteMember(2L)

        val result = viewModel.members.value
        assertEquals(1, result.size)
        assertEquals("ownerUid", result[0].uid)
        collectJob.cancel()
    }

    @Test
    fun `deleteMember shows success snackbar`() = runTest {
        val user = aUser(uid = "ownerUid")
        authRepository.setCurrentUser(user)
        memberRepository.setMembers(
            listOf(
                aMember(id = 1L, uid = "ownerUid", role = MemberRole.OWNER),
                aMember(id = 2L, uid = "targetUid", role = MemberRole.MEMBER)
            )
        )
        viewModel = createViewModel()

        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.members.collect {} }
        viewModel.members.first { it.isNotEmpty() }

        viewModel.snackbarController.events.test {
            viewModel.deleteMember(2L)
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.member_deleted, (event as SnackbarEvent.WithResId).messageResId)
            cancelAndIgnoreRemainingEvents()
        }
        collectJob.cancel()
    }

    @Test
    fun `deleteMember shows error snackbar on failure`() = runTest {
        val user = aUser(uid = "ownerUid")
        authRepository.setCurrentUser(user)
        memberRepository.setMembers(
            listOf(
                aMember(id = 1L, uid = "ownerUid", role = MemberRole.OWNER),
                aMember(id = 2L, uid = "targetUid", role = MemberRole.MEMBER)
            )
        )
        viewModel = createViewModel()

        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.members.collect {} }
        viewModel.members.first { it.isNotEmpty() }
        memberRepository.shouldFail = true

        viewModel.snackbarController.events.test {
            viewModel.deleteMember(2L)
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.member_delete_failed, (event as SnackbarEvent.WithResId).messageResId)
            cancelAndIgnoreRemainingEvents()
        }
        collectJob.cancel()
    }

    @Test
    fun `cancelInvitation removes invitation`() = runTest {
        val user = aUser(uid = "ownerUid")
        authRepository.setCurrentUser(user)
        memberRepository.setMembers(
            listOf(aMember(id = 1L, uid = "ownerUid", role = MemberRole.OWNER))
        )
        val invitation = aInvitation(id = 1L, status = InvitationStatus.PENDING)
        invitationRepository.setInvitations(listOf(invitation))
        viewModel = createViewModel()

        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.members.collect {} }
        viewModel.members.first { it.isNotEmpty() }

        viewModel.cancelInvitation(1L)

        viewModel.pendingInvitations.test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
        collectJob.cancel()
    }

    @Test
    fun `cancelInvitation shows error on failure`() = runTest {
        val user = aUser(uid = "ownerUid")
        authRepository.setCurrentUser(user)
        memberRepository.setMembers(
            listOf(aMember(id = 1L, uid = "ownerUid", role = MemberRole.OWNER))
        )
        val invitation = aInvitation(id = 1L, status = InvitationStatus.PENDING)
        invitationRepository.setInvitations(listOf(invitation))
        invitationRepository.shouldFail = true
        viewModel = createViewModel()

        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.members.collect {} }
        viewModel.members.first { it.isNotEmpty() }

        viewModel.snackbarController.events.test {
            viewModel.cancelInvitation(1L)
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.invitation_cancel_failed, (event as SnackbarEvent.WithResId).messageResId)
            cancelAndIgnoreRemainingEvents()
        }
        collectJob.cancel()
    }

    @Test
    fun `cancelInvitation shows permission error for non-owner`() = runTest {
        val user = aUser(uid = "memberUid")
        authRepository.setCurrentUser(user)
        memberRepository.setMembers(
            listOf(aMember(id = 1L, uid = "memberUid", role = MemberRole.MEMBER))
        )
        val invitation = aInvitation(id = 1L, status = InvitationStatus.PENDING)
        invitationRepository.setInvitations(listOf(invitation))
        viewModel = createViewModel()

        viewModel.snackbarController.events.test {
            viewModel.cancelInvitation(1L)
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.invitation_cancel_permission_error, (event as SnackbarEvent.WithResId).messageResId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cancelInvitation succeeds for owner`() = runTest {
        val user = aUser(uid = "ownerUid")
        authRepository.setCurrentUser(user)
        memberRepository.setMembers(
            listOf(aMember(id = 1L, uid = "ownerUid", role = MemberRole.OWNER))
        )
        val invitation = aInvitation(id = 1L, status = InvitationStatus.PENDING)
        invitationRepository.setInvitations(listOf(invitation))
        viewModel = createViewModel()

        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.members.collect {} }
        viewModel.members.first { it.isNotEmpty() }

        viewModel.snackbarController.events.test {
            viewModel.cancelInvitation(1L)
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.invitation_cancelled, (event as SnackbarEvent.WithResId).messageResId)
            cancelAndIgnoreRemainingEvents()
        }
        collectJob.cancel()
    }
}
