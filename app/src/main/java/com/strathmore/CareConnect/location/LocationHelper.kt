package com.strathmore.CareConnect.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
    val city: String
)

/**
 * Wraps FusedLocationProviderClient (Google Play Services, free, no API key) to fetch
 * the device's current coordinates, then uses Android's built-in Geocoder to resolve
 * those coordinates into a human-readable city name for display.
 */
object LocationHelper {

    @SuppressLint("MissingPermission") // caller is required to have requested the permission first
    suspend fun getCurrentLocation(context: Context): DeviceLocation? = suspendCancellableCoroutine { cont ->
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location == null) {
                    cont.resume(null)
                    return@addOnSuccessListener
                }
                val city = resolveCity(context, location.latitude, location.longitude)
                cont.resume(DeviceLocation(location.latitude, location.longitude, city))
            }
            .addOnFailureListener {
                cont.resume(null)
            }
    }

    private fun resolveCity(context: Context, latitude: Double, longitude: Double): String {
        return try {
            @Suppress("DEPRECATION")
            val addresses = Geocoder(context, Locale.getDefault()).getFromLocation(latitude, longitude, 1)
            addresses?.firstOrNull()?.locality
                ?: addresses?.firstOrNull()?.subAdminArea
                ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}