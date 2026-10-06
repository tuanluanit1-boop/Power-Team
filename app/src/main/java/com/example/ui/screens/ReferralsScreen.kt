package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.CloseDealDialog
import com.example.ui.theme.*

@Composable
fun ReferralsScreen(
    currentUserId: String,
    members: List<Member>,
    givenReferrals: List<Referral>,
    receivedReferrals: List<Referral>,
    onCreateReferralClick: () -> Unit,
    onUpdateDealStatus: (referralId: String, newStatus: DealStatus, tyfcb: Double?, commPct: Double?, fundPct: Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Nhận cơ hội (Received), 1: Trao cơ hội (Sent)
    var selectedUrgencyFilter by remember { mutableStateOf<UrgencyLevel?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<DealStatus?>(null) }
    var referralToCloseSuccess by remember { mutableStateOf<Referral?>(null) }

    val activeList = if (selectedTab == 0) receivedReferrals else givenReferrals
    val filteredList = activeList.filter { ref ->
        (selectedUrgencyFilter == null || ref.urgency == selectedUrgencyFilter) &&
        (selectedStatusFilter == null || ref.status == selectedStatusFilter)
    }

    if (referralToCloseSuccess != null) {
        CloseDealDialog(
            referral = referralToCloseSuccess!!,
            onDismiss = { referralToCloseSuccess = null },
            onConfirmSuccess = { tyfcb, commPct, fundPct ->
                onUpdateDealStatus(referralToCloseSuccess!!.id, DealStatus.CLOSED_SUCCESS, tyfcb, commPct, fundPct)
                referralToCloseSuccess = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateReferralClick,
                containerColor = Orange500,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_create_referral")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tạo cơ hội")
                    Text("Trao cơ hội", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = SlateBg,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector: Nhận cơ hội (Received) vs Trao cơ hội (Sent)
            Surface(
                color = Color.White,
                tonalElevation = 2.dp,
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
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Nhận cơ hội",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                Surface(
                                    color = if (selectedTab == 0) Orange100 else SlateBg,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = receivedReferrals.size.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == 0) Orange600 else SlateTextSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.testTag("tab_take_received")
                    )

                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Trao cơ hội",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                Surface(
                                    color = if (selectedTab == 1) Orange100 else SlateBg,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = givenReferrals.size.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == 1) Orange600 else SlateTextSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.testTag("tab_give_sent")
                    )
                }
            }

            // Bộ lọc nhanh
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedUrgencyFilter == null && selectedStatusFilter == null,
                        onClick = {
                            selectedUrgencyFilter = null
                            selectedStatusFilter = null
                        },
                        label = { Text("Tất cả (${activeList.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Navy700,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                items(UrgencyLevel.values()) { level ->
                    FilterChip(
                        selected = selectedUrgencyFilter == level,
                        onClick = {
                            selectedUrgencyFilter = if (selectedUrgencyFilter == level) null else level
                        },
                        label = { Text(level.shortLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (level) {
                                UrgencyLevel.HOT -> HotRed
                                UrgencyLevel.WARM -> Orange500
                                UrgencyLevel.COLD -> ColdBlue
                            },
                            selectedLabelColor = Color.White
                        )
                    )
                }

                items(DealStatus.values()) { status ->
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == status) null else status
                        },
                        label = { Text(status.label) }
                    )
                }
            }

            // Danh sách cơ hội
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = SlateTextMuted,
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "Chưa Có Cơ Hội Nào",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Navy900
                        )
                        Text(
                            text = "Kết nối và trao cơ hội kinh doanh liên ngành cho các thành viên Chapter.",
                            fontSize = 12.sp,
                            color = SlateTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredList, key = { it.id }) { ref ->
                        ReferralDetailedCard(
                            referral = ref,
                            isReceived = selectedTab == 0,
                            members = members,
                            onAdvanceStatus = { nextStatus ->
                                if (nextStatus == DealStatus.CLOSED_SUCCESS) {
                                    referralToCloseSuccess = ref
                                } else {
                                    onUpdateDealStatus(ref.id, nextStatus, null, null, null)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReferralDetailedCard(
    referral: Referral,
    isReceived: Boolean,
    members: List<Member>,
    onAdvanceStatus: (DealStatus) -> Unit
) {
    val context = LocalContext.current
    val counterpartMember = members.find {
        it.id == (if (isReceived) referral.giverMemberId else referral.takerMemberId)
    }

    val urgencyColor = when (referral.urgency) {
        UrgencyLevel.HOT -> HotRed
        UrgencyLevel.WARM -> Orange500
        UrgencyLevel.COLD -> ColdBlue
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("referral_card_${referral.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cảnh báo quá hạn
            if (referral.isOverdueReceived()) {
                Surface(
                    color = HotRedContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = HotRed, modifier = Modifier.size(16.dp))
                        Text(
                            text = "QUÁ HẠN: Mới nhận > 3 ngày chưa liên hệ khách hàng!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HotRed
                        )
                    }
                }
            } else if (referral.isOverdueProcessing()) {
                Surface(
                    color = Orange100,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = Orange600, modifier = Modifier.size(16.dp))
                        Text(
                            text = "NHẮC NHỞ: Đang xử lý > 7 ngày chưa cập nhật báo giá/kết quả.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Orange600
                        )
                    }
                }
            }

            // Hàng đầu: Độ khẩn cấp & Trạng thái
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = urgencyColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(urgencyColor)
                        )
                        Text(
                            text = referral.urgency.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = urgencyColor
                        )
                    }
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
                        fontSize = 11.sp,
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

            // Tiêu đề cơ hội
            Text(
                text = referral.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Navy900
            )

            // Thành viên đối tác
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isReceived) "Người trao:" else "Trao cho:",
                    fontSize = 12.sp,
                    color = SlateTextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(counterpartMember?.avatarColorHex ?: 0xFF1E3A8A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = counterpartMember?.avatarInitials ?: "TV",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${counterpartMember?.name} (${counterpartMember?.industry})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Navy700
                )
            }

            // Thông tin khách hàng
            Surface(
                color = SlateBg,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = referral.clientName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Navy900
                        )
                        Text(
                            text = "${referral.clientPhone} • ${referral.clientEmail}",
                            fontSize = 11.sp,
                            color = SlateTextSecondary
                        )
                    }

                    // Nút gọi điện
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${referral.clientPhone}"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Navy100)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Gọi cho khách",
                            tint = Navy700,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Thông số B2B / Xây dựng
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, SlateBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Gói dịch vụ:", fontSize = 11.sp, color = SlateTextSecondary)
                    Text(referral.contractType, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Navy900)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Dự toán:", fontSize = 11.sp, color = SlateTextSecondary)
                    Text("${String.format("%,.0f", referral.estimatedBudget)} đ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Quy mô / Diện tích:", fontSize = 11.sp, color = SlateTextSecondary)
                    Text(referral.scaleAreaM2, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Navy900)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Địa điểm:", fontSize = 11.sp, color = SlateTextSecondary)
                    Text(referral.projectLocation, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Navy900)
                }
            }

            if (referral.notes.isNotBlank()) {
                Text(
                    text = "Ghi chú: ${referral.notes}",
                    fontSize = 11.sp,
                    color = SlateTextSecondary
                )
            }

            // Doanh số TYFCB nếu đã chốt
            if (referral.status == DealStatus.CLOSED_SUCCESS && referral.tyfcbAmount != null) {
                Surface(
                    color = SuccessGreenContainer,
                    shape = RoundedCornerShape(10.dp),
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
                                text = "DOANH SỐ CHỐT TYFCB",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                            Text(
                                text = "${String.format("%,.0f", referral.tyfcbAmount)} đ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = SuccessGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Hoa hồng người trao: ${referral.commissionPercent ?: 5.0}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                            Text(
                                text = "Trích Quỹ nhóm: ${referral.commonFundPercent ?: 1.0}%",
                                fontSize = 10.sp,
                                color = SlateTextSecondary
                            )
                        }
                    }
                }
            }

            // Nút chuyển trạng thái
            if (referral.status != DealStatus.CLOSED_SUCCESS && referral.status != DealStatus.CLOSED_FAILED) {
                Divider(color = SlateBorder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tiến độ Deal:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextSecondary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        when (referral.status) {
                            DealStatus.RECEIVED -> {
                                Button(
                                    onClick = { onAdvanceStatus(DealStatus.CONTACTED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ColdBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("action_advance_contacted_${referral.id}")
                                ) {
                                    Text("Đã liên hệ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            DealStatus.CONTACTED -> {
                                Button(
                                    onClick = { onAdvanceStatus(DealStatus.QUOTED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PurpleStatus),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("action_advance_quoted_${referral.id}")
                                ) {
                                    Text("Gửi báo giá", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            DealStatus.QUOTED -> {
                                OutlinedButton(
                                    onClick = { onAdvanceStatus(DealStatus.CLOSED_FAILED) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HotRed),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("Thất bại", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { onAdvanceStatus(DealStatus.CLOSED_SUCCESS) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("action_advance_success_${referral.id}")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Chốt thành công", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}
