package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val empId: String,
    val name: String,
    val password: String = "password",
    val role: String = ROLE_EMPLOYEE, // "Admin", "Employee", "Team Lead", "Site Manager"
    val joiningDate: String = "",
    val dob: String = "",
    val permanentAddress: String = "",
    val currentAddress: String = "",
    val lastKnownLat: Double? = null,
    val lastKnownLng: Double? = null,
    val lastKnownAddress: String? = null,
    val lastActiveTimestamp: Long = System.currentTimeMillis(),

    // Extra Role Permissions granted by Admin
    val canExportExcel: Boolean = false,
    val canViewAllEmployees: Boolean = false,
    val canTrackLocations: Boolean = false,

    // Distance & Travel Route Tracking
    val travelDistanceKm: Double = 0.0,
    val routeWaypoints: String = "", // Semicolon-separated "lat,lng;lat,lng"

    // Leave Bucket (Accrued and carried forward from previous months)
    val paidLeaveBalance: Int = 1,      // 1 Paid leave credited per month
    val sickLeaveBalance: Int = 1,      // 1 Sick leave credited per 3 months
    val compOffBalance: Int = 0,        // Compensative off
    val lastLeaveCreditMonthYear: String = "" // e.g. "October 2026"
) {
    val isAdmin: Boolean
        get() = role.equals(ROLE_ADMIN, ignoreCase = true)

    val totalLeaveBalance: Int
        get() = paidLeaveBalance + sickLeaveBalance + compOffBalance

    companion object {
        const val ROLE_ADMIN = "Admin"
        const val ROLE_EMPLOYEE = "Employee"
        const val ROLE_TEAM_LEAD = "Team Lead"
        const val ROLE_SITE_MANAGER = "Site Manager"

        const val MAX_ADMIN_CAPACITY = 5

        val AVAILABLE_ROLES = listOf(
            ROLE_EMPLOYEE,
            ROLE_TEAM_LEAD,
            ROLE_SITE_MANAGER,
            ROLE_ADMIN
        )
    }
}
