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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity

@Composable
fun ManageRoleDialog(
    employee: UserEntity,
    onDismiss: () -> Unit,
    onSave: (role: String, canExport: Boolean, canViewAll: Boolean, canTrack: Boolean) -> Unit
) {
    var selectedRole by remember { mutableStateOf(employee.role) }
    var canExport by remember { mutableStateOf(employee.canExportExcel) }
    var canViewAll by remember { mutableStateOf(employee.canViewAllEmployees) }
    var canTrack by remember { mutableStateOf(employee.canTrackLocations) }

    val roles = listOf(
        UserEntity.ROLE_EMPLOYEE,
        UserEntity.ROLE_TEAM_LEAD,
        UserEntity.ROLE_SITE_MANAGER
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Role & Extra Access Permissions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "${employee.name} (${employee.empId})",
                    fontSize = 13.sp,
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Assign Role & Designation *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    OutlinedTextField(
                        value = selectedRole,
                        onValueChange = { newRole ->
                            selectedRole = newRole
                            if (newRole.equals(UserEntity.ROLE_SITE_MANAGER, ignoreCase = true)) {
                                canExport = true
                                canViewAll = true
                                canTrack = true
                            } else if (newRole.equals(UserEntity.ROLE_TEAM_LEAD, ignoreCase = true)) {
                                canViewAll = true
                                canTrack = true
                            }
                        },
                        label = { Text("Role / Job Title *", fontWeight = FontWeight.SemiBold) },
                        placeholder = { Text("e.g. Employee, Team Lead, Manager, Engineer...") },
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = Color(0xFF1E3A8A)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF1E3A8A),
                            unfocusedBorderColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.fillMaxWidth()
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
                        items(roles) { r ->
                            val isSelected = selectedRole.equals(r, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedRole = r
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

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Permit Extra Access Permissions:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val roleToSave = selectedRole.trim().ifBlank { UserEntity.ROLE_EMPLOYEE }
                    onSave(roleToSave, canExport, canViewAll, canTrack)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Save Permissions")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
