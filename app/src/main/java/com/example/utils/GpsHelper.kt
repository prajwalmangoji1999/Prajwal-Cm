package com.example.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class GpsLocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val accuracy: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

data class WaypointPoint(
    val latitude: Double,
    val longitude: Double,
    val label: String = ""
)

object GpsHelper {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): GpsLocationData {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val location: Location? = try {
            suspendCancellableCoroutine { continuation ->
                val cts = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).addOnSuccessListener { loc ->
                    continuation.resume(loc)
                }.addOnFailureListener {
                    // Try last location as fallback
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        continuation.resume(lastLoc)
                    }.addOnFailureListener {
                        continuation.resume(null)
                    }
                }

                continuation.invokeOnCancellation {
                    cts.cancel()
                }
            }
        } catch (e: Exception) {
            null
        }

        val lat = location?.latitude ?: 37.7749
        val lng = location?.longitude ?: -122.4194
        val acc = location?.accuracy ?: 10f

        val address = getAddressFromCoordinates(context, lat, lng)

        return GpsLocationData(
            latitude = lat,
            longitude = lng,
            address = address,
            accuracy = acc
        )
    }

    private fun getAddressFromCoordinates(context: Context, lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val street = addr.thoroughfare ?: addr.subLocality ?: ""
                val locality = addr.locality ?: addr.adminArea ?: ""
                val country = addr.countryName ?: ""
                listOf(street, locality, country).filter { it.isNotBlank() }.joinToString(", ")
                    .ifBlank { "GPS: ${String.format(Locale.US, "%.4f, %.4f", lat, lng)}" }
            } else {
                "GPS: ${String.format(Locale.US, "%.4f, %.4f", lat, lng)}"
            }
        } catch (e: Exception) {
            "GPS: ${String.format(Locale.US, "%.4f, %.4f", lat, lng)}"
        }
    }

    /**
     * Calculates distance between two coordinates in Kilometers using Haversine formula
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    /**
     * Parses semicolon-separated coordinates "lat,lng;lat,lng" into list of WaypointPoints
     */
    fun parseWaypoints(waypointsStr: String): List<WaypointPoint> {
        if (waypointsStr.isBlank()) return emptyList()
        return waypointsStr.split(";").mapNotNull { part ->
            val coords = part.split(",")
            if (coords.size >= 2) {
                val lat = coords[0].toDoubleOrNull()
                val lng = coords[1].toDoubleOrNull()
                if (lat != null && lng != null) {
                    WaypointPoint(lat, lng)
                } else null
            } else null
        }
    }
}
