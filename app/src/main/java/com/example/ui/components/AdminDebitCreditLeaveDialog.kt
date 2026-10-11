package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaveRequestEntity
import com.example.data.model.UserEntity

/**
 * Dialog for Admin to Debit and Credit leave balance from an employee's bucket.
 */
@Composable
fun AdminDebitCreditLeaveDialog(
    employee: UserEntity,
    onDismiss: () -> Unit,
    onConfirm: (leaveType: String, delta: Int, reason: String) -> Unit
) {
    // 0: Credit (+), 1: Debit (-)
    var actionType by remember { mutableIntStateOf(0) }
    var selectedLeaveType by remember { mutableStateOf(LeaveRequestEntity.LEAVE_TYPE_PL) }
    var daysCountText by remember { mutableStateOf("1") }
    var reasonText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val daysCount = daysCountText.toIntOrNull() ?: 1

    val currentBalanceForType = when (selectedLeaveType) {
        LeaveRequestEntity.LEAVE_TYPE_PL -> employee.paidLeaveBalance
        LeaveRequestEntity.LEAVE_TYPE_SL -> employee.sickLeaveBalance
        LeaveRequestEntity.LEAVE_TYPE_CO -> employee.compOffBalance
        else -> 0
    }

    val projectedBalance = if (actionType == 0) {
        currentBalanceForType + daysCount
    } else {
        (currentBalanceForType - daysCount).coerceAtLeast(0)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = if (actionType == 0) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (actionType == 0) Icons.Default.Add else Icons.Default.Remove,
                            contentDescription = null,
                            tint = if (actionType == 0) Color(0xFF059669) else Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (actionType == 0) "Credit Leave to Bucket" else "Debit Leave from Bucket",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "${employee.name} (${employee.empId})",
                        fontSize = 12.sp,
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Current Bucket Snapshot Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Current Bucket Balances:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "PL: ${employee.paidLeaveBalance}d",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "SL: ${employee.sickLeaveBalance}d",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "CO: ${employee.compOffBalance}d",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "Total: ${employee.totalLeaveBalance}d",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E3A8A)
                            )
                        }
                    }
                }

                // Action Toggle: Credit vs Debit
                Text(
                    text = "Select Operation:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = actionType == 0,
                        onClick = {
                            actionType = 0
                            errorMessage = null
                        },
                        label = { Text("＋ Credit (Add Leaves)", fontWeight = FontWeight.Bold) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_chip_credit"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF059669),
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = actionType == 1,
                        onClick = {
                            actionType = 1
                            errorMessage = null
                        },
                        label = { Text("－ Debit (Deduct)", fontWeight = FontWeight.Bold) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_chip_debit"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFDC2626),
                            selectedLabelColor = Color.White
                        )
                    )
                }

                // Leave Type Selection
                Text(
                    text = "Select Leave Type:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        LeaveRequestEntity.LEAVE_TYPE_PL to "Paid Leave (PL)",
                        LeaveRequestEntity.LEAVE_TYPE_SL to "Sick (SL)",
                        LeaveRequestEntity.LEAVE_TYPE_CO to "Comp Off (CO)"
                    ).forEach { (type, label) ->
                        FilterChip(
                            selected = selectedLeaveType == type,
                            onClick = {
                                selectedLeaveType = type
                                errorMessage = null
                            },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Quick Count Increments
                Text(
                    text = "Number of Days to ${if (actionType == 0) "Credit" else "Debit"}:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 2, 3, 5).forEach { count ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (daysCount == count) Color(0xFF1E3A8A) else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            onClick = {
                                daysCountText = count.toString()
                                errorMessage = null
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$count Day${if (count > 1) "s" else ""}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (daysCount == count) Color.White else Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = daysCountText,
                    onValueChange = {
                        daysCountText = it.filter { ch -> ch.isDigit() }
                        errorMessage = null
                    },
                    label = { Text("Custom Days Count") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Projected Balance Calculation Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (actionType == 0) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, if (actionType == 0) Color(0xFFA7F3D0) else Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current $selectedLeaveType Balance:",
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                            Text(
                                text = "$currentBalanceForType Day(s)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Adjustment:",
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                            Text(
                                text = if (actionType == 0) "+$daysCount Day(s)" else "-$daysCount Day(s)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (actionType == 0) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "New Resulting Balance:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "$projectedBalance Day(s)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (actionType == 0) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                        }
                    }
                }

                // Reason / Notes
                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text("Reason / Remarks (Optional)") },
                    placeholder = { Text("e.g. Incentive bonus, adjustment, etc.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Error Message
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFDC2626),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = daysCountText.toIntOrNull() ?: 0
                    if (count <= 0) {
                        errorMessage = "Please enter a valid number of days (≥ 1)."
                        return@Button
                    }
                    if (actionType == 1 && currentBalanceForType == 0) {
                        errorMessage = "Cannot debit. Employee has 0 $selectedLeaveType balance."
                        return@Button
                    }

                    val delta = if (actionType == 0) count else -count
                    onConfirm(selectedLeaveType, delta, reasonText)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (actionType == 0) Color(0xFF059669) else Color(0xFFDC2626)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("dialog_confirm_adjust_btn")
            ) {
                Text(
                    text = if (actionType == 0) "Credit +$daysCount Day(s)" else "Debit -$daysCount Day(s)",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
