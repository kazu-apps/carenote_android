package com.carenote.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.carenote.app.data.local.entity.InvitationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvitationDao {

    @Query("SELECT * FROM invitations WHERE care_recipient_id = :careRecipientId ORDER BY created_at DESC")
    fun getAllInvitations(careRecipientId: Long): Flow<List<InvitationEntity>>

    @Query("SELECT * FROM invitations WHERE id = :id")
    fun getInvitationById(id: Long): Flow<InvitationEntity?>

    @Query("SELECT * FROM invitations WHERE token = :token")
    fun getInvitationByToken(token: String): Flow<InvitationEntity?>

    @Query("SELECT * FROM invitations WHERE invitee_email = :email AND status = :status ORDER BY created_at DESC")
    fun getInvitationsByEmail(email: String, status: String): Flow<List<InvitationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvitation(invitation: InvitationEntity): Long

    @Update
    suspend fun updateInvitation(invitation: InvitationEntity)

    @Query("DELETE FROM invitations WHERE id = :id")
    suspend fun deleteInvitation(id: Long)

    @Query("SELECT * FROM invitations WHERE created_at > :lastSyncTime")
    suspend fun getModifiedSince(lastSyncTime: String): List<InvitationEntity>
}
