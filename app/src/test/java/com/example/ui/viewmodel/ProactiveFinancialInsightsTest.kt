package com.example.ui.viewmodel

import com.example.data.model.ExpenseRecord
import com.example.data.model.JarvisChatMessage
import com.example.data.model.KhataFriendRecord
import com.example.data.model.UserSettings
import com.example.ui.jarvis.parseNaturalLanguageActionProposal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProactiveFinancialInsightsTest {

    @Test
    fun emptyState_whenLessThanThreeOutflows_emitsStarterGuide() {
        val settings = UserSettings(dailyLimit = 500L)
        val insights = KharcheeViewModel.generateProactiveFinancialInsights(
            expenses = emptyList(),
            settings = settings,
            khata = emptyList()
        )

        assertTrue(insights.any { it.id == "empty_starter" })
        val starter = insights.first { it.id == "empty_starter" }
        assertEquals("Guide", starter.category)
        assertTrue(starter.message.contains("3+ UPI or cash expenses"))
    }

    @Test
    fun dailyLimitBreach_whenTodaySpentExceedsLimit_emitsWarningInsight() {
        val now = System.currentTimeMillis()
        val settings = UserSettings(dailyLimit = 400L)
        val expenses = listOf(
            ExpenseRecord(id = "1", title = "Lunch", amount = 300L, category = "Food", txEpochMillis = now),
            ExpenseRecord(id = "2", title = "Dinner", amount = 250L, category = "Food", txEpochMillis = now),
            ExpenseRecord(id = "3", title = "Groceries", amount = 100L, category = "Grocery", txEpochMillis = now - 2 * 86_400_000L)
        )

        val insights = KharcheeViewModel.generateProactiveFinancialInsights(
            expenses = expenses,
            settings = settings
        )

        val breach = insights.find { it.id == "daily_limit_exceeded" }
        assertNotNull("Expected daily limit breach insight", breach)
        assertEquals("warning", breach?.severity)
        assertTrue(breach!!.message.contains("above your ₹400 daily ceiling"))
        assertTrue(breach.suggestedPrompt.contains("overspent my daily limit"))
    }

    @Test
    fun foodSpendSurge_whenWeeklyFoodIncreasesBy15Percent_emitsFoodSurgeInsight() {
        val now = System.currentTimeMillis()
        val twoDaysAgo = now - 2 * 86_400_000L
        val tenDaysAgo = now - 10 * 86_400_000L

        val expenses = listOf(
            // This week: Food ₹1000
            ExpenseRecord(id = "1", title = "Biryani Feast", amount = 600L, category = "Food", txEpochMillis = now),
            ExpenseRecord(id = "2", title = "Office Lunch", amount = 400L, category = "Food", txEpochMillis = twoDaysAgo),
            // Last week: Food ₹500
            ExpenseRecord(id = "3", title = "Previous Week Food", amount = 500L, category = "Food", txEpochMillis = tenDaysAgo),
            ExpenseRecord(id = "4", title = "Metro Pass", amount = 300L, category = "Transport", txEpochMillis = tenDaysAgo)
        )

        val insights = KharcheeViewModel.generateProactiveFinancialInsights(
            expenses = expenses,
            settings = UserSettings(dailyLimit = 1500L)
        )

        val surge = insights.find { it.id == "food_surge" }
        assertNotNull("Expected food spend surge insight", surge)
        assertEquals("surge", surge?.severity)
        assertTrue(surge!!.title.contains("Food Spend Surged"))
        assertTrue(surge.suggestedPrompt.contains("Your food spending has increased this week. Want to see why?"))
    }

    @Test
    fun tapriStreak_whenTwoOrMoreChaiVisitsToday_emitsChaiInsight() {
        val now = System.currentTimeMillis()
        val expenses = listOf(
            ExpenseRecord(id = "1", title = "Morning Chai", amount = 20L, category = "Chai & Snacks", txEpochMillis = now),
            ExpenseRecord(id = "2", title = "Evening Cutting Chai & Bun Maska", amount = 40L, category = "Chai & Snacks", txEpochMillis = now),
            ExpenseRecord(id = "3", title = "Bus ticket", amount = 50L, category = "Transport", txEpochMillis = now - 86_400_000L)
        )

        val insights = KharcheeViewModel.generateProactiveFinancialInsights(
            expenses = expenses,
            settings = UserSettings(dailyLimit = 500L)
        )

        val chai = insights.find { it.id == "chai_frequency" }
        assertNotNull("Expected chai frequency insight", chai)
        assertTrue(chai!!.title.contains("Tapri Streak"))
        assertTrue(chai.suggestedPrompt.contains("chai/snacks stops today"))
    }

    @Test
    fun khataBalancePending_whenFriendsOweUser_emitsRecoveryPrompt() {
        val now = System.currentTimeMillis()
        val expenses = listOf(
            ExpenseRecord(id = "1", title = "Lunch", amount = 100L, category = "Food", txEpochMillis = now),
            ExpenseRecord(id = "2", title = "Snack", amount = 50L, category = "Food", txEpochMillis = now),
            ExpenseRecord(id = "3", title = "Fuel", amount = 200L, category = "Transport", txEpochMillis = now)
        )
        val khata = listOf(
            KhataFriendRecord(id = "k1", name = "Rohan", balance = 450L),
            KhataFriendRecord(id = "k2", name = "Aman", balance = -200L)
        )

        val insights = KharcheeViewModel.generateProactiveFinancialInsights(
            expenses = expenses,
            settings = UserSettings(dailyLimit = 800L),
            khata = khata
        )

        val khataInsight = insights.find { it.id == "khata_pending" }
        assertNotNull("Expected khata recovery insight", khataInsight)
        assertTrue(khataInsight!!.message.contains("1 friends owe you money"))
        assertTrue(khataInsight.suggestedPrompt.contains("remind them on WhatsApp"))
    }

    @Test
    fun proTierSettings_defaultsAndUpgrades_behaveCorrectly() {
        val defaultSettings = UserSettings(userId = "user_1")
        assertFalse(defaultSettings.isPro)
        assertEquals("free", defaultSettings.proTier)
        assertEquals(0L, defaultSettings.proExpiryMillis)

        val now = System.currentTimeMillis()
        val expiry30Days = now + 30 * 86_400_000L
        val upgradedSettings = defaultSettings.copy(
            isPro = true,
            proTier = "platinum",
            proExpiryMillis = expiry30Days
        )

        assertTrue(upgradedSettings.isPro)
        assertEquals("platinum", upgradedSettings.proTier)
        assertTrue(upgradedSettings.proExpiryMillis > now)
    }

    @Test
    fun conversationContextSummary_compressesOlderMessagesWhenOverEightTurns() {
        val shortHistory = (1..6).map { idx ->
            JarvisChatMessage(id = "$idx", role = if (idx % 2 == 1) "user" else "jarvis", text = "Message $idx")
        }
        assertEquals("", KharcheeViewModel.buildConversationContextSummary(shortHistory))

        val longHistory = (1..12).map { idx ->
            JarvisChatMessage(
                id = "$idx",
                role = if (idx % 2 == 1) "user" else "jarvis",
                text = if (idx == 1) "How much did I spend on Swiggy last week?" else "Turn $idx content"
            )
        }
        val summary = KharcheeViewModel.buildConversationContextSummary(longHistory)
        assertTrue(summary.contains("Earlier in this conversation"))
        assertTrue(summary.contains("How much did I spend on Swiggy last week?"))
    }

    @Test
    fun naturalLanguageActionProposal_detectsExpenseLimitAndNavigationActions() {
        val expenseProposal = parseNaturalLanguageActionProposal("Log ₹120 for Masala Chai")
        assertNotNull(expenseProposal)
        assertEquals("LOG_EXPENSE", expenseProposal?.actionType)
        assertEquals(120L, expenseProposal?.amount)
        assertEquals("Chai & Snacks", expenseProposal?.category)

        val limitProposal = parseNaturalLanguageActionProposal("Set daily limit to ₹950")
        assertNotNull(limitProposal)
        assertEquals("SET_DAILY_LIMIT", limitProposal?.actionType)
        assertEquals(950L, limitProposal?.amount)

        val navProposal = parseNaturalLanguageActionProposal("Open Khata split ledger")
        assertNotNull(navProposal)
        assertEquals("NAVIGATE_TAB", navProposal?.actionType)
        assertEquals("khata", navProposal?.targetTabId)
    }
}
