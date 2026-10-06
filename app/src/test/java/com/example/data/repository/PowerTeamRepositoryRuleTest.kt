package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.ChapterGoalConfig
import com.example.data.model.NewMemberForm
import com.example.data.model.ReportPeriod
import com.example.data.model.UrgencyLevel
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.*
import org.junit.Test

class PowerTeamRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun testAuthenticatedUser_canCreateMemberAndRead(): Unit = runBlocking {
        val uid = signInTestUser("member_test@toanthang.vn")
        assertTrue(uid.isNotEmpty())

        val repository = PowerTeamRepository(firestore = firestore)

        val form = NewMemberForm(
            fullName = "Lê Quốc Toàn",
            companyName = "Toàn Cầu HVAC",
            industry = "Cơ Điện Lạnh HVAC",
            phone = "0912176050",
            email = "toan.le@toancau.vn",
            coreProducts = "Hệ thống thông gió công nghiệp",
            sponsorName = "Luận PCCC"
        )

        val created = repository.registerNewMember(form)
        assertEquals("Lê Quốc Toàn", created.name)

        // Assert against local Firestore emulator
        val doc = firestore.collection("members").document(created.id).get().await()
        assertTrue(doc.exists())
        assertEquals("Toàn Cầu HVAC", doc.getString("company"))
        assertEquals("Cơ Điện Lạnh HVAC", doc.getString("industry"))
    }

    @Test
    fun testAuthenticatedUser_canCreateReferralAndRead(): Unit = runBlocking {
        val uid = signInTestUser("giver_test@toanthang.vn")
        assertTrue(uid.isNotEmpty())

        val repository = PowerTeamRepository(firestore = firestore)

        val referral = repository.createReferral(
            takerMemberId = "m_sarah",
            clientName = "Nguyễn Hoàng Nam",
            clientPhone = "0987654321",
            clientEmail = "nam.nguyen@vincom.vn",
            urgency = UrgencyLevel.HOT,
            contractType = "Thiết kế & Thi công PCCC",
            estimatedBudget = 2500000000.0,
            scaleAreaM2 = "3500 m2",
            projectLocation = "Khu Công Nghiệp VSIP 2",
            notes = "Cần khảo sát và báo giá gấp trong 2 ngày"
        )

        assertTrue(referral.id.isNotEmpty())

        // Assert against local Firestore emulator
        val doc = firestore.collection("referrals").document(referral.id).get().await()
        assertTrue(doc.exists())
        assertEquals("Nguyễn Hoàng Nam", doc.getString("clientName"))
        assertEquals("HOT", doc.getString("urgency"))
    }

    @Test
    fun testAuthenticatedUser_canUpdateChapterGoal(): Unit = runBlocking {
        val uid = signInTestUser("admin_test@toanthang.vn")
        assertTrue(uid.isNotEmpty())

        val repository = PowerTeamRepository(firestore = firestore)

        val newGoal = ChapterGoalConfig(
            period = ReportPeriod.MONTH,
            targetTyfcbRevenue = 20000000000.0, // 20 Tỷ VNĐ
            currentTyfcbRevenue = 15000000000.0,
            targetReferrals = 50,
            currentReferrals = 38,
            targetMeetings121 = 40,
            currentMeetings121 = 30
        )

        repository.updateChapterGoal(newGoal)

        // Assert against local Firestore emulator
        val doc = firestore.collection("chapter_goals").document("current_goal").get().await()
        assertTrue(doc.exists())
        assertEquals(20000000000.0, doc.getDouble("targetTyfcbRevenue") ?: 0.0, 0.01)
        assertEquals(50L, doc.getLong("targetReferrals") ?: 0L)
    }

    @Test
    fun testUnauthenticatedUser_rejectedBySecurityRules(): Unit = runBlocking {
        // Sign out user completely
        auth.signOut()

        // Attempt direct Firestore operation without authentication
        var failed = false
        try {
            firestore.collection("members").document("unauth_doc").set(
                mapOf(
                    "id" to "unauth_doc",
                    "userId" to "nobody",
                    "name" to "Hacker",
                    "company" to "Unknown",
                    "industry" to "Unknown",
                    "role" to "MEMBER",
                    "email" to "hacker@test.com",
                    "phone" to "0000"
                )
            ).await()
        } catch (e: FirebaseFirestoreException) {
            failed = true
        } catch (e: Exception) {
            failed = true
        }
        assertTrue("Unauthenticated write must be rejected by rules", failed)
    }
}
