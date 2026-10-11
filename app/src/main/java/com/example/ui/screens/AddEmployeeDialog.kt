package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddEmployeeDialog(
    currentCount: Int,
    maxCapacity: Int = 100,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        empId: String,
        role: String,
        joiningDate: String,
        dob: String,
        permanentAddress: String,
        currentAddress: String,
        password: String,
        canExport: Boolean,
        canViewAll: Boolean,
        canTrack: Boolean
    ) -> Unit
) {
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var name by remember { mutableStateOf("") }
    var empId by remember { mutableStateOf("EMP" + (100 + currentCount + 1)) }
    var role by remember { mutableStateOf(UserEntity.ROLE_EMPLOYEE) }
    var joiningDate by remember { mutableStateOf(todayDate) }
    var dob by remember { mutableStateOf("1998-05-15") }
    var permanentAddress by remember { mutableStateOf("") }
    var currentAddress by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("pass123") }

    // Extra permissions (Role-based access control)
    var canExport by remember { mutableStateOf(false) }
    var canViewAll by remember { mutableStateOf(false) }
    var canTrack by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val availableRoles = listOf(
        UserEntity.ROLE_EMPLOYEE,
        UserEntity.ROLE_TEAM_LEAD,
        UserEntity.ROLE_SITE_MANAGER,
        "Field Executive",
        "Office Staff",
        "Operations Specialist"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "New Joinee Credentials & Role",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Employee Capacity: $currentCount / $maxCapacity",
                    fontSize = 12.sp,
                    color = if (currentCount >= maxCapacity) Color(0xFFDC2626) else Color(0xFF2563EB),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEE2E2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFF991B1B),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                val dialogFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF0F172A),
                    unfocusedTextColor = Color(0xFF0F172A),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedLabelColor = Color(0xFF1E3A8A),
                    unfocusedLabelColor = Color(0xFF1E293B),
                    focusedPlaceholderColor = Color(0xFF475569),
                    unfocusedPlaceholderColor = Color(0xFF64748B),
                    focusedBorderColor = Color(0xFF1E3A8A),
                    unfocusedBorderColor = Color(0xFF94A3B8)
                )

                // 1. Employee Role Entry (Option to type/enter role or choose preset)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Employee Role & Designation *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    OutlinedTextField(
                        value = role,
                        onValueChange = { newRole ->
                            role = newRole
                            // Auto-suggest default permissions based on role
                            if (newRole.equals(UserEntity.ROLE_SITE_MANAGER, ignoreCase = true)) {
                                canExport = true
                                canViewAll = true
                                canTrack = true
                            } else if (newRole.equals(UserEntity.ROLE_TEAM_LEAD, ignoreCase = true)) {
                                canViewAll = true
                                canTrack = true
                            }
                        },
                        label = { Text("Enter Role / Title *", fontWeight = FontWeight.SemiBold) },
                        placeholder = { Text("e.g. Employee, Team Lead, Site Manager, Engineer...") },
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = Color(0xFF1E3A8A)) },
                        singleLine = true,
                        colors = dialogFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_employee_role")
                    )

                    Text(
                        text = "Quick Presets:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableRoles) { r ->
                            val isSelected = role.equals(r, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    role = r
                                    // Auto-suggest default permissions based on role
                                    if (r.equals(UserEntity.ROLE_SITE_MANAGER, ignoreCase = true)) {
                                        canExport = true
                                        canViewAll = true
                                        canTrack = true
                                    } else if (r.equals(UserEntity.ROLE_TEAM_LEAD, ignoreCase = true)) {
                                        canViewAll = true
                                        canTrack = true
                                    }
                                },
                                label = { Text(r) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2563EB),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // 2. Extra Access Permissions for this Employee
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Permit Extra Access to this Employee:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { canExport = !canExport }
                        ) {
                            Checkbox(
                                checked = canExport,
                                onCheckedChange = { canExport = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
                            )
                            Text("Download Excel Attendance Reports", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { canTrack = !canTrack }
                        ) {
                            Checkbox(
                                checked = canTrack,
                                onCheckedChange = { canTrack = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
                            )
                            Text("Access Live Team Map Tracking", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { canViewAll = !canViewAll }
                        ) {
                            Checkbox(
                                checked = canViewAll,
                                onCheckedChange = { canViewAll = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
                            )
                            Text("View All Staff Attendance Logs", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1E3A8A)) },
                    singleLine = true,
                    colors = dialogFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_employee_name")
                )

                OutlinedTextField(
                    value = empId,
                    onValueChange = { empId = it },
                    label = { Text("Employee ID *", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF1E3A8A)) },
                    singleLine = true,
                    colors = dialogFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_employee_id")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = joiningDate,
                        onValueChange = { joiningDate = it },
                        label = { Text("Joining Date", fontWeight = FontWeight.SemiBold) },
                        placeholder = { Text("YYYY-MM-DD") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF1E3A8A)) },
                        singleLine = true,
                        colors = dialogFieldColors,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text("Date of Birth", fontWeight = FontWeight.SemiBold) },
                        placeholder = { Text("YYYY-MM-DD") },
                        singleLine = true,
                        colors = dialogFieldColors,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = permanentAddress,
                    onValueChange = { permanentAddress = it },
                    label = { Text("Permanent Address *", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF1E3A8A)) },
                    singleLine = true,
                    colors = dialogFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_employee_perm_address")
                )

                OutlinedTextField(
                    value = currentAddress,
                    onValueChange = { currentAddress = it },
                    label = { Text("Current Address *", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF1E3A8A)) },
                    singleLine = true,
                    colors = dialogFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_employee_curr_address")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Initial Login Password *", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF1E3A8A)) },
                    singleLine = true,
                    colors = dialogFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter Employee Full Name"
                        return@Button
                    }
                    if (empId.isBlank()) {
                        errorMessage = "Please enter Employee ID"
                        return@Button
                    }
                    if (role.isBlank()) {
                        errorMessage = "Please enter Employee Role / Designation"
                        return@Button
                    }
                    if (permanentAddress.isBlank() || currentAddress.isBlank()) {
                        errorMessage = "Please enter both Permanent and Current address"
                        return@Button
                    }
                    if (password.isBlank()) {
                        errorMessage = "Please specify a password"
                        return@Button
                    }

                    onConfirm(
                        name,
                        empId,
                        role,
                        joiningDate,
                        dob,
                        permanentAddress,
                        currentAddress,
                        password,
                        canExport,
                        canViewAll,
                        canTrack
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier.testTag("confirm_create_employee")
            ) {
                Text("Create Credentials")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
