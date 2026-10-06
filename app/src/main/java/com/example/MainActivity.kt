package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.PowerTeamViewModel
import com.example.ui.auth.SignInScreen
import com.example.ui.auth.signOut
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SlateBg
import androidx.credentials.CredentialManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: PowerTeamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                if (currentUser == null) {
                    SignInScreen(
                        onAuthSuccess = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Đăng nhập thành công với tài khoản Google!")
                            }
                        },
                        onAuthError = { err ->
                            scope.launch {
                                snackbarHostState.showSnackbar(err)
                            }
                        }
                    )
                } else {
                    val session by viewModel.session.collectAsStateWithLifecycle()
                    val members by viewModel.members.collectAsStateWithLifecycle()
                    val currentMember by viewModel.currentMember.collectAsStateWithLifecycle()
                    val givenReferrals by viewModel.givenReferrals.collectAsStateWithLifecycle()
                    val receivedReferrals by viewModel.receivedReferrals.collectAsStateWithLifecycle()
                    val overdueReferrals by viewModel.overdueReferrals.collectAsStateWithLifecycle()
                    val commissions by viewModel.commissions.collectAsStateWithLifecycle()
                    val commissionsReceivable by viewModel.myCommissionsReceivable.collectAsStateWithLifecycle()
                    val commissionsPayable by viewModel.myCommissionsPayable.collectAsStateWithLifecycle()
                    val meetings by viewModel.meetings121.collectAsStateWithLifecycle()
                    val interactionMatrix by viewModel.interactionMatrix.collectAsStateWithLifecycle()
                    val groupFundBalance by viewModel.groupFundBalance.collectAsStateWithLifecycle()
                    val groupFundTransactions by viewModel.groupFundTransactions.collectAsStateWithLifecycle()
                    val penalties by viewModel.penalties.collectAsStateWithLifecycle()
                    val myUnpaidPenalties by viewModel.myUnpaidPenalties.collectAsStateWithLifecycle()
                    val licenseKeys by viewModel.licenseKeys.collectAsStateWithLifecycle()
                    val kpiConfig by viewModel.kpiConfig.collectAsStateWithLifecycle()
                    val chapterGoal by viewModel.chapterGoal.collectAsStateWithLifecycle()
                    val selectedReportPeriod by viewModel.selectedReportPeriod.collectAsStateWithLifecycle()
                    val memberReports by viewModel.memberReports.collectAsStateWithLifecycle()
                    val systemAlerts by viewModel.systemAlerts.collectAsStateWithLifecycle()

                    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
                    var showCreateReferralSheet by remember { mutableStateOf(false) }
                    var showActivateLicenseDialog by remember { mutableStateOf(false) }
                    var showSchedule121Dialog by remember { mutableStateOf(false) }
                    var showInstallGuideDialog by remember { mutableStateOf(false) }
                    var showManualAndRegisterDialog by remember { mutableStateOf(false) }

                    // Phím Back quay lại màn hình Trang chủ nếu đang ở tab khác
                    BackHandler(enabled = currentTab != NavigationTab.HOME) {
                        currentTab = NavigationTab.HOME
                    }

                // Hộp thoại Hướng dẫn cài đặt Android & iOS
                if (showInstallGuideDialog) {
                    InstallationGuideDialog(
                        onDismiss = { showInstallGuideDialog = false }
                    )
                }

                // Hộp thoại Sổ tay hướng dẫn & Đăng ký hội viên mới
                if (showManualAndRegisterDialog) {
                    UserManualAndRegistrationDialog(
                        onDismiss = { showManualAndRegisterDialog = false },
                        onRegisterMember = { form ->
                            val newMem = viewModel.registerNewMember(form)
                            scope.launch {
                                snackbarHostState.showSnackbar("Đã thêm hội viên ${newMem.name} (${newMem.company}) vào Chapter!")
                            }
                        }
                    )
                }

                // Hộp thoại tạo cơ hội kinh doanh mới
                if (showCreateReferralSheet) {
                    CreateReferralSheet(
                        members = members,
                        currentUserId = session.currentUserId,
                        canCreateReferral = session.canCreateReferral,
                        onDismiss = { showCreateReferralSheet = false },
                        onActivateLicenseClick = {
                            showCreateReferralSheet = false
                            showActivateLicenseDialog = true
                        },
                        onSubmit = { takerId, name, phone, email, urgency, contractType, budget, scale, loc, notes ->
                            viewModel.createReferral(
                                takerMemberId = takerId,
                                clientName = name,
                                clientPhone = phone,
                                clientEmail = email,
                                urgency = urgency,
                                contractType = contractType,
                                estimatedBudget = budget,
                                scaleAreaM2 = scale,
                                projectLocation = loc,
                                notes = notes
                            )
                            showCreateReferralSheet = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Đã trao cơ hội kinh doanh cho đồng đội thành công!")
                            }
                        }
                    )
                }

                // Hộp thoại kích hoạt bản quyền
                if (showActivateLicenseDialog) {
                    ActivateLicenseDialog(
                        availableKeys = licenseKeys,
                        onDismiss = { showActivateLicenseDialog = false },
                        onActivate = { key ->
                            viewModel.activateLicenseKey(key)
                        }
                    )
                }

                // Hộp thoại đặt lịch 1-2-1
                if (showSchedule121Dialog) {
                    Schedule121Dialog(
                        members = members,
                        currentUserId = session.currentUserId,
                        onDismiss = { showSchedule121Dialog = false },
                        onSchedule = { guestId, time, loc ->
                            viewModel.schedule121(guestId, time, loc)
                            showSchedule121Dialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Đã lên lịch hẹn 1-2-1 thành công!")
                            }
                        },
                        onSaveMinutes = { mId, core, ideal, commits ->
                            viewModel.complete121Minutes(mId, core, ideal, commits)
                            showSchedule121Dialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Đã lưu biên bản họp 1-2-1 thành công!")
                            }
                        }
                    )
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopHeaderBar(
                            session = session,
                            onRoleToggle = { newRole ->
                                viewModel.switchRole(newRole)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (newRole == com.example.data.model.UserRole.ADMIN) "Đã chuyển sang Chế độ Trưởng ban (Admin)" else "Đã chuyển sang Chế độ Hội viên"
                                    )
                                }
                            },
                            onActivateLicenseClick = { showActivateLicenseDialog = true }
                        )
                    },
                    bottomBar = {
                        BottomNavBar(
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it },
                            activeRole = session.activeRole,
                            referralsBadgeCount = overdueReferrals.size,
                            penaltiesBadgeCount = myUnpaidPenalties.size
                        )
                    },
                    containerColor = SlateBg,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            NavigationTab.HOME -> {
                                HomeScreen(
                                    session = session,
                                    currentMember = currentMember,
                                    givenReferrals = givenReferrals,
                                    receivedReferrals = receivedReferrals,
                                    overdueReferrals = overdueReferrals,
                                    meetings = meetings,
                                    commissionsReceivable = commissionsReceivable,
                                    kpiConfig = kpiConfig,
                                    onNavigateTab = { currentTab = it },
                                    onCreateReferralClick = { showCreateReferralSheet = true },
                                    onSchedule121Click = { showSchedule121Dialog = true },
                                    onActivateLicenseClick = { showActivateLicenseDialog = true },
                                    onToggleTrialSimulated = { viewModel.toggleTrialExpiredSimulated() },
                                    onReferralSelected = {
                                        currentTab = NavigationTab.REFERRALS
                                    },
                                    onOpenInstallGuide = { showInstallGuideDialog = true },
                                    onOpenManualAndRegister = { showManualAndRegisterDialog = true }
                                )
                            }
                            NavigationTab.REFERRALS -> {
                                ReferralsScreen(
                                    currentUserId = session.currentUserId,
                                    members = members,
                                    givenReferrals = givenReferrals,
                                    receivedReferrals = receivedReferrals,
                                    onCreateReferralClick = { showCreateReferralSheet = true },
                                    onUpdateDealStatus = { refId, newStatus, tyfcb, commPct, fundPct ->
                                        viewModel.updateDealStatus(refId, newStatus, tyfcb, commPct, fundPct)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Tiến độ cơ hội đã cập nhật: ${newStatus.label}")
                                        }
                                    }
                                )
                            }
                            NavigationTab.MEETINGS_121 -> {
                                Meetings121Screen(
                                    currentUserId = session.currentUserId,
                                    members = members,
                                    meetings = meetings,
                                    interactionMatrix = interactionMatrix,
                                    onScheduleMeeting = { guestId, time, loc ->
                                        viewModel.schedule121(guestId, time, loc)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Đã đặt lịch hẹn 1-2-1")
                                        }
                                    },
                                    onSaveMinutes = { meetingId, core, ideal, commits ->
                                        viewModel.complete121Minutes(meetingId, core, ideal, commits)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Đã lưu biên bản họp 1-2-1 thành công")
                                        }
                                    }
                                )
                            }
                            NavigationTab.FINANCE -> {
                                FinanceScreen(
                                    currentUserId = session.currentUserId,
                                    commissions = commissions,
                                    commissionsReceivable = commissionsReceivable,
                                    commissionsPayable = commissionsPayable,
                                    groupFundBalance = groupFundBalance,
                                    groupFundTransactions = groupFundTransactions,
                                    penalties = penalties,
                                    myUnpaidPenalties = myUnpaidPenalties,
                                    onMarkCommissionPaid = { commId, note ->
                                        viewModel.markCommissionPaid(commId, note)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Đã xác nhận thanh toán hoa hồng")
                                        }
                                    },
                                    onPayPenalty = { penId ->
                                        viewModel.payPenalty(penId)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Đã nộp phạt thành công vào Quỹ nhóm Chapter")
                                        }
                                    }
                                )
                            }
                            NavigationTab.PROFILE -> {
                                AdminProfileScreen(
                                    session = session,
                                    currentMember = currentMember,
                                    members = members,
                                    licenseKeys = licenseKeys,
                                    kpiConfig = kpiConfig,
                                    chapterGoal = chapterGoal,
                                    selectedReportPeriod = selectedReportPeriod,
                                    memberReports = memberReports,
                                    systemAlerts = systemAlerts,
                                    onSetReportPeriod = { period ->
                                        viewModel.setReportPeriod(period)
                                    },
                                    onActivateLicenseClick = { showActivateLicenseDialog = true },
                                    onGenerateKey = { note ->
                                        viewModel.generateLicenseKey(note)
                                    },
                                    onRunDisciplineScan = {
                                        viewModel.runDisciplineScan()
                                    },
                                    onUpdateKpiConfig = { updatedConfig ->
                                        viewModel.updateKpiConfig(updatedConfig)
                                    },
                                    onSwitchRole = { newRole ->
                                        viewModel.switchRole(newRole)
                                    },
                                    onOpenInstallGuide = { showInstallGuideDialog = true },
                                    onOpenManualAndRegister = { showManualAndRegisterDialog = true },
                                    onSignOut = {
                                        signOut(
                                            context = this@MainActivity,
                                            credentialManager = CredentialManager.create(this@MainActivity),
                                            onSignOutComplete = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Đã đăng xuất tài khoản Google")
                                                }
                                            },
                                            scope = scope
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                }
            }
        }
    }
}
