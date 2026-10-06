package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun AdminProfileScreen(
    session: UserSession,
    currentMember: Member?,
    members: List<Member>,
    licenseKeys: List<LicenseKey>,
    kpiConfig: KpiConfig,
    chapterGoal: ChapterGoalConfig,
    selectedReportPeriod: ReportPeriod,
    memberReports: List<MemberPeriodReport>,
    systemAlerts: List<SystemAlert>,
    onSetReportPeriod: (ReportPeriod) -> Unit,
    onActivateLicenseClick: () -> Unit,
    onGenerateKey: (note: String) -> String,
    onRunDisciplineScan: () -> Int,
    onUpdateKpiConfig: (KpiConfig) -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    onOpenInstallGuide: () -> Unit,
    onOpenManualAndRegister: () -> Unit,
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentAuthUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    var newKeyNote by remember { mutableStateOf("") }
    var generatedKeyFeedback by remember { mutableStateOf<String?>(null) }
    var disciplineScanFeedback by remember { mutableStateOf<String?>(null) }

    // Quản lý giá trị KPI
    var weeklyRefInput by remember { mutableStateOf(kpiConfig.weeklyReferralsTarget.toString()) }
    var monthly121Input by remember { mutableStateOf(kpiConfig.monthly121Target.toString()) }
    var overdueDaysInput by remember { mutableStateOf(kpiConfig.overdueResponseDaysLimit.toString()) }
    var penaltyAmountInput by remember { mutableStateOf(kpiConfig.overduePenaltyAmount.toLong().toString()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Thẻ thông tin cá nhân
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Navy700),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Orange500),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentMember?.avatarInitials ?: "TD",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Column {
                                Text(
                                    text = currentMember?.name ?: "Trần Tuấn Dũng",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "${currentMember?.industry} • ${currentMember?.company}",
                                    fontSize = 11.sp,
                                    color = Navy100
                                )
                            }
                        }

                        // Nhãn vai trò
                        Surface(
                            color = if (session.activeRole == UserRole.ADMIN) Orange500 else Navy800,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (session.activeRole == UserRole.ADMIN) "CHẾ ĐỘ ADMIN" else "HỘI VIÊN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Thông tin bản quyền
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Navy800)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (session.licenseType == LicenseType.LIFETIME) Icons.Default.Verified else Icons.Default.Timer,
                                contentDescription = null,
                                tint = Orange400,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (session.licenseType == LicenseType.LIFETIME) "Bản quyền: Vĩnh viễn (Trọn đời)" else "Bản quyền: Dùng thử 7 ngày (Còn ${session.trialDaysRemaining} ngày)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (session.licenseType != LicenseType.LIFETIME) {
                            TextButton(
                                onClick = onActivateLicenseClick,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.testTag("profile_upgrade_button")
                            ) {
                                Text("Nhập mã", color = Orange400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // PHÍM TẮT: CÀI ĐẶT ANDROID / IOS & HƯỚNG DẪN / ĐĂNG KÝ
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenInstallGuide() }
                        .testTag("card_install_guide")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Navy100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Navy700, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Cài Đặt App", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Navy900)
                            Text("Android & iOS", fontSize = 10.sp, color = SlateTextSecondary)
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenManualAndRegister() }
                        .testTag("card_manual_and_reg")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Orange100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = Orange600, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Sổ Tay & Đăng Ký", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Navy900)
                            Text("Quy trình & Hội viên", fontSize = 10.sp, color = SlateTextSecondary)
                        }
                    }
                }
            }
        }

        // TÀI KHOẢN GOOGLE & ĐỒNG BỘ CLOUD FIRESTORE
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Navy100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = Navy700,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Firebase Cloud Firestore",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Navy900
                            )
                            Text(
                                text = currentAuthUser?.email ?: "Đang đồng bộ trực tuyến thời gian thực",
                                fontSize = 11.sp,
                                color = SlateTextSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Đăng xuất",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // BÁO CÁO DÀNH CHO TRƯỞNG BAN / QUẢN LÝ (REPORT MODULE)
        if (session.activeRole == UserRole.ADMIN) {
            // Mục tiêu & Đo lường mục tiêu Chapter
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = Orange500)
                                Text(
                                    text = "MỤC TIÊU & ĐO LƯỜNG CHAPTER",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                            }
                            Surface(
                                color = Orange100,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Tháng 10/2026",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Orange600,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Đo lường Doanh số TYFCB
                        val tyfcbPercent = (chapterGoal.currentTyfcbRevenue / chapterGoal.targetTyfcbRevenue).toFloat().coerceIn(0f, 1f)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Mục tiêu Doanh số TYFCB:", fontSize = 12.sp, color = SlateTextPrimary)
                                Text(
                                    "${String.format("%,.0f", chapterGoal.currentTyfcbRevenue)} đ / ${String.format("%,.0f", chapterGoal.targetTyfcbRevenue)} đ (${(tyfcbPercent * 100).toInt()}%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                            LinearProgressIndicator(
                                progress = { tyfcbPercent },
                                color = SuccessGreen,
                                trackColor = SlateBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }

                        // Đo lường Cơ hội
                        val refPercent = (chapterGoal.currentReferrals.toFloat() / chapterGoal.targetReferrals.toFloat()).coerceIn(0f, 1f)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Mục tiêu Cơ hội trao đi:", fontSize = 12.sp, color = SlateTextPrimary)
                                Text(
                                    "${chapterGoal.currentReferrals} / ${chapterGoal.targetReferrals} cơ hội (${(refPercent * 100).toInt()}%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Orange600
                                )
                            }
                            LinearProgressIndicator(
                                progress = { refPercent },
                                color = Orange500,
                                trackColor = SlateBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }

                        // Đo lường 1-2-1
                        val meetPercent = (chapterGoal.currentMeetings121.toFloat() / chapterGoal.targetMeetings121.toFloat()).coerceIn(0f, 1f)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Mục tiêu Buổi gặp 1-2-1:", fontSize = 12.sp, color = SlateTextPrimary)
                                Text(
                                    "${chapterGoal.currentMeetings121} / ${chapterGoal.targetMeetings121} buổi (${(meetPercent * 100).toInt()}%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy700
                                )
                            }
                            LinearProgressIndicator(
                                progress = { meetPercent },
                                color = Navy600,
                                trackColor = SlateBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }

            // HỆ THỐNG CẢNH BÁO TỰ ĐỘNG
            if (systemAlerts.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HotRedContainer),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = HotRed)
                                Text(
                                    text = "CẢNH BÁO TIẾN ĐỘ & KỶ LUẬT (${systemAlerts.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HotRed
                                )
                            }

                            systemAlerts.take(3).forEach { alert ->
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HotRed)
                                            Text(alert.message, fontSize = 11.sp, color = SlateTextPrimary)
                                        }
                                        if (alert.actionText != null) {
                                            Surface(
                                                color = HotRedContainer,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = alert.actionText,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = HotRed,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // BÁO CÁO REPORT HÀNG TUẦN / THÁNG / NĂM
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Assessment, contentDescription = null, tint = Navy700)
                                Text(
                                    text = "BÁO CÁO THÀNH VIÊN THEO KỲ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                            }

                            IconButton(
                                onClick = {
                                    val summary = memberReports.joinToString("\n") {
                                        "• ${it.memberName} (${it.company}): Trao ${it.referralsGiven} ref | 1-2-1: ${it.meetings121Count} | TYFCB: ${String.format("%,.0f đ", it.tyfcbRevenue)}"
                                    }
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Báo cáo Chapter", "BÁO CÁO POWER TEAM TOPPRO:\n$summary")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Đã sao chép báo cáo gửi Zalo / Email!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Chia sẻ", tint = Navy700)
                            }
                        }

                        // Chọn kỳ báo cáo: Tuần / Tháng / Năm
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ReportPeriod.values().forEach { period ->
                                val isSelected = selectedReportPeriod == period
                                Surface(
                                    color = if (isSelected) Orange500 else SlateBg,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSetReportPeriod(period) }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (period) {
                                                ReportPeriod.WEEK -> "Tuần này"
                                                ReportPeriod.MONTH -> "Tháng này"
                                                ReportPeriod.YEAR -> "Năm nay"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Navy900
                                        )
                                    }
                                }
                            }
                        }

                        Divider(color = SlateBorder)

                        // Bảng số liệu chi tiết từng thành viên
                        memberReports.forEach { report ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SlateBg)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(report.memberName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Navy900)
                                        Text("${report.industry} • ${report.company}", fontSize = 10.sp, color = SlateTextSecondary)
                                    }
                                    Text(
                                        text = "${String.format("%,.0f", report.tyfcbRevenue)} đ",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = SuccessGreen
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Cơ hội trao / nhận:", fontSize = 11.sp, color = SlateTextSecondary)
                                    Text("Trao ${report.referralsGiven} (MT: ${report.referralsGivenGoal}) • Nhận ${report.referralsReceived}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Số buổi 1-2-1:", fontSize = 11.sp, color = SlateTextSecondary)
                                    Text("${report.meetings121Count} / ${report.meetings121Goal} buổi", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                if (report.warningMessage != null) {
                                    Surface(
                                        color = HotRedContainer,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = report.warningMessage,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = HotRed,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 1. Quản lý tạo mã kích hoạt bản quyền
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = Orange500)
                            Text(
                                text = "TẠO MÃ KÍCH HOẠT BẢN QUYỀN TOPPRO",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        }

                        Text(
                            text = "Admin có thể phát hành mã kích hoạt vĩnh viễn (TOPPRO-xxxx-xxxx) cấp cho thành viên mới gia nhập Power Team.",
                            fontSize = 12.sp,
                            color = SlateTextSecondary
                        )

                        OutlinedTextField(
                            value = newKeyNote,
                            onValueChange = { newKeyNote = it },
                            label = { Text("Ghi chú thành viên / Doanh nghiệp") },
                            placeholder = { Text("Ví dụ: Cấp cho Đặng Quang Huy (Logistics)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_key_note")
                        )

                        Button(
                            onClick = {
                                val generatedKey = onGenerateKey(newKeyNote)
                                generatedKeyFeedback = "Đã tạo mã: $generatedKey"
                                newKeyNote = ""
                                Toast.makeText(context, "Mã mới: $generatedKey", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("admin_generate_key_button")
                        ) {
                            Icon(Icons.Default.AddModerator, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tạo Mã Bản Quyền Mới", fontWeight = FontWeight.Bold)
                        }

                        if (generatedKeyFeedback != null) {
                            Surface(
                                color = SuccessGreenContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = generatedKeyFeedback ?: "",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = SuccessGreen
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("License Key", generatedKeyFeedback?.substringAfter(": ") ?: "")
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Đã sao chép mã vào bộ nhớ tạm", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Sao chép", tint = SuccessGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Chạy quét kỷ luật tự động
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Rule, contentDescription = null, tint = HotRed)
                            Text(
                                text = "QUÉT KIỂM TRA KỶ LUẬT TỰ ĐỘNG",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        }
                        Text(
                            text = "Rà soát toàn bộ cơ hội kinh doanh và biên bản họp trong Chapter. Tự động phạt tiền các trường hợp quá hạn phản hồi > ${kpiConfig.overdueResponseDaysLimit} ngày.",
                            fontSize = 12.sp,
                            color = SlateTextSecondary
                        )

                        OutlinedButton(
                            onClick = {
                                val count = onRunDisciplineScan()
                                disciplineScanFeedback = if (count > 0) "Hoàn tất: Đã lập $count phiếu phạt vi phạm mới." else "Hoàn tất: Toàn bộ thành viên tuân thủ đúng quy định."
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HotRed),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(HotRed)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("run_discipline_scan_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chạy Quét Vi Phạm Kỷ Luật", fontWeight = FontWeight.Bold)
                        }

                        if (disciplineScanFeedback != null) {
                            Text(
                                text = disciplineScanFeedback ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (disciplineScanFeedback!!.contains("phiếu phạt")) HotRed else SuccessGreen
                            )
                        }
                    }
                }
            }

            // 3. Cấu hình chỉ tiêu KPI
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = Navy700)
                            Text(
                                text = "CẤU HÌNH CHỈ TIÊU KPI & MỨC PHẠT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = weeklyRefInput,
                                onValueChange = { weeklyRefInput = it },
                                label = { Text("Chỉ tiêu Cơ hội/Tuần") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = monthly121Input,
                                onValueChange = { monthly121Input = it },
                                label = { Text("Chỉ tiêu 1-2-1/Tháng") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = overdueDaysInput,
                                onValueChange = { overdueDaysInput = it },
                                label = { Text("Hạn phản hồi (Ngày)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = penaltyAmountInput,
                                onValueChange = { penaltyAmountInput = it },
                                label = { Text("Mức phạt vi phạm (VNĐ)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = {
                                val updated = kpiConfig.copy(
                                    weeklyReferralsTarget = weeklyRefInput.toIntOrNull() ?: 1,
                                    monthly121Target = monthly121Input.toIntOrNull() ?: 2,
                                    overdueResponseDaysLimit = overdueDaysInput.toIntOrNull() ?: 3,
                                    overduePenaltyAmount = penaltyAmountInput.toDoubleOrNull() ?: 500000.0
                                )
                                onUpdateKpiConfig(updated)
                                Toast.makeText(context, "Đã cập nhật chỉ tiêu KPI", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Navy700),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Lưu Cấu Hình KPI Chapter", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4. Danh sách mã bản quyền đã cấp
            item {
                Text(
                    text = "DANH SÁCH MÃ BẢN QUYỀN ĐÃ CẤP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextSecondary,
                    letterSpacing = 0.5.sp
                )
            }

            items(licenseKeys) { keyItem ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = keyItem.key,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Navy900
                            )
                            Text(
                                text = keyItem.note,
                                fontSize = 11.sp,
                                color = SlateTextSecondary
                            )
                        }

                        Surface(
                            color = if (keyItem.status == LicenseStatus.USED) SlateBg else SuccessGreenContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (keyItem.status == LicenseStatus.USED) "Đã dùng" else "Chưa dùng",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (keyItem.status == LicenseStatus.USED) SlateTextMuted else SuccessGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Chế độ Hội viên: Huy hiệu & Thành tích
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Orange500)
                            Text(
                                text = "PHÒNG TRUYỀN THỐNG & HUY HIỆU DANH DỰ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BadgeCard(
                                title = "CLB Doanh Số 1 Tỷ+",
                                subtitle = "Nhà vô địch TYFCB",
                                icon = Icons.Default.MonetizationOn,
                                unlocked = (currentMember?.tyfcbContributed ?: 0.0) >= 1000000000.0,
                                modifier = Modifier.weight(1f)
                            )
                            BadgeCard(
                                title = "Bậc Thầy Kết Nối",
                                subtitle = "5+ Cơ hội chất lượng",
                                icon = Icons.Default.VolunteerActivism,
                                unlocked = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BadgeCard(
                                title = "Tiên Phong 1-2-1",
                                subtitle = "Xây dựng hiệp lực",
                                icon = Icons.Default.Handshake,
                                unlocked = true,
                                modifier = Modifier.weight(1f)
                            )
                            BadgeCard(
                                title = "Kỷ Luật Chuẩn Mực",
                                subtitle = "Không vi phạm quy chế",
                                icon = Icons.Default.Shield,
                                unlocked = false,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // FOOTER BRANDING: THIẾT KẾ BỞI LUẬN PCCC - 0912176050
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Navy900),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("footer_branding_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "POWER TEAM TOPPRO",
                        color = Orange400,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Thiết kế bởi @ Luận PCCC - 0912176050",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Giải pháp chuyển đổi số quản trị Chapter & B2B Referral BNI",
                        color = Navy100,
                        fontSize = 11.sp
                    )
                    Button(
                        onClick = {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0912176050"))
                            try { context.startActivity(dialIntent) } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hotline / Zalo: 0912176050", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    unlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (unlocked) Orange50 else SlateBg,
        shape = RoundedCornerShape(12.dp),
        border = if (unlocked) androidx.compose.foundation.BorderStroke(1.dp, Orange200) else null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (unlocked) Orange500 else SlateBorder),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (unlocked) Color.White else SlateTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (unlocked) Navy900 else SlateTextMuted
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = if (unlocked) SlateTextSecondary else SlateTextMuted
            )
        }
    }
}
