package com.strathmore.CareConnect.data.repository

import com.strathmore.CareConnect.data.local.dao.BookingDao
import com.strathmore.CareConnect.data.local.dao.BookingWithNames
import com.strathmore.CareConnect.data.local.entities.Booking
import kotlinx.coroutines.flow.Flow

class BookingRepository(
    private val bookingDao: BookingDao,
    private val postVisitNoteDao: com.strathmore.CareConnect.data.local.dao.PostVisitNoteDao
) {
    suspend fun requestBooking(
        patientId: Long,
        nurseId: Long,
        requestedVisitTime: Long,
        notes: String
    ): Long {
        val booking = Booking(
            patientId = patientId,
            nurseId = nurseId,
            requestedAt = System.currentTimeMillis(),
            requestedVisitTime = requestedVisitTime,
            notes = notes.trim()
        )
        return bookingDao.insert(booking)
    }

    fun getBookingsForPatient(patientId: Long): Flow<List<BookingWithNames>> =
        bookingDao.getBookingsForPatient(patientId)

    fun getBookingsForNurse(nurseId: Long): Flow<List<BookingWithNames>> =
        bookingDao.getBookingsForNurse(nurseId)

    suspend fun respondToBooking(bookingId: Long, accept: Boolean) {
        bookingDao.updateStatus(bookingId, if (accept) "ACCEPTED" else "DECLINED")
    }

    suspend fun markCompleted(bookingId: Long) {
        bookingDao.updateStatus(bookingId, "COMPLETED")
    }

        suspend fun hasAcceptedBooking(nurseId: Long, patientId: Long): Boolean =
        postVisitNoteDao.hasAcceptedBooking(nurseId, patientId) > 0

    suspend fun addPostVisitNote(bookingId: Long, patientId: Long, nurseId: Long, note: String) {
        postVisitNoteDao.insert(
            com.strathmore.CareConnect.data.local.entities.PostVisitNote(
                bookingId = bookingId,
                patientId = patientId,
                nurseId = nurseId,
                note = note.trim(),
                createdAt = System.currentTimeMillis()
            )
        )
    }

    fun getNotesForPatient(patientId: Long) = postVisitNoteDao.getNotesForPatient(patientId)
}