package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "leave_requests",
    indices = [
        Index(value = ["empId"]),
        Index(value = ["status"]),
        Index(value = ["appliedAt"])
    ]
)
data class LeaveRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val empId: String,
    val employeeName: String,
    val leaveType: String,      // LEAVE_TYPE_PL, LEAVE_TYPE_SL, LEAVE_TYPE_CO
    val startDate: String,      // YYYY-MM-DD
    val endDate: String,        // YYYY-MM-DD
    val daysCount: Int = 1,
    val reason: String = "",
    val documentPath: String? = null, // Mandatory for Sick Leave (SL)
    val status: String = STATUS_PENDING, // PENDING, APPROVED, REJECTED
    val appliedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val reviewedBy: String? = null,
    val adminRemarks: String? = null
) {
    val isApproved: Boolean get() = status.equals(STATUS_APPROVED, ignoreCase = true)
    val isPending: Boolean get() = status.equals(STATUS_PENDING, ignoreCase = true)
    val isRejected: Boolean get() = status.equals(STATUS_REJECTED, ignoreCase = true)

    companion object {
        const val LEAVE_TYPE_PL = "PL"  // Paid Leave (1 credited/month, carried forward)
        const val LEAVE_TYPE_SL = "SL"  // Sick Leave (1 credited/3 months, document required, carried forward)
        const val LEAVE_TYPE_CO = "CO"  // Compensative Off

        const val STATUS_PENDING = "PENDING"
        const val STATUS_APPROVED = "APPROVED"
        const val STATUS_REJECTED = "REJECTED"

        fun getLeaveTypeName(type: String): String = when (type.uppercase()) {
            LEAVE_TYPE_PL -> "Paid Leave (PL)"
            LEAVE_TYPE_SL -> "Sick Leave (SL)"
            LEAVE_TYPE_CO -> "Compensative Off (CO)"
            else -> type
        }
    }
}
