package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.*
import com.example.ui.components.PenaltyReconciliationDialog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FinanceScreen(
    currentUserId: String,
    commissions: List<CommissionEntry>,
    commissionsReceivable: Double,
    commissionsPayable: Double,
    groupFundBalance: Double,
    groupFundTransactions: List<GroupFundTransaction>,
    penalties: List<PenaltyEntry>,
    myUnpaidPenalties: List<PenaltyEntry>,
    onMarkCommissionPaid: (commissionId: String, note: String) -> Unit,
    onPayPenalty: (penaltyId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFinanceTab by remember { mutableStateOf(0) } // 0: Ví hoa hồng, 1: Quỹ nhóm, 2: Kỷ luật & Phạt
    var penaltyToReconcile by remember { mutableStateOf<PenaltyEntry?>(null) }

    if (penaltyToReconcile != null) {
        PenaltyReconciliationDialog(
            penalty = penaltyToReconcile!!,
            onDismiss = { penaltyToReconcile = null },
            onConfirmPaid = {
                onPayPenalty(penaltyToReconcile!!.id)
                penaltyToReconcile = null
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBg)
    ) {
        // Tab Selector
        Surface(
            color = Color.White,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            TabRow(
                selectedTabIndex = selectedFinanceTab,
                containerColor = Color.White,
                contentColor = Orange500
            ) {
                Tab(
                    selected = selectedFinanceTab == 0,
                    onClick = { selectedFinanceTab = 0 },
                    text = { Text("Ví hoa hồng", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_sub_commissions")
                )
                Tab(
                    selected = selectedFinanceTab == 1,
                    onClick = { selectedFinanceTab = 1 },
                    text = { Text("Quỹ nhóm", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_sub_treasury")
                )
                Tab(
                    selected = selectedFinanceTab == 2,
                    onClick = { selectedFinanceTab = 2 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Kỷ luật & Phạt", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (myUnpaidPenalties.isNotEmpty()) {
                                Badge(containerColor = HotRed) {
                                    Text(
                                        text = myUnpaidPenalties.size.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_sub_penalties")
                )
            }
        }

        when (selectedFinanceTab) {
            0 -> {
                // Ví hoa hồng
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Navy700),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "SỔ CÁI HOA HỒNG (VÍ HOA HỒNG)",
                                    color = Navy100,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Cần thu (Người khác nợ tôi)",
                                            color = Navy100,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "${String.format("%,.0f", commissionsReceivable)} đ",
                                            color = SuccessGreen,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Cần trả (Tôi nợ người trao)",
                                            color = Navy100,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "${String.format("%,.0f", commissionsPayable)} đ",
                                            color = Orange400,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "TOÀN BỘ GIAO DỊCH HOA HỒNG",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(commissions) { comm ->
                        val isPayableByMe = comm.takerMemberId == currentUserId

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = comm.referralTitle,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Navy900
                                        )
                                        Text(
                                            text = "Người trao: ${comm.giverName} • Người nhận: ${comm.takerName}",
                                            fontSize = 11.sp,
                                            color = SlateTextSecondary
                                        )
                                    }

                                    Surface(
                                        color = if (comm.status == PaymentStatus.PAID) SuccessGreenContainer else Orange100,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (comm.status == PaymentStatus.PAID) "Đã thanh toán" else "Chưa thanh toán",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (comm.status == PaymentStatus.PAID) SuccessGreen else Orange600,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Doanh số TYFCB: ${String.format("%,.0f", comm.tyfcbRevenue)} đ (${comm.commissionPercent}%)",
                                            fontSize = 11.sp,
                                            color = SlateTextSecondary
                                        )
                                        if (comm.paymentNote.isNotBlank()) {
                                            Text(
                                                text = comm.paymentNote,
                                                fontSize = 10.sp,
                                                color = SlateTextMuted
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${String.format("%,.0f", comm.commissionAmount)} đ",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = if (comm.status == PaymentStatus.PAID) SlateTextSecondary else Orange500
                                    )
                                }

                                if (comm.status == PaymentStatus.UNPAID && isPayableByMe) {
                                    Button(
                                        onClick = { onMarkCommissionPaid(comm.id, "Đã chuyển khoản hoa hồng cho đồng đội") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
                                            .testTag("mark_commission_paid_${comm.id}")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Xác Nhận Đã Thanh Toán Hoa Hồng", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Quỹ nhóm (Treasury Fund)
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Navy700),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "SỐ DƯ QUỸ CHUNG CHAPTER (QUỸ NHÓM)",
                                    color = Navy100,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${String.format("%,.0f", groupFundBalance)} đ",
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Được quản lý bởi Ban Điều Hành phục vụ phòng họp định kỳ, ăn sáng, sự kiện và khen thưởng thành viên.",
                                    color = Navy100,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "LỊCH SỬ THU CHI QUỸ NHÓM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(groupFundTransactions) { tx ->
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val isPositive = tx.amount >= 0

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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isPositive) SuccessGreenContainer else HotRedContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isPositive) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = if (isPositive) SuccessGreen else HotRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = tx.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Navy900
                                        )
                                        Text(
                                            text = "${tx.description} • ${dateFormat.format(Date(tx.timestamp))}",
                                            fontSize = 11.sp,
                                            color = SlateTextSecondary
                                        )
                                    }
                                }

                                Text(
                                    text = "${if (isPositive) "+" else ""}${String.format("%,.0f", tx.amount)} đ",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = if (isPositive) SuccessGreen else HotRed
                                )
                            }
                        }
                    }
                }
            }
            2 -> {
                // Kỷ luật & Phạt
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = HotRedContainer),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
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
                                        Icon(Icons.Default.Gavel, contentDescription = null, tint = HotRed)
                                        Text(
                                            text = "KỶ LUẬT CHAPTER TỰ ĐỘNG",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = HotRed
                                        )
                                    }
                                    Text(
                                        text = "${penalties.size} Lượt ghi nhận",
                                        fontSize = 11.sp,
                                        color = HotRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Văn hóa cam kết cao: Tiền phạt được hệ thống tự động ghi nhận khi phản hồi cơ hội trễ quá 3 ngày, vắng mặt họp không phép, hoặc nộp trễ biên bản 1-2-1.",
                                    fontSize = 11.sp,
                                    color = SlateTextPrimary
                                )
                            }
                        }
                    }

                    if (myUnpaidPenalties.isNotEmpty()) {
                        item {
                            Text(
                                text = "TIỀN PHẠT CỦA TÔI CẦN NỘP VÀO QUỸ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HotRed,
                                letterSpacing = 0.5.sp
                            )
                        }

                        items(myUnpaidPenalties) { pen ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(14.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = pen.reason,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Navy900
                                            )
                                            Text(
                                                text = "Mã quy chế: ${pen.ruleCode}",
                                                fontSize = 11.sp,
                                                color = HotRed
                                            )
                                        }
                                        Text(
                                            text = "${String.format("%,.0f", pen.amount)} đ",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp,
                                            color = HotRed
                                        )
                                    }

                                    Button(
                                        onClick = { penaltyToReconcile = pen },
                                        colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
                                            .testTag("reconcile_penalty_button_${pen.id}")
                                    ) {
                                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Chuyển Khoản Nộp Phạt Vào Quỹ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "NHẬT KÝ KỶ LUẬT TOÀN CHAPTER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(penalties) { pen ->
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
                                    Text(
                                        text = "${pen.memberName}: ${pen.reason}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = Navy900
                                    )
                                    Text(
                                        text = "Trạng thái: ${if (pen.status == PaymentStatus.PAID) "Đã nộp vào quỹ" else "Chưa nộp"}",
                                        fontSize = 11.sp,
                                        color = if (pen.status == PaymentStatus.PAID) SuccessGreen else HotRed
                                    )
                                }
                                Text(
                                    text = "${String.format("%,.0f", pen.amount)} đ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (pen.status == PaymentStatus.PAID) SlateTextSecondary else HotRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
