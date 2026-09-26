package com.strathmore.CareConnect.data.local

import androidx.room.TypeConverter
import java.util.Date

/** Room stores dates as epoch millis (Long); these converters translate to/from java.util.Date. */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}