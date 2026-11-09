package com.strathmore.CareConnect.data.repository

import com.strathmore.CareConnect.data.local.dao.NurseDao
import com.strathmore.CareConnect.data.local.dao.NurseWithLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import com.strathmore.CareConnect.data.repository.NurseWithDistance
import kotlin.math.sqrt

data class NurseWithDistance(
    val nurse: NurseWithLocation,
    val distanceKm: Double
)

class NurseRepository(
    private val nurseDao: NurseDao
) {
    fun getAvailableNursesSortedByDistance(fromLat: Double, fromLng: Double): Flow<List<NurseWithDistance>> {
        return nurseDao.getAvailableNursesWithLocation().map { nurses ->
            nurses
                .map { NurseWithDistance(it, haversineKm(fromLat, fromLng, it.latitude, it.longitude)) }
                .sortedBy { it.distanceKm }
        }
    }

    suspend fun setAvailable(nurseId: Long, available: Boolean) {
        nurseDao.setAvailable(nurseId, available)
    }

    suspend fun getNurse(nurseId: Long) = nurseDao.getById(nurseId)

    /**
     * Straight-line distance between two GPS coordinates, in kilometers, using the
     * Haversine formula — the standard way to calculate distance on a sphere given
     * latitude/longitude, without needing a paid mapping API.
     */
    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}