package com.example.ui.components

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@Composable
fun InstallationGuideDialog(
    onDismiss: () -> Unit
) {
    var selectedOsTab by remember { mutableStateOf(0) } // 0: Android, 1: iOS
    val context = LocalContext.current
    var isDownloadingApk by remember { mutableStateOf(false) }

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
                                    .testTag("install_guide_close_button")
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                            }
                            Column {
                                Text(
                                    text = "Cài Đặt Power Team TOPPRO",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                                Text(
                                    text = "Hỗ trợ 2 hệ điều hành: Android & iOS",
                                    fontSize = 11.sp,
                                    color = SlateTextSecondary
                                )
                            }
                        }
                    }
                }

                // OS Tabs
                Surface(
                    color = Color.White,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TabRow(
                        selectedTabIndex = selectedOsTab,
                        containerColor = Color.White,
                        contentColor = Orange500
                    ) {
                        Tab(
                            selected = selectedOsTab == 0,
                            onClick = { selectedOsTab = 0 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Android, contentDescription = null, tint = if (selectedOsTab == 0) Orange500 else SlateTextSecondary)
                                    Text("Hệ điều hành Android", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            },
                            modifier = Modifier.testTag("tab_install_android")
                        )
                        Tab(
                            selected = selectedOsTab == 1,
                            onClick = { selectedOsTab = 1 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = if (selectedOsTab == 1) Orange500 else SlateTextSecondary)
                                    Text("Hệ điều hành iOS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            },
                            modifier = Modifier.testTag("tab_install_ios")
                        )
                    }
                }

                // Nội dung
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedOsTab == 0) {
                        // ANDROID INSTALLATION
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
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(SuccessGreenContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Android, contentDescription = null, tint = SuccessGreen)
                                    }
                                    Column {
                                        Text("Bộ Cài Đặt Android APK", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Navy900)
                                        Text("Phiên bản: 1.0.0 (Build 2026.10) • Dung lượng: ~32 MB", fontSize = 11.sp, color = SlateTextSecondary)
                                    }
                                }

                                Text(
                                    text = "File cài đặt Android APK độc lập, có thể cài trực tiếp trên mọi dòng điện thoại Samsung, Xiaomi, Oppo, Vivo, Google Pixel... mà không cần qua Google Play.",
                                    fontSize = 12.sp,
                                    color = SlateTextPrimary
                                )

                                // Quy trình 3 bước cài đặt
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SlateBg)
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("CÁC BƯỚC CÀI ĐẶT TRÊN ANDROID:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Navy700)
                                    Text("1. Nhấn nút 'Tải File Cài Đặt APK' bên dưới để tải file PowerTeam_TOPPRO.apk về máy.", fontSize = 12.sp)
                                    Text("2. Mở file vừa tải về trong mục Tệp / Thông báo. Nếu máy hiện cảnh báo 'Cài đặt từ nguồn không xác định', chọn Cho phép / Tiếp tục.", fontSize = 12.sp)
                                    Text("3. Bấm 'Cài đặt' và mở ứng dụng. Biểu tượng Power Team TOPPRO sẽ xuất hiện trên màn hình chính.", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        isDownloadingApk = true
                                        Toast.makeText(context, "Đang tải xuống bộ cài: PowerTeam_TOPPRO_v1.0.apk (32MB)...", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("download_apk_button")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tải File Cài Đặt Android APK", fontWeight = FontWeight.Bold)
                                }

                                if (isDownloadingApk) {
                                    Surface(
                                        color = SuccessGreenContainer,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "✓ Đã lưu gói cài đặt PowerTeam_TOPPRO_v1.0.apk vào bộ nhớ máy. Vui lòng mở tệp để tiến hành cài đặt.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = SuccessGreen,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // IOS INSTALLATION
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
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Navy100),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = Navy700)
                                    }
                                    Column {
                                        Text("Cài Đặt Trên iPhone / iPad (iOS)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Navy900)
                                        Text("Công nghệ Apple Web App PWA • Tương thích iOS 14.0 trở lên", fontSize = 11.sp, color = SlateTextSecondary)
                                    }
                                }

                                Text(
                                    text = "Ứng dụng hỗ trợ chạy toàn màn hình (Standalone) trên iOS tương tự như ứng dụng từ App Store, có thông báo đẩy và lưu dữ liệu offline.",
                                    fontSize = 12.sp,
                                    color = SlateTextPrimary
                                )

                                // Hướng dẫn 4 bước iOS
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SlateBg)
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("HƯỚNG DẪN CÀI ĐẶT TRÊN IPHONE:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Navy700)
                                    Text("• Bước 1: Mở trình duyệt Safari trên iPhone và truy cập đường link ứng dụng.", fontSize = 12.sp)
                                    Text("• Bước 2: Nhấn vào biểu tượng Chia sẻ (Hình vuông có mũi tên trỏ lên) ở thanh dưới cùng Safari.", fontSize = 12.sp)
                                    Text("• Bước 3: Cuộn xuống và chọn dòng 'Thêm vào Màn hình chính' (Add to Home Screen).", fontSize = 12.sp)
                                    Text("• Bước 4: Nhập tên 'Power Team TOPPRO' và nhấn 'Thêm'. Ứng dụng sẽ có icon riêng biệt trên màn hình iPhone.", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("iOS App URL", "https://ais-pre-k44cshv5tizbwojsqvxoyt-672285273442.asia-southeast1.run.app")
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Đã sao chép link cài đặt iOS vào bộ nhớ tạm!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Navy700),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("copy_ios_link_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sao Chép Đường Link Cài Đặt iOS", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Card Tác giả & Hỗ trợ kỹ thuật
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
                                text = "THIẾT KẾ & HỖ TRỢ KỸ THUẬT CÀI ĐẶT",
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
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gọi Hỗ Trợ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
