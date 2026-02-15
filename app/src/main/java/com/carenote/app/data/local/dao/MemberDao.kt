package com.carenote.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.carenote.app.data.local.entity.MemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {

    @Query("SELECT * FROM members WHERE care_recipient_id = :careRecipientId ORDER BY joined_at ASC")
    fun getAllMembers(careRecipientId: Long): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :id")
    fun getMemberById(id: Long): Flow<MemberEntity?>

    @Query("SELECT * FROM members WHERE care_recipient_id = :careRecipientId AND uid = :uid")
    fun getMemberByUid(careRecipientId: Long, uid: String): Flow<MemberEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity): Long

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Query("DELETE FROM members WHERE id = :id")
    suspend fun deleteMember(id: Long)

    @Query("SELECT * FROM members WHERE joined_at > :lastSyncTime")
    suspend fun getModifiedSince(lastSyncTime: String): List<MemberEntity>
}
