package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.model.LicenseKey
import com.example.ui.theme.*

@Composable
fun ActivateLicenseDialog(
    availableKeys: List<LicenseKey>,
    onDismiss: () -> Unit,
    onActivate: (String) -> Boolean
) {
    var keyInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    val sampleUnusedKey = availableKeys.firstOrNull { it.status == com.example.data.model.LicenseStatus.UNUSED }?.key ?: "TOPPRO-VIP"

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
                                .background(Orange100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = Orange600
                            )
                        }
                        Text(
                            text = "Kích Hoạt Bản Quyền",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Navy900
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("activate_license_close")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                if (isSuccess) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "Kích Hoạt Vĩnh Viễn Thành Công!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Navy900
                        )
                        Text(
                            text = "Tài khoản Power Team TOPPRO của bạn hiện đã được nâng cấp trọn đời. Toàn bộ giới hạn dùng thử đã được dỡ bỏ.",
                            fontSize = 13.sp,
                            color = SlateTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("license_success_done_button")
                        ) {
                            Text("Hoàn Tất & Bắt Đầu Sử Dụng", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = "Nhập mã kích hoạt (ví dụ: TOPPRO-XXXX-XXXX) được cấp bởi Ban Điều Hành Chapter để nâng cấp tài khoản vĩnh viễn.",
                        fontSize = 13.sp,
                        color = SlateTextSecondary
                    )

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = {
                            keyInput = it.uppercase()
                            errorMessage = null
                        },
                        label = { Text("Mã kích hoạt bản quyền") },
                        placeholder = { Text("TOPPRO-XXXX-XXXX") },
                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_license_key")
                    )

                    // Gợi ý mã mẫu test
                    Surface(
                        color = SlateBg,
                        shape = RoundedCornerShape(10.dp),
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
                                    text = "Mã thử nghiệm có sẵn:",
                                    fontSize = 10.sp,
                                    color = SlateTextMuted
                                )
                                Text(
                                    text = sampleUnusedKey,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Navy700
                                )
                            }
                            TextButton(
                                onClick = { keyInput = sampleUnusedKey },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("insert_sample_key_button")
                            ) {
                                Text("Chèn mã", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Orange600)
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = HotRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            if (keyInput.isBlank()) {
                                errorMessage = "Vui lòng nhập mã bản quyền."
                                return@Button
                            }
                            val success = onActivate(keyInput)
                            if (success) {
                                isSuccess = true
                            } else {
                                errorMessage = "Mã không hợp lệ hoặc đã qua sử dụng. Thử nhập 'TOPPRO-VIP' hoặc bấm 'Chèn mã'."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_activate_license_button")
                    ) {
                        Text("Xác Nhận & Nâng Cấp Vĩnh Viễn", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
