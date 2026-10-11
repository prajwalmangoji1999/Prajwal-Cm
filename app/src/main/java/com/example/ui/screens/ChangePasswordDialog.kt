package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity

@Composable
fun ChangePasswordDialog(
    targetUser: UserEntity,
    onDismiss: () -> Unit,
    onConfirm: (newPassword: String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Change Account Password",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Target: ${targetUser.name} (${targetUser.empId})",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
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

                Text(
                    text = "Enter a new secure password for this user ID.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E293B)
                )

                val pwdFieldColors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF0F172A),
                    unfocusedTextColor = Color(0xFF0F172A),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedLabelColor = Color(0xFF2563EB),
                    unfocusedLabelColor = Color(0xFF1E293B),
                    focusedPlaceholderColor = Color(0xFF475569),
                    unfocusedPlaceholderColor = Color(0xFF64748B),
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFF94A3B8)
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = null
                    },
                    label = { Text("New Password *", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2563EB)) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password",
                                tint = Color(0xFF334155)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = pwdFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_password_input")
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("Confirm New Password *", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2563EB)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = pwdFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_password_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPassword.isBlank()) {
                        errorMessage = "Password cannot be empty"
                        return@Button
                    }
                    if (newPassword.length < 4) {
                        errorMessage = "Password must be at least 4 characters"
                        return@Button
                    }
                    if (newPassword != confirmPassword) {
                        errorMessage = "Passwords do not match"
                        return@Button
                    }

                    onConfirm(newPassword.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier.testTag("save_password_button")
            ) {
                Text("Update Password")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
