package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun AddAdminDialog(
    currentAdminCount: Int,
    maxAdminCapacity: Int = UserEntity.MAX_ADMIN_CAPACITY,
    onDismiss: () -> Unit,
    onConfirm: (name: String, adminId: String, password: String, contact: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var adminId by remember { mutableStateOf("ADMIN00" + (currentAdminCount + 1)) }
    var password by remember { mutableStateOf("admin123") }
    var contact by remember { mutableStateOf("Corporate HQ Operations") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isCapacityFull = currentAdminCount >= maxAdminCapacity

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color(0xFF4338CA)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Admin Credentials",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
                Text(
                    text = "Capacity: $currentAdminCount / $maxAdminCapacity Admins maximum",
                    fontSize = 12.sp,
                    color = if (isCapacityFull) Color(0xFFDC2626) else Color(0xFF4338CA),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isCapacityFull) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEE2E2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Maximum capacity of 5 Admin credentials reached. Delete an existing admin to create a new one.",
                            color = Color(0xFF991B1B),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                } else if (errorMessage != null) {
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

                val adminFieldColors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF0F172A),
                    unfocusedTextColor = Color(0xFF0F172A),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedLabelColor = Color(0xFF4338CA),
                    unfocusedLabelColor = Color(0xFF1E293B),
                    focusedPlaceholderColor = Color(0xFF475569),
                    unfocusedPlaceholderColor = Color(0xFF64748B),
                    focusedBorderColor = Color(0xFF4338CA),
                    unfocusedBorderColor = Color(0xFF94A3B8)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Admin Full Name *", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF4338CA)) },
                    singleLine = true,
                    colors = adminFieldColors,
                    enabled = !isCapacityFull,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_name_input")
                )

                OutlinedTextField(
                    value = adminId,
                    onValueChange = { adminId = it },
                    label = { Text("Admin ID * (e.g. ADMIN002)", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF4338CA)) },
                    singleLine = true,
                    colors = adminFieldColors,
                    enabled = !isCapacityFull,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_id_input")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Initial Admin Password *", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF4338CA)) },
                    singleLine = true,
                    colors = adminFieldColors,
                    enabled = !isCapacityFull,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_password_input")
                )

                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Department / Location *", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF4338CA)) },
                    singleLine = true,
                    colors = adminFieldColors,
                    enabled = !isCapacityFull,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isCapacityFull) return@Button
                    if (name.isBlank()) {
                        errorMessage = "Please enter Admin Full Name"
                        return@Button
                    }
                    if (adminId.isBlank()) {
                        errorMessage = "Please enter Admin ID"
                        return@Button
                    }
                    if (password.isBlank()) {
                        errorMessage = "Please specify Admin password"
                        return@Button
                    }
                    onConfirm(name.trim(), adminId.trim(), password.trim(), contact.trim())
                },
                enabled = !isCapacityFull,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA)),
                modifier = Modifier.testTag("confirm_create_admin")
            ) {
                Text("Create Admin")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
