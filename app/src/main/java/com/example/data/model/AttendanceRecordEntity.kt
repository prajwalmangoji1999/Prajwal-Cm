package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    indices = [
        Index(value = ["empId", "dateString"], unique = true),
        Index(value = ["monthYear"]),
        Index(value = ["createdAt"])
    ]
)
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val empId: String,
    val employeeName: String,
    val dateString: String,      // YYYY-MM-DD
    val monthYear: String,       // "2026-09" or "September 2026"
    val loginTime: Long,         // epoch millis
    val logoutTime: Long? = null,
    val loginPhotoPath: String = "",
    val logoutPhotoPath: String? = null,
    val loginLat: Double = 0.0,
    val loginLng: Double = 0.0,
    val loginAddress: String = "",
    val logoutLat: Double? = null,
    val logoutLng: Double? = null,
    val logoutAddress: String? = null,
    val netMinutes: Long = 0L,
    val status: String = STATUS_CLOCKED_IN, // Present, Half Day, Absent, Clocked In
    val createdAt: Long = System.currentTimeMillis(),

    // Distance & Travel Route Tracking
    val travelDistanceKm: Double = 0.0,
    val routeWaypoints: String = "" // Semicolon-separated "lat,lng;lat,lng"
) {
    val isClockedOut: Boolean get() = logoutTime != null

    val isPaidDay: Boolean
        get() = status == STATUS_PRESENT ||
                status == STATUS_PAID_LEAVE ||
                status == STATUS_SICK_LEAVE ||
                status == STATUS_COMP_OFF ||
                status == STATUS_HALF_DAY

    val statusCode: String get() = when (status) {
        STATUS_PRESENT -> CODE_PRESENT
        STATUS_HALF_DAY -> CODE_HALF_DAY
        STATUS_ABSENT -> CODE_ABSENT
        STATUS_PAID_LEAVE -> CODE_PAID_LEAVE
        STATUS_SICK_LEAVE -> CODE_SICK_LEAVE
        STATUS_COMP_OFF -> CODE_COMP_OFF
        else -> "--"
    }

    companion object {
        const val STATUS_PRESENT = "Present"
        const val STATUS_HALF_DAY = "Half Day"
        const val STATUS_ABSENT = "Absent"
        const val STATUS_CLOCKED_IN = "Clocked In"
        const val STATUS_PAID_LEAVE = "Paid Leave (PL)"
        const val STATUS_SICK_LEAVE = "Sick Leave (SL)"
        const val STATUS_COMP_OFF = "Comp Off (CO)"

        // Codes requested: 1. Present -> "P", 2. Absent -> "A", 3. Half Day -> "HD", Paid Leave -> "PL", Sick Leave -> "SL", Comp Off -> "CO"
        const val CODE_PRESENT = "P"
        const val CODE_HALF_DAY = "HD"
        const val CODE_ABSENT = "A"
        const val CODE_PAID_LEAVE = "PL"
        const val CODE_SICK_LEAVE = "SL"
        const val CODE_COMP_OFF = "CO"

        // Thresholds in minutes
        // 8:45 Hours = 8 * 60 + 45 = 525 minutes
        const val MIN_MINUTES_PRESENT = 525L
        // 4:15 Hours = 4 * 60 + 15 = 255 minutes
        const val MIN_MINUTES_HALF_DAY = 255L

        /**
         * Calculate attendance status strictly according to requirement:
         * min 8:45 Hours -> Present
         * min 4:15 Hours -> Half Day
         * else -> Absent
         */
        fun calculateAttendanceStatus(loginTimeMillis: Long, logoutTimeMillis: Long): Pair<Long, String> {
            val durationMillis = (logoutTimeMillis - loginTimeMillis).coerceAtLeast(0L)
            val netMinutes = durationMillis / (1000 * 60)
            val status = when {
                netMinutes >= MIN_MINUTES_PRESENT -> STATUS_PRESENT
                netMinutes >= MIN_MINUTES_HALF_DAY -> STATUS_HALF_DAY
                else -> STATUS_ABSENT
            }
            return Pair(netMinutes, status)
        }

        fun formatDuration(minutes: Long): String {
            val hrs = minutes / 60
            val mins = minutes % 60
            return "${hrs}h ${mins}m"
        }
    }
}
