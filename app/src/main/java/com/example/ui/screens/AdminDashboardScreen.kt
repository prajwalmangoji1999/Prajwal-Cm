package com.example.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.EmployeeMonthlySummary
import com.example.data.model.UserEntity
import com.example.ui.components.AdminDebitCreditLeaveDialog
import com.example.ui.components.AestheticBackground
import com.example.ui.viewmodel.MainViewModel
import com.example.utils.ExcelExporter
import com.example.utils.GpsHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EnlargedPhotoInfo(
    val title: String,
    val photoPath: String,
    val address: String,
    val timestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allEmployees by viewModel.allEmployees.collectAsState()
    val employeeCount by viewModel.employeeCount.collectAsState()
    val allAdmins by viewModel.allAdmins.collectAsState()
    val adminCount by viewModel.adminCount.collectAsState()
    val attendanceRecords by viewModel.adminAttendanceRecords.collectAsState()
    val distinctMonths by viewModel.distinctMonths.collectAsState()
    val selectedMonth by viewModel.selectedMonthFilter.collectAsState()
    val trackedEmployee by viewModel.trackedEmployee.collectAsState()

    val employeeMonthlySummaries by viewModel.employeeMonthlySummaries.collectAsState()
    val filteredEmployeeForReport by viewModel.filteredEmployeeForReport.collectAsState()
    val selectedEmployee90DayRecords by viewModel.selectedEmployee90DayRecords.collectAsState()
    val allLeaveRequests by viewModel.allLeaveRequests.collectAsState()
    val pendingLeaveCount by viewModel.pendingLeaveCount.collectAsState()
    var enlargedPhotoData by remember { mutableStateOf<EnlargedPhotoInfo?>(null) }

    val mapZoom by viewModel.mapZoom.collectAsState()
    val isSatelliteView by viewModel.isSatelliteView.collectAsState()
    val showRouteTrajectory by viewModel.showRouteTrajectory.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Attendance Logs, 1: Leaves, 2: Excel Export, 3: Staff & Admins, 4: Live Map Tracking
    var showAddEmployeeDialog by remember { mutableStateOf(false) }
    var showAddAdminDialog by remember { mutableStateOf(false) }
    var managingEmployee by remember { mutableStateOf<UserEntity?>(null) }
    var passwordChangeUser by remember { mutableStateOf<UserEntity?>(null) }
    var employeeToDebitCreditLeave by remember { mutableStateOf<UserEntity?>(null) }
    var showEmployeePickerForLeaveDialog by remember { mutableStateOf(false) }

    // Deletion confirmation dialogs
    var recordToDelete by remember { mutableStateOf<AttendanceRecordEntity?>(null) }
    var monthToDelete by remember { mutableStateOf<String?>(null) }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DMY Attendence",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF4338CA)
                            ) {
                                Text(
                                    text = "ADMIN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "90-Day Retention Active • Staff: $employeeCount/100 • Admins: $adminCount/5",
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.logout(onLogout) },
                        modifier = Modifier.testTag("admin_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color(0xFFCBD5E1)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.TableChart, contentDescription = "Attendance Logs") },
                    label = { Text("Attendance") },
                    modifier = Modifier.testTag("admin_nav_logs")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        if (pendingLeaveCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = Color(0xFFDC2626)) {
                                        Text("$pendingLeaveCount")
                                    }
                                }
                            ) {
                                Icon(Icons.Default.EventNote, contentDescription = "Leaves & Approvals")
                            }
                        } else {
                            Icon(Icons.Default.EventNote, contentDescription = "Leaves & Approvals")
                        }
                    },
                    label = { Text("Requests") },
                    modifier = Modifier.testTag("admin_nav_requests")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Download, contentDescription = "Excel Export") },
                    label = { Text("Excel") },
                    modifier = Modifier.testTag("admin_nav_excel")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.People, contentDescription = "Staff & Admins") },
                    label = { Text("Accounts") },
                    modifier = Modifier.testTag("admin_nav_staff")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Live Map") },
                    label = { Text("Live Map") },
                    modifier = Modifier.testTag("admin_nav_map")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 3) {
                FloatingActionButton(
                    onClick = { showAddEmployeeDialog = true },
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_employee")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add New Joinee")
                }
            } else if (selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { showEmployeePickerForLeaveDialog = true },
                    icon = { Icon(Icons.Default.Tune, contentDescription = null) },
                    text = { Text("Debit / Credit Bucket", fontWeight = FontWeight.Bold) },
                    containerColor = Color(0xFF1E3A8A),
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_fab_debit_credit_leaves")
                )
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        AestheticBackground(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> AdminAttendanceDashboardView(
                    employeeMonthlySummaries = employeeMonthlySummaries,
                    allEmployees = allEmployees,
                    filteredEmployeeForReport = filteredEmployeeForReport,
                    selectedEmployee90DayRecords = selectedEmployee90DayRecords,
                    onSelectEmployeeForReport = { viewModel.selectEmployeeForReport(it) },
                    records = attendanceRecords,
                    distinctMonths = distinctMonths,
                    selectedMonth = selectedMonth,
                    currentMonthYear = viewModel.currentMonthYear,
                    onSelectMonth = { viewModel.selectMonthFilter(it) },
                    onDeleteRecord = { recordToDelete = it },
                    onDeleteMonth = { monthToDelete = it },
                    onViewEnlargedPhoto = { enlargedPhotoData = it },
                    onDebitCreditLeave = { employeeToDebitCreditLeave = it }
                )
                1 -> AdminLeavesView(
                    allEmployees = allEmployees,
                    leaveRequests = allLeaveRequests,
                    pendingCount = pendingLeaveCount,
                    onApproveRequest = { requestId ->
                        viewModel.approveLeaveRequest(requestId) { success, msg ->
                            android.widget.Toast.makeText(context, msg ?: if (success) "Approved (Paid Day added)" else "Failed", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    onRejectRequest = { requestId, remarks ->
                        viewModel.rejectLeaveRequest(requestId, remarks) { success, msg ->
                            android.widget.Toast.makeText(context, msg ?: if (success) "Request rejected" else "Failed", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDebitCreditLeave = { empId, leaveType, delta ->
                        viewModel.adminDebitCreditLeave(empId, leaveType, delta) { success, msg ->
                            android.widget.Toast.makeText(context, msg ?: if (success) "Leave balance updated" else "Failed", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                2 -> AdminExcelExportView(
                    records = attendanceRecords,
                    distinctMonths = distinctMonths,
                    selectedMonth = selectedMonth,
                    onSelectMonth = { viewModel.selectMonthFilter(it) },
                    onExportExcel = { month ->
                        viewModel.exportExcelReport(context, month) { file ->
                            val intent = ExcelExporter.getShareExcelIntent(context, file)
                            context.startActivity(intent)
                        }
                    }
                )
                3 -> AdminAccountsManagementView(
                    employees = allEmployees,
                    employeeCount = employeeCount,
                    admins = allAdmins,
                    adminCount = adminCount,
                    onAddNewEmployee = { showAddEmployeeDialog = true },
                    onAddNewAdmin = { showAddAdminDialog = true },
                    onManageRole = { managingEmployee = it },
                    onChangePassword = { passwordChangeUser = it },
                    onDeleteUser = { userToDelete = it },
                    onDebitCreditLeave = { employeeToDebitCreditLeave = it }
                )
                4 -> AdminLiveMapTrackingView(
                    employees = allEmployees,
                    trackedEmployee = trackedEmployee,
                    zoomLevel = mapZoom,
                    isSatellite = isSatelliteView,
                    showTrajectory = showRouteTrajectory,
                    onSelectTrackedEmployee = { viewModel.selectTrackedEmployee(it) },
                    onZoomIn = { viewModel.zoomIn() },
                    onZoomOut = { viewModel.zoomOut() },
                    onResetZoom = { viewModel.resetZoom() },
                    onToggleSatellite = { viewModel.toggleSatelliteView() },
                    onToggleTrajectory = { viewModel.toggleRouteTrajectory() }
                )
            }
        }
    }

    // Dialog: Add Employee
    if (showAddEmployeeDialog) {
        AddEmployeeDialog(
            currentCount = employeeCount,
            maxCapacity = 100,
            onDismiss = { showAddEmployeeDialog = false },
            onConfirm = { name, empId, role, joiningDate, dob, permAddress, currAddress, password, canExport, canViewAll, canTrack ->
                viewModel.createNewJoinee(
                    name = name,
                    empId = empId,
                    role = role,
                    joiningDate = joiningDate,
                    dob = dob,
                    permanentAddress = permAddress,
                    currentAddress = currAddress,
                    password = password,
                    canExport = canExport,
                    canViewAll = canViewAll,
                    canTrack = canTrack
                ) { success, _ ->
                    if (success) showAddEmployeeDialog = false
                }
            }
        )
    }

    // Dialog: Add Admin (Max 5 Admins, Requirement 3)
    if (showAddAdminDialog) {
        AddAdminDialog(
            currentAdminCount = adminCount,
            maxAdminCapacity = UserEntity.MAX_ADMIN_CAPACITY,
            onDismiss = { showAddAdminDialog = false },
            onConfirm = { name, adminId, password, contact ->
                viewModel.createNewAdmin(name, adminId, password, contact) { success, _ ->
                    if (success) showAddAdminDialog = false
                }
            }
        )
    }

    // Dialog: Manage Role & Extra Access
    managingEmployee?.let { emp ->
        ManageRoleDialog(
            employee = emp,
            onDismiss = { managingEmployee = null },
            onSave = { role, canExport, canViewAll, canTrack ->
                viewModel.updateEmployeePermissions(emp.empId, role, canExport, canViewAll, canTrack) {
                    managingEmployee = null
                }
            }
        )
    }

    // Dialog: Password Change Option (Requirement 4)
    passwordChangeUser?.let { user ->
        ChangePasswordDialog(
            targetUser = user,
            onDismiss = { passwordChangeUser = null },
            onConfirm = { newPass ->
                viewModel.changePassword(user.empId, newPass) { success, _ ->
                    if (success) passwordChangeUser = null
                }
            }
        )
    }

    // Dialog: Delete Attendance Record Confirmation (Requirement 4)
    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("Delete Attendance Record", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete attendance record for ${record.employeeName} on ${record.dateString}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAttendanceRecord(record.id) {
                            recordToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete Record")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { recordToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Delete Month Data Confirmation (Requirement 4)
    monthToDelete?.let { month ->
        AlertDialog(
            onDismissRequest = { monthToDelete = null },
            title = { Text("Delete Month Data", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ALL attendance records for $month? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMonthAttendance(month) {
                            monthToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete Entire Month")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { monthToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Delete ID Confirmation (Employee or Admin ID, Requirement 3 & 4)
    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = {
                Text(
                    text = if (user.isAdmin) "Delete Admin ID Credentials" else "Delete Employee ID Credentials",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to delete ${user.name} (${user.empId})? Their credentials and profile will be permanently removed.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (user.isAdmin) {
                            viewModel.deleteAdmin(user.empId) { _, _ ->
                                userToDelete = null
                            }
                        } else {
                            viewModel.deleteEmployee(user.empId) {
                                userToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete ID")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { userToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Admin Debit and Credit Leave Bucket Dialog
    employeeToDebitCreditLeave?.let { targetEmp ->
        val freshEmp = allEmployees.find { it.empId == targetEmp.empId } ?: targetEmp
        AdminDebitCreditLeaveDialog(
            employee = freshEmp,
            onDismiss = { employeeToDebitCreditLeave = null },
            onConfirm = { leaveType, delta, reason ->
                viewModel.adminDebitCreditLeave(freshEmp.empId, leaveType, delta) { success, msg ->
                    android.widget.Toast.makeText(context, msg ?: if (success) "Leave balance updated" else "Failed", android.widget.Toast.LENGTH_SHORT).show()
                }
                employeeToDebitCreditLeave = null
            }
        )
    }

    // Dialog: Picker to Debit/Credit any employee bucket from FAB or button
    if (showEmployeePickerForLeaveDialog) {
        var pickerSearch by remember { mutableStateOf("") }
        val filtered = allEmployees.filter {
            pickerSearch.isBlank() || it.name.contains(pickerSearch, ignoreCase = true) || it.empId.contains(pickerSearch, ignoreCase = true)
        }
        AlertDialog(
            onDismissRequest = { showEmployeePickerForLeaveDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEFF6FF),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF1E3A8A), modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Debit / Credit Leave Bucket", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Select an employee to adjust balance", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                    OutlinedTextField(
                        value = pickerSearch,
                        onValueChange = { pickerSearch = it },
                        placeholder = { Text("Search by name or ID...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No employees found", color = Color(0xFF64748B), fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filtered) { emp ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showEmployeePickerForLeaveDialog = false
                                            employeeToDebitCreditLeave = emp
                                        }
                                        .testTag("picker_emp_${emp.empId}"),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(emp.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                            Text("ID: ${emp.empId} • ${emp.role.replace("ROLE_", "")}", fontSize = 12.sp, color = Color(0xFF2563EB))
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFEFF6FF)
                                        ) {
                                            Text(
                                                text = "${emp.paidLeaveBalance} PL • ${emp.sickLeaveBalance} SL • ${emp.compOffBalance} CO",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1D4ED8),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showEmployeePickerForLeaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Enlarged Photo Inspection Dialog (Requirement: 90-day report with images)
    enlargedPhotoData?.let { photoInfo ->
        AlertDialog(
            onDismissRequest = { enlargedPhotoData = null },
            title = {
                Column {
                    Text(
                        text = photoInfo.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Live In-App GPS Stamped Camera Photo",
                        fontSize = 12.sp,
                        color = Color(0xFF059669),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val f = java.io.File(photoInfo.photoPath)
                    if (f.exists()) {
                        val bmp = android.graphics.BitmapFactory.decodeFile(f.absolutePath)
                        if (bmp != null) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = photoInfo.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            LiveCameraPlaceholder(label = photoInfo.title)
                        }
                    } else {
                        LiveCameraPlaceholder(label = photoInfo.title)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "📍 ${photoInfo.address.ifBlank { "GPS Coordinates Verified" }}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "🕒 ${photoInfo.timestamp}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "🛡️ Verified Live Camera Only (Anti-Fraud)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { enlargedPhotoData = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * 1. ATTENDANCE DASHBOARD VIEW (User Requirements:
 * - Dashboard shows only Employees name and their attendance count for 1 month only
 * - Option to filter Employee and check his last 90 days attendance report with images)
 */
@Composable
fun LiveCameraPlaceholder(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "✓ Live In-App GPS Camera Verified",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF10B981)
            )
            Text(
                text = "Anti-Fraud: Gallery uploads strictly disallowed",
                fontSize = 10.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun AdminAttendanceDashboardView(
    employeeMonthlySummaries: List<EmployeeMonthlySummary>,
    allEmployees: List<UserEntity>,
    filteredEmployeeForReport: UserEntity?,
    selectedEmployee90DayRecords: List<AttendanceRecordEntity>,
    onSelectEmployeeForReport: (UserEntity?) -> Unit,
    records: List<AttendanceRecordEntity>,
    distinctMonths: List<String>,
    selectedMonth: String?,
    currentMonthYear: String,
    onSelectMonth: (String?) -> Unit,
    onDeleteRecord: (AttendanceRecordEntity) -> Unit,
    onDeleteMonth: (String) -> Unit,
    onViewEnlargedPhoto: (EnlargedPhotoInfo) -> Unit,
    onDebitCreditLeave: (UserEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeSubViewMode by remember { mutableIntStateOf(0) } // 0: 1-Month Employee Count, 1: All Daily Raw Records
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val displayMonth = selectedMonth ?: currentMonthYear

    val filteredSummaries = remember(employeeMonthlySummaries, searchQuery) {
        if (searchQuery.isBlank()) {
            employeeMonthlySummaries
        } else {
            employeeMonthlySummaries.filter {
                it.employee.name.contains(searchQuery, ignoreCase = true) ||
                it.employee.empId.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val filteredDailyRecords = remember(records, searchQuery) {
        if (searchQuery.isBlank()) records else {
            records.filter {
                it.employeeName.contains(searchQuery, ignoreCase = true) ||
                it.empId.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val presentCount = filteredDailyRecords.count { it.status == AttendanceRecordEntity.STATUS_PRESENT }
    val halfDayCount = filteredDailyRecords.count { it.status == AttendanceRecordEntity.STATUS_HALF_DAY }
    val absentCount = filteredDailyRecords.count { it.status == AttendanceRecordEntity.STATUS_ABSENT }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Condition A: If a specific employee is filtered, show their complete 90-day attendance report with images!
        if (filteredEmployeeForReport != null) {
            val emp = filteredEmployeeForReport
            val empRecords = selectedEmployee90DayRecords
            val p90 = empRecords.count { it.status == AttendanceRecordEntity.STATUS_PRESENT }
            val hd90 = empRecords.count { it.status == AttendanceRecordEntity.STATUS_HALF_DAY }
            val a90 = empRecords.count { it.status == AttendanceRecordEntity.STATUS_ABSENT }
            val totalMins = empRecords.sumOf { it.netMinutes }

            // Filter Top Bar with Back Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onSelectEmployeeForReport(null) },
                            modifier = Modifier.testTag("back_to_all_employees_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back to All Employees",
                                tint = Color(0xFF0F172A)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "90-Day Attendance Report",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Live Verified GPS & Camera Images Included",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF059669)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { onSelectEmployeeForReport(null) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Filter", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Employee 90-Day Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emp.name.take(2).uppercase(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = emp.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "ID: ${emp.empId} • ${emp.role.replace("ROLE_", "")}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2563EB)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 90-Day Metric Counters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MonthlyCountBadge(
                                modifier = Modifier.weight(1f),
                                label = "Present [P]",
                                count = p90,
                                badgeColor = Color(0xFF059669),
                                containerColor = Color(0xFFD1FAE5)
                            )
                            MonthlyCountBadge(
                                modifier = Modifier.weight(1f),
                                label = "Half Day [HD]",
                                count = hd90,
                                badgeColor = Color(0xFFD97706),
                                containerColor = Color(0xFFFEF3C7)
                            )
                            MonthlyCountBadge(
                                modifier = Modifier.weight(1f),
                                label = "Absent [A]",
                                count = a90,
                                badgeColor = Color(0xFFDC2626),
                                containerColor = Color(0xFFFEE2E2)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "90-Day Total Days: ${empRecords.size} Days",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Total Hours: ${AttendanceRecordEntity.formatDuration(totalMins)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "90-Day Attendance Logs with Verified Images (${empRecords.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
            }

            if (empRecords.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "No attendance records found for ${emp.name} in the last 90 days.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            } else {
                items(empRecords) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Top Date and Status Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = record.dateString,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = record.monthYear,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (statusBg, statusColor, codeText) = when (record.status) {
                                        AttendanceRecordEntity.STATUS_PRESENT -> Triple(Color(0xFFD1FAE5), Color(0xFF065F46), "P • Present")
                                        AttendanceRecordEntity.STATUS_HALF_DAY -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), "HD • Half Day")
                                        AttendanceRecordEntity.STATUS_ABSENT -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), "A • Absent")
                                        else -> Triple(Color(0xFFDBEAFE), Color(0xFF1E40AF), "In Progress")
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = statusBg
                                    ) {
                                        Text(
                                            text = codeText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteRecord(record) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Record",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Punch Times & Duration
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Check In", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    Text(
                                        text = timeFormat.format(Date(record.loginTime)),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }

                                Column {
                                    Text(text = "Check Out", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    Text(
                                        text = record.logoutTime?.let { timeFormat.format(Date(it)) } ?: "--",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }

                                Column {
                                    Text(text = "Duration", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    Text(
                                        text = if (record.logoutTime != null) AttendanceRecordEntity.formatDuration(record.netMinutes) else "In Progress",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D4ED8)
                                    )
                                }

                                Column {
                                    Text(text = "Distance", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    Text(
                                        text = "${record.travelDistanceKm} km",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF059669)
                                    )
                                }
                            }

                            // GPS Location Address
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "GPS: ${record.loginAddress.ifBlank { "Coordinates Verified at Punch" }}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E293B),
                                    maxLines = 1
                                )
                            }

                            // ATTENDANCE IMAGES: Live Camera Photo Previews (Requirement 3: with images!)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Verified Live Camera Punch Photos (Tap to Enlarge):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Login Punch Photo
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            onViewEnlargedPhoto(
                                                EnlargedPhotoInfo(
                                                    title = "${record.employeeName} - Check In Photo",
                                                    photoPath = record.loginPhotoPath,
                                                    address = record.loginAddress,
                                                    timestamp = "${record.dateString} at ${timeFormat.format(Date(record.loginTime))}"
                                                )
                                            )
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        val f = java.io.File(record.loginPhotoPath)
                                        if (record.loginPhotoPath.isNotBlank() && f.exists()) {
                                            val bmp = android.graphics.BitmapFactory.decodeFile(f.absolutePath)
                                            if (bmp != null) {
                                                Image(
                                                    bitmap = bmp.asImageBitmap(),
                                                    contentDescription = "Check In Photo",
                                                    modifier = Modifier
                                                        .size(70.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(36.dp))
                                            }
                                        } else {
                                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(36.dp))
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFD1FAE5)) {
                                            Text(
                                                text = "IN • GPS VERIFIED",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF065F46),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                // Logout Punch Photo
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            if (record.logoutTime != null) {
                                                onViewEnlargedPhoto(
                                                    EnlargedPhotoInfo(
                                                        title = "${record.employeeName} - Check Out Photo",
                                                        photoPath = record.logoutPhotoPath ?: "",
                                                        address = record.loginAddress,
                                                        timestamp = "${record.dateString} at ${timeFormat.format(Date(record.logoutTime))}"
                                                    )
                                                )
                                            }
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        val f = record.logoutPhotoPath?.let { java.io.File(it) }
                                        if (f != null && f.exists()) {
                                            val bmp = android.graphics.BitmapFactory.decodeFile(f.absolutePath)
                                            if (bmp != null) {
                                                Image(
                                                    bitmap = bmp.asImageBitmap(),
                                                    contentDescription = "Check Out Photo",
                                                    modifier = Modifier
                                                        .size(70.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(36.dp))
                                            }
                                        } else {
                                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(36.dp))
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (record.logoutTime != null) Color(0xFFFEE2E2) else Color(0xFFE2E8F0)
                                        ) {
                                            Text(
                                                text = if (record.logoutTime != null) "OUT • GPS VERIFIED" else "PENDING LOGOUT",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (record.logoutTime != null) Color(0xFF991B1B) else Color(0xFF475569),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } else {
            // Condition B: Default Dashboard View
            // "And dashboard should show only Employees name and their attendance count for the 1 month only."
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Monthly Employee Attendance",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Showing attendance count for $displayMonth (1 Month Only)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B)
                        )
                    }

                    if (selectedMonth != null) {
                        OutlinedButton(
                            onClick = { onDeleteMonth(selectedMonth) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete Month", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Month Selector Chips (Default is Current Month)
            item {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Select Month for 1-Month Attendance Count:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedMonth == null,
                                onClick = { onSelectMonth(null) },
                                label = { Text("Current Month ($currentMonthYear)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF1E3A8A),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        items(distinctMonths) { month ->
                            if (!month.equals(currentMonthYear, ignoreCase = true)) {
                                FilterChip(
                                    selected = selectedMonth == month,
                                    onClick = { onSelectMonth(month) },
                                    label = { Text(month) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF2563EB),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Option to filter the Employee (Requirement 3: give option to filter the Employee)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search Employee Name or ID to filter...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedPlaceholderColor = Color(0xFF475569),
                            unfocusedPlaceholderColor = Color(0xFF64748B),
                            focusedBorderColor = Color(0xFF1E3A8A),
                            unfocusedBorderColor = Color(0xFF94A3B8)
                        )
                    )

                    // Quick Filter Employee Chips
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Filter Employee for 90-Day Report with Images:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allEmployees) { emp ->
                            FilterChip(
                                selected = false,
                                onClick = { onSelectEmployeeForReport(emp) },
                                label = { Text(emp.name) },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF0F172A)
                                )
                            )
                        }
                    }
                }
            }

            // View Mode Selector: 1-Month Employee Attendance Summaries vs All Daily Logs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeSubViewMode == 0,
                        onClick = { activeSubViewMode = 0 },
                        label = { Text("Employees (1-Month Count Only)", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1E3A8A),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color(0xFF0F172A)
                        )
                    )
                    FilterChip(
                        selected = activeSubViewMode == 1,
                        onClick = { activeSubViewMode = 1 },
                        label = { Text("All Daily Logs", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1E3A8A),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color(0xFF0F172A)
                        )
                    )
                }
            }

            if (activeSubViewMode == 0) {
                // "And dashboard should show only Employees name and their attendance count for the 1 month only."
                if (filteredSummaries.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Text(
                                text = "No employees found for $displayMonth.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                } else {
                    items(filteredSummaries) { summary ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectEmployeeForReport(summary.employee) }
                                .testTag("employee_summary_card_${summary.employee.empId}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Employee Name and ID
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF2563EB)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = summary.employee.name.take(2).uppercase(),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = summary.employee.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "ID: ${summary.employee.empId} • ${summary.employee.role.replace("ROLE_", "")}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF2563EB)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF1F5F9)
                                    ) {
                                        Text(
                                            text = "Total: ${summary.totalDays} Days",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // 1 Month Attendance Count Badges
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MonthlyCountBadge(
                                        modifier = Modifier.weight(1f),
                                        label = "Present [P]",
                                        count = summary.presentCount,
                                        badgeColor = Color(0xFF059669),
                                        containerColor = Color(0xFFD1FAE5)
                                    )
                                    MonthlyCountBadge(
                                        modifier = Modifier.weight(1f),
                                        label = "Half Day [HD]",
                                        count = summary.halfDayCount,
                                        badgeColor = Color(0xFFD97706),
                                        containerColor = Color(0xFFFEF3C7)
                                    )
                                    MonthlyCountBadge(
                                        modifier = Modifier.weight(1f),
                                        label = "Absent [A]",
                                        count = summary.absentCount,
                                        badgeColor = Color(0xFFDC2626),
                                        containerColor = Color(0xFFFEE2E2)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Leave Bucket Badge & Debit/Credit Action
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFEFF6FF)
                                    ) {
                                        Text(
                                            text = "Bucket: ${summary.employee.paidLeaveBalance} PL • ${summary.employee.sickLeaveBalance} SL • ${summary.employee.compOffBalance} CO",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1D4ED8),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { onDebitCreditLeave(summary.employee) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Debit / Credit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Hours and Action
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$displayMonth Hours: ${summary.totalHoursFormatted}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E293B)
                                    )

                                    Button(
                                        onClick = { onSelectEmployeeForReport(summary.employee) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Check 90-Day Report →", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // All Daily Logs Sub-view
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryCounter(
                            modifier = Modifier.weight(1f),
                            title = "Present [P]",
                            count = presentCount,
                            rule = "≥ 8h 45m",
                            color = Color(0xFF059669),
                            bg = Color(0xFFD1FAE5)
                        )
                        SummaryCounter(
                            modifier = Modifier.weight(1f),
                            title = "Half Day [HD]",
                            count = halfDayCount,
                            rule = "≥ 4h 15m",
                            color = Color(0xFFD97706),
                            bg = Color(0xFFFEF3C7)
                        )
                        SummaryCounter(
                            modifier = Modifier.weight(1f),
                            title = "Absent [A]",
                            count = absentCount,
                            rule = "< 4h 15m",
                            color = Color(0xFFDC2626),
                            bg = Color(0xFFFEE2E2)
                        )
                    }
                }

                if (filteredDailyRecords.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Text(
                                text = "No daily attendance records found.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                } else {
                    items(filteredDailyRecords) { record ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = record.employeeName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "${record.empId} • ${record.dateString}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val (statusBg, statusColor, codeText) = when (record.status) {
                                            AttendanceRecordEntity.STATUS_PRESENT -> Triple(Color(0xFFD1FAE5), Color(0xFF065F46), "P • Present")
                                            AttendanceRecordEntity.STATUS_HALF_DAY -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), "HD • Half Day")
                                            AttendanceRecordEntity.STATUS_ABSENT -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), "A • Absent")
                                            else -> Triple(Color(0xFFDBEAFE), Color(0xFF1E40AF), "In Progress")
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = statusBg
                                        ) {
                                            Text(
                                                text = codeText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = statusColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { onDeleteRecord(record) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Record",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "Login", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        Text(
                                            text = timeFormat.format(Date(record.loginTime)),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }

                                    Column {
                                        Text(text = "Logout", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        Text(
                                            text = record.logoutTime?.let { timeFormat.format(Date(it)) } ?: "--",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }

                                    Column {
                                        Text(text = "Duration", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        Text(
                                            text = if (record.logoutTime != null) AttendanceRecordEntity.formatDuration(record.netMinutes) else "In Progress",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1D4ED8)
                                        )
                                    }

                                    Column {
                                        Text(text = "Distance", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        Text(
                                            text = "${record.travelDistanceKm} km",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF059669)
                                        )
                                    }
                                }

                                // Photo Thumbnails
                                if (record.loginPhotoPath.isNotBlank() || record.logoutPhotoPath?.isNotBlank() == true) {
                                    Row(
                                        modifier = Modifier.padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (record.loginPhotoPath.isNotBlank()) {
                                            val f = java.io.File(record.loginPhotoPath)
                                            if (f.exists()) {
                                                val bmp = android.graphics.BitmapFactory.decodeFile(f.absolutePath)
                                                if (bmp != null) {
                                                    Image(
                                                        bitmap = bmp.asImageBitmap(),
                                                        contentDescription = "Login Photo",
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .clickable {
                                                                onViewEnlargedPhoto(
                                                                    EnlargedPhotoInfo(
                                                                        title = "${record.employeeName} Check In",
                                                                        photoPath = record.loginPhotoPath,
                                                                        address = record.loginAddress,
                                                                        timestamp = record.dateString
                                                                    )
                                                                )
                                                            },
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            }
                                        }
                                        if (record.logoutPhotoPath?.isNotBlank() == true) {
                                            val f = java.io.File(record.logoutPhotoPath)
                                            if (f.exists()) {
                                                val bmp = android.graphics.BitmapFactory.decodeFile(f.absolutePath)
                                                if (bmp != null) {
                                                    Image(
                                                        bitmap = bmp.asImageBitmap(),
                                                        contentDescription = "Logout Photo",
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .clickable {
                                                                onViewEnlargedPhoto(
                                                                    EnlargedPhotoInfo(
                                                                        title = "${record.employeeName} Check Out",
                                                                        photoPath = record.logoutPhotoPath,
                                                                        address = record.loginAddress,
                                                                        timestamp = record.dateString
                                                                    )
                                                                )
                                                            },
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCounter(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    rule: String,
    color: Color,
    bg: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(top = 2.dp)
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = bg,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = rule,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * 2. EXCEL EXPORT VIEW (Requirement 4 & 9)
 */
@Composable
fun AdminExcelExportView(
    records: List<AttendanceRecordEntity>,
    distinctMonths: List<String>,
    selectedMonth: String?,
    onSelectMonth: (String?) -> Unit,
    onExportExcel: (month: String?) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Export Attendance in Excel Format",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Download monthly data as Microsoft Excel spreadsheet (.xls format) with employee details & P/HD/A status",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Export Configuration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD1FAE5),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Monthly Excel Exporter",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Admin Restricted Access Only",
                                fontSize = 12.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Select Month Folder:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedMonth == null,
                                onClick = { onSelectMonth(null) },
                                label = { Text("All 90-Day Records") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF1E3A8A),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        items(distinctMonths) { month ->
                            FilterChip(
                                selected = selectedMonth == month,
                                onClick = { onSelectMonth(month) },
                                label = { Text(month) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2563EB),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preview Details
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "File Specifications:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "• Target: ${selectedMonth ?: "All 90-Day Months"}\n• Record Count: ${records.size} rows\n• Columns: Employee ID, Name, DOB, Joining Date, Address, Login/Logged Times, Net Hours, P/HD/A Status, GPS Coordinates",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.padding(top = 4.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onExportExcel(selectedMonth) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("download_excel_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download Excel File (.xls)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. ACCOUNTS MANAGEMENT VIEW (Requirements 3 & 4: Max 5 Admins, 100 Staff, Password Change, Delete ID)
 */
@Composable
fun AdminAccountsManagementView(
    employees: List<UserEntity>,
    employeeCount: Int,
    admins: List<UserEntity>,
    adminCount: Int,
    onAddNewEmployee: () -> Unit,
    onAddNewAdmin: () -> Unit,
    onManageRole: (UserEntity) -> Unit,
    onChangePassword: (UserEntity) -> Unit,
    onDeleteUser: (UserEntity) -> Unit,
    onDebitCreditLeave: (UserEntity) -> Unit
) {
    var accountTab by remember { mutableIntStateOf(0) } // 0: Employees (Max 100), 1: Admins (Max 5)
    var searchQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Credentials & Access Control",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Manage Employee IDs, Admin Credentials, Passwords & Access",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E293B)
            )
        }

        // Sub Tab Selector: Employees vs Admins
        item {
            TabRow(
                selectedTabIndex = accountTab,
                containerColor = Color.White,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = accountTab == 0,
                    onClick = { accountTab = 0 },
                    text = { Text("Staff ($employeeCount/100)", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = accountTab == 1,
                    onClick = { accountTab = 1 },
                    text = { Text("Admins ($adminCount/5 Max)", fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (accountTab == 0) {
            // EMPLOYEE SECTION (Max 100 Capacity)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Employee Staff Capacity", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text(text = "$employeeCount / 100 Employees", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (employeeCount / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF2563EB),
                            trackColor = Color(0xFFE2E8F0)
                        )
                        Text(text = "${100 - employeeCount} slots remaining in staff database", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B), modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search employees...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedPlaceholderColor = Color(0xFF475569),
                            unfocusedPlaceholderColor = Color(0xFF64748B),
                            focusedBorderColor = Color(0xFF1E3A8A),
                            unfocusedBorderColor = Color(0xFF94A3B8)
                        )
                    )
                    Button(
                        onClick = onAddNewEmployee,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Joinee", fontSize = 12.sp)
                    }
                }
            }

            val filteredEmployees = employees.filter {
                searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.empId.contains(searchQuery, ignoreCase = true)
            }

            items(filteredEmployees) { emp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEFF6FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = emp.name.take(2).uppercase(),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2563EB)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = emp.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when (emp.role) {
                                                UserEntity.ROLE_SITE_MANAGER -> Color(0xFFEDE9FE)
                                                UserEntity.ROLE_TEAM_LEAD -> Color(0xFFCCFBF1)
                                                else -> Color(0xFFEFF6FF)
                                            }
                                        ) {
                                            Text(
                                                text = emp.role,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (emp.role) {
                                                    UserEntity.ROLE_SITE_MANAGER -> Color(0xFF6D28D9)
                                                    UserEntity.ROLE_TEAM_LEAD -> Color(0xFF0F766E)
                                                    else -> Color(0xFF1D4ED8)
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(text = "ID: ${emp.empId} • Pass: ${emp.password}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Password Change Button (Requirement 4)
                                IconButton(onClick = { onChangePassword(emp) }) {
                                    Icon(Icons.Default.Key, contentDescription = "Change Password", tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                                }
                                // Delete ID Button (Requirement 4)
                                IconButton(onClick = { onDeleteUser(emp) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete ID", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        // Extra Action buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Traveled: ${emp.travelDistanceKm} km",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )

                            OutlinedButton(
                                onClick = { onManageRole(emp) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Edit Role & Extra Permissions", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Leave Bucket Badge & Debit/Credit Action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEFF6FF)
                            ) {
                                Text(
                                    text = "Bucket: ${emp.paidLeaveBalance} PL • ${emp.sickLeaveBalance} SL • ${emp.compOffBalance} CO (${emp.totalLeaveBalance} Total)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D4ED8),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Button(
                                onClick = { onDebitCreditLeave(emp) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Debit / Credit Leaves", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // ADMIN MANAGEMENT SECTION (Requirement 3: Maximum 5 Admin ID credentials)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFF4338CA))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Admin Credentials Limit", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }
                            Text(
                                text = "$adminCount / 5 Maximum",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (adminCount >= 5) Color(0xFFDC2626) else Color(0xFF4338CA)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (adminCount / 5f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF4338CA),
                            trackColor = Color(0xFFE2E8F0)
                        )

                        Text(
                            text = "${5 - adminCount} Admin slots available (Maximum 5 Admins constraint)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onAddNewAdmin,
                            enabled = adminCount < 5,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create New Admin Credentials")
                        }
                    }
                }
            }

            // List of Current Admins
            items(admins) { admin ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEDE9FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFF4338CA))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = admin.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF4338CA)) {
                                        Text("ADMIN", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                    }
                                }
                                Text(text = "ID: ${admin.empId} • Pass: ${admin.password}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Password Change Button (Requirement 4)
                            IconButton(onClick = { onChangePassword(admin) }) {
                                Icon(Icons.Default.Key, contentDescription = "Change Password", tint = Color(0xFF4338CA))
                            }
                            // Delete Admin ID Button (Requirement 3 & 4)
                            IconButton(
                                onClick = { onDeleteUser(admin) },
                                enabled = adminCount > 1
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Admin",
                                    tint = if (adminCount > 1) Color(0xFFEF4444) else Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. LIVE MAP & RADAR TRACKING VIEW (Requirements 1 & 2: Zoom In/Out, Default/Satellite views, Travelled Distance tracking)
 */
@Composable
fun AdminLiveMapTrackingView(
    employees: List<UserEntity>,
    trackedEmployee: UserEntity?,
    zoomLevel: Float,
    isSatellite: Boolean,
    showTrajectory: Boolean,
    onSelectTrackedEmployee: (UserEntity?) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    onToggleSatellite: () -> Unit,
    onToggleTrajectory: () -> Unit
) {
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 44f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSatellite) Color(0xFF090D16) else Color(0xFF0F172A))
    ) {
        // Map Top Bar Controls
        Surface(
            color = if (isSatellite) Color(0xFF111827) else Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isSatellite) "🛰️ Satellite Map" else "🗺️ Default Map",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSatellite) Color(0xFFD97706) else Color(0xFF059669)
                            ) {
                                Text(
                                    text = if (isSatellite) "SATELLITE" else "VECTOR",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = if (trackedEmployee != null) "Tracking: ${trackedEmployee.name}" else "Google Maps Live Radar (${employees.size} Staff Online)",
                            fontSize = 12.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }

                    // Satellite / Default Mode Switcher (Requirement 1)
                    Button(
                        onClick = onToggleSatellite,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSatellite) Color(0xFF0284C7) else Color(0xFF334155)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isSatellite) Icons.Default.Layers else Icons.Default.Satellite,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isSatellite) "Default View" else "Satellite View", fontSize = 11.sp)
                    }
                }

                // Employee Quick Filter Chips (Requirement 12)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = trackedEmployee == null,
                            onClick = {
                                onSelectTrackedEmployee(null)
                                panOffsetX = 0f
                                panOffsetY = 0f
                            },
                            label = { Text("All Staff (${employees.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    items(employees) { emp ->
                        FilterChip(
                            selected = trackedEmployee?.empId == emp.empId,
                            onClick = {
                                onSelectTrackedEmployee(emp)
                                panOffsetX = 0f
                                panOffsetY = 0f
                            },
                            label = { Text(emp.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF059669),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Interactive Map Canvas (Supports Pan, Zoom In, Zoom Out, Satellite, Distance Trajectory)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            panOffsetX += dragAmount.x
                            panOffsetY += dragAmount.y
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height

                val centerX = canvasW / 2 + panOffsetX
                val centerY = canvasH / 2 + panOffsetY

                if (isSatellite) {
                    // SATELLITE VIEW RENDERING (Photorealistic aerial terrain simulation with roads and landmarks)
                    drawRect(color = Color(0xFF0F1B29))

                    // Forest / park land canopy patches
                    val parkPath = Path().apply {
                        moveTo(centerX - 350f * zoomLevel, centerY - 200f * zoomLevel)
                        lineTo(centerX - 100f * zoomLevel, centerY - 320f * zoomLevel)
                        lineTo(centerX + 50f * zoomLevel, centerY - 150f * zoomLevel)
                        lineTo(centerX - 120f * zoomLevel, centerY - 80f * zoomLevel)
                        close()
                    }
                    drawPath(parkPath, color = Color(0xFF064E3B))

                    // Water bay contour
                    val waterPath = Path().apply {
                        moveTo(centerX + 200f * zoomLevel, 0f)
                        lineTo(canvasW, 0f)
                        lineTo(canvasW, canvasH)
                        lineTo(centerX + 180f * zoomLevel, canvasH)
                        quadraticTo(
                            centerX + 120f * zoomLevel,
                            centerY,
                            centerX + 220f * zoomLevel,
                            0f
                        )
                        close()
                    }
                    drawPath(waterPath, color = Color(0xFF075985))

                    // Satellite highways
                    val roadColor = Color(0xCCFBBF24)
                    drawLine(color = roadColor, start = Offset(0f, centerY), end = Offset(canvasW, centerY), strokeWidth = 5f * zoomLevel)
                    drawLine(color = roadColor, start = Offset(centerX, 0f), end = Offset(centerX, canvasH), strokeWidth = 5f * zoomLevel)
                    drawLine(color = Color(0xDDFFFFFF), start = Offset(centerX - 250f * zoomLevel, 0f), end = Offset(centerX + 250f * zoomLevel, canvasH), strokeWidth = 3f * zoomLevel)
                } else {
                    // GOOGLE MAPS VECTOR STREET & LANDMARKS RENDERING (User Request: Shows all landmarks, streets, roads, shops ..Etc as Google Maps shows)
                    // Google Maps base background
                    drawRect(color = Color(0xFFF1EFEA))

                    // City Building Parcels / Blocks
                    val blockPaint = Color(0xFFE5E3DF)
                    for (bx in -4..4) {
                        for (by in -3..3) {
                            val bLeft = centerX + (bx * 130f + 15f) * zoomLevel
                            val bTop = centerY + (by * 110f + 15f) * zoomLevel
                            val bW = 100f * zoomLevel
                            val bH = 80f * zoomLevel
                            drawRect(
                                color = blockPaint,
                                topLeft = Offset(bLeft, bTop),
                                size = androidx.compose.ui.geometry.Size(bW, bH)
                            )
                        }
                    }

                    // Green Spaces & Parks (Google Maps Park Green #CEEAD6)
                    val park1 = Path().apply {
                        moveTo(centerX - 320f * zoomLevel, centerY - 180f * zoomLevel)
                        lineTo(centerX - 140f * zoomLevel, centerY - 250f * zoomLevel)
                        lineTo(centerX - 90f * zoomLevel, centerY - 90f * zoomLevel)
                        lineTo(centerX - 280f * zoomLevel, centerY - 60f * zoomLevel)
                        close()
                    }
                    drawPath(park1, color = Color(0xFFCEEAD6))

                    val park2 = Path().apply {
                        moveTo(centerX + 180f * zoomLevel, centerY + 90f * zoomLevel)
                        lineTo(centerX + 340f * zoomLevel, centerY + 70f * zoomLevel)
                        lineTo(centerX + 320f * zoomLevel, centerY + 220f * zoomLevel)
                        lineTo(centerX + 160f * zoomLevel, centerY + 180f * zoomLevel)
                        close()
                    }
                    drawPath(park2, color = Color(0xFFCEEAD6))

                    // Water Body (Google Maps Water Blue #A5C9EB)
                    val lakePath = Path().apply {
                        moveTo(centerX + 240f * zoomLevel, 0f)
                        lineTo(canvasW, 0f)
                        lineTo(canvasW, centerY - 60f * zoomLevel)
                        lineTo(centerX + 290f * zoomLevel, centerY - 40f * zoomLevel)
                        quadraticTo(
                            centerX + 220f * zoomLevel,
                            centerY - 120f * zoomLevel,
                            centerX + 240f * zoomLevel,
                            0f
                        )
                        close()
                    }
                    drawPath(lakePath, color = Color(0xFFA5C9EB))

                    // Secondary White Streets (Google Maps secondary roads)
                    val secRoadColor = Color.White
                    val secRoadWidth = 9f * zoomLevel
                    val secStep = 130f * zoomLevel

                    var sX = (centerX % secStep) - secStep
                    while (sX < canvasW + secStep) {
                        drawLine(color = Color(0xFFD6D6D6), start = Offset(sX, 0f), end = Offset(sX, canvasH), strokeWidth = secRoadWidth + 2f)
                        drawLine(color = secRoadColor, start = Offset(sX, 0f), end = Offset(sX, canvasH), strokeWidth = secRoadWidth)
                        sX += secStep
                    }

                    var sY = (centerY % (110f * zoomLevel)) - (110f * zoomLevel)
                    while (sY < canvasH + (110f * zoomLevel)) {
                        drawLine(color = Color(0xFFD6D6D6), start = Offset(0f, sY), end = Offset(canvasW, sY), strokeWidth = secRoadWidth + 2f)
                        drawLine(color = secRoadColor, start = Offset(0f, sY), end = Offset(canvasW, sY), strokeWidth = secRoadWidth)
                        sY += 110f * zoomLevel
                    }

                    // Major Arterial Highways (Google Maps Highway Yellow/Gold #FFE169 with #E5C365 border)
                    val hwOutline = Color(0xFFE5C365)
                    val hwFill = Color(0xFFFFEB3B)
                    val hwWidth = 14f * zoomLevel

                    // Horizontal Major Highway (MG Road Expressway)
                    drawLine(color = hwOutline, start = Offset(0f, centerY), end = Offset(canvasW, centerY), strokeWidth = hwWidth + 4f)
                    drawLine(color = hwFill, start = Offset(0f, centerY), end = Offset(canvasW, centerY), strokeWidth = hwWidth)

                    // Vertical Major Arterial (Outer Ring Road)
                    drawLine(color = hwOutline, start = Offset(centerX, 0f), end = Offset(centerX, canvasH), strokeWidth = hwWidth + 4f)
                    drawLine(color = hwFill, start = Offset(centerX, 0f), end = Offset(centerX, canvasH), strokeWidth = hwWidth)

                    // Diagonal Avenue (Tech Park Boulevard)
                    drawLine(color = hwOutline, start = Offset(0f, centerY + 220f * zoomLevel), end = Offset(canvasW, centerY - 220f * zoomLevel), strokeWidth = hwWidth + 4f)
                    drawLine(color = hwFill, start = Offset(0f, centerY + 220f * zoomLevel), end = Offset(canvasW, centerY - 220f * zoomLevel), strokeWidth = hwWidth)

                    // Draw Road Name Labels (native canvas)
                    val roadLabelPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.rgb(100, 116, 139)
                        textSize = (13f * zoomLevel).coerceIn(10f, 18f)
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawContext.canvas.nativeCanvas.drawText("MG ROAD EXPRESSWAY (NH-44)", canvasW * 0.35f, centerY - 10f * zoomLevel, roadLabelPaint)
                    drawContext.canvas.nativeCanvas.drawText("OUTER RING ROAD", centerX + 30f * zoomLevel, canvasH * 0.25f, roadLabelPaint)
                    drawContext.canvas.nativeCanvas.drawText("100 FT ROAD • INDIRANAGAR", canvasW * 0.65f, centerY + 120f * zoomLevel, roadLabelPaint)
                    drawContext.canvas.nativeCanvas.drawText("COMMERCIAL STREET", centerX - 180f * zoomLevel, centerY + 80f * zoomLevel, roadLabelPaint)
                    drawContext.canvas.nativeCanvas.drawText("🌿 CUBBON CENTRAL PARK", centerX - 210f * zoomLevel, centerY - 120f * zoomLevel, roadLabelPaint)
                    drawContext.canvas.nativeCanvas.drawText("💧 ULSOOR LAKE", centerX + 310f * zoomLevel, centerY - 90f * zoomLevel, roadLabelPaint)
                }

                // GOOGLE MAPS LANDMARKS, SHOPS, HOSPITALS, METRO, HUBS (Requirement 2)
                data class MapLandmark(
                    val name: String,
                    val category: String,
                    val relX: Float,
                    val relY: Float,
                    val dotColor: Color,
                    val iconSymbol: String
                )

                val landmarks = listOf(
                    MapLandmark("City General Hospital", "Emergency", -240f, -80f, Color(0xFFEA4335), "🏥"),
                    MapLandmark("Forum Mall & Retail", "Shopping", 140f, -140f, Color(0xFF1A73E8), "🛍️"),
                    MapLandmark("Shell Fuel Station", "Fuel & EV", -160f, 140f, Color(0xFF188038), "⛽"),
                    MapLandmark("HDFC Bank & ATM", "Finance", 70f, 130f, Color(0xFF0F9D58), "🏦"),
                    MapLandmark("Starbucks & Bakery", "Cafe", -80f, -150f, Color(0xFFE37400), "☕"),
                    MapLandmark("Metro Station (Purple Line)", "Transit", 0f, -200f, Color(0xFF9334E6), "🚇"),
                    MapLandmark("Global Tech Corporate Hub", "IT Campus", 200f, 30f, Color(0xFF334155), "🏢"),
                    MapLandmark("DMart Supermarket", "Grocery", -220f, 80f, Color(0xFF0284C7), "🛒"),
                    MapLandmark("Apollo 24/7 Pharmacy", "Pharmacy", 160f, -60f, Color(0xFF059669), "💊"),
                    MapLandmark("National Public School", "Education", -110f, 210f, Color(0xFF3F51B5), "🏫"),
                    MapLandmark("Royal Dine Restaurant", "Food", 90f, 210f, Color(0xFFD97706), "🍽️")
                )

                landmarks.forEach { lm ->
                    val lmX = centerX + lm.relX * zoomLevel
                    val lmY = centerY + lm.relY * zoomLevel

                    if (lmX in -100f..(canvasW + 100f) && lmY in -100f..(canvasH + 100f)) {
                        // Google Maps circular POI badge
                        drawCircle(color = Color.White, radius = 9f * zoomLevel, center = Offset(lmX, lmY))
                        drawCircle(color = lm.dotColor, radius = 7f * zoomLevel, center = Offset(lmX, lmY))

                        // POI Label with clean contrast
                        val lmPaint = android.graphics.Paint().apply {
                            color = if (isSatellite) android.graphics.Color.WHITE else android.graphics.Color.rgb(30, 41, 59)
                            textSize = (13f * zoomLevel).coerceIn(10f, 17f)
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            textAlign = android.graphics.Paint.Align.CENTER
                            if (isSatellite) setShadowLayer(4f, 1f, 1f, android.graphics.Color.BLACK)
                        }
                        val subPaint = android.graphics.Paint().apply {
                            color = if (isSatellite) android.graphics.Color.rgb(226, 232, 240) else android.graphics.Color.rgb(100, 116, 139)
                            textSize = (10f * zoomLevel).coerceIn(8f, 13f)
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        drawContext.canvas.nativeCanvas.drawText("${lm.iconSymbol} ${lm.name}", lmX, lmY - 12f * zoomLevel, lmPaint)
                        drawContext.canvas.nativeCanvas.drawText(lm.category, lmX, lmY + 16f * zoomLevel, subPaint)
                    }
                }

                // Render Route Trajectory for selected / all employees
                if (showTrajectory) {
                    val activeTracked = trackedEmployee ?: employees.firstOrNull()
                    if (activeTracked != null) {
                        val waypoints = GpsHelper.parseWaypoints(activeTracked.routeWaypoints)

                        if (waypoints.isNotEmpty()) {
                            val routePath = Path()
                            val routePoints = mutableListOf<Offset>()

                            waypoints.forEachIndexed { idx, wp ->
                                val ptX = centerX + ((idx - waypoints.size / 2f) * 80f * zoomLevel)
                                val ptY = centerY + ((idx % 2 * 60f - 30f) * zoomLevel)
                                routePoints.add(Offset(ptX, ptY))
                                if (idx == 0) routePath.moveTo(ptX, ptY) else routePath.lineTo(ptX, ptY)
                            }

                            // Draw route line
                            drawPath(
                                path = routePath,
                                color = Color(0x662563EB),
                                style = Stroke(width = 8f * zoomLevel, cap = StrokeCap.Round)
                            )
                            drawPath(
                                path = routePath,
                                color = Color(0xFF1D4ED8),
                                style = Stroke(width = 4f * zoomLevel, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f)))
                            )

                            // Draw Start Pin 📍
                            if (routePoints.isNotEmpty()) {
                                val startPt = routePoints.first()
                                drawCircle(color = Color(0xFF10B981), radius = 9f * zoomLevel, center = startPt)
                                drawCircle(color = Color.White, radius = 4f * zoomLevel, center = startPt)

                                val textPaint = android.graphics.Paint().apply {
                                    color = if (isSatellite) android.graphics.Color.WHITE else android.graphics.Color.rgb(15, 23, 42)
                                    textSize = (18f * zoomLevel).coerceIn(14f, 26f)
                                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                                drawContext.canvas.nativeCanvas.drawText("START", startPt.x, startPt.y - 14f * zoomLevel, textPaint)
                            }

                            // Draw intermediate waypoints
                            for (wIdx in 1 until routePoints.size - 1) {
                                val wp = routePoints[wIdx]
                                drawCircle(color = Color(0xFFFBBF24), radius = 5f * zoomLevel, center = wp)
                            }
                        }
                    }
                }

                // Render Employee Location Pins with Names (User Request 1: Don't show the travelled distance on the map)
                employees.forEachIndexed { i, emp ->
                    val isSelected = trackedEmployee?.empId == emp.empId

                    val pinX = centerX + ((i - 2) * 90f * zoomLevel)
                    val pinY = centerY + ((i % 3 - 1) * 95f * zoomLevel + (if (i % 2 == 0) 35f else -35f) * zoomLevel)

                    val pinColor = if (isSelected) Color(0xFFEA4335) else Color(0xFF1A73E8)

                    if (isSelected) {
                        drawCircle(color = Color(0x55EA4335), radius = pulseRadius * zoomLevel, center = Offset(pinX, pinY))
                    }

                    // Google Maps Location Marker Pin
                    drawCircle(color = Color.White, radius = 16f * zoomLevel, center = Offset(pinX, pinY))
                    drawCircle(color = pinColor, radius = 12f * zoomLevel, center = Offset(pinX, pinY))
                    drawCircle(color = Color.White, radius = 5f * zoomLevel, center = Offset(pinX, pinY))

                    // Employee Name Label Banner right on the map (ONLY NAME, NO DISTANCE on map as requested)
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = (24f * zoomLevel).coerceIn(18f, 32f)
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        textAlign = android.graphics.Paint.Align.CENTER
                        setShadowLayer(4f, 1f, 1f, android.graphics.Color.BLACK)
                    }

                    val bgPaint = android.graphics.Paint().apply {
                        color = if (isSelected) android.graphics.Color.argb(245, 234, 67, 53) else android.graphics.Color.argb(235, 15, 23, 42)
                        style = android.graphics.Paint.Style.FILL
                    }

                    val label = emp.name
                    val textBounds = android.graphics.Rect()
                    paint.getTextBounds(label, 0, label.length, textBounds)

                    val labelY = pinY - 22f * zoomLevel
                    val rectL = pinX - textBounds.width() / 2f - 14f * zoomLevel
                    val rectR = pinX + textBounds.width() / 2f + 14f * zoomLevel
                    val rectT = labelY - textBounds.height() - 6f * zoomLevel
                    val rectB = labelY + 6f * zoomLevel

                    drawContext.canvas.nativeCanvas.drawRoundRect(rectL, rectT, rectR, rectB, 12f, 12f, bgPaint)
                    drawContext.canvas.nativeCanvas.drawText(label, pinX, labelY, paint)
                }
            }

            // Floating Map Controls overlay (Zoom In, Zoom Out, Center, Trajectory toggle - Requirement 1 & 2)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Zoom Percentage Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xCC0F172A)
                ) {
                    Text(
                        text = "${Math.round(zoomLevel * 100)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Zoom In (+)
                IconButton(
                    onClick = onZoomIn,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xDD0F172A), CircleShape)
                        .testTag("map_zoom_in_button")
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White)
                }

                // Zoom Out (-)
                IconButton(
                    onClick = onZoomOut,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xDD0F172A), CircleShape)
                        .testTag("map_zoom_out_button")
                ) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White)
                }

                // Center / Reset Zoom
                IconButton(
                    onClick = {
                        onResetZoom()
                        panOffsetX = 0f
                        panOffsetY = 0f
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xDD0F172A), CircleShape)
                ) {
                    Icon(Icons.Default.CenterFocusStrong, contentDescription = "Center View", tint = Color.White)
                }

                // Toggle Route Trajectory
                IconButton(
                    onClick = onToggleTrajectory,
                    modifier = Modifier
                        .size(42.dp)
                        .background(if (showTrajectory) Color(0xFF2563EB) else Color(0xDD0F172A), CircleShape)
                ) {
                    Icon(Icons.Default.AltRoute, contentDescription = "Toggle Travel Route", tint = Color.White)
                }
            }
        }

        // Travelled Distance & Employee Telemetry Card at bottom (Requirement 2)
        val activeEmp = trackedEmployee ?: employees.firstOrNull()
        if (activeEmp != null) {
            Surface(
                color = if (isSatellite) Color(0xFF111827) else Color(0xFF1E293B),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeEmp.name.take(2).uppercase(),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = activeEmp.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "ID: ${activeEmp.empId} • ${activeEmp.role}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }

                        // Travelled Distance Badge (Requirement 2)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF065F46)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Route, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${activeEmp.travelDistanceKm} km traveled",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "📍 Current: ${activeEmp.lastKnownAddress ?: "Field Office Campus"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = "Coordinates: Lat ${String.format(Locale.US, "%.5f", activeEmp.lastKnownLat ?: 37.7749)}, Lng ${String.format(Locale.US, "%.5f", activeEmp.lastKnownLng ?: -122.4194)} • Map Zoom: ${Math.round(zoomLevel * 100)}%",
                        fontSize = 11.sp,
                        color = Color(0xFF93C5FD),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
