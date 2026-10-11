package com.example.data.repository

import com.example.data.dao.AttendanceDao
import com.example.data.dao.LeaveRequestDao
import com.example.data.dao.UserDao
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.LeaveRequestEntity
import com.example.data.model.UserEntity
import com.example.utils.GpsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AttendanceRepository(
    private val userDao: UserDao,
    private val attendanceDao: AttendanceDao,
    private val leaveRequestDao: LeaveRequestDao
) {
    // 90 Days in Milliseconds: 90 * 24 * 60 * 60 * 1000L
    val ninetyDaysCutoff: Long
        get() = System.currentTimeMillis() - (90L * 24L * 60L * 60L * 1000L)

    suspend fun authenticate(empId: String, pass: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.authenticate(empId.trim(), pass.trim())
    }

    suspend fun ensureInitialDataSeeded(context: android.content.Context? = null) = withContext(Dispatchers.IO) {
        val admin = userDao.getUserByEmpId("ADMIN001")
        if (admin == null) {
            com.example.data.database.AppDatabase.populateInitialData(
                context = context,
                userDao = userDao,
                attendanceDao = attendanceDao,
                leaveRequestDao = leaveRequestDao
            )
        }
    }

    suspend fun getUserByEmpId(empId: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByEmpId(empId.trim())
    }

    fun getAllEmployees(): Flow<List<UserEntity>> = userDao.getAllEmployeesFlow()

    fun getEmployeeCount(): Flow<Int> = userDao.getEmployeeCountFlow()

    // Admin Credentials Management (Requirement 3: Maximum 5 Admins)
    fun getAllAdmins(): Flow<List<UserEntity>> = userDao.getAllAdminsFlow()

    fun getAdminCount(): Flow<Int> = userDao.getAdminCountFlow()

    suspend fun createNewAdmin(admin: UserEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val currentAdminCount = userDao.getAdminCount()
        if (currentAdminCount >= UserEntity.MAX_ADMIN_CAPACITY) {
            return@withContext Result.failure(IllegalStateException("Maximum limit of 5 Admin credentials reached."))
        }

        val existing = userDao.getUserByEmpId(admin.empId)
        if (existing != null) {
            return@withContext Result.failure(IllegalArgumentException("Admin ID ${admin.empId} already exists."))
        }

        userDao.insertUser(admin.copy(role = UserEntity.ROLE_ADMIN))
        Result.success(Unit)
    }

    suspend fun deleteAdmin(adminId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val totalAdmins = userDao.getAdminCount()
        if (totalAdmins <= 1) {
            return@withContext Result.failure(IllegalStateException("Cannot delete the only existing Admin. Minimum 1 Admin required."))
        }
        userDao.deleteUser(adminId)
        Result.success(Unit)
    }

    // Employee Accounts Management (Requirement 3: Capacity up to 100 Employees)
    suspend fun createNewEmployee(employee: UserEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val currentCount = userDao.getEmployeeCount()
        if (currentCount >= 100) {
            return@withContext Result.failure(IllegalStateException("Capacity reached. Maximum 100 employee accounts allowed."))
        }

        val existing = userDao.getUserByEmpId(employee.empId)
        if (existing != null) {
            return@withContext Result.failure(IllegalArgumentException("Employee ID ${employee.empId} already exists."))
        }

        userDao.insertUser(employee)
        Result.success(Unit)
    }

    suspend fun deleteEmployee(empId: String) = withContext(Dispatchers.IO) {
        userDao.deleteUser(empId)
    }

    // Password Management (Requirement 4)
    suspend fun changePassword(empId: String, newPass: String) = withContext(Dispatchers.IO) {
        userDao.updatePassword(empId.trim(), newPass.trim())
    }

    // Attendance Records Management (90-Day Retention Policy)
    fun getAllAttendanceRecords(): Flow<List<AttendanceRecordEntity>> =
        attendanceDao.getAllRecordsWithin90DaysFlow(ninetyDaysCutoff)

    fun getRecordsByMonth(monthYear: String): Flow<List<AttendanceRecordEntity>> =
        attendanceDao.getRecordsByMonthFlow(monthYear, ninetyDaysCutoff)

    fun getEmployeeAttendanceHistory(empId: String): Flow<List<AttendanceRecordEntity>> =
        attendanceDao.getEmployeeRecordsFlow(empId, ninetyDaysCutoff)

    fun getTodayAttendance(empId: String, todayDate: String): Flow<AttendanceRecordEntity?> =
        attendanceDao.getTodayRecordFlow(empId, todayDate)

    fun getDistinctMonths(): Flow<List<String>> =
        attendanceDao.getDistinctMonthsFlow(ninetyDaysCutoff)

    suspend fun getTodayRecordOnce(empId: String, todayDate: String): AttendanceRecordEntity? = withContext(Dispatchers.IO) {
        attendanceDao.getTodayRecord(empId, todayDate)
    }

    suspend fun clockIn(
        empId: String,
        employeeName: String,
        photoPath: String,
        lat: Double,
        lng: Double,
        address: String
    ): Result<AttendanceRecordEntity> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
        val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(now))

        val existing = attendanceDao.getTodayRecord(empId, todayDate)
        if (existing != null) {
            return@withContext Result.failure(IllegalStateException("Already clocked in for today."))
        }

        val record = AttendanceRecordEntity(
            empId = empId,
            employeeName = employeeName,
            dateString = todayDate,
            monthYear = monthYear,
            loginTime = now,
            loginPhotoPath = photoPath,
            loginLat = lat,
            loginLng = lng,
            loginAddress = address,
            status = AttendanceRecordEntity.STATUS_CLOCKED_IN
        )
        val id = attendanceDao.insertRecord(record)
        Result.success(record.copy(id = id))
    }

    suspend fun clockIn(record: AttendanceRecordEntity): Result<Long> = withContext(Dispatchers.IO) {
        val existing = attendanceDao.getTodayRecord(record.empId, record.dateString)
        if (existing != null) {
            return@withContext Result.failure(IllegalStateException("Already clocked in for today."))
        }
        val id = attendanceDao.insertRecord(record)
        Result.success(id)
    }

    suspend fun clockOut(
        empId: String,
        todayDate: String,
        logoutTimeMillis: Long,
        logoutPhotoPath: String,
        logoutLat: Double,
        logoutLng: Double,
        logoutAddress: String
    ): Result<AttendanceRecordEntity> = withContext(Dispatchers.IO) {
        val existing = attendanceDao.getTodayRecord(empId, todayDate)
            ?: return@withContext Result.failure(IllegalStateException("No clock-in record found for today."))

        if (existing.logoutTime != null) {
            return@withContext Result.failure(IllegalStateException("Already clocked out for today."))
        }

        val (netMins, finalStatus) = AttendanceRecordEntity.calculateAttendanceStatus(
            existing.loginTime,
            logoutTimeMillis
        )

        val updated = existing.copy(
            logoutTime = logoutTimeMillis,
            logoutPhotoPath = logoutPhotoPath,
            logoutLat = logoutLat,
            logoutLng = logoutLng,
            logoutAddress = logoutAddress,
            netMinutes = netMins,
            status = finalStatus
        )

        attendanceDao.updateRecord(updated)
        Result.success(updated)
    }

    // Attendance Data Deletion (Requirement 4)
    suspend fun deleteAttendanceRecord(recordId: Long) = withContext(Dispatchers.IO) {
        attendanceDao.deleteRecordById(recordId)
    }

    suspend fun deleteMonthAttendance(monthYear: String) = withContext(Dispatchers.IO) {
        attendanceDao.deleteRecordsByMonth(monthYear)
    }

    suspend fun updateEmployeeTrackingLocation(
        empId: String,
        lat: Double,
        lng: Double,
        address: String
    ) = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmpId(empId) ?: return@withContext
        val now = System.currentTimeMillis()

        val lastLat = user.lastKnownLat
        val lastLng = user.lastKnownLng

        if (lastLat != null && lastLng != null) {
            val incrementalDist = GpsHelper.calculateDistanceKm(lastLat, lastLng, lat, lng)
            val totalDist = Math.round((user.travelDistanceKm + incrementalDist) * 100.0) / 100.0

            val currentWaypoints = user.routeWaypoints
            val newWaypoints = if (currentWaypoints.isBlank()) {
                "$lat,$lng"
            } else {
                "$currentWaypoints;$lat,$lng"
            }

            userDao.updateLocationAndRoute(empId, lat, lng, address, now, totalDist, newWaypoints)
        } else {
            userDao.updateLocation(empId, lat, lng, address, now)
        }
    }

    suspend fun updateEmployeePermissions(
        empId: String,
        role: String,
        canExport: Boolean,
        canViewAll: Boolean,
        canTrack: Boolean
    ) = withContext(Dispatchers.IO) {
        userDao.updatePermissions(empId, role, canExport, canViewAll, canTrack)
    }

    suspend fun purgeRecordsOlderThan90Days(): Int = withContext(Dispatchers.IO) {
        attendanceDao.deleteRecordsOlderThan(ninetyDaysCutoff)
    }

    // =========================================================================
    // LEAVE BUCKET, AUTOMATED MONTHLY ACCRUAL & ADMIN DEBIT/CREDIT
    // =========================================================================

    /**
     * Automatic Monthly Leave Accrual & Carry Forward:
     * - Every employee gets 1 Paid Leave (PL) per month.
     * - Every employee gets 1 Sick Leave (SL) per 3 months.
     * - Carries forward previous month count and adds current month count.
     */
    suspend fun processMonthlyLeaveAccrual() = withContext(Dispatchers.IO) {
        val currentMonthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val employees = userDao.getAllEmployees()
        val calendar = Calendar.getInstance()
        val monthNumber = calendar.get(Calendar.MONTH) + 1 // 1-12
        val isSickLeaveQuarter = (monthNumber % 3 == 1) // e.g., Jan (1), Apr (4), Jul (7), Oct (10)

        for (emp in employees) {
            if (emp.lastLeaveCreditMonthYear != currentMonthKey) {
                val newPL = emp.paidLeaveBalance + 1
                val newSL = if (isSickLeaveQuarter) emp.sickLeaveBalance + 1 else emp.sickLeaveBalance
                userDao.updateLeaveBalances(
                    empId = emp.empId,
                    pl = newPL,
                    sl = newSL,
                    co = emp.compOffBalance,
                    monthYear = currentMonthKey
                )
            }
        }
    }

    fun getAllLeaveRequests(): Flow<List<LeaveRequestEntity>> =
        leaveRequestDao.getAllLeaveRequestsFlow()

    fun getEmployeeLeaveRequests(empId: String): Flow<List<LeaveRequestEntity>> =
        leaveRequestDao.getEmployeeLeaveRequestsFlow(empId)

    fun getPendingLeaveCount(): Flow<Int> =
        leaveRequestDao.getPendingLeaveCountFlow()

    /**
     * Raise a request for leave approval:
     * - If employee has leave balance in his/her bucket then only they have option to raise request.
     * - Sick Leave requires medical document upload.
     */
    suspend fun submitLeaveRequest(request: LeaveRequestEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmpId(request.empId)
            ?: return@withContext Result.failure(IllegalArgumentException("Employee not found."))

        when (request.leaveType.uppercase()) {
            LeaveRequestEntity.LEAVE_TYPE_PL -> {
                if (user.paidLeaveBalance < request.daysCount) {
                    return@withContext Result.failure(
                        IllegalStateException("Insufficient Paid Leave (PL) balance. Available: ${user.paidLeaveBalance} day(s).")
                    )
                }
            }
            LeaveRequestEntity.LEAVE_TYPE_SL -> {
                if (request.documentPath.isNullOrBlank()) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Document upload is mandatory for Sick Leave (SL). Please attach a medical certificate.")
                    )
                }
                if (user.sickLeaveBalance < request.daysCount) {
                    return@withContext Result.failure(
                        IllegalStateException("Insufficient Sick Leave (SL) balance. Available: ${user.sickLeaveBalance} day(s).")
                    )
                }
            }
            LeaveRequestEntity.LEAVE_TYPE_CO -> {
                if (user.compOffBalance < request.daysCount) {
                    return@withContext Result.failure(
                        IllegalStateException("Insufficient Comp Off (CO) balance. Available: ${user.compOffBalance} day(s).")
                    )
                }
            }
            else -> {
                return@withContext Result.failure(IllegalArgumentException("Invalid leave type: ${request.leaveType}"))
            }
        }

        leaveRequestDao.insertLeaveRequest(request)
        Result.success(Unit)
    }

    /**
     * Admin Approve Leave Request:
     * - Deducts the approved leave count from employee's bucket.
     * - Creates or converts attendance records for those dates into PAID DAYS.
     * - Updates Total Paid Days count.
     */
    suspend fun approveLeaveRequest(requestId: Long, adminId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val req = leaveRequestDao.getLeaveRequestById(requestId)
            ?: return@withContext Result.failure(IllegalArgumentException("Leave request #$requestId not found."))

        if (req.isApproved) {
            return@withContext Result.failure(IllegalStateException("Leave request is already approved."))
        }

        val user = userDao.getUserByEmpId(req.empId)
            ?: return@withContext Result.failure(IllegalArgumentException("Employee ${req.empId} not found."))

        // Deduct from bucket
        when (req.leaveType.uppercase()) {
            LeaveRequestEntity.LEAVE_TYPE_PL -> userDao.adjustPaidLeaveBalance(req.empId, -req.daysCount)
            LeaveRequestEntity.LEAVE_TYPE_SL -> userDao.adjustSickLeaveBalance(req.empId, -req.daysCount)
            LeaveRequestEntity.LEAVE_TYPE_CO -> userDao.adjustCompOffBalance(req.empId, -req.daysCount)
        }

        // Mark request as APPROVED
        leaveRequestDao.updateLeaveRequest(
            req.copy(
                status = LeaveRequestEntity.STATUS_APPROVED,
                reviewedAt = System.currentTimeMillis(),
                reviewedBy = adminId
            )
        )

        // Turn dates into Paid Day attendance records
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val startCal = Calendar.getInstance().apply {
            time = try { sdf.parse(req.startDate) ?: Date() } catch (e: Exception) { Date() }
        }
        val endCal = Calendar.getInstance().apply {
            time = try { sdf.parse(req.endDate) ?: Date() } catch (e: Exception) { Date() }
        }

        val leaveStatus = when (req.leaveType.uppercase()) {
            LeaveRequestEntity.LEAVE_TYPE_PL -> AttendanceRecordEntity.STATUS_PAID_LEAVE
            LeaveRequestEntity.LEAVE_TYPE_SL -> AttendanceRecordEntity.STATUS_SICK_LEAVE
            else -> AttendanceRecordEntity.STATUS_COMP_OFF
        }

        while (!startCal.after(endCal)) {
            val dateStr = sdf.format(startCal.time)
            val monthStr = monthFormat.format(startCal.time)
            val dayMillis = startCal.timeInMillis

            val existing = attendanceDao.getTodayRecord(req.empId, dateStr)
            val record = existing?.copy(
                status = leaveStatus,
                netMinutes = 540L, // 9 hours full paid day
                loginAddress = "Approved ${LeaveRequestEntity.getLeaveTypeName(req.leaveType)} (Paid Day)"
            ) ?: AttendanceRecordEntity(
                empId = req.empId,
                employeeName = req.employeeName,
                dateString = dateStr,
                monthYear = monthStr,
                loginTime = dayMillis + (9 * 60 * 60 * 1000L),
                logoutTime = dayMillis + (18 * 60 * 60 * 1000L),
                netMinutes = 540L,
                status = leaveStatus,
                loginAddress = "Approved ${LeaveRequestEntity.getLeaveTypeName(req.leaveType)} (Paid Day)",
                createdAt = dayMillis
            )

            attendanceDao.insertRecord(record)
            startCal.add(Calendar.DAY_OF_MONTH, 1)
        }

        Result.success(Unit)
    }

    /**
     * Admin Reject Leave Request
     */
    suspend fun rejectLeaveRequest(requestId: Long, adminId: String, remarks: String = ""): Result<Unit> = withContext(Dispatchers.IO) {
        val req = leaveRequestDao.getLeaveRequestById(requestId)
            ?: return@withContext Result.failure(IllegalArgumentException("Leave request #$requestId not found."))

        leaveRequestDao.updateLeaveRequest(
            req.copy(
                status = LeaveRequestEntity.STATUS_REJECTED,
                reviewedAt = System.currentTimeMillis(),
                reviewedBy = adminId,
                adminRemarks = remarks.ifBlank { "Rejected by Administrator." }
            )
        )
        Result.success(Unit)
    }

    /**
     * Admin Debit and Credit leave balance:
     * - Option to debit (delta < 0) and credit (delta > 0) leave balance from employees bucket.
     */
    suspend fun adminDebitCreditLeave(
        empId: String,
        leaveType: String,
        delta: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmpId(empId)
            ?: return@withContext Result.failure(IllegalArgumentException("Employee $empId not found."))

        when (leaveType.uppercase()) {
            LeaveRequestEntity.LEAVE_TYPE_PL -> userDao.adjustPaidLeaveBalance(empId, delta)
            LeaveRequestEntity.LEAVE_TYPE_SL -> userDao.adjustSickLeaveBalance(empId, delta)
            LeaveRequestEntity.LEAVE_TYPE_CO -> userDao.adjustCompOffBalance(empId, delta)
            else -> return@withContext Result.failure(IllegalArgumentException("Invalid leave type: $leaveType"))
        }

        Result.success(Unit)
    }

    suspend fun adminSetLeaveBalances(
        empId: String,
        pl: Int,
        sl: Int,
        co: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        userDao.setLeaveBalances(
            empId = empId,
            pl = pl.coerceAtLeast(0),
            sl = sl.coerceAtLeast(0),
            co = co.coerceAtLeast(0)
        )
        Result.success(Unit)
    }
}
