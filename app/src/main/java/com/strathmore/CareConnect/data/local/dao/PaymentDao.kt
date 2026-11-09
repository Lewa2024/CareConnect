package com.strathmore.CareConnect.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.strathmore.CareConnect.data.local.entities.Payment

@Dao
interface PaymentDao {
    @Insert
    suspend fun insert(payment: Payment): Long

    @Query("SELECT * FROM payments WHERE bookingId = :bookingId LIMIT 1")
    suspend fun getByBookingId(bookingId: Long): Payment?
}