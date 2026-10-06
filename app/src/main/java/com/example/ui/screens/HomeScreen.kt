package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.NavigationTab
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    session: UserSession,
    currentMember: Member?,
    givenReferrals: List<Referral>,
    receivedReferrals: List<Referral>,
    overdueReferrals: List<Referral>,
    meetings: List<Meeting121>,
    commissionsReceivable: Double,
    kpiConfig: KpiConfig,
    onNavigateTab: (NavigationTab) -> Unit,
    onCreateReferralClick: () -> Unit,
    onSchedule121Click: () -> Unit,
    onActivateLicenseClick: () -> Unit,
    onToggleTrialSimulated: () -> Unit,
    onReferralSelected: (Referral) -> Unit,
    onOpenInstallGuide: () -> Unit = {},
    onOpenManualAndRegister: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val completedMeetingsThisMonth = meetings.count { it.isCompleted }
    val givenCount = givenReferrals.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Banner Trạng thái Bản quyền & Dùng thử
        item {
            if (session.licenseType == LicenseType.LIFETIME) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Navy700),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lifetime_active_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Orange500),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Column {
                                Text(
                                    text = "BẢN QUYỀN VĨNH VIỄN",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Không giới hạn cơ hội kinh doanh & Deal pipeline",
                                    color = Navy100,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            } else if (session.isTrialExpiredSimulated) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = HotRedContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, HotRed, RoundedCornerShape(16.dp))
                        .testTag("trial_expired_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                Icon(Icons.Default.Lock, contentDescription = null, tint = HotRed)
                                Text(
                                    text = "Hết Hạn Dùng Thử 7 Ngày",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = HotRed
                                )
                            }
                            TextButton(
                                onClick = onToggleTrialSimulated,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Khôi phục", fontSize = 11.sp, color = SlateTextSecondary)
                            }
                        }
                        Text(
                            text = "Tính năng tạo cơ hội kinh doanh mới đã bị tạm khóa. Vui lòng nhập mã kích hoạt từ Ban Điều Hành để mở khóa toàn bộ tính năng.",
                            fontSize = 12.sp,
                            color = SlateTextPrimary
                        )
                        Button(
                            onClick = onActivateLicenseClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("banner_upgrade_now_button")
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nhập Mã Kích Hoạt Vĩnh Viễn", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Orange50),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Orange200, RoundedCornerShape(16.dp))
                        .testTag("trial_countdown_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                Icon(Icons.Default.Timer, contentDescription = null, tint = Orange600)
                                Text(
                                    text = "Dùng Thử: Còn ${session.trialDaysRemaining} Ngày",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Orange600
                                )
                            }
                            TextButton(
                                onClick = onToggleTrialSimulated,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Thử hết hạn", fontSize = 10.sp, color = SlateTextMuted)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Đã có mã bản quyền? Kích hoạt vĩnh viễn.",
                                fontSize = 11.sp,
                                color = SlateTextSecondary
                            )
                            Button(
                                onClick = onActivateLicenseClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Nâng cấp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Nhắc nhở thông minh: Cơ hội quá hạn
        if (overdueReferrals.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = HotRedContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("overdue_alert_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(HotRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NHẮC NHỞ: Cơ hội quá hạn phản hồi!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = HotRed
                            )
                            Text(
                                text = "${overdueReferrals.first().title} chưa được liên hệ quá 3 ngày. Nguy cơ bị phạt kỷ luật!",
                                fontSize = 11.sp,
                                color = SlateTextPrimary
                            )
                        }
                        Button(
                            onClick = { onReferralSelected(overdueReferrals.first()) },
                            colors = ButtonDefaults.buttonColors(containerColor = HotRed),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Xử lý", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Phím tắt thao tác nhanh
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "THAO TÁC NHANH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextSecondary,
                    letterSpacing = 0.5.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "Trao cơ hội",
                        subtitle = "Tạo cơ hội mới",
                        icon = Icons.Default.AddBusiness,
                        accentColor = Orange500,
                        tag = "quick_action_new_referral",
                        modifier = Modifier.weight(1f),
                        onClick = onCreateReferralClick
                    )
                    QuickActionCard(
                        title = "Hẹn 1-2-1",
                        subtitle = "Gặp gỡ đồng đội",
                        icon = Icons.Default.Handshake,
                        accentColor = Navy600,
                        tag = "quick_action_schedule_121",
                        modifier = Modifier.weight(1f),
                        onClick = onSchedule121Click
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "Ví hoa hồng",
                        subtitle = "${String.format("%,.0f", commissionsReceivable)} đ cần thu",
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = SuccessGreen,
                        tag = "quick_action_commissions",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(NavigationTab.FINANCE) }
                    )
                    QuickActionCard(
                        title = "Ma trận 1-2-1",
                        subtitle = "Kết nối chéo tháng",
                        icon = Icons.Default.GridOn,
                        accentColor = PurpleStatus,
                        tag = "quick_action_matrix",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(NavigationTab.MEETINGS_121) }
                    )
                }
            }
        }

        // 4. Chỉ tiêu KPI Cá nhân
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("kpi_progress_card")
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
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Orange500)
                            Text(
                                text = "CHỈ TIÊU KPI CÁ NHÂN",
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
                                text = "Mục tiêu Tuần/Tháng",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Orange600,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // KPI 1: Cơ hội đã trao
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Cơ hội đã trao (Chỉ tiêu: ≥ ${kpiConfig.weeklyReferralsTarget}/tuần)",
                                fontSize = 12.sp,
                                color = SlateTextPrimary
                            )
                            Text(
                                text = "$givenCount / ${kpiConfig.weeklyReferralsTarget * 2}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (givenCount >= kpiConfig.weeklyReferralsTarget) SuccessGreen else Orange600
                            )
                        }
                        val refProgress = (givenCount.toFloat() / (kpiConfig.weeklyReferralsTarget * 2).toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { refProgress },
                            color = Orange500,
                            trackColor = SlateBorder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }

                    // KPI 2: Họp 1-2-1
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Họp 1-2-1 đã hoàn thành (Chỉ tiêu: ≥ ${kpiConfig.monthly121Target}/tháng)",
                                fontSize = 12.sp,
                                color = SlateTextPrimary
                            )
                            Text(
                                text = "$completedMeetingsThisMonth / ${kpiConfig.monthly121Target}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (completedMeetingsThisMonth >= kpiConfig.monthly121Target) SuccessGreen else SlateTextSecondary
                            )
                        }
                        val meetingProgress = (completedMeetingsThisMonth.toFloat() / kpiConfig.monthly121Target.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { meetingProgress },
                            color = Navy600,
                            trackColor = SlateBorder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }

                    // Kỷ luật & Trạng thái
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SlateBg)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Ý thức kỷ luật:",
                                fontSize = 11.sp,
                                color = SlateTextSecondary
                            )
                        }
                        Text(
                            text = if (overdueReferrals.isEmpty()) "Chuẩn mực (0 vi phạm)" else "Cần xử lý quá hạn",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (overdueReferrals.isEmpty()) SuccessGreen else HotRed
                        )
                    }
                }
            }
        }

        // 5. Cơ hội kinh doanh gần đây
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CƠ HỘI ĐANG THEO DÕI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextSecondary,
                    letterSpacing = 0.5.sp
                )
                TextButton(
                    onClick = { onNavigateTab(NavigationTab.REFERRALS) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Xem tất cả", fontSize = 12.sp, color = Orange600, fontWeight = FontWeight.Bold)
                }
            }
        }

        items((receivedReferrals + givenReferrals).take(3)) { ref ->
            ReferralMiniCard(
                referral = ref,
                onClick = { onReferralSelected(ref) }
            )
        }

        // Phím tắt Hướng Dẫn & Cài Đặt
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenInstallGuide,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy700),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cài App Android/iOS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenManualAndRegister,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Orange600),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Đăng Ký Hội Viên", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // FOOTER BRANDING: THIẾT KẾ BỞI LUẬN PCCC - 0912176050
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Navy900),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
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
                        text = "Phần mềm Quản trị Referral & Văn hóa Chapter BNI",
                        color = Navy100,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Navy900
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = SlateTextSecondary
                )
            }
        }
    }
}

