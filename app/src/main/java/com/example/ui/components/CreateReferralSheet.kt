package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReferralSheet(
    members: List<Member>,
    currentUserId: String,
    canCreateReferral: Boolean,
    onDismiss: () -> Unit,
    onActivateLicenseClick: () -> Unit,
    onSubmit: (
        takerMemberId: String,
        clientName: String,
        clientPhone: String,
        clientEmail: String,
        urgency: UrgencyLevel,
        contractType: String,
        estimatedBudget: Double,
        scaleAreaM2: String,
        projectLocation: String,
        notes: String
    ) -> Unit
) {
    val availableRecipients = members.filter { it.id != currentUserId }
    var selectedRecipientId by remember { mutableStateOf(availableRecipients.firstOrNull()?.id ?: "") }
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var clientEmail by remember { mutableStateOf("") }
    var urgency by remember { mutableStateOf(UrgencyLevel.HOT) }
    var contractType by remember { mutableStateOf("Thi công Hoàn thiện Nội thất Văn phòng") }
    var estimatedBudgetStr by remember { mutableStateOf("1500000000") }
    var scaleAreaM2 by remember { mutableStateOf("450 m²") }
    var projectLocation by remember { mutableStateOf("Quận 1, TP. Hồ Chí Minh") }
    var notes by remember { mutableStateOf("") }

    var showOcrScanner by remember { mutableStateOf(false) }
    var showRecipientMenu by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    if (showOcrScanner) {
        OcrScannerDialog(
            onDismiss = { showOcrScanner = false },
            onCardScanned = { card ->
                clientName = card.clientName
                clientPhone = card.phone
                clientEmail = card.email
                contractType = card.industryType
                estimatedBudgetStr = card.estimatedBudget.toLong().toString()
                scaleAreaM2 = card.scaleArea
                projectLocation = card.location
                notes = card.notes
                showOcrScanner = false
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateBg),
            color = SlateBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Header
                Surface(
                    color = Color.White,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("referral_create_close_button")
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                            }
                            Text(
                                text = "Trao Cơ Hội Kinh Doanh",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        }

                        // Nút Quét OCR Danh Thiếp
                        OutlinedButton(
                            onClick = { showOcrScanner = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Orange600
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Orange500)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("ocr_scan_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Quét Card AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Cảnh báo hết hạn dùng thử
                if (!canCreateReferral) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HotRedContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = HotRed,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Hết Hạn Dùng Thử 7 Ngày",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = HotRed
                            )
                            Text(
                                text = "Tài khoản của bạn đã kết thúc thời gian trải nghiệm miễn phí. Vui lòng nhập mã kích hoạt từ Ban Điều Hành để mở khóa tạo cơ hội mới.",
                                fontSize = 13.sp,
                                color = SlateTextPrimary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = {
                                    onDismiss()
                                    onActivateLicenseClick()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("expired_trial_upgrade_button")
                            ) {
                                Icon(imageVector = Icons.Default.Key, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Kích Hoạt Bản Quyền Vĩnh Viễn", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Nội dung Form
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Mục 1: Chọn đồng đội nhận cơ hội
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "THÀNH VIÊN NHẬN CƠ HỘI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy700,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                val recipient = members.find { it.id == selectedRecipientId }
                                Box {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SlateBg)
                                            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                                            .clickable { showRecipientMenu = true }
                                            .padding(12.dp)
                                            .testTag("recipient_picker_button")
                                    ) {
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
                                                        .clip(CircleShape)
                                                        .background(Color(recipient?.avatarColorHex ?: 0xFF1E3A8A)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = recipient?.avatarInitials ?: "TV",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = recipient?.name ?: "Chọn thành viên",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = Navy900
                                                    )
                                                    Text(
                                                        text = "${recipient?.industry} • ${recipient?.company}",
                                                        fontSize = 11.sp,
                                                        color = SlateTextSecondary
                                                    )
                                                }
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                tint = Navy700
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = showRecipientMenu,
                                        onDismissRequest = { showRecipientMenu = false }
                                    ) {
                                        availableRecipients.forEach { mem ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(mem.name, fontWeight = FontWeight.Bold)
                                                        Text(
                                                            "${mem.industry} (${mem.company})",
                                                            fontSize = 11.sp,
                                                            color = SlateTextSecondary
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    selectedRecipientId = mem.id
                                                    showRecipientMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Mục 2: Mức độ khẩn cấp
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "MỨC ĐỘ KHẨN CẤP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy700,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    UrgencyLevel.values().forEach { level ->
                                        val isSelected = urgency == level
                                        val chipColor = when (level) {
                                            UrgencyLevel.HOT -> if (isSelected) HotRed else HotRedContainer
                                            UrgencyLevel.WARM -> if (isSelected) Orange500 else Orange100
                                            UrgencyLevel.COLD -> if (isSelected) ColdBlue else ColdBlueContainer
                                        }
                                        val textColor = if (isSelected) Color.White else Navy900

                                        Surface(
                                            color = chipColor,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { urgency = level }
                                                .testTag("urgency_chip_${level.name}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(vertical = 10.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = level.shortLabel,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Mục 3: Thông tin khách hàng
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "THÔNG TIN KHÁCH HÀNG / ĐỐI TÁC",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy700,
                                    letterSpacing = 0.5.sp
                                )

                                OutlinedTextField(
                                    value = clientName,
                                    onValueChange = { clientName = it },
                                    label = { Text("Họ và tên khách hàng *") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_client_name")
                                )

                                OutlinedTextField(
                                    value = clientPhone,
                                    onValueChange = { clientPhone = it },
                                    label = { Text("Số điện thoại liên hệ *") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_client_phone")
                                )

                                OutlinedTextField(
                                    value = clientEmail,
                                    onValueChange = { clientEmail = it },
                                    label = { Text("Địa chỉ Email") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_client_email")
                                )
                            }
                        }

                        // Mục 4: Thông số công trình & B2B
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Engineering,
                                        contentDescription = null,
                                        tint = Orange500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "THÔNG SỐ DỰ ÁN & HỢP ĐỒNG (B2B / XÂY DỰNG)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Navy700,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                OutlinedTextField(
                                    value = contractType,
                                    onValueChange = { contractType = it },
                                    label = { Text("Loại công trình / Gói dịch vụ *") },
                                    placeholder = { Text("Ví dụ: Hoàn thiện nội thất, Xây thô biệt thự...") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_contract_type")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = estimatedBudgetStr,
                                        onValueChange = { estimatedBudgetStr = it },
                                        label = { Text("Dự toán (VNĐ)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_estimated_budget")
                                    )
                                    OutlinedTextField(
                                        value = scaleAreaM2,
                                        onValueChange = { scaleAreaM2 = it },
                                        label = { Text("Quy mô / Diện tích (m²)") },
                                        placeholder = { Text("Ví dụ: 450 m²") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_scale_area")
                                    )
                                }

                                OutlinedTextField(
                                    value = projectLocation,
                                    onValueChange = { projectLocation = it },
                                    label = { Text("Địa điểm thi công / Tỉnh thành") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_project_location")
                                )

                                OutlinedTextField(
                                    value = notes,
                                    onValueChange = { notes = it },
                                    label = { Text("Nhu cầu chi tiết & Bối cảnh khách hàng") },
                                    minLines = 3,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_notes")
                                )
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
                    }

                    // Nút xác nhận gửi
                    Surface(
                        color = Color.White,
                        tonalElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            Button(
                                onClick = {
                                    if (clientName.isBlank()) {
                                        errorMessage = "Vui lòng nhập họ tên khách hàng."
                                        return@Button
                                    }
                                    if (clientPhone.isBlank()) {
                                        errorMessage = "Vui lòng nhập số điện thoại khách hàng."
                                        return@Button
                                    }
                                    if (selectedRecipientId.isBlank()) {
                                        errorMessage = "Vui lòng chọn thành viên nhận cơ hội."
                                        return@Button
                                    }
                                    val budget = estimatedBudgetStr.toDoubleOrNull() ?: 0.0
                                    onSubmit(
                                        selectedRecipientId,
                                        clientName,
                                        clientPhone,
                                        clientEmail,
                                        urgency,
                                        contractType,
                                        budget,
                                        scaleAreaM2,
                                        projectLocation,
                                        notes
                                    )
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Orange500,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("submit_referral_button")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Trao Cơ Hội Kinh Doanh Ngay",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
