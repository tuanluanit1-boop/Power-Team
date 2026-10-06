package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Meeting121
import com.example.data.model.Member
import com.example.ui.theme.*

@Composable
fun Schedule121Dialog(
    members: List<Member>,
    currentUserId: String,
    preselectedMemberId: String? = null,
    existingMeetingToComplete: Meeting121? = null,
    onDismiss: () -> Unit,
    onSchedule: (guestMemberId: String, scheduledTime: Long, location: String) -> Unit,
    onSaveMinutes: (meetingId: String, coreProducts: String, idealClient: String, commitments: String) -> Unit
) {
    val otherMembers = members.filter { it.id != currentUserId }
    var selectedMemberId by remember {
        mutableStateOf(
            preselectedMemberId ?: existingMeetingToComplete?.guestMemberId ?: otherMembers.firstOrNull()?.id ?: ""
        )
    }
    var location by remember { mutableStateOf(existingMeetingToComplete?.location ?: "Highlands Coffee - Dinh Độc Lập, Q.1") }
    var coreProducts by remember { mutableStateOf(existingMeetingToComplete?.coreProducts ?: "") }
    var idealClient by remember { mutableStateOf(existingMeetingToComplete?.idealClientProfile ?: "") }
    var mutualCommitments by remember { mutableStateOf(existingMeetingToComplete?.mutualCommitments ?: "") }
    var showMemberDropdown by remember { mutableStateOf(false) }

    val isMinutesMode = existingMeetingToComplete != null

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
                                    .testTag("schedule_121_dismiss")
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                            }
                            Text(
                                text = if (isMinutesMode) "Ghi Biên Bản 1-2-1" else "Đặt Lịch Hẹn 1-2-1",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Chọn thành viên
                    val chosenMember = members.find { it.id == selectedMemberId }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ĐỒNG ĐỘI HẸN 1-2-1",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy700
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Box {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SlateBg)
                                        .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                                        .clickable(enabled = !isMinutesMode) { showMemberDropdown = true }
                                        .padding(12.dp)
                                        .testTag("meeting_partner_picker")
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
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(chosenMember?.avatarColorHex ?: 0xFF1E3A8A)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = chosenMember?.avatarInitials ?: "TV",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = chosenMember?.name ?: "Chọn thành viên",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = chosenMember?.industry ?: "",
                                                    fontSize = 11.sp,
                                                    color = SlateTextSecondary
                                                )
                                            }
                                        }
                                        if (!isMinutesMode) {
                                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                        }
                                    }
                                }

                                DropdownMenu(
                                    expanded = showMemberDropdown,
                                    onDismissRequest = { showMemberDropdown = false }
                                ) {
                                    otherMembers.forEach { mem ->
                                        DropdownMenuItem(
                                            text = { Text("${mem.name} (${mem.industry})") },
                                            onClick = {
                                                selectedMemberId = mem.id
                                                showMemberDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Địa điểm
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
                                text = "ĐỊA ĐIỂM / HÌNH THỨC GẶP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy700
                            )

                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = { Text("Địa điểm gặp hoặc Link Zoom/Google Meet") },
                                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("meeting_location_input")
                            )
                        }
                    }

                    // Biên bản họp 1-2-1
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
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = Orange500,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "BIÊN BẢN HỌP 1-2-1 & VĂN HÓA CHAPTER",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy700
                                )
                            }

                            OutlinedTextField(
                                value = coreProducts,
                                onValueChange = { coreProducts = it },
                                label = { Text("Sản phẩm / Dịch vụ thế mạnh cốt lõi") },
                                placeholder = { Text("Điểm mạnh, lợi thế cạnh tranh, sản phẩm chủ lực") },
                                minLines = 2,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("minutes_core_products")
                            )

                            OutlinedTextField(
                                value = idealClient,
                                onValueChange = { idealClient = it },
                                label = { Text("Chân dung khách hàng lý tưởng (Dấu hiệu Referral)") },
                                placeholder = { Text("Những ai cần dịch vụ của bạn? Dấu hiệu nhận diện cơ hội") },
                                minLines = 2,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("minutes_ideal_client")
                            )

                            OutlinedTextField(
                                value = mutualCommitments,
                                onValueChange = { mutualCommitments = it },
                                label = { Text("Cam kết hành động cụ thể trong tháng") },
                                placeholder = { Text("Lời hứa kết nối hoặc hành động hỗ trợ lẫn nhau") },
                                minLines = 2,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("minutes_commitments")
                            )
                        }
                    }
                }

                // Nút Lưu
                Surface(
                    color = Color.White,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = {
                                if (isMinutesMode) {
                                    onSaveMinutes(
                                        existingMeetingToComplete!!.id,
                                        coreProducts,
                                        idealClient,
                                        mutualCommitments
                                    )
                                } else {
                                    val now = System.currentTimeMillis()
                                    onSchedule(selectedMemberId, now + 86400000L, location)
                                }
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("save_meeting_button")
                        ) {
                            Icon(
                                imageVector = if (isMinutesMode) Icons.Default.Save else Icons.Default.CalendarMonth,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isMinutesMode) "Lưu Biên Bản Họp 1-2-1" else "Xác Nhận & Lên Lịch Hẹn",
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
