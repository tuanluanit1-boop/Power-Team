package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Referral
import com.example.ui.theme.*

@Composable
fun CloseDealDialog(
    referral: Referral,
    onDismiss: () -> Unit,
    onConfirmSuccess: (tyfcbAmount: Double, commissionPct: Double, fundPct: Double) -> Unit
) {
    var tyfcbInput by remember { mutableStateOf(referral.estimatedBudget.toLong().toString()) }
    var commissionPctInput by remember { mutableStateOf("5.0") }
    var fundPctInput by remember { mutableStateOf("1.0") }

    val tyfcbAmount = tyfcbInput.toDoubleOrNull() ?: 0.0
    val commissionPct = commissionPctInput.toDoubleOrNull() ?: 0.0
    val fundPct = fundPctInput.toDoubleOrNull() ?: 0.0

    val calculatedCommission = tyfcbAmount * (commissionPct / 100.0)
    val calculatedFund = tyfcbAmount * (fundPct / 100.0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SuccessGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen
                            )
                        }
                        Text(
                            text = "Chốt Deal Thành Công",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Navy900
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_deal_dialog_dismiss")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                Text(
                    text = "Báo cáo doanh số TYFCB (Thank You For Closed Business) cho hợp đồng '${referral.title}' với khách hàng ${referral.clientName}.",
                    fontSize = 13.sp,
                    color = SlateTextSecondary
                )

                // Nhập doanh số TYFCB
                OutlinedTextField(
                    value = tyfcbInput,
                    onValueChange = { tyfcbInput = it },
                    label = { Text("Doanh số thực tế TYFCB (VNĐ) *") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_tyfcb_amount")
                )

                // Cấu hình hoa hồng & phí cảm ơn
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "CẤU HÌNH HOA HỒNG & ĐÓNG GÓP QUỸ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy700
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = commissionPctInput,
                                onValueChange = { commissionPctInput = it },
                                label = { Text("% Hoa hồng (Giver)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_commission_pct")
                            )

                            OutlinedTextField(
                                value = fundPctInput,
                                onValueChange = { fundPctInput = it },
                                label = { Text("% Quỹ nhóm") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_fund_pct")
                            )
                        }

                        // Phân bổ tính toán
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Hoa hồng cảm ơn người trao:",
                                fontSize = 12.sp,
                                color = SlateTextSecondary
                            )
                            Text(
                                text = "${String.format("%,.0f", calculatedCommission)} đ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Đóng góp Quỹ chung Power Team:",
                                fontSize = 12.sp,
                                color = SlateTextSecondary
                            )
                            Text(
                                text = "${String.format("%,.0f", calculatedFund)} đ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Orange600
                            )
                        }
                    }
                }

                // Nút Xác nhận
                Button(
                    onClick = {
                        if (tyfcbAmount > 0) {
                            onConfirmSuccess(tyfcbAmount, commissionPct, fundPct)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_close_deal_button")
                ) {
                    Icon(imageVector = Icons.Default.Celebration, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Xác Nhận & Ghi Nhận Doanh Số TYFCB",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
