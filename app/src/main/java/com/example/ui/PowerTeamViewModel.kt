package com.example.ui

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.PowerTeamRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.*

data class MemberPairStatus(
    val member1: Member,
    val member2: Member,
    val hasMetThisMonth: Boolean,
    val meetingId: String? = null,
    val lastMeetingDate: Long? = null
)

class PowerTeamViewModel(
    application: Application,
    private val repository: PowerTeamRepository = PowerTeamRepository(application)
) : AndroidViewModel(application) {

    // Trạng thái xác thực Google
    private val _currentUser = MutableStateFlow<FirebaseUser?>(Firebase.auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        Firebase.auth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
        }
    }

    val session: StateFlow<UserSession> = repository.session
    val members: StateFlow<List<Member>> = repository.members
    val referrals: StateFlow<List<Referral>> = repository.referrals
    val commissions: StateFlow<List<CommissionEntry>> = repository.commissions
    val meetings121: StateFlow<List<Meeting121>> = repository.meetings121
    val groupFundTransactions: StateFlow<List<GroupFundTransaction>> = repository.groupFundTransactions
    val penalties: StateFlow<List<PenaltyEntry>> = repository.penalties
    val licenseKeys: StateFlow<List<LicenseKey>> = repository.licenseKeys
    val kpiConfig: StateFlow<KpiConfig> = repository.kpiConfig
    val chapterGoal: StateFlow<ChapterGoalConfig> = repository.chapterGoal
    val selectedReportPeriod: StateFlow<ReportPeriod> = repository.selectedReportPeriod

    // Thông tin thành viên đang đăng nhập
    val currentMember: StateFlow<Member?> = combine(session, members) { sess, mems ->
        mems.find { it.id == sess.currentUserId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Báo cáo thành viên theo kỳ đang chọn (Tuần / Tháng / Năm)
    val memberReports: StateFlow<List<MemberPeriodReport>> = combine(
        selectedReportPeriod,
        members,
        referrals,
        meetings121,
        kpiConfig
    ) { period, _, _, _, _ ->
        repository.getMemberReports(period)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getMemberReports(ReportPeriod.MONTH))

    // Cảnh báo thông minh toàn hệ thống
    val systemAlerts: StateFlow<List<SystemAlert>> = combine(
        referrals,
        penalties,
        memberReports
    ) { _, _, _ ->
        repository.getSystemAlerts()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getSystemAlerts())

    // Cơ hội do tôi trao đi
    val givenReferrals: StateFlow<List<Referral>> = combine(session, referrals) { sess, refs ->
        refs.filter { it.giverMemberId == sess.currentUserId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cơ hội tôi nhận về
    val receivedReferrals: StateFlow<List<Referral>> = combine(session, referrals) { sess, refs ->
        refs.filter { it.takerMemberId == sess.currentUserId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cơ hội quá hạn cần đôn đốc
    val overdueReferrals: StateFlow<List<Referral>> = combine(session, referrals) { sess, refs ->
        refs.filter {
            (it.takerMemberId == sess.currentUserId || sess.activeRole == UserRole.ADMIN) &&
            (it.isOverdueReceived() || it.isOverdueProcessing()) &&
            it.status != DealStatus.CLOSED_SUCCESS &&
            it.status != DealStatus.CLOSED_FAILED
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Số dư Quỹ chung Chapter
    val groupFundBalance: StateFlow<Double> = groupFundTransactions.map { txs ->
        txs.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Phiếu phạt chưa nộp của tôi
    val myUnpaidPenalties: StateFlow<List<PenaltyEntry>> = combine(session, penalties) { sess, pens ->
        pens.filter { it.memberId == sess.currentUserId && it.status == PaymentStatus.UNPAID }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Hoa hồng cần thu
    val myCommissionsReceivable: StateFlow<Double> = combine(session, commissions) { sess, comms ->
        comms.filter { it.giverMemberId == sess.currentUserId && it.status == PaymentStatus.UNPAID }
            .sumOf { it.commissionAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Hoa hồng cần trả
    val myCommissionsPayable: StateFlow<Double> = combine(session, commissions) { sess, comms ->
        comms.filter { it.takerMemberId == sess.currentUserId && it.status == PaymentStatus.UNPAID }
            .sumOf { it.commissionAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Ma trận tương tác 1-2-1
    val interactionMatrix: StateFlow<List<MemberPairStatus>> = combine(members, meetings121) { mems, meets ->
        val pairs = mutableListOf<MemberPairStatus>()
        val oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)

        for (i in mems.indices) {
            for (j in (i + 1) until mems.size) {
                val m1 = mems[i]
                val m2 = mems[j]
                val meeting = meets.find {
                    ((it.hostMemberId == m1.id && it.guestMemberId == m2.id) ||
                     (it.hostMemberId == m2.id && it.guestMemberId == m1.id)) &&
                     it.isCompleted &&
                     (it.completionDate ?: 0) >= oneMonthAgo
                }
                pairs.add(
                    MemberPairStatus(
                        member1 = m1,
                        member2 = m2,
                        hasMetThisMonth = meeting != null,
                        meetingId = meeting?.id,
                        lastMeetingDate = meeting?.completionDate
                    )
                )
            }
        }
        pairs
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Thao tác đổi kỳ báo cáo
    fun setReportPeriod(period: ReportPeriod) {
        repository.setReportPeriod(period)
    }

    // Đăng ký thành viên mới
    fun registerNewMember(form: NewMemberForm): Member {
        return repository.registerNewMember(form)
    }

    // Cập nhật mục tiêu Chapter
    fun updateChapterGoal(newGoal: ChapterGoalConfig) {
        repository.updateChapterGoal(newGoal)
    }

    fun switchRole(role: UserRole) {
        repository.switchRole(role)
    }

    fun toggleTrialExpiredSimulated() {
        repository.toggleTrialExpiredSimulated()
    }

    fun activateLicenseKey(key: String): Boolean {
        return repository.activateLicenseKey(key)
    }

    fun generateLicenseKey(note: String): String {
        return repository.generateLicenseKey(note)
    }

    fun createReferral(
        takerMemberId: String,
        clientName: String,
        clientPhone: String,
        clientEmail: String,
        urgency: UrgencyLevel,
        contractType: String,
        estimatedBudget: Double,
        scaleAreaM2: String,
        projectLocation: String,
        notes: String
    ): Referral {
        return repository.createReferral(
            takerMemberId,
            clientName,
            clientPhone,
            clientEmail,
            urgency,
            contractType,
            estimatedBudget,
            scaleAreaM2,
            projectLocation,
            notes
        )
    }

    fun updateDealStatus(
        referralId: String,
        newStatus: DealStatus,
        tyfcbAmount: Double? = null,
        commissionPercent: Double? = null,
        commonFundPercent: Double? = null
    ) {
        repository.updateDealStatus(referralId, newStatus, tyfcbAmount, commissionPercent, commonFundPercent)
    }

    fun markCommissionPaid(commissionId: String, note: String) {
        repository.markCommissionPaid(commissionId, note)
    }

    fun schedule121(guestMemberId: String, scheduledTime: Long, location: String) {
        repository.schedule121(guestMemberId, scheduledTime, location)
    }

    fun complete121Minutes(
        meetingId: String,
        coreProducts: String,
        idealClientProfile: String,
        mutualCommitments: String
    ) {
        repository.complete121Minutes(meetingId, coreProducts, idealClientProfile, mutualCommitments)
    }

    fun runDisciplineScan(): Int {
        return repository.runAutomatedDisciplineScan()
    }

    fun payPenalty(penaltyId: String) {
        repository.payPenalty(penaltyId)
    }

    fun updateKpiConfig(config: KpiConfig) {
        repository.updateKpiConfig(config)
    }
}
