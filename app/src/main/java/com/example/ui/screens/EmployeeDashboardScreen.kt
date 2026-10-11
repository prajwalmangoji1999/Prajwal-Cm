package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.MonthAttendanceCount
import com.example.data.model.UserEntity
import com.example.ui.components.AestheticBackground
import com.example.ui.viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDashboardScreen(
    viewModel: MainViewModel,
    onOpenGpsCamera: (mode: String) -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val todayRecord by viewModel.todayRecord.collectAsState()
    val history by viewModel.employeeHistory.collectAsState()
    val currentMonthStats by viewModel.currentEmployeeMonthlyStats.collectAsState()
    val currentEmployeeLeaveRequests by viewModel.currentEmployeeLeaveRequests.collectAsState()

    val currentMonthName = viewModel.currentMonthYear
    val currentMonthRecords = remember(history, currentMonthName) {
        history.filter { it.monthYear.equals(currentMonthName, ignoreCase = true) }
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Today, 1: Leaves, 2: Current Month Logs, 3: Profile

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentUser?.name ?: "Employee Portal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "ID: ${currentUser?.empId ?: ""} • DMY Attendence",
                            fontSize = 12.sp,
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
                        modifier = Modifier.testTag("logout_button")
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
                    icon = { Icon(Icons.Default.AccessTime, contentDescription = "Today") },
                    label = { Text("Today") },
                    modifier = Modifier.testTag("nav_today")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.EventNote, contentDescription = "Leaves") },
                    label = { Text("Leaves") },
                    modifier = Modifier.testTag("nav_leaves")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Month Logs") },
                    label = { Text("Month Logs") },
                    modifier = Modifier.testTag("nav_month_logs")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        AestheticBackground(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> TodayAttendanceView(
                    currentUser = currentUser,
                    todayRecord = todayRecord,
                    currentMonthStats = currentMonthStats,
                    onOpenGpsCamera = onOpenGpsCamera,
                    onNavigateToLeaves = { selectedTab = 1 }
                )
                1 -> EmployeeLeavesView(
                    currentUser = currentUser,
                    currentMonthStats = currentMonthStats,
                    leaveRequests = currentEmployeeLeaveRequests,
                    onSubmitLeaveRequest = { type, start, end, count, reason, docPath, onResult ->
                        viewModel.submitLeaveRequest(type, start, end, count, reason, docPath, onResult)
                    }
                )
                2 -> EmployeeCurrentMonthLogsView(
                    currentMonthRecords = currentMonthRecords,
                    currentMonthName = currentMonthName
                )
                3 -> EmployeeProfileView(currentUser = currentUser, currentMonthStats = currentMonthStats)
            }
        }
    }
}

@Composable
fun MonthlyCountBadge(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    badgeColor: Color,
    containerColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = badgeColor
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )
        }
    }
}

