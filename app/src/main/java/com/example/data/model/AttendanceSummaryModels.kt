package com.example.data.model

data class MonthAttendanceCount(
    val monthYear: String,
    val presentCount: Int,
    val halfDayCount: Int,
    val absentCount: Int,
    val paidLeaveCount: Int = 0,
    val totalDays: Int,
    val totalHoursFormatted: String
) {
    val totalPaidDays: Double
        get() = presentCount.toDouble() + (halfDayCount * 0.5) + paidLeaveCount.toDouble()

    val totalPaidDaysFormatted: String
        get() = if (totalPaidDays % 1.0 == 0.0) "${totalPaidDays.toInt()}" else String.format(java.util.Locale.US, "%.1f", totalPaidDays)
}

data class EmployeeMonthlySummary(
    val employee: UserEntity,
    val monthYear: String,
    val presentCount: Int,
    val halfDayCount: Int,
    val absentCount: Int,
    val paidLeaveCount: Int = 0,
    val totalDays: Int,
    val totalMinutes: Long
) {
    val totalPaidDays: Double
        get() = presentCount.toDouble() + (halfDayCount * 0.5) + paidLeaveCount.toDouble()

    val totalPaidDaysFormatted: String
        get() = if (totalPaidDays % 1.0 == 0.0) "${totalPaidDays.toInt()}" else String.format(java.util.Locale.US, "%.1f", totalPaidDays)

    val totalHoursFormatted: String get() = AttendanceRecordEntity.formatDuration(totalMinutes)
}
