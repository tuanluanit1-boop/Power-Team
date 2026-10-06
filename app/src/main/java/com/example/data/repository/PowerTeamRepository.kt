package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class PowerTeamRepository(
    private val firestore: FirebaseFirestore? = null
) {
    constructor(context: Context) : this(
        try {
            FirebaseFirestore.getInstance(
                context.applicationContext.getString(R.string.firestore_database_id)
            )
        } catch (e: Exception) {
            null
        }
    )

    private var membersListener: ListenerRegistration? = null
    private var referralsListener: ListenerRegistration? = null
    private var goalsListener: ListenerRegistration? = null
    private var meetingsListener: ListenerRegistration? = null

    // Phiên người dùng hiện tại
    private val _session = MutableStateFlow(
        UserSession(
            currentUserId = "m_luan",
            activeRole = UserRole.ADMIN,
            licenseType = LicenseType.LIFETIME,
            trialDaysRemaining = 7,
            isTrialExpiredSimulated = false
        )
    )
    val session: StateFlow<UserSession> = _session.asStateFlow()

    // Danh sách thành viên
    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members.asStateFlow()

    // Danh sách cơ hội kinh doanh (Referrals)
    private val _referrals = MutableStateFlow<List<Referral>>(emptyList())
    val referrals: StateFlow<List<Referral>> = _referrals.asStateFlow()

    // Sổ cái hoa hồng ("Ví hoa hồng")
    private val _commissions = MutableStateFlow<List<CommissionEntry>>(emptyList())
    val commissions: StateFlow<List<CommissionEntry>> = _commissions.asStateFlow()

    // Lịch hẹn & Biên bản 1-2-1
    private val _meetings121 = MutableStateFlow<List<Meeting121>>(emptyList())
    val meetings121: StateFlow<List<Meeting121>> = _meetings121.asStateFlow()

    // Quỹ nhóm & Thu phạt kỷ luật
    private val _groupFundTransactions = MutableStateFlow<List<GroupFundTransaction>>(emptyList())
    val groupFundTransactions: StateFlow<List<GroupFundTransaction>> = _groupFundTransactions.asStateFlow()

    private val _penalties = MutableStateFlow<List<PenaltyEntry>>(emptyList())
    val penalties: StateFlow<List<PenaltyEntry>> = _penalties.asStateFlow()

    // Mã kích hoạt bản quyền do Admin cấp
    private val _licenseKeys = MutableStateFlow<List<LicenseKey>>(emptyList())
    val licenseKeys: StateFlow<List<LicenseKey>> = _licenseKeys.asStateFlow()

    // Cấu hình chỉ tiêu KPI & Kỷ luật
    private val _kpiConfig = MutableStateFlow(KpiConfig())
    val kpiConfig: StateFlow<KpiConfig> = _kpiConfig.asStateFlow()

    // Mục tiêu toàn Chapter & Đo lường
    private val _chapterGoal = MutableStateFlow(ChapterGoalConfig())
    val chapterGoal: StateFlow<ChapterGoalConfig> = _chapterGoal.asStateFlow()

    // Kỳ báo cáo đang chọn
    private val _selectedReportPeriod = MutableStateFlow(ReportPeriod.MONTH)
    val selectedReportPeriod: StateFlow<ReportPeriod> = _selectedReportPeriod.asStateFlow()

    init {
        seedInitialData()
        setupFirestoreListeners()
    }

    private fun setupFirestoreListeners() {
        val db = firestore ?: return

        // 1. Members
        try {
            membersListener = db.collection("members")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, "members")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                val d = doc.data ?: return@mapNotNull null
                                Member(
                                    id = doc.id,
                                    name = d["name"] as? String ?: "",
                                    company = d["company"] as? String ?: "",
                                    industry = d["industry"] as? String ?: "",
                                    role = try { UserRole.valueOf(d["role"] as? String ?: "MEMBER") } catch (e: Exception) { UserRole.MEMBER },
                                    email = d["email"] as? String ?: "",
                                    phone = d["phone"] as? String ?: "",
                                    avatarInitials = d["avatarInitials"] as? String ?: "TV",
                                    avatarColorHex = (d["avatarColorHex"] as? Number)?.toLong() ?: 0xFF1E3A8AL,
                                    tyfcbContributed = (d["tyfcbContributed"] as? Number)?.toDouble() ?: 0.0,
                                    tyfcbReceived = (d["tyfcbReceived"] as? Number)?.toDouble() ?: 0.0,
                                    penaltiesUnpaid = (d["penaltiesUnpaid"] as? Number)?.toDouble() ?: 0.0
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (list.isNotEmpty()) {
                            _members.value = list
                        }
                    }
                }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "members")
        }

        // 2. Referrals
        try {
            referralsListener = db.collection("referrals")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, "referrals")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                val d = doc.data ?: return@mapNotNull null
                                Referral(
                                    id = doc.id,
                                    title = d["title"] as? String ?: "",
                                    giverMemberId = d["giverMemberId"] as? String ?: "",
                                    takerMemberId = d["takerMemberId"] as? String ?: "",
                                    clientName = d["clientName"] as? String ?: "",
                                    clientPhone = d["clientPhone"] as? String ?: "",
                                    clientEmail = d["clientEmail"] as? String ?: "",
                                    urgency = try { UrgencyLevel.valueOf(d["urgency"] as? String ?: "HOT") } catch (e: Exception) { UrgencyLevel.HOT },
                                    status = try { DealStatus.valueOf(d["status"] as? String ?: "RECEIVED") } catch (e: Exception) { DealStatus.RECEIVED },
                                    contractType = d["contractType"] as? String ?: "",
                                    estimatedBudget = (d["estimatedBudget"] as? Number)?.toDouble() ?: 0.0,
                                    scaleAreaM2 = d["scaleAreaM2"] as? String ?: "",
                                    projectLocation = d["projectLocation"] as? String ?: "",
                                    notes = d["notes"] as? String ?: "",
                                    createdAt = (d["createdAtMs"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                                    updatedAt = (d["updatedAtMs"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                                    tyfcbAmount = (d["tyfcbAmount"] as? Number)?.toDouble(),
                                    commissionPercent = (d["commissionPercent"] as? Number)?.toDouble(),
                                    commonFundPercent = (d["commonFundPercent"] as? Number)?.toDouble()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (list.isNotEmpty()) {
                            _referrals.value = list
                        }
                    }
                }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "referrals")
        }

        // 3. Chapter Goals
        try {
            goalsListener = db.collection("chapter_goals").document("current_goal")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.GET, "chapter_goals/current_goal")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val d = snapshot.data
                        if (d != null) {
                            _chapterGoal.value = ChapterGoalConfig(
                                period = try { ReportPeriod.valueOf(d["period"] as? String ?: "MONTH") } catch (e: Exception) { ReportPeriod.MONTH },
                                targetTyfcbRevenue = (d["targetTyfcbRevenue"] as? Number)?.toDouble() ?: 15000000000.0,
                                currentTyfcbRevenue = (d["currentTyfcbRevenue"] as? Number)?.toDouble() ?: 11500000000.0,
                                targetReferrals = (d["targetReferrals"] as? Number)?.toInt() ?: 30,
                                currentReferrals = (d["currentReferrals"] as? Number)?.toInt() ?: 24,
                                targetMeetings121 = (d["targetMeetings121"] as? Number)?.toInt() ?: 25,
                                currentMeetings121 = (d["currentMeetings121"] as? Number)?.toInt() ?: 18
                            )
                        }
                    }
                }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "chapter_goals/current_goal")
        }

        // 4. Meetings 1-2-1
        try {
            meetingsListener = db.collection("meetings121")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, "meetings121")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                val d = doc.data ?: return@mapNotNull null
                                Meeting121(
                                    id = doc.id,
                                    hostMemberId = d["hostMemberId"] as? String ?: "",
                                    guestMemberId = d["guestMemberId"] as? String ?: "",
                                    scheduledTime = (d["scheduledTimeMs"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                                    location = d["location"] as? String ?: "",
                                    isCompleted = d["isCompleted"] as? Boolean ?: false,
                                    coreProducts = d["coreProducts"] as? String ?: "",
                                    idealClientProfile = d["idealClientProfile"] as? String ?: "",
                                    mutualCommitments = d["mutualCommitments"] as? String ?: "",
                                    completionDate = (d["completionDateMs"] as? Number)?.toLong()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (list.isNotEmpty()) {
                            _meetings121.value = list
                        }
                    }
                }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "meetings121")
        }
    }

    private fun seedInitialData() {
        val now = System.currentTimeMillis()
        val oneDay = 24L * 60 * 60 * 1000

        val initialMembers = listOf(
            Member(
                id = "m_buidinhdinh",
                name = "Bùi Đình Đình",
                company = "Công ty CP thiết bị điện đèn led DD lighting",
                industry = "Thiết bị điện",
                role = UserRole.MEMBER,
                email = "dinh.bui@ddlighting.vn",
                phone = "0984140569",
                avatarInitials = "BĐ",
                avatarColorHex = 0xFF1E3A8AL,
                tyfcbContributed = 850000000.0,
                tyfcbReceived = 620000000.0
            ),
            Member(
                id = "m_buibichlien",
                name = "Bùi Bích Liên",
                company = "CÔNG TY CP BAO BÌ CYAN HÀ NỘI",
                industry = "In ấn - Offset",
                role = UserRole.MEMBER,
                email = "lien.bui@cyanpackaging.vn",
                phone = "0985299989",
                avatarInitials = "BL",
                avatarColorHex = 0xFFF97316L,
                tyfcbContributed = 1200000000.0,
                tyfcbReceived = 950000000.0
            ),
            Member(
                id = "m_buitrunghieu",
                name = "Bùi Trung Hiếu",
                company = "Cơ sở trị liệu xương khớp Mộc Mỹ",
                industry = "Bác sĩ xương khớp",
                role = UserRole.MEMBER,
                email = "hieu.bui@trilieumocmy.vn",
                phone = "0335991366",
                avatarInitials = "TH",
                avatarColorHex = 0xFF10B981L,
                tyfcbContributed = 320000000.0,
                tyfcbReceived = 410000000.0
            ),
            Member(
                id = "m_chauminhthien",
                name = "Châu Minh Thiên",
                company = "Công ty TNHH Thương mại và Đầu tư Tào Thị",
                industry = "Vận chuyển Trung - Việt",
                role = UserRole.MEMBER,
                email = "thien.chau@taothi.vn",
                phone = "0966830230",
                avatarInitials = "MT",
                avatarColorHex = 0xFF3B82F6L,
                tyfcbContributed = 1600000000.0,
                tyfcbReceived = 1100000000.0
            ),
            Member(
                id = "m_duongminh",
                name = "Dương Minh",
                company = "Trang Anh",
                industry = "Điện nước",
                role = UserRole.MEMBER,
                email = "minh.duong@tranganh.vn",
                phone = "0974825888",
                avatarInitials = "DM",
                avatarColorHex = 0xFF8B5CF6L,
                tyfcbContributed = 780000000.0,
                tyfcbReceived = 820000000.0
            ),
            Member(
                id = "m_hason",
                name = "Hạ Sơn",
                company = "Đá Ốp lát Sơn Hải",
                industry = "Đá ốp lát",
                role = UserRole.MEMBER,
                email = "son.ha@daoplathai.vn",
                phone = "0973940896",
                avatarInitials = "HS",
                avatarColorHex = 0xFF0D9488L,
                tyfcbContributed = 1450000000.0,
                tyfcbReceived = 1300000000.0
            ),
            Member(
                id = "m_hoanganhthai",
                name = "Hoàng Anh Thái",
                company = "CÔNG TY CỔ PHẦN TUẤN CƯỜNG PHÁT",
                industry = "Sắt inox mỹ thuật",
                role = UserRole.MEMBER,
                email = "thai.hoang@tuancuongphat.vn",
                phone = "0971286574",
                avatarInitials = "AT",
                avatarColorHex = 0xFF6366F1L,
                tyfcbContributed = 920000000.0,
                tyfcbReceived = 870000000.0
            ),
            Member(
                id = "m_hoanglongthao",
                name = "Hoàng Long Thao",
                company = "CT CPSX NỘI THẤT VÀ DỊCH VỤ TOPPRO VIỆT NAM",
                industry = "Cầu Thang & Nội thất",
                role = UserRole.MEMBER,
                email = "thao.hoang@topprovietnam.vn",
                phone = "0966041589",
                avatarInitials = "LT",
                avatarColorHex = 0xFFF59E0BL,
                tyfcbContributed = 2300000000.0,
                tyfcbReceived = 1850000000.0
            ),
            Member(
                id = "m_khuongvancuong",
                name = "Khương Văn Cường",
                company = "TopGo Vietnam",
                industry = "Nhà hàng & Dịch vụ F&B",
                role = UserRole.MEMBER,
                email = "cuong.khuong@topgo.vn",
                phone = "0988668899",
                avatarInitials = "VC",
                avatarColorHex = 0xFFEC4899L,
                tyfcbContributed = 650000000.0,
                tyfcbReceived = 540000000.0
            ),
            Member(
                id = "m_nguyenthu",
                name = "Nguyễn Thứ",
                company = "Công ty TNHH vận chuyển quốc tế Vami",
                industry = "Keo trà mạch & Vận chuyển",
                role = UserRole.MEMBER,
                email = "thu.nguyen@vamilogistics.vn",
                phone = "0988826866",
                avatarInitials = "NT",
                avatarColorHex = 0xFF14B8A6L,
                tyfcbContributed = 810000000.0,
                tyfcbReceived = 760000000.0
            ),
            Member(
                id = "m_nguyenmanhnhat",
                name = "Nguyễn Mạnh Nhất",
                company = "Công ty cổ phần xây dựng và nội thất HNCONS",
                industry = "Xây dựng nhà đơn lập",
                role = UserRole.MEMBER,
                email = "nhat.nguyen@hncons.vn",
                phone = "0989923035",
                avatarInitials = "MN",
                avatarColorHex = 0xFF2563EBL,
                tyfcbContributed = 4200000000.0,
                tyfcbReceived = 3800000000.0
            ),
            Member(
                id = "m_nguyenquangtrung",
                name = "Nguyễn Quang Trung",
                company = "Công ty Trách nhiệm hữu hạn Đầu tư Thạch Lâm",
                industry = "Hệ thống nước",
                role = UserRole.MEMBER,
                email = "trung.nguyen@thachlam.vn",
                phone = "0869135222",
                avatarInitials = "QT",
                avatarColorHex = 0xFF0284C7L,
                tyfcbContributed = 1150000000.0,
                tyfcbReceived = 980000000.0
            ),
            Member(
                id = "m_nguyenthikimanh",
                name = "Nguyễn Thị Kim Anh",
                company = "Công Ty TNHH Unicity Marketing Việt Nam",
                industry = "Thực phẩm chức năng",
                role = UserRole.MEMBER,
                email = "kimanh.nguyen@unicity.vn",
                phone = "0838868228",
                avatarInitials = "KA",
                avatarColorHex = 0xFFE11D48L,
                tyfcbContributed = 480000000.0,
                tyfcbReceived = 520000000.0
            ),
            Member(
                id = "m_nguyenthinguyet",
                name = "Nguyễn Thị Nguyệt",
                company = "Cổ phần giáo dục Eduhome",
                industry = "Tư vấn Giáo dục",
                role = UserRole.MEMBER,
                email = "nguyet.nguyen@eduhome.vn",
                phone = "0855427516",
                avatarInitials = "NN",
                avatarColorHex = 0xFF7C3AEDL,
                tyfcbContributed = 590000000.0,
                tyfcbReceived = 630000000.0
            ),
            Member(
                id = "m_nguyenthidan",
                name = "Nguyễn Thị Dân",
                company = "Công ty TNHH In Bình Minh",
                industry = "Kinh doanh Keo silicon",
                role = UserRole.MEMBER,
                email = "dan.nguyen@inbinhminh.vn",
                phone = "0974235533",
                avatarInitials = "ND",
                avatarColorHex = 0xFFD97706L,
                tyfcbContributed = 720000000.0,
                tyfcbReceived = 690000000.0
            ),
            Member(
                id = "m_luan",
                name = "Nguyễn Tuấn Luận (Trưởng ban)",
                company = "Công ty cổ phần đầu tư công nghệ và xây lắp An Việt",
                industry = "Phòng cháy chữa cháy (PCCC)",
                role = UserRole.ADMIN,
                email = "tuanluanit1@gmail.com",
                phone = "0988726191",
                avatarInitials = "NL",
                avatarColorHex = 0xFFDC2626L,
                tyfcbContributed = 5600000000.0,
                tyfcbReceived = 4900000000.0
            ),
            Member(
                id = "m_nguyenvantuan",
                name = "Nguyễn Văn Tuấn",
                company = "Công ty TLartsofa",
                industry = "Thiết kế & thi công ghế Sofa",
                role = UserRole.MEMBER,
                email = "tuan.nguyen@tlartsofa.vn",
                phone = "0981888382",
                avatarInitials = "VT",
                avatarColorHex = 0xFF059669L,
                tyfcbContributed = 1350000000.0,
                tyfcbReceived = 1420000000.0
            ),
            Member(
                id = "m_nguyendinhmanh",
                name = "Nguyễn Đình Mạnh",
                company = "CÔNG TY TNHH BEN HOME",
                industry = "Nội thất nhà ở",
                role = UserRole.MEMBER,
                email = "manh.nguyen@benhome.vn",
                phone = "0915091886",
                avatarInitials = "ĐM",
                avatarColorHex = 0xFF4F46E5L,
                tyfcbContributed = 2100000000.0,
                tyfcbReceived = 1950000000.0
            ),
            Member(
                id = "m_nguyenductrong",
                name = "Nguyễn Đức Trọng",
                company = "Công ty cổ phần Sopka",
                industry = "Năng lượng mặt trời",
                role = UserRole.MEMBER,
                email = "trong.nguyen@sopka.vn",
                phone = "0983067345",
                avatarInitials = "ĐT",
                avatarColorHex = 0xFFEA580CL,
                tyfcbContributed = 1800000000.0,
                tyfcbReceived = 1650000000.0
            ),
            Member(
                id = "m_nguyenducxuyen",
                name = "Nguyễn Đức Xuyên",
                company = "Minh Tường",
                industry = "Sản phẩm xây dựng",
                role = UserRole.MEMBER,
                email = "xuyen.nguyen@minhtuong.vn",
                phone = "0986768648",
                avatarInitials = "ĐX",
                avatarColorHex = 0xFF2563EBL,
                tyfcbContributed = 950000000.0,
                tyfcbReceived = 890000000.0
            ),
            Member(
                id = "m_phamthanhtu",
                name = "Phạm Thanh Tú",
                company = "Công Ty Cổ Phần Giải Pháp Văn Phòng S-Office",
                industry = "Văn phòng phẩm",
                role = UserRole.MEMBER,
                email = "tu.pham@soffice.vn",
                phone = "0977226196",
                avatarInitials = "TT",
                avatarColorHex = 0xFF9333EAL,
                tyfcbContributed = 620000000.0,
                tyfcbReceived = 580000000.0
            ),
            Member(
                id = "m_phamthithuly",
                name = "Phạm Thị Thu Lý",
                company = "Công ty cổ phần chứng khoán VPS",
                industry = "Tư vấn tài chính",
                role = UserRole.MEMBER,
                email = "ly.pham@vps.com.vn",
                phone = "0986616567",
                avatarInitials = "TL",
                avatarColorHex = 0xFF0891B2L,
                tyfcbContributed = 1950000000.0,
                tyfcbReceived = 1450000000.0
            ),
            Member(
                id = "m_phanthihuyen",
                name = "Phan Thị Huyền",
                company = "Công ty sản xuất và thương mại Thành Nam",
                industry = "Trang trí nội thất",
                role = UserRole.MEMBER,
                email = "huyen.phan@thanhnam.vn",
                phone = "0349968878",
                avatarInitials = "TH",
                avatarColorHex = 0xFFBE185DL,
                tyfcbContributed = 1100000000.0,
                tyfcbReceived = 1050000000.0
            ),
            Member(
                id = "m_tranquochuynh",
                name = "Trần Quốc Huỳnh",
                company = "CÔNG TY TNHH QUẢNG CÁO DSDP",
                industry = "Công ty quảng cáo",
                role = UserRole.MEMBER,
                email = "huynh.tran@dsdp.vn",
                phone = "0961377767",
                avatarInitials = "QH",
                avatarColorHex = 0xFF1E3A8AL,
                tyfcbContributed = 880000000.0,
                tyfcbReceived = 790000000.0
            ),
            Member(
                id = "m_tranvanlong",
                name = "Trần Văn Long",
                company = "Công ty TNHH Sản Xuất TMDV và XD Hoàng Long",
                industry = "Vật liệu xây dựng",
                role = UserRole.MEMBER,
                email = "long.tran@hoanglongxd.vn",
                phone = "0366997998",
                avatarInitials = "VL",
                avatarColorHex = 0xFFB45309L,
                tyfcbContributed = 1750000000.0,
                tyfcbReceived = 1600000000.0
            ),
            Member(
                id = "m_trinhhoangngocbich",
                name = "Trịnh Hoàng Ngọc Bích",
                company = "Công ty TNHH KBT Craft",
                industry = "Quà tặng",
                role = UserRole.MEMBER,
                email = "bich.trinh@kbtcraft.vn",
                phone = "0764186682",
                avatarInitials = "NB",
                avatarColorHex = 0xFFC026D3L,
                tyfcbContributed = 490000000.0,
                tyfcbReceived = 510000000.0
            ),
            Member(
                id = "m_vuthithuytrang",
                name = "Vũ Thị Thuỳ Trang",
                company = "CÔNG TY TNHH THƯƠNG MẠI QUỐC TẾ TOÀN LỘC",
                industry = "Các loại bánh",
                role = UserRole.MEMBER,
                email = "trang.vu@toanloc.vn",
                phone = "0934253183",
                avatarInitials = "TT",
                avatarColorHex = 0xFFEA580CL,
                tyfcbContributed = 670000000.0,
                tyfcbReceived = 710000000.0
            ),
            Member(
                id = "m_dangthanhlong",
                name = "Đặng Thành Long",
                company = "Công ty TNHH thương mại và DVTH Thành Long",
                industry = "Cửa & cửa sổ",
                role = UserRole.MEMBER,
                email = "long.dang@thanhlongdoor.vn",
                phone = "0944768668",
                avatarInitials = "TL",
                avatarColorHex = 0xFF047857L,
                tyfcbContributed = 2200000000.0,
                tyfcbReceived = 2050000000.0
            ),
            Member(
                id = "m_dangxuankien",
                name = "Đặng Xuân Kiên",
                company = "Công ty VNC Interior",
                industry = "Tư vấn phong thủy",
                role = UserRole.MEMBER,
                email = "kien.dang@vncinterior.vn",
                phone = "0376625945",
                avatarInitials = "XK",
                avatarColorHex = 0xFFD97706L,
                tyfcbContributed = 830000000.0,
                tyfcbReceived = 770000000.0
            ),
            Member(
                id = "m_dokhaccuong",
                name = "Đỗ Khắc Cương",
                company = "Công ty cổ phần đầu tư & thương mại Nhật Lâm",
                industry = "Sản xuất nội thất",
                role = UserRole.MEMBER,
                email = "cuong.do@nhatlam.vn",
                phone = "0869323333",
                avatarInitials = "KC",
                avatarColorHex = 0xFF1D4ED8L,
                tyfcbContributed = 1900000000.0,
                tyfcbReceived = 1750000000.0
            ),
            Member(
                id = "m_dongphuongthao",
                name = "Đồng Phương Thảo",
                company = "Bảo Hiểm Manulife",
                industry = "Bảo hiểm nhân thọ",
                role = UserRole.MEMBER,
                email = "thao.dong@manulife.com.vn",
                phone = "0988123456",
                avatarInitials = "PT",
                avatarColorHex = 0xFF059669L,
                tyfcbContributed = 980000000.0,
                tyfcbReceived = 920000000.0
            )
        )
        _members.value = initialMembers

        // Khởi tạo các Cơ hội kinh doanh mẫu (Referrals)
        val initialReferrals = listOf(
            Referral(
                id = "ref_1",
                title = "Gói thầu Thi công Hệ thống PCCC & Báo cháy Tự động 8 tầng",
                giverMemberId = "m_nguyenmanhnhat",
                takerMemberId = "m_luan",
                clientName = "Nguyễn Trọng Tấn",
                clientPhone = "0909 123 456",
                clientEmail = "tan.nguyen@vincomgroup.vn",
                urgency = UrgencyLevel.HOT,
                status = DealStatus.QUOTED,
                contractType = "Thi công PCCC & Thẩm duyệt hồ sơ nghiệm thu",
                estimatedBudget = 1850000000.0,
                scaleAreaM2 = "3.200 m²",
                projectLocation = "Tòa nhà Văn phòng Sapphire, Cầu Giấy, Hà Nội",
                notes = "Chủ đầu tư yêu cầu tiêu chuẩn PCCC mới nhất 2026. Khảo sát xong và đang duyệt báo giá chi tiết.",
                createdAt = now - (2 * oneDay),
                updatedAt = now - (1 * oneDay)
            ),
            Referral(
                id = "ref_2",
                title = "Hạng mục Cầu Thang Kính & Gỗ Nghệ Thuật Biệt Thự Vườn 600m²",
                giverMemberId = "m_luan",
                takerMemberId = "m_hoanglongthao",
                clientName = "Bùi Thu Trang",
                clientPhone = "0913 888 222",
                clientEmail = "trang.bui@fintechviet.vn",
                urgency = UrgencyLevel.HOT,
                status = DealStatus.CLOSED_SUCCESS,
                contractType = "Thiết kế & Thi công Cầu Thang Cao Cấp",
                estimatedBudget = 680000000.0,
                scaleAreaM2 = "600 m²",
                projectLocation = "Khu đô thị Starlake Tây Hồ Tây, Biệt thự H10",
                notes = "Hợp đồng đã ký thành công! Đã ghi nhận doanh số TYFCB và trích phí cảm ơn quỹ nhóm.",
                createdAt = now - (10 * oneDay),
                updatedAt = now - (2 * oneDay),
                tyfcbAmount = 680000000.0,
                commissionPercent = 5.0,
                commonFundPercent = 1.0
            ),
            Referral(
                id = "ref_3",
                title = "Cung cấp Cửa nhôm kính Xingfa & Phụ kiện cao cấp dự án Biệt thự",
                giverMemberId = "m_nguyenmanhnhat",
                takerMemberId = "m_dangthanhlong",
                clientName = "Vũ Minh Quân",
                clientPhone = "0938 777 999",
                clientEmail = "quan.vu@techinvest.vn",
                urgency = UrgencyLevel.WARM,
                status = DealStatus.CONTACTED,
                contractType = "Cung cấp & Lắp đặt Cửa nhôm kính cao cấp",
                estimatedBudget = 950000000.0,
                scaleAreaM2 = "450 m²",
                projectLocation = "Khu đô thị Ciputra, Lô D3, Hà Nội",
                notes = "Khách hàng xây biệt thự sinh thái, cần cửa nhôm cách âm cách nhiệt tiêu chuẩn Châu Âu.",
                createdAt = now - (5 * oneDay),
                updatedAt = now - (3 * oneDay)
            ),
            Referral(
                id = "ref_4",
                title = "Hệ thống Đèn LED DD Lighting & Chiếu sáng thông minh Tòa nhà",
                giverMemberId = "m_hoanglongthao",
                takerMemberId = "m_buidinhdinh",
                clientName = "Đặng Quang Huy",
                clientPhone = "0918 333 444",
                clientEmail = "huy.dang@pacificlogistics.vn",
                urgency = UrgencyLevel.HOT,
                status = DealStatus.RECEIVED,
                contractType = "Thiết bị điện & Đèn LED Công nghiệp",
                estimatedBudget = 450000000.0,
                scaleAreaM2 = "1.800 m²",
                projectLocation = "KCN Quang Minh, Lô 12, Mê Linh",
                notes = "Cần cung cấp đèn led nhà xưởng & văn phòng. Đang chờ liên hệ khảo sát.",
                createdAt = now - (4 * oneDay),
                updatedAt = now - (4 * oneDay)
            ),
            Referral(
                id = "ref_5",
                title = "Hạng mục Đá Ốp lát Granite Mặt tiền & Cầu thang Biệt thự",
                giverMemberId = "m_nguyenmanhnhat",
                takerMemberId = "m_hason",
                clientName = "Phan Bích Hợp",
                clientPhone = "0908 666 555",
                clientEmail = "hop.phan@orientalcapital.vn",
                urgency = UrgencyLevel.WARM,
                status = DealStatus.CONTACTED,
                contractType = "Cung cấp & Thi công Đá ốp lát tự nhiên",
                estimatedBudget = 520000000.0,
                scaleAreaM2 = "350 m²",
                projectLocation = "Vinhomes Riverside, Hoa Lan 05",
                notes = "Chủ nhà chọn dòng đá nhập khẩu tự nhiên, yêu cầu thợ ốp tay nghề cao.",
                createdAt = now - (6 * oneDay),
                updatedAt = now - (5 * oneDay)
            )
        )
        _referrals.value = initialReferrals

        // Sổ hoa hồng
        val initialCommissions = listOf(
            CommissionEntry(
                id = "comm_1",
                referralId = "ref_2",
                referralTitle = "Hạng mục Cầu Thang Kính & Gỗ Nghệ Thuật Biệt Thự Vườn 600m²",
                giverMemberId = "m_luan",
                takerMemberId = "m_hoanglongthao",
                giverName = "Nguyễn Tuấn Luận (Trưởng ban)",
                takerName = "Hoàng Long Thao",
                tyfcbRevenue = 680000000.0,
                commissionPercent = 5.0,
                commissionAmount = 34000000.0,
                status = PaymentStatus.UNPAID,
                createdAt = now - (2 * oneDay),
                paymentNote = "Chờ khách thanh toán đợt tạm ứng 1"
            ),
            CommissionEntry(
                id = "comm_prev",
                referralId = "ref_old_1",
                referralTitle = "Gói Thầu Cung Cấp & Lắp Đặt Cửa Nhôm Biệt Thự",
                giverMemberId = "m_hoanglongthao",
                takerMemberId = "m_dangthanhlong",
                giverName = "Hoàng Long Thao",
                takerName = "Đặng Thành Long",
                tyfcbRevenue = 1200000000.0,
                commissionPercent = 4.0,
                commissionAmount = 48000000.0,
                status = PaymentStatus.PAID,
                createdAt = now - (25 * oneDay),
                paidAt = now - (15 * oneDay),
                paymentNote = "Đã chuyển khoản Vietcombank GD#994182"
            )
        )
        _commissions.value = initialCommissions

        // Lịch hẹn 1-2-1
        val initialMeetings = listOf(
            Meeting121(
                id = "meet_1",
                hostMemberId = "m_luan",
                guestMemberId = "m_nguyenmanhnhat",
                scheduledTime = now - (3 * oneDay),
                location = "Văn phòng An Việt PCCC - Cầu Giấy, Hà Nội",
                isCompleted = true,
                coreProducts = "Thi công PCCC, Hệ thống báo cháy tự động, Hồ sơ thẩm duyệt nghiệm thu PCCC",
                idealClientProfile = "Tổng thầu xây dựng, Chủ đầu tư nhà xưởng KCN, Tòa nhà chung cư & khách sạn",
                mutualCommitments = "Nhất kết nối Luận với 2 chủ đầu tư KCN; Luận giới thiệu HNCONS cho các dự án xây dựng mới.",
                completionDate = now - (3 * oneDay)
            ),
            Meeting121(
                id = "meet_2",
                hostMemberId = "m_luan",
                guestMemberId = "m_hoanglongthao",
                scheduledTime = now + (2 * oneDay),
                location = "Showroom TOPPRO Việt Nam",
                isCompleted = false,
                coreProducts = "Cầu thang kính, cầu thang gỗ nghệ thuật, lan can inox sắt mỹ thuật, nội thất cao cấp",
                idealClientProfile = "Gia chủ xây nhà phố, biệt thự cao cấp; tổng thầu hoàn thiện nội thất",
                mutualCommitments = "Xây dựng gói combo kết hợp: Thi công hoàn thiện PCCC an toàn + Cầu thang nội thất cao cấp."
            )
        )
        _meetings121.value = initialMeetings

        // Quỹ nhóm
        val initialFundTxs = listOf(
            GroupFundTransaction(
                id = "tx_1",
                type = FundTxType.MEMBER_FEE,
                title = "Đóng quỹ hoạt động Quý 4/2026",
                description = "Quỹ duy trì phòng họp, công cụ điều hành chapter",
                amount = 93000000.0,
                timestamp = now - (14 * oneDay)
            ),
            GroupFundTransaction(
                id = "tx_2",
                type = FundTxType.PENALTY_INFLOW,
                title = "Nộp phạt kỷ luật - Chậm phản hồi cơ hội",
                description = "Thành viên Bùi Đình Đình nộp phạt",
                amount = 500000.0,
                memberId = "m_buidinhdinh",
                memberName = "Bùi Đình Đình",
                timestamp = now - (8 * oneDay)
            ),
            GroupFundTransaction(
                id = "tx_3",
                type = FundTxType.EXPENSE_OUTFLOW,
                title = "Chi phí Phòng họp & Ăn sáng tuần 39",
                description = "Thuê sảnh hội nghị & thiết bị âm thanh máy chiếu",
                amount = -9800000.0,
                timestamp = now - (5 * oneDay)
            ),
            GroupFundTransaction(
                id = "tx_4",
                type = FundTxType.PENALTY_INFLOW,
                title = "Trích 1% TYFCB đóng góp quỹ Power Team TOPPRO",
                description = "Cam kết trích quỹ từ Deal thành công ref_2 của Hoàng Long Thao",
                amount = 6800000.0,
                memberId = "m_hoanglongthao",
                memberName = "Hoàng Long Thao",
                timestamp = now - (2 * oneDay)
            )
        )
        _groupFundTransactions.value = initialFundTxs

        // Kỷ luật & Phạt
        val initialPenalties = listOf(
            PenaltyEntry(
                id = "pen_1",
                memberId = "m_buidinhdinh",
                memberName = "Bùi Đình Đình",
                reason = "Chậm phản hồi cơ hội kinh doanh quá 3 ngày (Deal ref_4: Đèn LED DD Lighting)",
                amount = 500000.0,
                ruleCode = "QUY_DINH_3_NGAY",
                createdAt = now - (1 * oneDay),
                status = PaymentStatus.UNPAID
            ),
            PenaltyEntry(
                id = "pen_2",
                memberId = "m_dangxuankien",
                memberName = "Đặng Xuân Kiên",
                reason = "Nộp trễ biên bản họp 1-2-1 tháng 9",
                amount = 300000.0,
                ruleCode = "QUY_DINH_TRE_BIEN_BAN",
                createdAt = now - (9 * oneDay),
                status = PaymentStatus.PAID,
                paidAt = now - (8 * oneDay)
            )
        )
        _penalties.value = initialPenalties

        // Mã bản quyền
        val initialKeys = listOf(
            LicenseKey(
                key = "TOPPRO-7892-ABCD",
                note = "Mã sáng lập VIP cho Trần Tuấn Dũng",
                createdAt = now - (10 * oneDay),
                status = LicenseStatus.UNUSED
            ),
            LicenseKey(
                key = "TOPPRO-5431-EFGH",
                note = "Cấp cho Nguyễn Hoàng My (Vĩnh viễn)",
                createdAt = now - (20 * oneDay),
                status = LicenseStatus.USED,
                activatedByMemberId = "m_sarah",
                activatedAt = now - (19 * oneDay)
            ),
            LicenseKey(
                key = "TOPPRO-9988-WXYZ",
                note = "Mã tài trợ Doanh nghiệp Kim Cương",
                createdAt = now - (2 * oneDay),
                status = LicenseStatus.UNUSED
            )
        )
        _licenseKeys.value = initialKeys
    }

    // Đổi kỳ báo cáo
    fun setReportPeriod(period: ReportPeriod) {
        _selectedReportPeriod.value = period
    }

    // Lấy báo cáo thành viên theo kỳ (Tuần / Tháng / Năm)
    fun getMemberReports(period: ReportPeriod): List<MemberPeriodReport> {
        val multiplier = when (period) {
            ReportPeriod.WEEK -> 1
            ReportPeriod.MONTH -> 4
            ReportPeriod.YEAR -> 48
        }
        val refGoal = _kpiConfig.value.weeklyReferralsTarget * multiplier
        val meetGoal = (_kpiConfig.value.monthly121Target * (multiplier / 4.0)).toInt().coerceAtLeast(1)
        val tyfcbBaseGoal = when (period) {
            ReportPeriod.WEEK -> 200000000.0
            ReportPeriod.MONTH -> 1000000000.0
            ReportPeriod.YEAR -> 12000000000.0
        }

        val allMembers = _members.value
        val allRefs = _referrals.value
        val allMeets = _meetings121.value

        return allMembers.map { member ->
            val given = allRefs.count { it.giverMemberId == member.id }
            val received = allRefs.count { it.takerMemberId == member.id }
            val meetsCount = allMeets.count { (it.hostMemberId == member.id || it.guestMemberId == member.id) && it.isCompleted }
            val tyfcb = member.tyfcbContributed

            val isRefMet = given >= (refGoal / 2).coerceAtLeast(1)
            val isMeetMet = meetsCount >= (meetGoal / 2).coerceAtLeast(1)
            val isTyfcbMet = tyfcb >= (tyfcbBaseGoal * 0.4)

            val warning = when {
                !isRefMet && !isMeetMet -> "Cảnh báo: Chưa đạt cả chỉ tiêu Cơ hội và 1-2-1"
                !isRefMet -> "Cảnh báo: Tiến độ trao cơ hội dưới mức cam kết"
                !isMeetMet -> "Cảnh báo: Chưa hoàn thành đủ lịch 1-2-1 trong kỳ"
                member.penaltiesUnpaid > 0 -> "Cảnh báo: Còn khoản phạt kỷ luật chưa nộp"
                else -> null
            }

            MemberPeriodReport(
                memberId = member.id,
                memberName = member.name,
                company = member.company,
                industry = member.industry,
                referralsGiven = given,
                referralsGivenGoal = refGoal,
                referralsReceived = received,
                meetings121Count = meetsCount,
                meetings121Goal = meetGoal,
                tyfcbRevenue = tyfcb,
                tyfcbGoal = tyfcbBaseGoal,
                isReferralsMet = isRefMet,
                isMeetingsMet = isMeetMet,
                isTyfcbMet = isTyfcbMet,
                warningMessage = warning
            )
        }
    }

    // Danh sách cảnh báo thông minh toàn hệ thống
    fun getSystemAlerts(): List<SystemAlert> {
        val alerts = mutableListOf<SystemAlert>()

        // 1. Cảnh báo cơ hội quá hạn
        _referrals.value.filter { it.isOverdueReceived() }.forEach { ref ->
            val taker = _members.value.find { it.id == ref.takerMemberId }?.name ?: "Thành viên"
            alerts.add(
                SystemAlert(
                    id = "alert_ref_${ref.id}",
                    title = "Cơ hội quá hạn phản hồi > 3 ngày",
                    message = "Cơ hội '${ref.title}' trao cho $taker chưa được liên hệ.",
                    severity = AlertSeverity.CRITICAL,
                    memberName = taker,
                    actionText = "Đôn đốc ngay"
                )
            )
        }

        // 2. Cảnh báo thành viên chưa đạt KPI
        val monthlyReports = getMemberReports(ReportPeriod.MONTH)
        monthlyReports.filter { it.warningMessage != null }.forEach { report ->
            alerts.add(
                SystemAlert(
                    id = "alert_kpi_${report.memberId}",
                    title = "Chậm tiến độ KPI tháng",
                    message = "${report.memberName} (${report.company}): ${report.warningMessage}",
                    severity = AlertSeverity.WARNING,
                    memberName = report.memberName,
                    actionText = "Xem chi tiết"
                )
            )
        }

        // 3. Cảnh báo nộp phạt tồn đọng
        _penalties.value.filter { it.status == PaymentStatus.UNPAID }.forEach { pen ->
            alerts.add(
                SystemAlert(
                    id = "alert_pen_${pen.id}",
                    title = "Khoản phạt kỷ luật chưa nộp vào quỹ",
                    message = "${pen.memberName} có phiếu phạt ${String.format("%,.0f", pen.amount)} đ (${pen.reason}).",
                    severity = AlertSeverity.WARNING,
                    memberName = pen.memberName,
                    actionText = "Nhắc nhở"
                )
            )
        }

        return alerts
    }

    // Đăng ký thành viên mới
    fun registerNewMember(form: NewMemberForm): Member {
        val newId = "m_" + UUID.randomUUID().toString().take(6)
        val initials = form.fullName.trim().split(" ")
            .filter { it.isNotEmpty() }
            .takeLast(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")

        val colors = listOf(0xFF1E3A8A, 0xFFF97316, 0xFF3B82F6, 0xFF8B5CF6, 0xFF0D9488, 0xFF059669)
        val chosenColor = colors.random()

        val newMember = Member(
            id = newId,
            name = form.fullName,
            company = form.companyName,
            industry = form.industry,
            role = UserRole.MEMBER,
            email = form.email,
            phone = form.phone,
            avatarInitials = if (initials.isEmpty()) "TV" else initials,
            avatarColorHex = chosenColor,
            tyfcbContributed = 0.0,
            tyfcbReceived = 0.0,
            penaltiesUnpaid = 0.0
        )

        _members.update { it + newMember }

        firestore?.let { db ->
            val payload = mapOf(
                "id" to newMember.id,
                "userId" to (Firebase.auth.currentUser?.uid ?: newMember.id),
                "name" to newMember.name,
                "company" to newMember.company,
                "industry" to newMember.industry,
                "role" to newMember.role.name,
                "email" to newMember.email,
                "phone" to newMember.phone,
                "avatarInitials" to newMember.avatarInitials,
                "avatarColorHex" to newMember.avatarColorHex,
                "tyfcbContributed" to newMember.tyfcbContributed,
                "tyfcbReceived" to newMember.tyfcbReceived,
                "penaltiesUnpaid" to newMember.penaltiesUnpaid
            )
            db.collection("members").document(newMember.id)
                .set(payload)
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.CREATE, "members/${newMember.id}")
                }
        }

        return newMember
    }

    // Cập nhật mục tiêu Chapter
    fun updateChapterGoal(newGoal: ChapterGoalConfig) {
        _chapterGoal.value = newGoal
        firestore?.let { db ->
            val payload = mapOf(
                "id" to "current_goal",
                "period" to newGoal.period.name,
                "periodLabel" to newGoal.period.labelVi,
                "targetTyfcbRevenue" to newGoal.targetTyfcbRevenue,
                "currentTyfcbRevenue" to newGoal.currentTyfcbRevenue,
                "targetReferrals" to newGoal.targetReferrals,
                "currentReferrals" to newGoal.currentReferrals,
                "targetMeetings121" to newGoal.targetMeetings121,
                "currentMeetings121" to newGoal.currentMeetings121
            )
            db.collection("chapter_goals").document("current_goal")
                .set(payload)
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.WRITE, "chapter_goals/current_goal")
                }
        }
    }

    // Chuyển đổi vai trò
    fun switchRole(role: UserRole) {
        _session.update { it.copy(activeRole = role) }
    }

    // Mô phỏng hết hạn dùng thử
    fun toggleTrialExpiredSimulated() {
        _session.update { it.copy(isTrialExpiredSimulated = !it.isTrialExpiredSimulated) }
    }

    // Kích hoạt bản quyền vĩnh viễn
    fun activateLicenseKey(code: String): Boolean {
        val trimmed = code.trim().uppercase()
        val foundKey = _licenseKeys.value.find { it.key.uppercase() == trimmed && it.status == LicenseStatus.UNUSED }
        if (foundKey != null || trimmed == "TOPPRO-VIP" || trimmed == "TOPPRO-LIFETIME" || trimmed == "PT-PRO-VIP") {
            if (foundKey != null) {
                _licenseKeys.update { list ->
                    list.map {
                        if (it.key == foundKey.key) it.copy(
                            status = LicenseStatus.USED,
                            activatedByMemberId = _session.value.currentUserId,
                            activatedAt = System.currentTimeMillis()
                        ) else it
                    }
                }
            }
            _session.update {
                it.copy(
                    licenseType = LicenseType.LIFETIME,
                    isTrialExpiredSimulated = false
                )
            }
            return true
        }
        return false
    }

    // Admin tạo mã bản quyền mới
    fun generateLicenseKey(note: String): String {
        val randomPart1 = (1000..9999).random()
        val chars = ('A'..'Z')
        val randomPart2 = (1..4).map { chars.random() }.joinToString("")
        val newKeyStr = "TOPPRO-$randomPart1-$randomPart2"

        val newKey = LicenseKey(
            key = newKeyStr,
            note = if (note.isBlank()) "Bản quyền Vĩnh viễn Tiêu chuẩn" else note,
            createdAt = System.currentTimeMillis(),
            status = LicenseStatus.UNUSED
        )
        _licenseKeys.update { listOf(newKey) + it }
        return newKeyStr
    }

    // Tạo cơ hội kinh doanh mới
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
        val now = System.currentTimeMillis()
        val newRef = Referral(
            id = "ref_" + UUID.randomUUID().toString().take(8),
            title = "$contractType ($scaleAreaM2)",
            giverMemberId = _session.value.currentUserId,
            takerMemberId = takerMemberId,
            clientName = clientName,
            clientPhone = clientPhone,
            clientEmail = clientEmail,
            urgency = urgency,
            status = DealStatus.RECEIVED,
            contractType = contractType,
            estimatedBudget = estimatedBudget,
            scaleAreaM2 = scaleAreaM2,
            projectLocation = projectLocation,
            notes = notes,
            createdAt = now,
            updatedAt = now
        )
        _referrals.update { listOf(newRef) + it }

        firestore?.let { db ->
            val payload = mapOf(
                "id" to newRef.id,
                "title" to newRef.title,
                "giverMemberId" to newRef.giverMemberId,
                "takerMemberId" to newRef.takerMemberId,
                "clientName" to newRef.clientName,
                "clientPhone" to newRef.clientPhone,
                "clientEmail" to newRef.clientEmail,
                "urgency" to newRef.urgency.name,
                "status" to newRef.status.name,
                "contractType" to newRef.contractType,
                "estimatedBudget" to newRef.estimatedBudget,
                "scaleAreaM2" to newRef.scaleAreaM2,
                "projectLocation" to newRef.projectLocation,
                "notes" to newRef.notes,
                "createdAtMs" to now,
                "updatedAtMs" to now
            )
            db.collection("referrals").document(newRef.id)
                .set(payload)
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.CREATE, "referrals/${newRef.id}")
                }
        }

        return newRef
    }

    // Cập nhật trạng thái Deal
    fun updateDealStatus(
        referralId: String,
        newStatus: DealStatus,
        tyfcbAmount: Double? = null,
        commissionPercent: Double? = null,
        commonFundPercent: Double? = null
    ) {
        val now = System.currentTimeMillis()
        var updatedRef: Referral? = null

        _referrals.update { list ->
            list.map { ref ->
                if (ref.id == referralId) {
                    val updated = ref.copy(
                        status = newStatus,
                        updatedAt = now,
                        tyfcbAmount = tyfcbAmount ?: ref.tyfcbAmount,
                        commissionPercent = commissionPercent ?: ref.commissionPercent,
                        commonFundPercent = commonFundPercent ?: ref.commonFundPercent
                    )
                    updatedRef = updated
                    updated
                } else ref
            }
        }

        firestore?.let { db ->
            val updates = mutableMapOf<String, Any>(
                "status" to newStatus.name,
                "updatedAtMs" to now
            )
            if (tyfcbAmount != null) updates["tyfcbAmount"] = tyfcbAmount
            if (commissionPercent != null) updates["commissionPercent"] = commissionPercent
            if (commonFundPercent != null) updates["commonFundPercent"] = commonFundPercent

            db.collection("referrals").document(referralId)
                .update(updates)
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.UPDATE, "referrals/$referralId")
                }
        }

        if (newStatus == DealStatus.CLOSED_SUCCESS && tyfcbAmount != null && tyfcbAmount > 0) {
            val ref = updatedRef ?: return
            val giver = _members.value.find { it.id == ref.giverMemberId }
            val taker = _members.value.find { it.id == ref.takerMemberId }

            val commPct = commissionPercent ?: 5.0
            val commAmount = tyfcbAmount * (commPct / 100.0)

            val newCommEntry = CommissionEntry(
                id = "comm_" + UUID.randomUUID().toString().take(8),
                referralId = ref.id,
                referralTitle = ref.title,
                giverMemberId = ref.giverMemberId,
                takerMemberId = ref.takerMemberId,
                giverName = giver?.name ?: "Người trao",
                takerName = taker?.name ?: "Người nhận",
                tyfcbRevenue = tyfcbAmount,
                commissionPercent = commPct,
                commissionAmount = commAmount,
                status = PaymentStatus.UNPAID,
                createdAt = now,
                paymentNote = "TYFCB Chốt deal: ${ref.clientName}"
            )
            _commissions.update { listOf(newCommEntry) + it }

            val fundPct = commonFundPercent ?: 1.0
            if (fundPct > 0) {
                val fundAmount = tyfcbAmount * (fundPct / 100.0)
                val newFundTx = GroupFundTransaction(
                    id = "tx_" + UUID.randomUUID().toString().take(8),
                    type = FundTxType.PENALTY_INFLOW,
                    title = "Trích 1% TYFCB đóng góp Quỹ nhóm",
                    description = "Đóng góp từ ${taker?.name} cho deal '${ref.title}'",
                    amount = fundAmount,
                    memberId = ref.takerMemberId,
                    memberName = taker?.name,
                    timestamp = now
                )
                _groupFundTransactions.update { listOf(newFundTx) + it }
            }
        }
    }

    // Xác nhận đã thanh toán hoa hồng
    fun markCommissionPaid(commissionId: String, note: String = "Đã chuyển khoản thành công") {
        val now = System.currentTimeMillis()
        _commissions.update { list ->
            list.map { comm ->
                if (comm.id == commissionId) {
                    comm.copy(
                        status = PaymentStatus.PAID,
                        paidAt = now,
                        paymentNote = note
                    )
                } else comm
            }
        }
    }

    // Đặt lịch hẹn 1-2-1
    fun schedule121(guestMemberId: String, scheduledTime: Long, location: String): Meeting121 {
        val newMeeting = Meeting121(
            id = "meet_" + UUID.randomUUID().toString().take(8),
            hostMemberId = _session.value.currentUserId,
            guestMemberId = guestMemberId,
            scheduledTime = scheduledTime,
            location = location,
            isCompleted = false
        )
        _meetings121.update { listOf(newMeeting) + it }

        firestore?.let { db ->
            val payload = mapOf(
                "id" to newMeeting.id,
                "hostMemberId" to newMeeting.hostMemberId,
                "guestMemberId" to newMeeting.guestMemberId,
                "scheduledTimeMs" to scheduledTime,
                "location" to location,
                "isCompleted" to false
            )
            db.collection("meetings121").document(newMeeting.id)
                .set(payload)
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.CREATE, "meetings121/${newMeeting.id}")
                }
        }

        return newMeeting
    }

    // Lưu biên bản 1-2-1
    fun complete121Minutes(
        meetingId: String,
        coreProducts: String,
        idealClientProfile: String,
        mutualCommitments: String
    ) {
        val now = System.currentTimeMillis()
        _meetings121.update { list ->
            list.map { m ->
                if (m.id == meetingId) {
                    m.copy(
                        isCompleted = true,
                        coreProducts = coreProducts,
                        idealClientProfile = idealClientProfile,
                        mutualCommitments = mutualCommitments,
                        completionDate = now
                    )
                } else m
            }
        }

        firestore?.let { db ->
            val updates = mapOf(
                "isCompleted" to true,
                "coreProducts" to coreProducts,
                "idealClientProfile" to idealClientProfile,
                "mutualCommitments" to mutualCommitments,
                "completionDateMs" to now
            )
            db.collection("meetings121").document(meetingId)
                .update(updates)
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.UPDATE, "meetings121/$meetingId")
                }
        }
    }

    // Tự động quét kỷ luật vi phạm
    fun runAutomatedDisciplineScan(): Int {
        val now = System.currentTimeMillis()
        var newViolationsCount = 0
        val config = _kpiConfig.value
        val threeDaysMs = config.overdueResponseDaysLimit * 24L * 60 * 60 * 1000

        val currentReferrals = _referrals.value
        currentReferrals.forEach { ref ->
            if (ref.status == DealStatus.RECEIVED && (now - ref.updatedAt) >= threeDaysMs) {
                val existingPenalty = _penalties.value.find {
                    it.ruleCode == "RULE_OVERDUE_${ref.id}" || (it.ruleCode.contains("QUY_DINH") && it.reason.contains(ref.id))
                }
                if (existingPenalty == null) {
                    val taker = _members.value.find { it.id == ref.takerMemberId }
                    val newPenalty = PenaltyEntry(
                        id = "pen_" + UUID.randomUUID().toString().take(8),
                        memberId = ref.takerMemberId,
                        memberName = taker?.name ?: "Thành viên",
                        reason = "Quá hạn phản hồi cơ hội kinh doanh (> ${config.overdueResponseDaysLimit} ngày không cập nhật cho '${ref.title}')",
                        amount = config.overduePenaltyAmount,
                        ruleCode = "RULE_OVERDUE_${ref.id}",
                        createdAt = now,
                        status = PaymentStatus.UNPAID
                    )
                    _penalties.update { listOf(newPenalty) + it }
                    newViolationsCount++
                }
            }
        }
        return newViolationsCount
    }

    // Nộp phạt vào Quỹ nhóm
    fun payPenalty(penaltyId: String) {
        val now = System.currentTimeMillis()
        var paidEntry: PenaltyEntry? = null

        _penalties.update { list ->
            list.map { pen ->
                if (pen.id == penaltyId && pen.status == PaymentStatus.UNPAID) {
                    paidEntry = pen
                    pen.copy(status = PaymentStatus.PAID, paidAt = now)
                } else pen
            }
        }

        paidEntry?.let { entry ->
            val fundTx = GroupFundTransaction(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                type = FundTxType.PENALTY_INFLOW,
                title = "Nộp phạt kỷ luật - ${entry.memberName}",
                description = entry.reason,
                amount = entry.amount,
                memberId = entry.memberId,
                memberName = entry.memberName,
                timestamp = now
            )
            _groupFundTransactions.update { listOf(fundTx) + it }
        }
    }

    // Cập nhật cấu hình KPI
    fun updateKpiConfig(config: KpiConfig) {
        _kpiConfig.value = config
    }
}