@Composable
fun TodayAttendanceView(
    currentUser: UserEntity?,
    todayRecord: AttendanceRecordEntity?,
    currentMonthStats: MonthAttendanceCount,
    onOpenGpsCamera: (mode: String) -> Unit,
    onNavigateToLeaves: () -> Unit = {}
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()) }
    val todayDateStr = remember { dateFormat.format(Date()) }

    var liveCurrentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            liveCurrentTime = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Header
        item {
            Text(
                text = todayDateStr,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Daily GPS Attendance",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        // Current Month Attendance Count Card (Requirement: Employee login dashboard shows current month attendance count)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("employee_current_month_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentMonthStats.monthYear,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Current Month Attendance Count",
                                    fontSize = 11.sp,
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
                                text = "Total: ${currentMonthStats.totalDays} Days",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MonthlyCountBadge(
                            modifier = Modifier.weight(1f),
                            label = "Present [P]",
                            count = currentMonthStats.presentCount,
                            badgeColor = Color(0xFF059669),
                            containerColor = Color(0xFFD1FAE5)
                        )
                        MonthlyCountBadge(
                            modifier = Modifier.weight(1f),
                            label = "Half Day [HD]",
                            count = currentMonthStats.halfDayCount,
                            badgeColor = Color(0xFFD97706),
                            containerColor = Color(0xFFFEF3C7)
                        )
                        MonthlyCountBadge(
                            modifier = Modifier.weight(1f),
                            label = "Absent [A]",
                            count = currentMonthStats.absentCount,
                            badgeColor = Color(0xFFDC2626),
                            containerColor = Color(0xFFFEE2E2)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Total Paid Days Attendance Count (User Request 2: "where its turns into paid days which will show in the dashboard of both admin and employee login dashboard, total paid days attendance count")
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL PAID DAYS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )
                                Text(
                                    text = "${currentMonthStats.totalPaidDaysFormatted} Days",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1D4ED8)
                                )
                            }

                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFD1FAE5)) {
                                Text(
                                    text = "Paid Leaves: ${currentMonthStats.paidLeaveCount}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Logged Hours this Month:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = currentMonthStats.totalHoursFormatted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8)
                        )
                    }
                }
            }
        }

        // Leave Bucket Summary in Employee ID Dashboard (User Request 2: "Every employee will get 1 Paid leave per month, 1 Sick leave per 3 months... This count should show in the employee ID dashboard. Every month these count should carry forward...")
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLeaves() }
                    .testTag("employee_leave_bucket_quick_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Leave Bucket Balance",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Text(
                            text = "Apply Leaves →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD1FAE5)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${currentUser?.paidLeaveBalance ?: 0}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF065F46))
                                Text("Paid Leave (PL)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                Text("1/month", fontSize = 9.sp, color = Color(0xFF047857))
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEE2E2)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${currentUser?.sickLeaveBalance ?: 0}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF991B1B))
                                Text("Sick Leave (SL)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                Text("1/3 months", fontSize = 9.sp, color = Color(0xFFB91C1C))
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${currentUser?.compOffBalance ?: 0}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF92400E))
                                Text("Comp Off (CO)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                Text("Carried Fwd", fontSize = 9.sp, color = Color(0xFFB45309))
                            }
                        }
                    }
                }
            }
        }

        // Attendance Status Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("attendance_status_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        todayRecord == null -> Color(0xFFF1F5F9)
                        todayRecord.isClockedOut -> when (todayRecord.status) {
                            AttendanceRecordEntity.STATUS_PRESENT -> Color(0xFFD1FAE5)
                            AttendanceRecordEntity.STATUS_HALF_DAY -> Color(0xFFFEF3C7)
                            else -> Color(0xFFFEE2E2)
                        }
                        else -> Color(0xFFDBEAFE) // Clocked in currently
                    }
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TODAY'S STATUS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        val badgeColor = when {
                            todayRecord == null -> Color(0xFF1E293B)
                            todayRecord.isClockedOut -> when (todayRecord.status) {
                                AttendanceRecordEntity.STATUS_PRESENT -> Color(0xFF065F46)
                                AttendanceRecordEntity.STATUS_HALF_DAY -> Color(0xFF92400E)
                                else -> Color(0xFF991B1B)
                            }
                            else -> Color(0xFF1E40AF)
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White
                        ) {
                            Text(
                                text = when {
                                    todayRecord == null -> "Not Clocked In"
                                    todayRecord.isClockedOut -> todayRecord.status
                                    else -> "In Progress"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (todayRecord != null && !todayRecord.isClockedOut) {
                        // Live Duration counter
                        val elapsedMillis = (liveCurrentTime - todayRecord.loginTime).coerceAtLeast(0L)
                        val elapsedMins = elapsedMillis / (1000 * 60)
                        val hrs = elapsedMins / 60
                        val mins = elapsedMins % 60
                        val secs = (elapsedMillis / 1000) % 60

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = String.format(Locale.US, "%02dh : %02dm : %02ds", hrs, mins, secs),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A)
                            )
                        }
                        Text(
                            text = "Target for Full Day (Present): 8h 45m | Half Day: 4h 15m",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else if (todayRecord != null && todayRecord.isClockedOut) {
                        Text(
                            text = "Shift Duration: ${AttendanceRecordEntity.formatDuration(todayRecord.netMinutes)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Shift marked as: ${todayRecord.status}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    } else {
                        Text(
                            text = "You haven't clocked in today",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Use the live GPS camera below to check in.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // Login & Logout Details Timeline Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Today's GPS Punch Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Login row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = if (todayRecord != null) Color(0xFFD1FAE5) else Color(0xFFF1F5F9)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (todayRecord != null) Color(0xFF059669) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Check In (Login)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            if (todayRecord != null) {
                                Text(
                                    text = timeFormat.format(Date(todayRecord.loginTime)),
                                    fontSize = 13.sp,
                                    color = Color(0xFF2563EB),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "📍 ${todayRecord.loginAddress}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E293B),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            } else {
                                Text(
                                    text = "Pending clock-in",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                            }
                        }

                        // Photo preview if available
                        if (todayRecord != null && todayRecord.loginPhotoPath.isNotBlank()) {
                            val photoFile = File(todayRecord.loginPhotoPath)
                            if (photoFile.exists()) {
                                val bmp = BitmapFactory.decodeFile(photoFile.absolutePath)
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "Check-in Photo",
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Logout row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = if (todayRecord?.logoutTime != null) Color(0xFFFEE2E2) else Color(0xFFF1F5F9)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = null,
                                    tint = if (todayRecord?.logoutTime != null) Color(0xFFDC2626) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Check Out (Logout)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            if (todayRecord?.logoutTime != null) {
                                Text(
                                    text = timeFormat.format(Date(todayRecord.logoutTime)),
                                    fontSize = 13.sp,
                                    color = Color(0xFFDC2626),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "📍 ${todayRecord.logoutAddress ?: ""}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E293B),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            } else {
                                Text(
                                    text = if (todayRecord != null) "In progress (Not checked out)" else "Pending",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                            }
                        }

                        // Photo preview if available
                        if (todayRecord?.logoutPhotoPath?.isNotBlank() == true) {
                            val photoFile = File(todayRecord.logoutPhotoPath)
                            if (photoFile.exists()) {
                                val bmp = BitmapFactory.decodeFile(photoFile.absolutePath)
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "Check-out Photo",
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Shutter Button
        item {
            if (todayRecord == null) {
                // Must Clock In
                Button(
                    onClick = { onOpenGpsCamera("CHECK_IN") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("clock_in_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Take GPS Photo & Clock In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (!todayRecord.isClockedOut) {
                // Must Clock Out
                Button(
                    onClick = { onOpenGpsCamera("CHECK_OUT") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("clock_out_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E))
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Take GPS Photo & Clock Out",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Completed today
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Attendance completed for today. See you tomorrow!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }
        }

        // Attendance Rules Information Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Rules",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Attendance Duration Rules",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "• Present: Min 8:45 net login hours\n• Half Day: Min 4:15 net login hours\n• Absent: Less than 4:15 login hours\n• Photos: Live GPS camera capture only (Gallery upload disabled)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(top = 4.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeCurrentMonthLogsView(
    currentMonthRecords: List<AttendanceRecordEntity>,
    currentMonthName: String
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "My Attendance Data",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Current Month Attendance Only ($currentMonthName) • ${currentMonthRecords.size} Record(s)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2563EB),
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
            )
        }

        if (currentMonthRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "No attendance records found for the current month ($currentMonthName).",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(currentMonthRecords) { record ->
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
                            Text(
                                text = record.dateString,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )

                            val (bgColor, textColor) = when (record.status) {
                                AttendanceRecordEntity.STATUS_PRESENT -> Pair(Color(0xFFD1FAE5), Color(0xFF065F46))
                                AttendanceRecordEntity.STATUS_HALF_DAY -> Pair(Color(0xFFFEF3C7), Color(0xFF92400E))
                                AttendanceRecordEntity.STATUS_ABSENT -> Pair(Color(0xFFFEE2E2), Color(0xFF991B1B))
                                AttendanceRecordEntity.STATUS_PAID_LEAVE,
                                AttendanceRecordEntity.STATUS_SICK_LEAVE,
                                AttendanceRecordEntity.STATUS_COMP_OFF -> Pair(Color(0xFFD1FAE5), Color(0xFF047857))
                                else -> Pair(Color(0xFFDBEAFE), Color(0xFF1E40AF))
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = bgColor
                            ) {
                                Text(
                                    text = if (record.isPaidDay && record.status != AttendanceRecordEntity.STATUS_PRESENT && record.status != AttendanceRecordEntity.STATUS_HALF_DAY) {
                                        "PAID DAY (${record.status})"
                                    } else {
                                        record.status
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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
                        }

                        if (record.loginAddress.isNotBlank()) {
                            Text(
                                text = "📍 ${record.loginAddress}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeHistoryView(history: List<AttendanceRecordEntity>) {
    EmployeeCurrentMonthLogsView(currentMonthRecords = history, currentMonthName = "Current Month")
}

@Composable
fun EmployeeProfileView(
    currentUser: UserEntity?,
    currentMonthStats: MonthAttendanceCount? = null
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser?.name?.take(2)?.uppercase() ?: "EM",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentUser?.name ?: "Employee",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEFF6FF),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "EMP ID: ${currentUser?.empId ?: ""}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        if (currentMonthStats != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${currentMonthStats.monthYear} Attendance Summary",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MonthlyCountBadge(
                                modifier = Modifier.weight(1f),
                                label = "Present [P]",
                                count = currentMonthStats.presentCount,
                                badgeColor = Color(0xFF059669),
                                containerColor = Color(0xFFD1FAE5)
                            )
                            MonthlyCountBadge(
                                modifier = Modifier.weight(1f),
                                label = "Half Day [HD]",
                                count = currentMonthStats.halfDayCount,
                                badgeColor = Color(0xFFD97706),
                                containerColor = Color(0xFFFEF3C7)
                            )
                            MonthlyCountBadge(
                                modifier = Modifier.weight(1f),
                                label = "Absent [A]",
                                count = currentMonthStats.absentCount,
                                badgeColor = Color(0xFFDC2626),
                                containerColor = Color(0xFFFEE2E2)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Total Logged: ${currentMonthStats.totalDays} Days • Hours: ${currentMonthStats.totalHoursFormatted}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Employment Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileField(label = "Joining Date", value = currentUser?.joiningDate?.ifBlank { "Not provided" } ?: "--")
                    ProfileField(label = "Date of Birth", value = currentUser?.dob?.ifBlank { "Not provided" } ?: "--")
                    ProfileField(label = "Current Address", value = currentUser?.currentAddress?.ifBlank { "Not provided" } ?: "--")
                    ProfileField(label = "Permanent Address", value = currentUser?.permanentAddress?.ifBlank { "Not provided" } ?: "--")
                    ProfileField(label = "Data Policy", value = "90 Days Attendance Retention Active")
                }
            }
        }
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 14.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
    }
}
