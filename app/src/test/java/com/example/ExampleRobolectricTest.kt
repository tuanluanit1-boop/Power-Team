package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Power Team TOPPRO", appName)
  }

  @Test
  fun `verify license key activation and discipline rules`() {
    val repo = com.example.data.repository.PowerTeamRepository()
    // Test initial trial state
    assertEquals(com.example.data.model.LicenseType.TRIAL, repo.session.value.licenseType)

    // Test activating VIP key
    val activated = repo.activateLicenseKey("PT-PRO-VIP")
    assertEquals(true, activated)
    assertEquals(com.example.data.model.LicenseType.LIFETIME, repo.session.value.licenseType)

    // Test running discipline scan
    val violations = repo.runAutomatedDisciplineScan()
    assertTrue(violations >= 0)
  }
}
