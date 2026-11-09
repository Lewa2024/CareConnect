package com.strathmore.CareConnect.data.local.dao

import androidx.room.*
import com.strathmore.CareConnect.data.local.entities.Booking
import kotlinx.coroutines.flow.Flow

data class BookingWithNames(
    val bookingId: Long,
    val patientId: Long,
    val patientName: String,
    val nurseFirstName: String,
    val nurseLastName: String,
    val requestedAt: Long,
    val requestedVisitTime: Long,
    val notes: String,
    val status: String,
    val paymentAmountKsh: Double? = null,
    val paymentMpesaCode: String? = null,
    val paymentPaidAt: Long? = null
)

@Dao
interface BookingDao {
    @Insert
    suspend fun insert(booking: Booking): Long

    @Query("UPDATE bookings SET status = :status WHERE bookingId = :bookingId")
    suspend fun updateStatus(bookingId: Long, status: String)

     @Query("""
    SELECT b.bookingId AS bookingId, b.patientId AS patientId, p.name AS patientName,
           u.firstName AS nurseFirstName, u.lastName AS nurseLastName,
           b.requestedAt AS requestedAt, b.requestedVisitTime AS requestedVisitTime,
           b.notes AS notes, b.status AS status,
           pay.amountKsh AS paymentAmountKsh, pay.mpesaCode AS paymentMpesaCode, pay.paidAt AS paymentPaidAt
    FROM bookings b
    INNER JOIN patients p ON p.patientId = b.patientId
    INNER JOIN nurses n ON n.nurseId = b.nurseId
    INNER JOIN users u ON u.userId = n.userId
    LEFT JOIN payments pay ON pay.bookingId = b.bookingId
    WHERE b.patientId = :patientId
    ORDER BY b.requestedAt DESC
""")
fun getBookingsForPatient(patientId: Long): Flow<List<BookingWithNames>> 

   
    fun getBookingsForNurse(nurseId: Long): Flow<List<BookingWithNames>>
}