package com.strathmore.CareConnect.data.local.dao

import androidx.room.*
import com.strathmore.CareConnect.data.local.entities.PostVisitNote
import kotlinx.coroutines.flow.Flow

data class PostVisitNoteWithNurse(
    val noteId: Long,
    val note: String,
    val createdAt: Long,
    val nurseFirstName: String,
    val nurseLastName: String
)

@Dao
interface PostVisitNoteDao {
    @Insert
    suspend fun insert(note: PostVisitNote): Long

    @Query("""
        SELECT pvn.noteId AS noteId, pvn.note AS note, pvn.createdAt AS createdAt,
               u.firstName AS nurseFirstName, u.lastName AS nurseLastName
        FROM post_visit_notes pvn
        INNER JOIN nurses n ON n.nurseId = pvn.nurseId
        INNER JOIN users u ON u.userId = n.userId
        WHERE pvn.patientId = :patientId
        ORDER BY pvn.createdAt DESC
    """)
    fun getNotesForPatient(patientId: Long): Flow<List<PostVisitNoteWithNurse>>

    @Query("SELECT COUNT(*) FROM bookings WHERE nurseId = :nurseId AND patientId = :patientId AND status = 'ACCEPTED'")
    suspend fun hasAcceptedBooking(nurseId: Long, patientId: Long): Int
}