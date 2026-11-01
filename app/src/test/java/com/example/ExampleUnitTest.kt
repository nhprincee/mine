package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ExpenseRecord
import com.example.data.model.NightGuardChallengeEngine
import com.example.data.model.NightGuardCooldownStore
import com.example.data.model.NightGuardEventRecord
import com.example.data.model.NightGuardSchedule
import com.example.data.model.NightSpendingAnalytics
import com.example.data.model.UserSettings
import com.example.ui.auth.normalizeE164PhoneNumber
import com.example.ui.theme.DEFAULT_FONT_ID
import com.example.ui.theme.FontPreferenceManager
import com.example.ui.theme.getAppFontOptionById
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {

    @Test
    fun defaultFont_newUser_isManropeExtraBoldAndInter() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("kharchee_font_preferences", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        val resolved = FontPreferenceManager.ensureInitialized(context)
        assertEquals("manrope", resolved)
        assertEquals("Manrope ExtraBold + Inter", getAppFontOptionById(DEFAULT_FONT_ID).name)
    }

    @Test
    fun existingUserSavedFontPreference_isNotOverwritten() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("kharchee_font_preferences", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_font_id", "outfit").commit()

        val resolved = FontPreferenceManager.ensureInitialized(context)
        assertEquals("outfit", resolved)
        assertEquals("Outfit", getAppFontOptionById(resolved).name)
    }

    @Test
    fun normalizeE164PhoneNumber_validatesIndianAndInternationalNumbers() {
        assertEquals("+919876543210", normalizeE164PhoneNumber("98765 43210"))
        assertEquals("+919876543210", normalizeE164PhoneNumber("09876543210"))
        assertEquals("+919876543210", normalizeE164PhoneNumber("+91-98765-43210"))
        assertNull(normalizeE164PhoneNumber("12345"))
        assertNull(normalizeE164PhoneNumber("abcdefghij"))
    }

    @Test
    fun nightGuardSchedule_overnightWindow_detectsLateNightAndEarlyMorning() {
        val cal2330 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 30)
        }
        val cal0215 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 2)
            set(Calendar.MINUTE, 15)
        }
        val cal1400 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 0)
        }

        assertTrue(NightGuardSchedule.isWithinNightGuardWindow(cal2330.timeInMillis, 22, 0, 5, 0))
        assertTrue(NightGuardSchedule.isWithinNightGuardWindow(cal0215.timeInMillis, 22, 0, 5, 0))
        assertFalse(NightGuardSchedule.isWithinNightGuardWindow(cal1400.timeInMillis, 22, 0, 5, 0))
    }

    @Test
    fun nightGuardChallengeEngine_hasAllFiveCategoriesAndAvoidsImmediateRepeats() {
        val allCategories = NightGuardChallengeEngine.CHALLENGE_CATEGORIES.toSet()
        val presentCategories = NightGuardChallengeEngine.getAllChallenges().map { it.category }.toSet()
        assertEquals(allCategories, presentCategories)

        val recentIds = mutableSetOf<String>()
        repeat(10) {
            val next = NightGuardChallengeEngine.pickNextChallenge(excludeIds = recentIds)
            assertFalse("Should not immediately repeat excluded challenge", recentIds.contains(next.id))
            recentIds.add(next.id)
            assertTrue(NightGuardChallengeEngine.validateAnswer(next, next.correctAnswer))
            val wrongAnswer = next.options.first { it != next.correctAnswer }
            assertFalse(NightGuardChallengeEngine.validateAnswer(next, wrongAnswer))
        }
    }

    @Test
    fun nightGuardCooldownStore_persistsFiveMinuteWallClockCooldown() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val userId = "test_cooldown_user"

        val now = 1_700_000_000_000L
        val endMillis = now + NightGuardChallengeEngine.FIVE_MINUTES_MS
        NightGuardCooldownStore.saveLocalCooldownEndMillis(context, userId, endMillis)

        val resolvedEnd = NightGuardCooldownStore.resolveEffectiveCooldownEndMillis(
            context = context,
            userId = userId,
            firestoreCooldownEndMillis = 0L
        )
        assertEquals(endMillis, resolvedEnd)

        val remainingHalfway = NightGuardCooldownStore.getRemainingCooldownMillis(
            nowMillis = now + 120_000L,
            effectiveCooldownEndMillis = resolvedEnd
        )
        assertEquals(180_000L, remainingHalfway)
        assertEquals("03:00", NightGuardCooldownStore.formatRemainingCooldown(remainingHalfway))
    }

    @Test
    fun nightSpendingAnalytics_computesStrictSavingsFromCancelledEventsOnly() {
        val settings = UserSettings(
            nightGuardEnabled = true,
            nightGuardStartHour = 22,
            nightGuardStartMinute = 0,
            nightGuardEndHour = 5,
            nightGuardEndMinute = 0
        )
        val nightCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 15)
        }
        val dayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 13)
            set(Calendar.MINUTE, 0)
        }

        val expenses = listOf(
            ExpenseRecord(id = "1", title = "Late Zomato", amount = 650L, category = "Food", type = "expense", txEpochMillis = nightCal.timeInMillis),
            ExpenseRecord(id = "2", title = "Lunch", amount = 300L, category = "Food", type = "expense", txEpochMillis = dayCal.timeInMillis)
        )
        val events = listOf(
            NightGuardEventRecord(id = "e1", title = "Gaming Skin", amount = 1200L, outcome = "cancelled", eventEpochMillis = nightCal.timeInMillis),
            NightGuardEventRecord(id = "e2", title = "Sneakers", amount = 2500L, outcome = "postponed", eventEpochMillis = nightCal.timeInMillis),
            NightGuardEventRecord(id = "e3", title = "Late Zomato", amount = 650L, outcome = "proceeded", eventEpochMillis = nightCal.timeInMillis)
        )

        val summary = NightSpendingAnalytics.computeSummary(
            expenses = expenses,
            nightGuardEvents = events,
            settings = settings,
            nowMillis = nightCal.timeInMillis + 60_000L
        )

        assertNotNull(summary)
        assertEquals(650L, summary.totalNightSpentAllTime)
        assertEquals(1, summary.cancelledCount)
        assertEquals(1, summary.postponedCount)
        assertEquals(1200L, summary.savedFromCancelledAmount)
        assertEquals(2500L, summary.postponedAmount)
    }
}

