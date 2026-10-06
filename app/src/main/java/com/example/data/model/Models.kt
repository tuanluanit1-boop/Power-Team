package com.example.data.model

enum class UserRole(val labelVi: String) {
    MEMBER("Hội viên"),
    ADMIN("Ban điều hành / Trưởng ban")
}

enum class LicenseType(val labelVi: String) {
    TRIAL("Dùng thử"),
    LIFETIME("Vĩnh viễn")
}

enum class UrgencyLevel(val label: String, val shortLabel: String) {
    HOT("Nóng - Rất khẩn cấp", "Nóng"),
    WARM("Ấm - Trong 1-2 tuần", "Ấm"),
    COLD("Nguội - Tiềm năng", "Nguội")
}

enum class DealStatus(val label: String, val stepIndex: Int) {
    RECEIVED("Mới nhận", 0),
    CONTACTED("Đã liên hệ", 1),
    QUOTED("Đã báo giá", 2),
    CLOSED_SUCCESS("Thành công", 3),
    CLOSED_FAILED("Thất bại", 3)
}

enum class PaymentStatus(val labelVi: String) {
    UNPAID("Chưa thanh toán"),
    PAID("Đã thanh toán")
}

enum class FundTxType(val labelVi: String) {
    PENALTY_INFLOW("Thu nộp phạt"),
    MEMBER_FEE("Đóng quỹ thành viên"),
    EXPENSE_OUTFLOW("Chi tiêu chapter")
}

enum class LicenseStatus(val labelVi: String) {
    UNUSED("Chưa dùng"),
    USED("Đã dùng")
}

enum class ReportPeriod(val labelVi: String) {
    WEEK("Hàng tuần (Tuần 40)"),
    MONTH("Hàng tháng (Tháng 10/2026)"),
    YEAR("Hàng năm (Năm 2026)")
}

enum class AlertSeverity {
    WARNING,
    CRITICAL,
    INFO
}

data class SystemAlert(
    val id: String,
    val title: String,
    val message: String,
    val severity: AlertSeverity,
    val memberName: String? = null,
    val actionText: String? = null
)

data class MemberPeriodReport(
    val memberId: String,
    val memberName: String,
    val company: String,
    val industry: String,
    val referralsGiven: Int,
    val referralsGivenGoal: Int,
    val referralsReceived: Int,
    val meetings121Count: Int,
    val meetings121Goal: Int,
    val tyfcbRevenue: Double,
    val tyfcbGoal: Double,
    val isReferralsMet: Boolean,
    val isMeetingsMet: Boolean,
    val isTyfcbMet: Boolean,
    val warningMessage: String? = null
)

data class ChapterGoalConfig(
    val period: ReportPeriod = ReportPeriod.MONTH,
    val targetTyfcbRevenue: Double = 15000000000.0, // 15 Tỷ VNĐ
    val currentTyfcbRevenue: Double = 11500000000.0,
    val targetReferrals: Int = 30,
    val currentReferrals: Int = 24,
    val targetMeetings121: Int = 25,
    val currentMeetings121: Int = 18
)

data class Member(
    val id: String,
    val name: String,
    val company: String,
    val industry: String,
    val role: UserRole = UserRole.MEMBER,
    val email: String,
    val phone: String,
    val avatarInitials: String,
    val avatarColorHex: Long,
    val tyfcbContributed: Double = 0.0,
    val tyfcbReceived: Double = 0.0,
    val penaltiesUnpaid: Double = 0.0
)

data class Referral(
    val id: String,
    val title: String,
    val giverMemberId: String,
    val takerMemberId: String,
    val clientName: String,
    val clientPhone: String,
    val clientEmail: String,
    val urgency: UrgencyLevel,
    val status: DealStatus,
    val contractType: String,
    val estimatedBudget: Double, // VNĐ
    val scaleAreaM2: String,
    val projectLocation: String,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long,
    val tyfcbAmount: Double? = null,
    val commissionPercent: Double? = null,
    val commonFundPercent: Double? = null
) {
    fun isOverdueReceived(): Boolean {
        if (status == DealStatus.RECEIVED) {
            val threeDaysMs = 3L * 24 * 60 * 60 * 1000
            val age = System.currentTimeMillis() - updatedAt
            return age >= threeDaysMs
        }
        return false
    }

    fun isOverdueProcessing(): Boolean {
        if (status == DealStatus.CONTACTED || status == DealStatus.QUOTED) {
            val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
            val age = System.currentTimeMillis() - updatedAt
            return age >= sevenDaysMs
        }
        return false
    }
}

data class CommissionEntry(
    val id: String,
    val referralId: String,
    val referralTitle: String,
    val giverMemberId: String,
    val takerMemberId: String,
    val giverName: String,
    val takerName: String,
    val tyfcbRevenue: Double,
    val commissionPercent: Double,
    val commissionAmount: Double,
    val status: PaymentStatus,
    val createdAt: Long,
    val paidAt: Long? = null,
    val paymentNote: String = ""
)

data class Meeting121(
    val id: String,
    val hostMemberId: String,
    val guestMemberId: String,
    val scheduledTime: Long,
    val location: String,
    val isCompleted: Boolean = false,
    val coreProducts: String = "",
    val idealClientProfile: String = "",
    val mutualCommitments: String = "",
    val completionDate: Long? = null
)

data class GroupFundTransaction(
    val id: String,
    val type: FundTxType,
    val title: String,
    val description: String,
    val amount: Double,
    val memberId: String? = null,
    val memberName: String? = null,
    val timestamp: Long,
    val isVerified: Boolean = true
)

data class PenaltyEntry(
    val id: String,
    val memberId: String,
    val memberName: String,
    val reason: String,
    val amount: Double,
    val ruleCode: String,
    val createdAt: Long,
    val status: PaymentStatus,
    val paidAt: Long? = null
)

data class LicenseKey(
    val key: String,
    val note: String,
    val createdAt: Long,
    val status: LicenseStatus,
    val activatedByMemberId: String? = null,
    val activatedAt: Long? = null
)

data class KpiConfig(
    val weeklyReferralsTarget: Int = 1,
    val monthly121Target: Int = 2,
    val overdueResponseDaysLimit: Int = 3,
    val overduePenaltyAmount: Double = 500000.0,
    val absencePenaltyAmount: Double = 1000000.0,
    val late121PenaltyAmount: Double = 300000.0
)

data class UserSession(
    val currentUserId: String,
    val activeRole: UserRole,
    val licenseType: LicenseType,
    val trialDaysRemaining: Int = 5,
    val isTrialExpiredSimulated: Boolean = false
) {
    val canCreateReferral: Boolean
        get() = licenseType == LicenseType.LIFETIME || (!isTrialExpiredSimulated && trialDaysRemaining > 0)
}

data class BusinessCardSample(
    val title: String,
    val company: String,
    val clientName: String,
    val phone: String,
    val email: String,
    val industryType: String,
    val estimatedBudget: Double,
    val scaleArea: String,
    val location: String,
    val notes: String
)

data class NewMemberForm(
    val fullName: String,
    val companyName: String,
    val industry: String,
    val phone: String,
    val email: String,
    val coreProducts: String,
    val sponsorName: String
)
