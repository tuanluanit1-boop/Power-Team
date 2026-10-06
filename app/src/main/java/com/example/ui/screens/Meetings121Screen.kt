package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Meeting121
import com.example.data.model.Member
import com.example.ui.MemberPairStatus
import com.example.ui.components.Schedule121Dialog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Meetings121Screen(
    currentUserId: String,
    members: List<Member>,
    meetings: List<Meeting121>,
    interactionMatrix: List<MemberPairStatus>,
    onScheduleMeeting: (guestId: String, time: Long, location: String) -> Unit,
    onSaveMinutes: (meetingId: String, coreProducts: String, idealClient: String, commitments: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableStateOf(0) } // 0: Lịch 1-2-1 của tôi, 1: Ma trận kết nối, 2: Bảng xếp hạng
    var showScheduleDialog by remember { mutableStateOf(false) }
    var preselectedMemberIdForSchedule by remember { mutableStateOf<String?>(null) }
    var meetingToLogMinutes by remember { mutableStateOf<Meeting121?>(null) }

    val myMeetings = meetings.filter { it.hostMemberId == currentUserId || it.guestMemberId == currentUserId }

    if (showScheduleDialog) {
        Schedule121Dialog(
            members = members,
            currentUserId = currentUserId,
            preselectedMemberId = preselectedMemberIdForSchedule,
            existingMeetingToComplete = meetingToLogMinutes,
            onDismiss = {
                showScheduleDialog = false
                preselectedMemberIdForSchedule = null
                meetingToLogMinutes = null
            },
            onSchedule = onScheduleMeeting,
            onSaveMinutes = onSaveMinutes
        )
    }

    Scaffold(
        floatingActionButton = {
            if (selectedSubTab != 2) {
                FloatingActionButton(
                    onClick = {
                        preselectedMemberIdForSchedule = null
                        meetingToLogMinutes = null
                        showScheduleDialog = true
                    },
                    containerColor = Orange500,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_schedule_121")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Lên lịch 1-2-1")
                        Text("Hẹn 1-2-1", fontWeight = FontWeight.Bold)
                    }
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
            // Thanh chuyển tab con
            Surface(
                color = Color.White,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = selectedSubTab,
                    containerColor = Color.White,
                    contentColor = Orange500
                ) {
                    Tab(
                        selected = selectedSubTab == 0,
                        onClick = { selectedSubTab = 0 },
                        text = { Text("Lịch của tôi (${myMeetings.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        modifier = Modifier.testTag("subtab_my_121")
                    )
                    Tab(
                        selected = selectedSubTab == 1,
                        onClick = { selectedSubTab = 1 },
                        text = { Text("Ma trận kết nối", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        modifier = Modifier.testTag("subtab_interaction_matrix")
                    )
                    Tab(
                        selected = selectedSubTab == 2,
                        onClick = { selectedSubTab = 2 },
                        text = { Text("Bảng xếp hạng", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        modifier = Modifier.testTag("subtab_leaderboard")
                    )
                }
            }

            when (selectedSubTab) {
                0 -> {
                    // Danh sách lịch 1-2-1 của tôi
                    if (myMeetings.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Chưa có lịch hẹn 1-2-1 nào.", color = SlateTextSecondary)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(myMeetings) { meeting ->
                                val partnerId = if (meeting.hostMemberId == currentUserId) meeting.guestMemberId else meeting.hostMemberId
                                val partner = members.find { it.id == partnerId }

                                MeetingCard(
                                    meeting = meeting,
                                    partner = partner,
                                    onLogMinutesClick = {
                                        meetingToLogMinutes = meeting
                                        showScheduleDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Ma trận tương tác: Ai chưa gặp ai trong tháng
                    val unmetPairs = interactionMatrix.filter { !it.hasMetThisMonth }
                    val metPairs = interactionMatrix.filter { it.hasMetThisMonth }

                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Navy700),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
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
                                            text = "MA TRẬN KẾT NỐI 1-2-1 TRONG THÁNG",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "${metPairs.size} cặp đã gặp • ${unmetPairs.size} cặp cần kết nối",
                                            color = Navy100,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Orange500),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Hub, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "CÁC CẶP CHƯA GẶP 1-2-1 TRONG THÁNG (${unmetPairs.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HotRed,
                                letterSpacing = 0.5.sp
                            )
                        }

                        items(unmetPairs) { pair ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(14.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = pair.member1.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Navy900
                                            )
                                            Text("↔", color = Orange500, fontWeight = FontWeight.Black)
                                            Text(
                                                text = pair.member2.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Navy900
                                            )
                                        }
                                        Text(
                                            text = "${pair.member1.industry} & ${pair.member2.industry}",
                                            fontSize = 11.sp,
                                            color = SlateTextSecondary
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            preselectedMemberIdForSchedule = pair.member2.id
                                            showScheduleDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("schedule_unmet_pair_${pair.member1.id}_${pair.member2.id}")
                                    ) {
                                        Text("Lên lịch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "CÁC CẶP ĐÃ GẶP 1-2-1 THÀNH CÔNG (${metPairs.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                letterSpacing = 0.5.sp
                            )
                        }

                        items(metPairs) { pair ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(14.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "${pair.member1.name} & ${pair.member2.name}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Navy900
                                            )
                                        }
                                        Text(
                                            text = "Đã 1-2-1 & ghi nhận biên bản tháng này",
                                            fontSize = 11.sp,
                                            color = SlateTextSecondary
                                        )
                                    }
                                    Surface(
                                        color = SuccessGreenContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Đã gặp",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Bảng xếp hạng vinh danh
                    val sortedByContributed = members.sortedByDescending { it.tyfcbContributed }
                    val sortedByReceived = members.sortedByDescending { it.tyfcbReceived }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Bảng Top Giver (Doanh số trao đi)
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Orange500)
                                            Text(
                                                text = "TOP GIVER (DOANH SỐ TRAO ĐI)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Navy900
                                            )
                                        }
                                        Text("Toàn thời gian", fontSize = 11.sp, color = SlateTextSecondary)
                                    }

                                    sortedByContributed.take(4).forEachIndexed { index, member ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Surface(
                                                    color = when (index) {
                                                        0 -> Orange500
                                                        1 -> Navy600
                                                        2 -> PurpleStatus
                                                        else -> SlateBg
                                                    },
                                                    shape = CircleShape,
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = "#${index + 1}",
                                                            color = if (index < 3) Color.White else SlateTextPrimary,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                Column {
                                                    Text(member.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text(member.company, fontSize = 10.sp, color = SlateTextSecondary)
                                                }
                                            }
                                            Text(
                                                text = "${String.format("%,.0f", member.tyfcbContributed)} đ",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp,
                                                color = SuccessGreen
                                            )
                                        }
                                        if (index < 3) Divider(color = SlateBorder.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }

                        // Bảng Top Doanh số nhận được
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Navy600)
                                        Text(
                                            text = "TOP DOANH SỐ NHẬN ĐƯỢC (TYFCB)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Navy900
                                        )
                                    }

                                    sortedByReceived.take(4).forEachIndexed { index, member ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Text("#${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateTextMuted)
                                                Column {
                                                    Text(member.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text(member.industry, fontSize = 10.sp, color = SlateTextSecondary)
                                                }
                                            }
                                            Text(
                                                text = "${String.format("%,.0f", member.tyfcbReceived)} đ",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Navy700
                                            )
                                        }
                                        if (index < 3) Divider(color = SlateBorder.copy(alpha = 0.5f))
                                    }
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
fun MeetingCard(
    meeting: Meeting121,
    partner: Member?,
    onLogMinutesClick: () -> Unit
) {
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(partner?.avatarColorHex ?: 0xFF1E3A8A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = partner?.avatarInitials ?: "TV",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Column {
                        Text(
                            text = "Hẹn 1-2-1 với ${partner?.name ?: "Đồng đội"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Navy900
                        )
                        Text(
                            text = "${partner?.industry} • ${partner?.company}",
                            fontSize = 11.sp,
                            color = SlateTextSecondary
                        )
                    }
                }

                Surface(
                    color = if (meeting.isCompleted) SuccessGreenContainer else Orange100,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (meeting.isCompleted) "Đã hoàn thành" else "Đã lên lịch",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (meeting.isCompleted) SuccessGreen else Orange600,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Địa điểm
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Place, contentDescription = null, tint = SlateTextSecondary, modifier = Modifier.size(16.dp))
                Text(meeting.location, fontSize = 12.sp, color = SlateTextPrimary)
            }

            // Tóm tắt biên bản
            if (meeting.isCompleted) {
                Surface(
                    color = SlateBg,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("TÓM TẮT BIÊN BẢN HỌP 1-2-1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Navy700)
                        if (meeting.coreProducts.isNotBlank()) {
                            Text("• Thế mạnh: ${meeting.coreProducts}", fontSize = 11.sp, color = SlateTextSecondary)
                        }
                        if (meeting.idealClientProfile.isNotBlank()) {
                            Text("• Khách hàng mục tiêu: ${meeting.idealClientProfile}", fontSize = 11.sp, color = SlateTextSecondary)
                        }
                        if (meeting.mutualCommitments.isNotBlank()) {
                            Text("• Cam kết hỗ trợ: ${meeting.mutualCommitments}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Orange600)
                        }
                    }
                }
            }

            // Nút biên bản
            Button(
                onClick = onLogMinutesClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (meeting.isCompleted) Navy100 else Orange500,
                    contentColor = if (meeting.isCompleted) Navy700 else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("log_minutes_button_${meeting.id}")
            ) {
                Icon(
                    imageVector = if (meeting.isCompleted) Icons.Default.EditNote else Icons.Default.PostAdd,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (meeting.isCompleted) "Xem & Cập Nhật Biên Bản" else "Ghi Biên Bản Họp 1-2-1",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
