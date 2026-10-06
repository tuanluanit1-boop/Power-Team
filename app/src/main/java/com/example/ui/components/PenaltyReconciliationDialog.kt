package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PenaltyEntry
import com.example.ui.theme.*

@Composable
fun PenaltyReconciliationDialog(
    penalty: PenaltyEntry,
    onDismiss: () -> Unit,
    onConfirmPaid: () -> Unit
) {
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(HotRedContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = HotRed
                            )
                        }
                        Text(
                            text = "Nộp Phạt Kỷ Luật",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Navy900
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("penalty_reconciliation_dismiss")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                Text(
                    text = penalty.reason,
                    fontSize = 13.sp,
                    color = SlateTextPrimary,
                    fontWeight = FontWeight.Medium
                )

                // Khung số tiền phạt
                Card(
                    colors = CardDefaults.cardColors(containerColor = HotRedContainer),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TIỀN PHẠT NỘP QUỸ NHÓM",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = HotRed
                            )
                            Text(
                                text = "Mã quy định: ${penalty.ruleCode}",
                                fontSize = 11.sp,
                                color = SlateTextSecondary
                            )
                        }
                        Text(
                            text = "${String.format("%,.0f", penalty.amount)} đ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = HotRed
                        )
                    }
                }

                // Thông tin chuyển khoản VietQR
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateBg),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Navy700, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "THÔNG TIN CHUYỂN KHOẢN VÀO QUỸ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy700
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ngân hàng:", fontSize = 12.sp, color = SlateTextSecondary)
                                Text("Vietcombank (Hội sở)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tên thụ hưởng:", fontSize = 12.sp, color = SlateTextSecondary)
                                Text("QUY POWER TEAM TOPPRO", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Số tài khoản:", fontSize = 12.sp, color = SlateTextSecondary)
                                Text("9882 1004 5591", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Nội dung CK:", fontSize = 12.sp, color = SlateTextSecondary)
                                Text("TOPPRO PHAT ${penalty.id.takeLast(6).uppercase()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Orange600)
                            }
                        }
                    }
                }

                // Xác nhận đã nộp
                Button(
                    onClick = {
                        onConfirmPaid()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_penalty_payment_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Xác Nhận Đã Nộp & Nhập Vào Quỹ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
