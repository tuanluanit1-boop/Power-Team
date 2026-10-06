package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LicenseType
import com.example.data.model.UserRole
import com.example.data.model.UserSession
import com.example.ui.theme.*

@Composable
fun TopHeaderBar(
    session: UserSession,
    onRoleToggle: (UserRole) -> Unit,
    onActivateLicenseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Navy700,
        tonalElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo & Tên ứng dụng Power Team TOPPRO
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Orange500),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = "Logo Power Team TOPPRO",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "POWER TEAM ",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "TOPPRO",
                                color = Orange400,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                        Text(
                            text = "Chapter Xây Dựng & B2B Apex",
                            color = Navy100,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Trạng thái Bản quyền & Nút chuyển đổi vai trò
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Huy hiệu bản quyền
                    if (session.licenseType == LicenseType.LIFETIME) {
                        Surface(
                            color = Orange500,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "VĨNH VIỄN",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = if (session.isTrialExpiredSimulated) HotRed else Orange100,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clickable { onActivateLicenseClick() }
                                .testTag("license_banner_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (session.isTrialExpiredSimulated) Icons.Default.Lock else Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (session.isTrialExpiredSimulated) Color.White else Orange600,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (session.isTrialExpiredSimulated) "HẾT HẠN" else "${session.trialDaysRemaining}N DÙNG THỬ",
                                    color = if (session.isTrialExpiredSimulated) Color.White else Orange600,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Chuyển đổi vai trò (Hội viên / Admin)
                    Surface(
                        color = Navy800,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clickable {
                                onRoleToggle(
                                    if (session.activeRole == UserRole.MEMBER) UserRole.ADMIN else UserRole.MEMBER
                                )
                            }
                            .testTag("role_switcher_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (session.activeRole == UserRole.ADMIN) Color(0xFF10B981) else Orange400)
                            )
                            Text(
                                text = if (session.activeRole == UserRole.ADMIN) "ADMIN" else "HỘI VIÊN",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Chuyển vai trò",
                                tint = Navy100,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
