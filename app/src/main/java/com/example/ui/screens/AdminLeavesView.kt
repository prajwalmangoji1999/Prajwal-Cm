package com.example.ui.screens

import android.graphics.BitmapFactory
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.LeaveRequestEntity
import com.example.data.model.UserEntity
import com.example.ui.components.AdminDebitCreditLeaveDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLeavesView(
    allEmployees: List<UserEntity>,
    leaveRequests: List<LeaveRequestEntity>,
    pendingCount: Int,
    onApproveRequest: (requestId: Long) -> Unit,
    onRejectRequest: (requestId: Long, remarks: String) -> Unit,
    onDebitCreditLeave: (empId: String, leaveType: String, delta: Int) -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0: Pending Requests, 1: Debit/Credit Bucket, 2: History
    var selectedEmployeeForBucket by remember { mutableStateOf<UserEntity?>(allEmployees.firstOrNull()) }
    var selectedDocumentPath by remember { mutableStateOf<String?>(null) }
    var rejectingRequestId by remember { mutableStateOf<Long?>(null) }
    var rejectionRemarks by remember { mutableStateOf("") }
    var employeeForCustomAdjust by remember { mutableStateOf<UserEntity?>(null) }
    var bucketSearchQuery by remember { mutableStateOf("") }

    // Synchronize default employee if null
    if (selectedEmployeeForBucket == null && allEmployees.isNotEmpty()) {
        selectedEmployeeForBucket = allEmployees.first()
    } else if (selectedEmployeeForBucket != null) {
        // Keep updated balance from allEmployees
        val updated = allEmployees.find { it.empId == selectedEmployeeForBucket?.empId }
        if (updated != null && updated != selectedEmployeeForBucket) {
            selectedEmployeeForBucket = updated
        }
    }

    val pendingRequests = remember(leaveRequests) {
        leaveRequests.filter { it.isPending }
    }

    val pastRequests = remember(leaveRequests) {
        leaveRequests.filter { !it.isPending }
    }

    val matchingEmployees = remember(allEmployees, bucketSearchQuery) {
        if (bucketSearchQuery.isBlank()) allEmployees else {
            allEmployees.filter {
                it.name.contains(bucketSearchQuery, ignoreCase = true) ||
                it.empId.contains(bucketSearchQuery, ignoreCase = true)
            }
        }
    }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Leave Management & Approvals",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Approve leave requests into Paid Days & manage employee leave buckets",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569)
                )
            }
        }

        // Sub-tabs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E3A8A),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = Color(0xFF1E3A8A),
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        modifier = Modifier.testTag("admin_leaves_tab_approvals"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EventNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (pendingRequests.isNotEmpty()) "Approvals (${pendingRequests.size})" else "Approvals",
                                    fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        modifier = Modifier.testTag("admin_leaves_tab_debit_credit"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Debit / Credit",
                                    fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        modifier = Modifier.testTag("admin_leaves_tab_history"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Past Logs",
                                    fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    )
                }
            }
        }

        // TAB 0: PENDING APPROVALS
        if (activeTab == 0) {
            if (pendingRequests.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "All Leave Requests Reviewed",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "No pending leave approval requests at this moment.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { activeTab = 1 },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                modifier = Modifier.testTag("go_to_debit_credit_btn")
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Debit / Credit Employee Leave Buckets", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                items(pendingRequests) { req ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pending_leave_card_${req.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header: Employee Name and Leave Type
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2563EB)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = req.employeeName.take(2).uppercase(),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = req.employeeName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "ID: ${req.empId}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF2563EB)
                                        )
                                    }
                                }

                                val (badgeBg, badgeText) = when (req.leaveType.uppercase()) {
                                    LeaveRequestEntity.LEAVE_TYPE_PL -> Pair(Color(0xFFD1FAE5), Color(0xFF065F46))
                                    LeaveRequestEntity.LEAVE_TYPE_SL -> Pair(Color(0xFFFEE2E2), Color(0xFF991B1B))
                                    else -> Pair(Color(0xFFFEF3C7), Color(0xFF92400E))
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = badgeBg
                                ) {
                                    Text(
                                        text = LeaveRequestEntity.getLeaveTypeName(req.leaveType),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = badgeText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Details
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Dates:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF475569)
                                        )
                                        Text(
                                            text = "${req.startDate} to ${req.endDate} (${req.daysCount} Day${if (req.daysCount > 1) "s" else ""})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }

                                    if (req.reason.isNotBlank()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Reason:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF475569)
                                            )
                                            Text(
                                                text = req.reason,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF1E293B)
                                            )
                                        }
                                    }
                                }
                            }

                            // Document Upload Indicator (Required for Sick Leave)
                            if (!req.documentPath.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedDocumentPath = req.documentPath },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = null,
                                                tint = Color(0xFF2563EB),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Medical Certificate / Document Uploaded",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1E3A8A)
                                                )
                                                Text(
                                                    text = "Mandatory for Sick Leave • Tap to inspect",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF3B82F6)
                                                )
                                            }
                                        }

                                        Text(
                                            text = "View ↗",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1D4ED8)
                                        )
                                    }
                                }
                            } else if (req.leaveType.equals(LeaveRequestEntity.LEAVE_TYPE_SL, ignoreCase = true)) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Warning: Sick Leave requires medical certificate document.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626)
                                    )
                                }
                            }

                            // Current Employee Leave Bucket Status
                            val empUser = allEmployees.find { it.empId == req.empId }
                            if (empUser != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEFF6FF),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Bucket: ${empUser.paidLeaveBalance} PL • ${empUser.sickLeaveBalance} SL • ${empUser.compOffBalance} CO",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1D4ED8)
                                        )
                                        Button(
                                            onClick = { employeeForCustomAdjust = empUser },
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("adjust_bucket_from_pending_${req.id}")
                                        ) {
                                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Debit / Credit", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action Buttons: Approve (turns into Paid Days) & Reject
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        rejectingRequestId = req.id
                                        rejectionRemarks = ""
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("reject_leave_btn_${req.id}"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { onApproveRequest(req.id) },
                                    modifier = Modifier
                                        .weight(1.4f)
                                        .testTag("approve_leave_btn_${req.id}"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve (Paid Day)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 1: ADMIN DEBIT & CREDIT LEAVE BALANCES (All Employees)
        if (activeTab == 1) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF2563EB))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Employee Leave Bucket Balances",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Debit or credit leave balances directly from any employee's bucket",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = bucketSearchQuery,
                            onValueChange = { bucketSearchQuery = it },
                            placeholder = { Text("Search employee by name or ID...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                            trailingIcon = {
                                if (bucketSearchQuery.isNotBlank()) {
                                    IconButton(onClick = { bucketSearchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_bucket_search_field"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )
                    }
                }
            }

            if (matchingEmployees.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "No employees found matching '$bucketSearchQuery'.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            } else {
                items(matchingEmployees) { emp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("employee_bucket_card_${emp.empId}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Employee Header
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
                                            text = emp.name.take(2).uppercase(),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = emp.name,
                                            fontSize = 15.sp,
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

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (emp.totalLeaveBalance > 0) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                                ) {
                                    Text(
                                        text = "${emp.totalLeaveBalance} Total Leaves",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (emp.totalLeaveBalance > 0) Color(0xFF065F46) else Color(0xFF991B1B),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 3 Metric Boxes: PL, SL, CO with Inline Debit & Credit Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // PL Box
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFECFDF5),
                                    border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("${emp.paidLeaveBalance}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF065F46))
                                        Text("PL (Paid)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (emp.paidLeaveBalance > 0) Color(0xFFFEE2E2) else Color(0xFFF1F5F9),
                                                enabled = emp.paidLeaveBalance > 0,
                                                onClick = { onDebitCreditLeave(emp.empId, LeaveRequestEntity.LEAVE_TYPE_PL, -1) },
                                                modifier = Modifier.size(30.dp).testTag("debit_pl_${emp.empId}")
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Remove, contentDescription = "Debit PL", tint = if (emp.paidLeaveBalance > 0) Color(0xFFDC2626) else Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFD1FAE5),
                                                onClick = { onDebitCreditLeave(emp.empId, LeaveRequestEntity.LEAVE_TYPE_PL, 1) },
                                                modifier = Modifier.size(30.dp).testTag("credit_pl_${emp.empId}")
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Add, contentDescription = "Credit PL", tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                // SL Box
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFEF2F2),
                                    border = BorderStroke(1.dp, Color(0xFFFECACA))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("${emp.sickLeaveBalance}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF991B1B))
                                        Text("SL (Sick)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (emp.sickLeaveBalance > 0) Color(0xFFFEE2E2) else Color(0xFFF1F5F9),
                                                enabled = emp.sickLeaveBalance > 0,
                                                onClick = { onDebitCreditLeave(emp.empId, LeaveRequestEntity.LEAVE_TYPE_SL, -1) },
                                                modifier = Modifier.size(30.dp).testTag("debit_sl_${emp.empId}")
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Remove, contentDescription = "Debit SL", tint = if (emp.sickLeaveBalance > 0) Color(0xFFDC2626) else Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFD1FAE5),
                                                onClick = { onDebitCreditLeave(emp.empId, LeaveRequestEntity.LEAVE_TYPE_SL, 1) },
                                                modifier = Modifier.size(30.dp).testTag("credit_sl_${emp.empId}")
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Add, contentDescription = "Credit SL", tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                // CO Box
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFFFBEB),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("${emp.compOffBalance}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF92400E))
                                        Text("CO (Comp)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (emp.compOffBalance > 0) Color(0xFFFEE2E2) else Color(0xFFF1F5F9),
                                                enabled = emp.compOffBalance > 0,
                                                onClick = { onDebitCreditLeave(emp.empId, LeaveRequestEntity.LEAVE_TYPE_CO, -1) },
                                                modifier = Modifier.size(30.dp).testTag("debit_co_${emp.empId}")
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Remove, contentDescription = "Debit CO", tint = if (emp.compOffBalance > 0) Color(0xFFDC2626) else Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFD1FAE5),
                                                onClick = { onDebitCreditLeave(emp.empId, LeaveRequestEntity.LEAVE_TYPE_CO, 1) },
                                                modifier = Modifier.size(30.dp).testTag("credit_co_${emp.empId}")
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Add, contentDescription = "Credit CO", tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Custom Adjustment Button
                            OutlinedButton(
                                onClick = { employeeForCustomAdjust = emp },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("adjust_bucket_btn_${emp.empId}")
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Custom Multi-Day Debit / Credit →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // TAB 2: ALL PAST LEAVE LOGS
        if (activeTab == 2) {
            if (pastRequests.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "No past approved or rejected leave requests found.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            } else {
                items(pastRequests) { req ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = req.employeeName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "ID: ${req.empId} • ${LeaveRequestEntity.getLeaveTypeName(req.leaveType)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2563EB)
                                    )
                                }

                                val (statusBg, statusText) = if (req.isApproved) {
                                    Pair(Color(0xFFD1FAE5), Color(0xFF065F46))
                                } else {
                                    Pair(Color(0xFFFEE2E2), Color(0xFF991B1B))
                                }

                                Surface(shape = RoundedCornerShape(6.dp), color = statusBg) {
                                    Text(
                                        text = if (req.isApproved) "APPROVED (PAID DAY)" else "REJECTED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = statusText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Dates: ${req.startDate} to ${req.endDate} (${req.daysCount} Day${if (req.daysCount > 1) "s" else ""})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )

                            if (req.adminRemarks?.isNotBlank() == true) {
                                Text(
                                    text = "Remarks: ${req.adminRemarks}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Reject Remarks
    if (rejectingRequestId != null) {
        val reqId = rejectingRequestId!!
        AlertDialog(
            onDismissRequest = { rejectingRequestId = null },
            title = {
                Text(
                    text = "Reject Leave Request",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Specify reason for rejecting this leave request:",
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = rejectionRemarks,
                        onValueChange = { rejectionRemarks = it },
                        label = { Text("Admin Remarks / Reason") },
                        placeholder = { Text("e.g. Critical deployment day / Insufficient proof") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_rejection_remarks_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectRequest(reqId, rejectionRemarks)
                        rejectingRequestId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Reject Request")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { rejectingRequestId = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Custom Multi-Day Debit / Credit Dialog
    employeeForCustomAdjust?.let { targetEmp ->
        val freshEmp = allEmployees.find { it.empId == targetEmp.empId } ?: targetEmp
        AdminDebitCreditLeaveDialog(
            employee = freshEmp,
            onDismiss = { employeeForCustomAdjust = null },
            onConfirm = { leaveType, delta, reason ->
                onDebitCreditLeave(freshEmp.empId, leaveType, delta)
                employeeForCustomAdjust = null
            }
        )
    }

    // Dialog: Inspect Document / Medical Certificate
    selectedDocumentPath?.let { docPath ->
        AlertDialog(
            onDismissRequest = { selectedDocumentPath = null },
            title = {
                Text(
                    text = "Medical Certificate / Document",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val file = File(docPath)
                    if (file.exists()) {
                        val bmp = BitmapFactory.decodeFile(file.absolutePath)
                        if (bmp != null) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Medical Certificate",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEFF6FF)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Medical Document Verified", fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                                    Text(file.name, fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalHospital,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Uploaded Medical Certificate", fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                                Text("Document attached on request submission", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedDocumentPath = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun LeaveBucketAdjustmentRow(
    label: String,
    balance: Int,
    onCredit: () -> Unit,
    onDebit: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text("Current: $balance Day(s)", fontSize = 11.sp, color = Color(0xFF64748B))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Debit Button
                Button(
                    onClick = onDebit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color(0xFF991B1B)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    enabled = balance > 0
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Debit", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Debit -1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Credit Button
                Button(
                    onClick = onCredit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1FAE5), contentColor = Color(0xFF065F46)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Credit", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Credit +1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
