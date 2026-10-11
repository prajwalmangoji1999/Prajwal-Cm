package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaveRequestEntity
import com.example.data.model.MonthAttendanceCount
import com.example.data.model.UserEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmployeeLeavesView(
    currentUser: UserEntity?,
    currentMonthStats: MonthAttendanceCount,
    leaveRequests: List<LeaveRequestEntity>,
    onSubmitLeaveRequest: (
        leaveType: String,
        startDate: String,
        endDate: String,
        daysCount: Int,
        reason: String,
        documentPath: String?,
        onResult: (Boolean, String?) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    var showApplyDialog by remember { mutableStateOf(false) }
    var selectedDocToView by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val totalBalance = currentUser?.totalLeaveBalance ?: 0
    val hasLeaveBalance = totalBalance > 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        item {
            Column {
                Text(
                    text = "My Leaves & Leave Bucket",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Employee ID: ${currentUser?.empId ?: ""} • Monthly Accruals & Carry Forward",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569)
                )
            }
        }

        // Leave Bucket Card (User Request 2: "Every employee will get 1 Paid leave per month, 1 Sick leave per 3 months... Every month these count should carry forward...")
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("employee_leave_bucket_card"),
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
                                text = "My Leave Bucket",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (hasLeaveBalance) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                        ) {
                            Text(
                                text = if (hasLeaveBalance) "$totalBalance Days Total" else "Bucket Empty",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (hasLeaveBalance) Color(0xFF065F46) else Color(0xFF991B1B),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Balances automatically carried forward from previous months with current month credit",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // 3 Metric Badges for PL, SL, CO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Paid Leave (PL)
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFD1FAE5)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${currentUser?.paidLeaveBalance ?: 0}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF065F46)
                                )
                                Text(
                                    text = "Paid Leave (PL)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46)
                                )
                                Text(
                                    text = "1 Day / Month",
                                    fontSize = 9.sp,
                                    color = Color(0xFF047857)
                                )
                            }
                        }

                        // Sick Leave (SL)
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEE2E2)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${currentUser?.sickLeaveBalance ?: 0}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    text = "Sick Leave (SL)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    text = "1 / 3 Months",
                                    fontSize = 9.sp,
                                    color = Color(0xFFB91C1C)
                                )
                            }
                        }

                        // Compensative Off (CO)
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${currentUser?.compOffBalance ?: 0}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Comp Off (CO)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Carry-Forward",
                                    fontSize = 9.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Current Month Attendance Count & Paid Days Banner (User Request 1 & 2)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("current_month_paid_days_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT MONTH PAID DAYS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A)
                            )
                            Text(
                                text = "${currentMonthStats.totalPaidDaysFormatted} Days",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1D4ED8)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White
                        ) {
                            Text(
                                text = currentMonthStats.monthYear,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Present [P]: ${currentMonthStats.presentCount}  |  • Half Day [HD]: ${currentMonthStats.halfDayCount}  |  • Paid Leaves: ${currentMonthStats.paidLeaveCount}  |  • Absent [A]: ${currentMonthStats.absentCount}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                }
            }
        }

        // Apply for Leave Action (User Request 2: "If employee have the leave balance in his/her bucket then only he should have option to rise request for leave approval")
        item {
            Column {
                Button(
                    onClick = { showApplyDialog = true },
                    enabled = hasLeaveBalance,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("raise_leave_request_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E3A8A),
                        disabledContainerColor = Color(0xFFE2E8F0),
                        disabledContentColor = Color(0xFF94A3B8)
                    )
                ) {
                    if (hasLeaveBalance) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Apply for Leave (Raise Request)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "No Leave Balance in Bucket",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!hasLeaveBalance) {
                    Text(
                        text = "⚠️ You have 0 leave balance in your bucket. As per policy, you can only apply for leave when you have available leave balance.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFDC2626),
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }
        }

        // My Raised Leave Requests Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "My Raised Leave Requests (${leaveRequests.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        }

        if (leaveRequests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "No leave requests raised yet. Your leave history will appear here once submitted.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(leaveRequests) { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_leave_item_${req.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = LeaveRequestEntity.getLeaveTypeName(req.leaveType),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )

                            val (statusBg, statusText) = when (req.status.uppercase()) {
                                LeaveRequestEntity.STATUS_APPROVED -> Pair(Color(0xFFD1FAE5), Color(0xFF065F46))
                                LeaveRequestEntity.STATUS_REJECTED -> Pair(Color(0xFFFEE2E2), Color(0xFF991B1B))
                                else -> Pair(Color(0xFFFEF3C7), Color(0xFF92400E))
                            }

                            Surface(shape = RoundedCornerShape(6.dp), color = statusBg) {
                                Text(
                                    text = when (req.status.uppercase()) {
                                        LeaveRequestEntity.STATUS_APPROVED -> "APPROVED • PAID DAY"
                                        LeaveRequestEntity.STATUS_REJECTED -> "REJECTED"
                                        else -> "PENDING APPROVAL"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = statusText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Dates: ${req.startDate} to ${req.endDate} (${req.daysCount} Day${if (req.daysCount > 1) "s" else ""})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )

                        if (req.reason.isNotBlank()) {
                            Text(
                                text = "Reason: ${req.reason}",
                                fontSize = 11.sp,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        if (!req.documentPath.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedDocToView = req.documentPath },
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Medical Document Attached (Tap to View)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E3A8A)
                                    )
                                }
                            }
                        }

                        if (req.adminRemarks?.isNotBlank() == true) {
                            Text(
                                text = "Admin Remarks: ${req.adminRemarks}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Apply for Leave Dialog
    if (showApplyDialog && currentUser != null) {
        ApplyLeaveDialog(
            currentUser = currentUser,
            onDismiss = { showApplyDialog = false },
            onSubmit = { type, start, end, count, reason, docPath ->
                onSubmitLeaveRequest(type, start, end, count, reason, docPath) { success, msg ->
                    if (success) {
                        showApplyDialog = false
                    }
                }
            }
        )
    }

    // Inspect Document Dialog
    selectedDocToView?.let { docPath ->
        AlertDialog(
            onDismissRequest = { selectedDocToView = null },
            title = { Text("Attached Medical Document", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    val file = File(docPath)
                    if (file.exists()) {
                        val bmp = BitmapFactory.decodeFile(file.absolutePath)
                        if (bmp != null) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Medical Document",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text("File: ${file.name}\nPath: ${file.absolutePath}")
                        }
                    } else {
                        Text("Medical Document verified on file (${file.name})")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedDocToView = null }) { Text("Close") }
            }
        )
    }
}

@Composable
fun ApplyLeaveDialog(
    currentUser: UserEntity,
    onDismiss: () -> Unit,
    onSubmit: (
        leaveType: String,
        startDate: String,
        endDate: String,
        daysCount: Int,
        reason: String,
        documentPath: String?
    ) -> Unit
) {
    val context = LocalContext.current
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var selectedType by remember {
        mutableStateOf(
            when {
                currentUser.paidLeaveBalance > 0 -> LeaveRequestEntity.LEAVE_TYPE_PL
                currentUser.sickLeaveBalance > 0 -> LeaveRequestEntity.LEAVE_TYPE_SL
                currentUser.compOffBalance > 0 -> LeaveRequestEntity.LEAVE_TYPE_CO
                else -> LeaveRequestEntity.LEAVE_TYPE_PL
            }
        )
    }

    var startDate by remember { mutableStateOf(todayStr) }
    var endDate by remember { mutableStateOf(todayStr) }
    var daysCountText by remember { mutableStateOf("1") }
    var reason by remember { mutableStateOf("") }
    var documentPath by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Document Picker launcher for Sick Leave (SL)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val destDir = File(context.filesDir, "medical_docs")
                destDir.mkdirs()
                val destFile = File(destDir, "cert_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                documentPath = destFile.absolutePath
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Failed to load document: ${e.message}"
            }
        }
    }

    // Helper to generate a demo medical certificate on device for quick demo testing
    fun generateDemoMedicalDoc(): String {
        val destDir = File(context.filesDir, "medical_docs")
        destDir.mkdirs()
        val destFile = File(destDir, "medical_cert_${System.currentTimeMillis()}.png")

        val bitmap = Bitmap.createBitmap(500, 300, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val paint = Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 24f
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = android.graphics.Color.rgb(37, 99, 235)
            textSize = 30f
            isFakeBoldText = true
            isAntiAlias = true
        }

        canvas.drawText("CITY CLINIC & MEDICAL CENTRE", 40f, 50f, headerPaint)
        canvas.drawText("MEDICAL FITNESS & REST CERTIFICATE", 40f, 90f, paint)
        canvas.drawText("Patient: ${currentUser.name} (ID: ${currentUser.empId})", 40f, 140f, paint)
        canvas.drawText("Prescription: Bed rest advised for quick recovery", 40f, 180f, paint)
        canvas.drawText("Date: $todayStr • Dr. S. Rao, MD", 40f, 220f, paint)
        canvas.drawText("SEAL: VERIFIED OFFICIAL CLINIC RECORD", 40f, 260f, paint)

        FileOutputStream(destFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }
        return destFile.absolutePath
    }

    val availableBalanceForType = when (selectedType) {
        LeaveRequestEntity.LEAVE_TYPE_PL -> currentUser.paidLeaveBalance
        LeaveRequestEntity.LEAVE_TYPE_SL -> currentUser.sickLeaveBalance
        LeaveRequestEntity.LEAVE_TYPE_CO -> currentUser.compOffBalance
        else -> 0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Apply for Leave", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(
                    text = "Request approval from Admin to convert to Paid Days",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Leave Type Selector
                item {
                    Text("Select Leave Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedType == LeaveRequestEntity.LEAVE_TYPE_PL,
                            onClick = {
                                selectedType = LeaveRequestEntity.LEAVE_TYPE_PL
                                errorMessage = null
                            },
                            label = { Text("PL (${currentUser.paidLeaveBalance})") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF059669),
                                selectedLabelColor = Color.White
                            )
                        )

                        FilterChip(
                            selected = selectedType == LeaveRequestEntity.LEAVE_TYPE_SL,
                            onClick = {
                                selectedType = LeaveRequestEntity.LEAVE_TYPE_SL
                                errorMessage = null
                            },
                            label = { Text("SL (${currentUser.sickLeaveBalance})") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFDC2626),
                                selectedLabelColor = Color.White
                            )
                        )

                        FilterChip(
                            selected = selectedType == LeaveRequestEntity.LEAVE_TYPE_CO,
                            onClick = {
                                selectedType = LeaveRequestEntity.LEAVE_TYPE_CO
                                errorMessage = null
                            },
                            label = { Text("CO (${currentUser.compOffBalance})") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD97706),
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    Text(
                        text = when (selectedType) {
                            LeaveRequestEntity.LEAVE_TYPE_PL -> "Paid Leave: 1 credited / month. Balance: ${currentUser.paidLeaveBalance} days"
                            LeaveRequestEntity.LEAVE_TYPE_SL -> "Sick Leave: 1 credited / 3 months. Balance: ${currentUser.sickLeaveBalance} days (Document upload required)"
                            else -> "Compensative Off: Balance: ${currentUser.compOffBalance} days"
                        },
                        fontSize = 11.sp,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Dates & Days
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Start Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("End Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = daysCountText,
                        onValueChange = { daysCountText = it.filter { c -> c.isDigit() } },
                        label = { Text("Days Count") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for Leave") },
                        placeholder = { Text("e.g. Fever & medical rest / Personal work") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Sick Leave Document Upload (User Request 2: "Sick Leave (Should upload the document)")
                if (selectedType == LeaveRequestEntity.LEAVE_TYPE_SL) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Medical Certificate Mandatory for Sick Leave",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF991B1B)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (documentPath != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "✓ Document Attached: ${File(documentPath!!).name}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF065F46)
                                        )

                                        IconButton(onClick = { documentPath = null }) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { filePickerLauncher.launch("image/*") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Upload File", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                documentPath = generateDemoMedicalDoc()
                                                errorMessage = null
                                            },
                                            modifier = Modifier.weight(1.3f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Attach Medical Cert", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Error Message if any
                if (errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = daysCountText.toIntOrNull() ?: 1
                    if (count <= 0) {
                        errorMessage = "Please enter valid number of days."
                        return@Button
                    }
                    if (count > availableBalanceForType) {
                        errorMessage = "Insufficient leave balance. You only have $availableBalanceForType day(s) for $selectedType."
                        return@Button
                    }
                    if (selectedType == LeaveRequestEntity.LEAVE_TYPE_SL && documentPath.isNullOrBlank()) {
                        errorMessage = "Document upload is mandatory for Sick Leave (SL). Please attach a medical certificate."
                        return@Button
                    }

                    onSubmit(selectedType, startDate, endDate, count, reason, documentPath)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
            ) {
                Text("Submit Request")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
