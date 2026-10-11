package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.EmployeeMonthlySummary
import com.example.data.model.LeaveRequestEntity
import com.example.data.model.MonthAttendanceCount
import com.example.data.model.UserEntity
import com.example.data.repository.AttendanceRepository
import com.example.utils.ExcelExporter
import com.example.utils.GpsHelper
import com.example.utils.GpsLocationData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = AttendanceRepository(database.userDao(), database.attendanceDao(), database.leaveRequestDao())

    // Active session
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Auth error or feedback
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    // Today's date string
    val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val currentMonthYear: String
        get() = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

    // Employee State
    private val _todayRecord = MutableStateFlow<AttendanceRecordEntity?>(null)
    val todayRecord: StateFlow<AttendanceRecordEntity?> = _todayRecord.asStateFlow()

    private val _employeeHistory = MutableStateFlow<List<AttendanceRecordEntity>>(emptyList())
    val employeeHistory: StateFlow<List<AttendanceRecordEntity>> = _employeeHistory.asStateFlow()

    // Current Month Only Attendance Records for Employee (User Request: Employee can access only current month)
    val employeeCurrentMonthRecords: StateFlow<List<AttendanceRecordEntity>> = combine(
        _employeeHistory,
        _todayRecord
    ) { history, today ->
        val currentMonth = currentMonthYear
        val monthRecords = history.filter { it.monthYear.equals(currentMonth, ignoreCase = true) }
        val all = if (today != null && today.monthYear.equals(currentMonth, ignoreCase = true) && monthRecords.none { it.dateString == today.dateString }) {
            monthRecords + today
        } else {
            monthRecords
        }
        all.sortedByDescending { it.dateString }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Month Attendance Count for Employee Dashboard (Requirement 3: With Paid Leaves & Total Paid Days)
    val currentEmployeeMonthlyStats: StateFlow<MonthAttendanceCount> = combine(
        _employeeHistory,
        _todayRecord
    ) { history, today ->
        val currentMonth = currentMonthYear
        val monthRecords = history.filter { it.monthYear.equals(currentMonth, ignoreCase = true) }
        val allMonthRecords = if (today != null && today.monthYear.equals(currentMonth, ignoreCase = true) && monthRecords.none { it.dateString == today.dateString }) {
            monthRecords + today
        } else {
            monthRecords
        }
        val p = allMonthRecords.count { it.status == AttendanceRecordEntity.STATUS_PRESENT }
        val hd = allMonthRecords.count { it.status == AttendanceRecordEntity.STATUS_HALF_DAY }
        val a = allMonthRecords.count { it.status == AttendanceRecordEntity.STATUS_ABSENT }
        val pl = allMonthRecords.count {
            it.status == AttendanceRecordEntity.STATUS_PAID_LEAVE ||
            it.status == AttendanceRecordEntity.STATUS_SICK_LEAVE ||
            it.status == AttendanceRecordEntity.STATUS_COMP_OFF
        }
        val totalMinutes = allMonthRecords.sumOf { it.netMinutes }
        MonthAttendanceCount(
            monthYear = currentMonth,
            presentCount = p,
            halfDayCount = hd,
            absentCount = a,
            paidLeaveCount = pl,
            totalDays = allMonthRecords.size,
            totalHoursFormatted = AttendanceRecordEntity.formatDuration(totalMinutes)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthAttendanceCount(SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()), 0, 0, 0, 0, 0, "0h 0m"))

    // Admin State: Employees list (capacity 100)
    val allEmployees: StateFlow<List<UserEntity>> = repository.getAllEmployees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val employeeCount: StateFlow<Int> = repository.getEmployeeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Admin Credentials Management (Requirement 3: Maximum 5 Admins)
    val allAdmins: StateFlow<List<UserEntity>> = repository.getAllAdmins()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminCount: StateFlow<Int> = repository.getAdminCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val distinctMonths: StateFlow<List<String>> = repository.getDistinctMonths()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMonthFilter = MutableStateFlow<String?>(null)
    val selectedMonthFilter: StateFlow<String?> = _selectedMonthFilter.asStateFlow()

    // Admin Dashboard: 1 Month Only Attendance Summaries for each employee (Requirement 2: With Paid Leaves & Total Paid Days)
    val employeeMonthlySummaries: StateFlow<List<EmployeeMonthlySummary>> = combine(
        allEmployees,
        repository.getAllAttendanceRecords(),
        _selectedMonthFilter
    ) { employees, records, selectedMonth ->
        val targetMonth = if (!selectedMonth.isNullOrBlank()) selectedMonth else currentMonthYear
        val monthRecords = records.filter { it.monthYear.equals(targetMonth, ignoreCase = true) }
        employees.map { emp ->
            val empRecords = monthRecords.filter { it.empId == emp.empId }
            val p = empRecords.count { it.status == AttendanceRecordEntity.STATUS_PRESENT }
            val hd = empRecords.count { it.status == AttendanceRecordEntity.STATUS_HALF_DAY }
            val a = empRecords.count { it.status == AttendanceRecordEntity.STATUS_ABSENT }
            val pl = empRecords.count {
                it.status == AttendanceRecordEntity.STATUS_PAID_LEAVE ||
                it.status == AttendanceRecordEntity.STATUS_SICK_LEAVE ||
                it.status == AttendanceRecordEntity.STATUS_COMP_OFF
            }
            val totalMins = empRecords.sumOf { it.netMinutes }
            EmployeeMonthlySummary(
                employee = emp,
                monthYear = targetMonth,
                presentCount = p,
                halfDayCount = hd,
                absentCount = a,
                paidLeaveCount = pl,
                totalDays = empRecords.size,
                totalMinutes = totalMins
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Dashboard: Filtered Employee for Last 90 Days Attendance Report with Images (Requirement 1)
    private val _filteredEmployeeForReport = MutableStateFlow<UserEntity?>(null)
    val filteredEmployeeForReport: StateFlow<UserEntity?> = _filteredEmployeeForReport.asStateFlow()

    fun selectEmployeeForReport(employee: UserEntity?) {
        _filteredEmployeeForReport.value = employee
    }

    val selectedEmployee90DayRecords: StateFlow<List<AttendanceRecordEntity>> = combine(
        _filteredEmployeeForReport,
        repository.getAllAttendanceRecords()
    ) { selectedEmp, allRecords ->
        if (selectedEmp == null) {
            emptyList()
        } else {
            allRecords.filter { it.empId == selectedEmp.empId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered attendance for admin
    val adminAttendanceRecords: StateFlow<List<AttendanceRecordEntity>> = combine(
        repository.getAllAttendanceRecords(),
        _selectedMonthFilter
    ) { records, filter ->
        if (filter.isNullOrBlank()) {
            records
        } else {
            records.filter { it.monthYear.equals(filter, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tracked employee for Admin Real-time Map
    private val _trackedEmployee = MutableStateFlow<UserEntity?>(null)
    val trackedEmployee: StateFlow<UserEntity?> = _trackedEmployee.asStateFlow()

    // Map View Controls (Requirement 1: Zoom In/Out, Default/Satellite views)
    private val _mapZoom = MutableStateFlow(1.0f)
    val mapZoom: StateFlow<Float> = _mapZoom.asStateFlow()

    private val _isSatelliteView = MutableStateFlow(false)
    val isSatelliteView: StateFlow<Boolean> = _isSatelliteView.asStateFlow()

    // Travelled distance & route inspection (Requirement 2)
    private val _showRouteTrajectory = MutableStateFlow(true)
    val showRouteTrajectory: StateFlow<Boolean> = _showRouteTrajectory.asStateFlow()

    // Live GPS for current device
    private val _currentGps = MutableStateFlow<GpsLocationData?>(null)
    val currentGps: StateFlow<GpsLocationData?> = _currentGps.asStateFlow()

    private val _isLoadingGps = MutableStateFlow(false)
    val isLoadingGps: StateFlow<Boolean> = _isLoadingGps.asStateFlow()

    // Excel Export State
    private val _exportedFile = MutableStateFlow<File?>(null)
    val exportedFile: StateFlow<File?> = _exportedFile.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialDataSeeded(getApplication())
            repository.processMonthlyLeaveAccrual()
            val purgedCount = repository.purgeRecordsOlderThan90Days()
            if (purgedCount > 0) {
                _uiMessage.emit("Archived $purgedCount records older than 90 days.")
            }
        }
        viewModelScope.launch {
            allEmployees.collect { list ->
                val current = _currentUser.value ?: return@collect
                val updated = list.find { it.empId == current.empId }
                if (updated != null && (updated.paidLeaveBalance != current.paidLeaveBalance ||
                        updated.sickLeaveBalance != current.sickLeaveBalance ||
                        updated.compOffBalance != current.compOffBalance)) {
                    _currentUser.value = updated
                }
            }
        }
    }

    fun login(empId: String, pass: String, onSuccess: (UserEntity) -> Unit) {
        if (empId.isBlank()) {
            _authError.value = "Please enter your Employee ID"
            return
        }
        viewModelScope.launch {
            _authError.value = null
            var user = repository.authenticate(empId, pass)
            if (user == null) {
                repository.ensureInitialDataSeeded(getApplication())
                user = repository.authenticate(empId, pass)
            }
            if (user != null) {
                _currentUser.value = user
                loadUserSpecificData(user)
                onSuccess(user)
            } else {
                _authError.value = "Invalid Employee ID or Password"
            }
        }
    }

    fun quickLoginAsAdmin(onSuccess: (UserEntity) -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            var admin = repository.getUserByEmpId("ADMIN001")
            if (admin == null) {
                repository.ensureInitialDataSeeded(getApplication())
                admin = repository.getUserByEmpId("ADMIN001")
            }
            if (admin != null) {
                _currentUser.value = admin
                loadUserSpecificData(admin)
                onSuccess(admin)
            } else {
                _authError.value = "Default Admin not found"
            }
        }
    }

    fun quickLoginAsEmployee(empId: String = "EMP101", onSuccess: (UserEntity) -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            var emp = repository.getUserByEmpId(empId)
            if (emp == null) {
                repository.ensureInitialDataSeeded(getApplication())
                emp = repository.getUserByEmpId(empId)
            }
            if (emp != null) {
                _currentUser.value = emp
                loadUserSpecificData(emp)
                onSuccess(emp)
            } else {
                _authError.value = "Employee $empId not found"
            }
        }
    }

    fun logout(onLogout: () -> Unit) {
        _currentUser.value = null
        _todayRecord.value = null
        _employeeHistory.value = emptyList()
        _trackedEmployee.value = null
        _exportedFile.value = null
        onLogout()
    }

    fun loadUserSpecificData(user: UserEntity) {
        if (!user.isAdmin) {
            viewModelScope.launch {
                repository.getTodayAttendance(user.empId, todayDateString).collect {
                    _todayRecord.value = it
                }
            }
            viewModelScope.launch {
                repository.getEmployeeAttendanceHistory(user.empId).collect {
                    _employeeHistory.value = it
                }
            }
        }
        refreshGpsLocation()
    }

    fun refreshGpsLocation() {
        viewModelScope.launch {
            _isLoadingGps.value = true
            val loc = GpsHelper.getCurrentLocation(getApplication())
            _currentGps.value = loc
            _isLoadingGps.value = false

            _currentUser.value?.let { user ->
                if (!user.isAdmin) {
                    repository.updateEmployeeTrackingLocation(user.empId, loc.latitude, loc.longitude, loc.address)
                }
            }
        }
    }

    // Map controls (Requirement 1 & 2)
    fun zoomIn() {
        _mapZoom.value = (_mapZoom.value * 1.3f).coerceAtMost(3.5f)
    }

    fun zoomOut() {
        _mapZoom.value = (_mapZoom.value / 1.3f).coerceAtLeast(0.6f)
    }

    fun resetZoom() {
        _mapZoom.value = 1.0f
    }

    fun toggleSatelliteView() {
        _isSatelliteView.value = !_isSatelliteView.value
    }

    fun toggleRouteTrajectory() {
        _showRouteTrajectory.value = !_showRouteTrajectory.value
    }

    fun selectMonthFilter(month: String?) {
        _selectedMonthFilter.value = month
    }

    fun selectTrackedEmployee(employee: UserEntity?) {
        _trackedEmployee.value = employee
    }

    fun recordClockIn(
        photoPath: String,
        gpsData: GpsLocationData,
        onComplete: (Boolean, String) -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.clockIn(
                empId = user.empId,
                employeeName = user.name,
                photoPath = photoPath,
                lat = gpsData.latitude,
                lng = gpsData.longitude,
                address = gpsData.address
            )
            result.onSuccess {
                _todayRecord.value = it
                _uiMessage.emit("Clock-in recorded with GPS verification!")
                onComplete(true, "Clock-in successful!")
            }.onFailure {
                onComplete(false, it.message ?: "Failed to clock in.")
            }
        }
    }

    fun recordClockOut(
        photoPath: String,
        gpsData: GpsLocationData,
        onComplete: (Boolean, String) -> Unit
    ) {
        val user = _currentUser.value ?: return
        val current = _todayRecord.value
        if (current == null) {
            onComplete(false, "No active check-in record.")
            return
        }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val result = repository.clockOut(
                empId = user.empId,
                todayDate = current.dateString,
                logoutTimeMillis = now,
                logoutPhotoPath = photoPath,
                logoutLat = gpsData.latitude,
                logoutLng = gpsData.longitude,
                logoutAddress = gpsData.address
            )
            result.onSuccess { updated ->
                _todayRecord.value = updated
                val msg = "Clock-out recorded! Status: ${updated.status} (${AttendanceRecordEntity.formatDuration(updated.netMinutes)}) • Distance: ${updated.travelDistanceKm} km"
                _uiMessage.emit(msg)
                onComplete(true, msg)
            }.onFailure {
                onComplete(false, it.message ?: "Failed to clock out.")
            }
        }
    }

    fun createNewJoinee(
        name: String,
        empId: String,
        role: String = UserEntity.ROLE_EMPLOYEE,
        joiningDate: String,
        dob: String,
        permanentAddress: String,
        currentAddress: String,
        password: String,
        canExport: Boolean = false,
        canViewAll: Boolean = false,
        canTrack: Boolean = false,
        onResult: (Boolean, String) -> Unit
    ) {
        val currentCount = employeeCount.value
        if (currentCount >= 100) {
            onResult(false, "Capacity limit of 100 employees reached.")
            return
        }

        val newEmployee = UserEntity(
            empId = empId.trim().uppercase(Locale.getDefault()),
            name = name.trim(),
            password = password.ifBlank { "password" },
            role = role.trim().ifBlank { UserEntity.ROLE_EMPLOYEE },
            joiningDate = joiningDate.trim(),
            dob = dob.trim(),
            permanentAddress = permanentAddress.trim(),
            currentAddress = currentAddress.trim(),
            lastKnownLat = 37.7749 + (Math.random() - 0.5) * 0.05,
            lastKnownLng = -122.4194 + (Math.random() - 0.5) * 0.05,
            lastKnownAddress = currentAddress.ifBlank { "Office Location" },
            canExportExcel = canExport,
            canViewAllEmployees = canViewAll,
            canTrackLocations = canTrack,
            travelDistanceKm = 0.0,
            routeWaypoints = ""
        )

        viewModelScope.launch {
            val result = repository.createNewEmployee(newEmployee)
            result.onSuccess {
                _uiMessage.emit("Employee ${newEmployee.name} (${newEmployee.empId} - ${newEmployee.role}) created successfully!")
                onResult(true, "Created successfully!")
            }.onFailure {
                onResult(false, it.message ?: "Error creating employee")
            }
        }
    }

    // Admin Credentials Management (Requirement 3: Maximum 5 Admins)
    fun createNewAdmin(
        name: String,
        adminId: String,
        password: String,
        contactAddress: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val currentCount = adminCount.value
        if (currentCount >= UserEntity.MAX_ADMIN_CAPACITY) {
            onResult(false, "Maximum 5 Admin credentials limit reached.")
            return
        }

        val newAdmin = UserEntity(
            empId = adminId.trim().uppercase(Locale.getDefault()),
            name = name.trim(),
            password = password.trim().ifBlank { "admin123" },
            role = UserEntity.ROLE_ADMIN,
            joiningDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            currentAddress = contactAddress.trim().ifBlank { "HQ Management" },
            canExportExcel = true,
            canViewAllEmployees = true,
            canTrackLocations = true
        )

        viewModelScope.launch {
            val result = repository.createNewAdmin(newAdmin)
            result.onSuccess {
                _uiMessage.emit("Admin ID ${newAdmin.empId} (${newAdmin.name}) created successfully.")
                onResult(true, "Admin created successfully!")
            }.onFailure {
                onResult(false, it.message ?: "Failed to create Admin")
            }
        }
    }

    fun deleteAdmin(adminId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteAdmin(adminId)
            result.onSuccess {
                _uiMessage.emit("Admin $adminId deleted.")
                onResult(true, "Admin deleted successfully.")
            }.onFailure {
                onResult(false, it.message ?: "Failed to delete Admin")
            }
        }
    }

    // Password Change Option for any ID (Requirement 4)
    fun changePassword(empId: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.changePassword(empId, newPass)
                _uiMessage.emit("Password updated for ID: $empId")
                onResult(true, "Password updated successfully!")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to update password")
            }
        }
    }

    // Attendance Data Deletion (Requirement 4)
    fun deleteAttendanceRecord(id: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.deleteAttendanceRecord(id)
            _uiMessage.emit("Attendance record deleted.")
            onResult(true)
        }
    }

    fun deleteMonthAttendance(monthYear: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val deletedCount = repository.deleteMonthAttendance(monthYear)
            _uiMessage.emit("Deleted $deletedCount records for $monthYear.")
            onResult(true)
        }
    }

    fun updateEmployeePermissions(
        empId: String,
        role: String,
        canExport: Boolean,
        canViewAll: Boolean,
        canTrack: Boolean,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.updateEmployeePermissions(empId, role, canExport, canViewAll, canTrack)
            _uiMessage.emit("Updated permissions for $empId ($role)")
            onComplete()
        }
    }

    fun deleteEmployee(empId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.deleteEmployee(empId)
            _uiMessage.emit("Employee ID $empId deleted.")
            onResult(true)
        }
    }

    fun exportExcelReport(context: Context, month: String?, onComplete: (File) -> Unit) {
        viewModelScope.launch {
            val targetRecords = if (month.isNullOrBlank()) {
                adminAttendanceRecords.value
            } else {
                adminAttendanceRecords.value.filter { it.monthYear.equals(month, ignoreCase = true) }
            }
            val title = month ?: "All_Months"
            val empMap = allEmployees.value.associateBy { it.empId }
            val file = ExcelExporter.generateExcelFile(context, targetRecords, title, empMap)
            _exportedFile.value = file
            _uiMessage.emit("Excel spreadsheet generated: ${file.name}")
            onComplete(file)
        }
    }

    // =========================================================================
    // LEAVE REQUESTS & LEAVE BUCKET OPERATIONS
    // =========================================================================

    val allLeaveRequests: StateFlow<List<LeaveRequestEntity>> = repository.getAllLeaveRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingLeaveCount: StateFlow<Int> = repository.getPendingLeaveCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentEmployeeLeaveRequests: StateFlow<List<LeaveRequestEntity>> = combine(
        allLeaveRequests,
        _currentUser
    ) { all, user ->
        if (user == null) emptyList() else all.filter { it.empId == user.empId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun submitLeaveRequest(
        leaveType: String,
        startDate: String,
        endDate: String,
        daysCount: Int,
        reason: String,
        documentPath: String?,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = _currentUser.value ?: run {
            onResult(false, "User not logged in.")
            return
        }

        val request = LeaveRequestEntity(
            empId = user.empId,
            employeeName = user.name,
            leaveType = leaveType,
            startDate = startDate,
            endDate = endDate,
            daysCount = daysCount,
            reason = reason,
            documentPath = documentPath,
            status = LeaveRequestEntity.STATUS_PENDING
        )

        viewModelScope.launch {
            val res = repository.submitLeaveRequest(request)
            if (res.isSuccess) {
                refreshCurrentUserData()
                onResult(true, "Leave request submitted for approval.")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to submit leave request.")
            }
        }
    }

    fun approveLeaveRequest(requestId: Long, onResult: (Boolean, String?) -> Unit) {
        val admin = _currentUser.value ?: run {
            onResult(false, "Admin not logged in.")
            return
        }
        viewModelScope.launch {
            val res = repository.approveLeaveRequest(requestId, admin.empId)
            if (res.isSuccess) {
                refreshCurrentUserData()
                onResult(true, "Leave request approved! Paid day credited to attendance.")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to approve leave.")
            }
        }
    }

    fun rejectLeaveRequest(requestId: Long, remarks: String, onResult: (Boolean, String?) -> Unit) {
        val admin = _currentUser.value ?: run {
            onResult(false, "Admin not logged in.")
            return
        }
        viewModelScope.launch {
            val res = repository.rejectLeaveRequest(requestId, admin.empId, remarks)
            if (res.isSuccess) {
                onResult(true, "Leave request rejected.")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to reject leave.")
            }
        }
    }

    // Admin Debit and Credit of Leave Balances (User Request)
    fun adminDebitCreditLeave(
        empId: String,
        leaveType: String,
        delta: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.adminDebitCreditLeave(empId, leaveType, delta)
            if (res.isSuccess) {
                refreshCurrentUserData()
                val actionName = if (delta > 0) "Credited +$delta" else "Debited $delta"
                onResult(true, "$actionName $leaveType leave(s) successfully.")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Leave adjustment failed.")
            }
        }
    }

    fun adminSetLeaveBalances(
        empId: String,
        pl: Int,
        sl: Int,
        co: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.adminSetLeaveBalances(empId, pl, sl, co)
            if (res.isSuccess) {
                refreshCurrentUserData()
                onResult(true, "Leave bucket updated successfully.")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to update leave bucket.")
            }
        }
    }

    fun refreshCurrentUserData() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val refreshed = repository.getUserByEmpId(user.empId)
            if (refreshed != null) {
                _currentUser.value = refreshed
            }
        }
    }
}
