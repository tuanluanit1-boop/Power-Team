package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BusinessCardSample
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val SAMPLE_CARDS = listOf(
    BusinessCardSample(
        title = "Chủ Penthouse VIP",
        company = "Sterling Assets Holdings",
        clientName = "Nguyễn Trọng Tấn",
        phone = "0909 123 456",
        email = "tan.nguyen@sterlinggroup.vn",
        industryType = "Hoàn thiện Nội thất Cao cấp",
        estimatedBudget = 2100000000.0,
        scaleArea = "520 m²",
        location = "Sapphire Tower, Penthouse B, Q.1",
        notes = "Khách hàng VIP cần thi công trọn gói nội thất cao cấp phong cách Hiện đại."
    ),
    BusinessCardSample(
        title = "TGĐ Logistics Công nghiệp",
        company = "Pacific Rim Logistics Corp",
        clientName = "Đặng Quang Huy",
        phone = "0918 333 444",
        email = "huy.dang@pacificlogistics.vn",
        industryType = "Xây dựng Nhà xưởng & Kho lạnh",
        estimatedBudget = 3200000000.0,
        scaleArea = "2.500 m²",
        location = "KCN Hiệp Phước, Lô C4, Nhà Bè",
        notes = "Mở rộng cụm nhà xưởng kho vận tiêu chuẩn ISO."
    ),
    BusinessCardSample(
        title = "GĐ Vận hành FinTech",
        company = "Fintech Global Vietnam",
        clientName = "Bùi Thu Trang",
        phone = "0913 888 222",
        email = "trang.bui@fintechviet.vn",
        industryType = "Thiết kế & Thi công Văn phòng",
        estimatedBudget = 2800000000.0,
        scaleArea = "1.200 m²",
        location = "Bitexco Financial Tower, Tầng 18",
        notes = "Thuê mới sàn văn phòng, cần cách âm tiêu chuẩn và thiết kế mở sáng tạo."
    ),
    BusinessCardSample(
        title = "Chủ đầu tư Biệt thự Sala",
        company = "Greenfield Sanctuary Estates",
        clientName = "Vũ Minh Quân",
        phone = "0938 777 999",
        email = "quan.vu@techinvest.vn",
        industryType = "Smarthome & Điện Năng lượng",
        estimatedBudget = 850000000.0,
        scaleArea = "400 m²",
        location = "KĐT Sala, Villa số 12, TP. Thủ Đức",
        notes = "Cần tích hợp hệ thống điện thông minh KNX và năng lượng mặt trời áp mái."
    )
)

@Composable
fun OcrScannerDialog(
    onDismiss: () -> Unit,
    onCardScanned: (BusinessCardSample) -> Unit
) {
    var selectedCard by remember { mutableStateOf(SAMPLE_CARDS.first()) }
    var isScanning by remember { mutableStateOf(false) }
    var scanStatusText by remember { mutableStateOf("Sẵn sàng quét danh thiếp") }
    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLineOffset"
    )

    Dialog(
        onDismissRequest = { if (!isScanning) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isScanning,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("ocr_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng máy quét",
                            tint = Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Máy Quét Danh Thiếp AI",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Trích xuất thông minh OCR",
                            color = Orange400,
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isScanning) Orange500 else Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Khung mô phỏng Camera
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B))
                        .border(2.dp, if (isScanning) Orange500 else Navy500, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Danh thiếp mẫu trực quan
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9))
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = selectedCard.company.uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Navy900,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = selectedCard.industryType,
                                    color = Orange600,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = Navy700,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = selectedCard.clientName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Navy900
                            )
                            Text(
                                text = selectedCard.phone,
                                fontSize = 12.sp,
                                color = SlateTextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = selectedCard.email,
                                fontSize = 12.sp,
                                color = SlateTextSecondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dự toán: ${String.format("%,.0f", selectedCard.estimatedBudget)} đ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy700
                            )
                            Text(
                                text = selectedCard.scaleArea,
                                fontSize = 11.sp,
                                color = SlateTextSecondary
                            )
                        }
                    }

                    // Tia laser quét động
                    if (isScanning) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = (248 * scanLineOffset).dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color.Transparent, Orange500, Color.White, Orange500, Color.Transparent)
                                    )
                                )
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.CropFree,
                        contentDescription = null,
                        tint = if (isScanning) Orange400 else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                }

                // Chọn danh thiếp mẫu để test
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Chọn danh thiếp mẫu để quét thử:",
                        color = Navy100,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(SAMPLE_CARDS) { card ->
                            val isChosen = selectedCard == card
                            Surface(
                                color = if (isChosen) Orange500 else Color(0xFF1E293B),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .clickable(enabled = !isScanning) { selectedCard = card }
                                    .testTag("ocr_card_preset_${card.clientName.replace(" ", "_")}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = card.title,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Nút chụp & trích xuất
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = scanStatusText,
                        color = if (isScanning) Orange400 else Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Button(
                        onClick = {
                            if (!isScanning) {
                                isScanning = true
                                scope.launch {
                                    scanStatusText = "🔍 Đang phân tích ký tự quang học (OCR)..."
                                    delay(600)
                                    scanStatusText = "📐 Đang đọc thông số quy mô & dự toán B2B..."
                                    delay(600)
                                    scanStatusText = "✨ Trích xuất thông tin liên hệ thành công!"
                                    delay(500)
                                    isScanning = false
                                    onCardScanned(selectedCard)
                                }
                            }
                        },
                        enabled = !isScanning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Orange500,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("capture_ocr_button")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Đang xử lý OCR...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tự Động Trích Xuất & Điền Form", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
