package com.example.data.model

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.KharcheeRepository
import com.example.ui.components.PremiumFeatureGate
import com.example.ui.viewmodel.KharcheeViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PremiumEntitlementGateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var repository: KharcheeRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("1:123456789:android:testkharchee")
                    .setApiKey("test-local-only-placeholder")
                    .setProjectId("test-kharchee-project")
                    .build()
            )
        }
        repository = KharcheeRepository(FirebaseFirestore.getInstance())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun evaluator_handlesFreeActiveExpiredLifetimePendingAndErrorStatesFailClosed() {
        val now = 1_750_000_000_000L

        // 1. Free user -> locked with FREE_TIER & Upgrade to Pro CTA
        val freeState = PremiumEntitlementEvaluator.evaluate(
            remoteSettings = UserSettings(userId = "u1", isPro = false, proTier = "free"),
            isSyncedFromFirestore = true,
            nowMillis = now
        )
        assertFalse(freeState.isProActive)
        val freeDecision = freeState.evaluateAccess(PremiumFeature.ADVANCED_ANALYTICS, now)
        assertTrue(freeDecision is PremiumAccessDecision.Locked)
        assertEquals(PremiumLockReason.FREE_TIER, (freeDecision as PremiumAccessDecision.Locked).reason)
        assertEquals("Upgrade to Pro", freeDecision.ctaTitle)

        // 2. Active Gold user (future expiry) -> Granted
        val goldState = PremiumEntitlementEvaluator.evaluate(
            remoteSettings = UserSettings(
                userId = "u1",
                isPro = true,
                proTier = "gold",
                proExpiryMillis = now + 86_400_000L * 15
            ),
            isSyncedFromFirestore = true,
            nowMillis = now
        )
        assertTrue(goldState.isProActive)
        val goldDecision = goldState.evaluateAccess(PremiumFeature.ADVANCED_ANALYTICS, now)
        assertTrue(goldDecision is PremiumAccessDecision.Granted)
        assertFalse((goldDecision as PremiumAccessDecision.Granted).isLifetime)

        // 3. Expired Gold user -> Locked with SUBSCRIPTION_EXPIRED
        val expiredState = PremiumEntitlementEvaluator.evaluate(
            remoteSettings = UserSettings(
                userId = "u1",
                isPro = true,
                proTier = "gold",
                proExpiryMillis = now - 60_000L
            ),
            isSyncedFromFirestore = true,
            nowMillis = now
        )
        assertFalse(expiredState.isProActive)
        assertTrue(expiredState.isExpired)
        val expiredDecision = expiredState.evaluateAccess(PremiumFeature.REPORT_EXPORTS, now)
        assertTrue(expiredDecision is PremiumAccessDecision.Locked)
        assertEquals(PremiumLockReason.SUBSCRIPTION_EXPIRED, (expiredDecision as PremiumAccessDecision.Locked).reason)

        // 4. Lifetime Platinum user -> Granted with isLifetime = true
        val lifetimeState = PremiumEntitlementEvaluator.evaluate(
            remoteSettings = UserSettings(
                userId = "u1",
                isPro = true,
                proTier = "platinum",
                proExpiryMillis = 0L
            ),
            isSyncedFromFirestore = true,
            nowMillis = now
        )
        assertTrue(lifetimeState.isProActive)
        assertTrue(lifetimeState.isLifetime)
        assertEquals("PRO LIFETIME", lifetimeState.statusBadgeLabel())

        // 5. Pending server verification -> Locked with PENDING_VERIFICATION
        val pendingState = PremiumEntitlementEvaluator.evaluate(
            remoteSettings = UserSettings(userId = "u1", isPro = false, proTier = "free"),
            isSyncedFromFirestore = true,
            hasPendingVerification = true,
            nowMillis = now
        )
        assertFalse(pendingState.isProActive)
        val pendingDecision = pendingState.evaluateAccess(PremiumFeature.CUSTOM_DATE_FILTERS, now)
        assertEquals(PremiumLockReason.PENDING_VERIFICATION, (pendingDecision as PremiumAccessDecision.Locked).reason)

        // 6. Firestore error -> Fail-closed Locked with FIRESTORE_ERROR
        val errorState = PremiumEntitlementEvaluator.evaluate(
            remoteSettings = null,
            isSyncedFromFirestore = false,
            errorMessage = "PERMISSION_DENIED",
            nowMillis = now
        )
        assertFalse(errorState.isProActive)
        val errDecision = errorState.evaluateAccess(PremiumFeature.ADVANCED_ANALYTICS, now)
        assertEquals(PremiumLockReason.FIRESTORE_ERROR, (errDecision as PremiumAccessDecision.Locked).reason)
    }

    @Test
    fun viewModel_premiumEntitlementState_reactsLiveToFirestoreSnapshotUpdates() = runTest(testDispatcher) {
        val futureExpiry = System.currentTimeMillis() + 30L * 86_400_000L
        val fakeFirestoreSettings = MutableStateFlow<UserSettings?>(
            UserSettings(userId = "user_a", isPro = false, proTier = "free")
        )
        val fakePendingVerification = MutableStateFlow(false)

        val vm = KharcheeViewModel(
            repository = repository,
            currentUserId = "user_a",
            entitlementSettingsFlowOverride = fakeFirestoreSettings,
            pendingVerificationFlowOverride = fakePendingVerification
        )

        // Starts locked on Free tier
        val initial = vm.premiumEntitlementState.first { it.isSyncedFromFirestore }
        assertFalse(initial.isProActive)
        assertFalse(initial.isFeatureUnlocked(PremiumFeature.ADVANCED_ANALYTICS))

        // Pending payment verification submitted
        fakePendingVerification.value = true
        advanceUntilIdle()
        val pending = vm.premiumEntitlementState.first { it.hasPendingVerification }
        assertFalse(pending.isProActive)
        assertEquals("PENDING VERIFICATION", pending.statusBadgeLabel())

        // Cloud Function verifies payment and updates /users/{uid} in Firestore
        fakeFirestoreSettings.value = UserSettings(
            userId = "user_a",
            isPro = true,
            proTier = "gold",
            proExpiryMillis = futureExpiry
        )
        fakePendingVerification.value = false
        advanceUntilIdle()
        val unlocked = vm.premiumEntitlementState.first { it.isProActive }
        assertTrue(unlocked.isFeatureUnlocked(PremiumFeature.ADVANCED_ANALYTICS))
        assertTrue(unlocked.isFeatureUnlocked(PremiumFeature.REPORT_EXPORTS))
        assertTrue(unlocked.isFeatureUnlocked(PremiumFeature.CUSTOM_DATE_FILTERS))
    }

    @Test
    fun premiumFeatureGateUi_showsUpgradeToProForFreeUserAndUnlocksForProUser() {
        var upgradeClickedCount = 0
        val freeEntitlement = PremiumEntitlementEvaluator.evaluate(
            remoteSettings = UserSettings(userId = "u1", isPro = false, proTier = "free"),
            isSyncedFromFirestore = true
        )

        composeTestRule.setContent {
            PremiumFeatureGate(
                feature = PremiumFeature.ADVANCED_ANALYTICS,
                entitlementState = freeEntitlement,
                onUpgradeToPro = { upgradeClickedCount++ }
            ) {
                Text(
                    text = "UNLOCKED_ADVANCED_ANALYTICS_BODY",
                    modifier = Modifier.testTag("unlocked_analytics_content")
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("premium_gate_card_advanced_analytics").assertIsDisplayed()
        composeTestRule.onNodeWithTag("upgrade_to_pro_cta_advanced_analytics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Upgrade to Pro").assertIsDisplayed()
        composeTestRule.onNodeWithTag("unlocked_analytics_content").assertDoesNotExist()

        composeTestRule.onNodeWithTag("upgrade_to_pro_cta_advanced_analytics").performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, upgradeClickedCount)
    }

    @Test
    fun premiumFeatureGateUi_rendersUnlockedContentWhenFirestoreConfirmsActivePro() {
        val activeProEntitlement = PremiumEntitlementEvaluator.evaluate(
            remoteSettings = UserSettings(
                userId = "u1",
                isPro = true,
                proTier = "platinum",
                proExpiryMillis = 0L
            ),
            isSyncedFromFirestore = true
        )

        composeTestRule.setContent {
            PremiumFeatureGate(
                feature = PremiumFeature.ADVANCED_ANALYTICS,
                entitlementState = activeProEntitlement,
                onUpgradeToPro = {}
            ) {
                Text(
                    text = "UNLOCKED_ADVANCED_ANALYTICS_BODY",
                    modifier = Modifier.testTag("unlocked_analytics_content")
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("unlocked_analytics_content").assertIsDisplayed()
        composeTestRule.onNodeWithTag("premium_gate_card_advanced_analytics").assertDoesNotExist()
    }
}
