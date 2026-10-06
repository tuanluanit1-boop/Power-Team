package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.NewMemberForm
import com.example.ui.theme.*

@Composable
fun UserManualAndRegistrationDialog(
    onDismiss: () -> Unit,
    onRegisterMember: (NewMemberForm) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Hướng Dẫn Sử Dụng, 1: Đăng Ký Thành Viên Mới
    val context = LocalContext.current

    // Form đăng ký hội viên mới
    var fullName by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var coreProducts by remember { mutableStateOf("") }
    var sponsorName by remember { mutableStateOf("Trần Tuấn Dũng") }
    var registrationSuccess by remember { mutableStateOf(false) }

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
                                    .testTag("manual_dialog_close")
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                            }
                            Column {
                                Text(
                                    text = "Sổ Tay & Đăng Ký Hội Viên",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                                Text(
                                    text = "Power Team TOPPRO • Chuẩn BNI Doanh Nghiệp",
                                    fontSize = 11.sp,
                                    color = SlateTextSecondary
                                )
                            }
                        }
                    }
                }

                // Sub Tabs
                Surface(
                    color = Color.White,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = Orange500
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Hướng Dẫn Sử Dụng", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            modifier = Modifier.testTag("tab_user_manual")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Đăng Ký Thành Viên", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            modifier = Modifier.testTag("tab_register_member")
                        )
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedTab == 0) {
                        // HƯỚNG DẪN SỬ DỤNG
                        ManualStepCard(
                            stepNumber = "1",
                            title = "Kích Hoạt Tài Khoản & Bản Quyền",
                            content = "Thành viên mới đăng ký được tự động cấp 7 ngày trải nghiệm miễn phí toàn bộ tính năng. Để duy trì quyền trao nhận cơ hội lâu dài, vào mục 'Quản trị / Hồ sơ' -> nhập mã kích hoạt vĩnh viễn (TOPPRO-xxxx-xxxx) do Trưởng ban phát hành."
                        )

                        ManualStepCard(
                            stepNumber = "2",
                            title = "Trao Cơ Hội Kinh Doanh & Quét Card AI",
                            content = "Khi có khách hàng cần dịch vụ từ đồng đội, bấm 'Trao cơ hội' ở Trang chủ hoặc màn hình Cơ hội. Bạn có thể bấm 'Quét Card AI' để chụp danh thiếp, hệ thống tự động bóc tách thông tin khách hàng, quy mô và dự toán công trình."
                        )

                        ManualStepCard(
                            stepNumber = "3",
                            title = "Quy Trình Họp 1-2-1 Hiệu Quả",
                            content = "Mỗi tháng thành viên cần hoàn thành tối thiểu 2 buổi họp 1-2-1 với đồng đội. Đặt lịch tại tab '1-2-1'. Sau buổi gặp, ghi lại 3 nội dung quan trọng: Sản phẩm thế mạnh, Dấu hiệu nhận diện cơ hội và Cam kết kết nối cụ thể."
                        )

                        ManualStepCard(
                            stepNumber = "4",
                            title = "Chốt Deal & Báo Cáo Doanh Số TYFCB",
                            content = "Khi cơ hội chuyển thành hợp đồng thành công, người nhận bấm 'Chốt thành công', nhập doanh số thực tế TYFCB (VNĐ) và chia sẻ % hoa hồng cảm ơn người trao (Giver) cũng như 1% trích vào Quỹ chung Power Team."
                        )

                        ManualStepCard(
                            stepNumber = "5",
                            title = "Kỷ Luật Tự Động & Quản Lý Quỹ",
                            content = "Để đảm bảo uy tín và cam kết, hệ thống tự động phạt thành viên để cơ hội quá 3 ngày không liên hệ, vắng mặt không phép hoặc nộp trễ biên bản. Tiền phạt nộp thẳng vào Quỹ nhóm để phục vụ phòng họp định kỳ."
                        )
                    } else {
                        // FORM ĐĂNG KÝ THÀNH VIÊN MỚI
                        if (registrationSuccess) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SuccessGreenContainer),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(48.dp))
                                    Text("Đăng Ký Thành Viên Thành Công!", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Navy900)
                                    Text(
                                        text = "Hồ sơ của thành viên $fullName ($companyName) đã được tiếp nhận và đưa vào danh sách Chapter Power Team TOPPRO với 7 ngày dùng thử miễn phí.",
                                        fontSize = 12.sp,
                                        color = SlateTextPrimary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Button(
                                        onClick = {
                                            registrationSuccess = false
                                            fullName = ""
                                            companyName = ""
                                            industry = ""
                                            phone = ""
                                            email = ""
                                            coreProducts = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                    ) {
                                        Text("Đăng ký thêm thành viên khác", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
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
                                        text = "PHIẾU GIA NHẬP POWER TEAM TOPPRO",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Navy700
                                    )
                                    Text(
                                        text = "Mỗi ngành nghề chỉ đại diện bởi 1 doanh nghiệp độc quyền trong Power Team để tránh cạnh tranh nội bộ.",
                                        fontSize = 11.sp,
                                        color = SlateTextSecondary
                                    )

                                    OutlinedTextField(
                                        value = fullName,
                                        onValueChange = { fullName = it },
                                        label = { Text("Họ và tên thành viên *") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("input_reg_fullname")
                                    )

                                    OutlinedTextField(
                                        value = companyName,
                                        onValueChange = { companyName = it },
                                        label = { Text("Tên công ty / Doanh nghiệp *") },
                                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("input_reg_company")
                                    )

                                    OutlinedTextField(
                                        value = industry,
                                        onValueChange = { industry = it },
                                        label = { Text("Ngành nghề đăng ký độc quyền *") },
                                        placeholder = { Text("Ví dụ: Thiết kế PCCC, Thi công Đá Granite...") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("input_reg_industry")
                                    )

                                    OutlinedTextField(
                                        value = phone,
                                        onValueChange = { phone = it },
                                        label = { Text("Số điện thoại / Hotline *") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("input_reg_phone")
                                    )

                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = { Text("Email liên hệ") },
                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("input_reg_email")
                                    )

                                    OutlinedTextField(
                                        value = coreProducts,
                                        onValueChange = { coreProducts = it },
                                        label = { Text("Sản phẩm & Dịch vụ chủ lực") },
                                        minLines = 2,
                                        modifier = Modifier.fillMaxWidth().testTag("input_reg_products")
                                    )

                                    OutlinedTextField(
                                        value = sponsorName,
                                        onValueChange = { sponsorName = it },
                                        label = { Text("Người bảo trợ / Giới thiệu") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("input_reg_sponsor")
                                    )

                                    Button(
                                        onClick = {
                                            if (fullName.isBlank() || companyName.isBlank() || industry.isBlank() || phone.isBlank()) {
                                                Toast.makeText(context, "Vui lòng nhập đầy đủ các trường bắt buộc (*)", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            val form = NewMemberForm(
                                                fullName = fullName,
                                                companyName = companyName,
                                                industry = industry,
                                                phone = phone,
                                                email = email,
                                                coreProducts = coreProducts,
                                                sponsorName = sponsorName
                                            )
                                            onRegisterMember(form)
                                            registrationSuccess = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("submit_registration_button")
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Xác Nhận Đăng Ký Hội Viên Mới", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Tác giả & Hotline
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Orange50),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Orange200, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "BẢN QUYỀN & PHÁT TRIỂN HỆ THỐNG",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Orange600
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Thiết kế bởi: Luận PCCC",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Navy900
                                    )
                                    Text(
                                        text = "Hotline / Zalo: 0912176050",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Orange600
                                    )
                                }

                                Button(
                                    onClick = {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0912176050"))
                                        try { context.startActivity(dialIntent) } catch (_: Exception) {}
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Liên hệ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManualStepCard(
    stepNumber: String,
    title: String,
    content: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Orange500),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Navy900)
                Text(text = content, fontSize = 11.sp, color = SlateTextSecondary, lineHeight = 16.sp)
            }
        }
    }
}