@Composable
fun ReferralMiniCard(
    referral: Referral,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("referral_mini_card_${referral.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val urgencyColor = when (referral.urgency) {
                        UrgencyLevel.HOT -> HotRed
                        UrgencyLevel.WARM -> Orange500
                        UrgencyLevel.COLD -> ColdBlue
                    }
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(urgencyColor)
                    )
                    Text(
                        text = referral.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Navy900
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${referral.clientName} • Dự toán: ${String.format("%,.0f", referral.estimatedBudget)} đ",
                    fontSize = 11.sp,
                    color = SlateTextSecondary
                )
            }

            Surface(
                color = when (referral.status) {
                    DealStatus.CLOSED_SUCCESS -> SuccessGreenContainer
                    DealStatus.CLOSED_FAILED -> HotRedContainer
                    DealStatus.QUOTED -> PurpleContainer
                    DealStatus.CONTACTED -> ColdBlueContainer
                    DealStatus.RECEIVED -> Orange100
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = referral.status.label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (referral.status) {
                        DealStatus.CLOSED_SUCCESS -> SuccessGreen
                        DealStatus.CLOSED_FAILED -> HotRed
                        DealStatus.QUOTED -> PurpleStatus
                        DealStatus.CONTACTED -> ColdBlue
                        DealStatus.RECEIVED -> Orange600
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
