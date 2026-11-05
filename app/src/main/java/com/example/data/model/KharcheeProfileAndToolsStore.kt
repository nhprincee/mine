package com.example.data.model

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

data class UserProfileMetadata(
    val userId: String,
    val customDisplayName: String = "",
    val email: String = "",
    val phone: String = "",
    val profilePhotoUri: String = "",
    val monthlyBudget: Long = 15000L,
    val primaryWallet: String = "UPI",
    val accountCreatedDate: String = "01 Nov 2026",
    val vaultPinProtectionEnabled: Boolean = true,
    val sharePrivacyHideBalances: Boolean = true
)

data class SubscriptionItem(
    val id: String,
    val name: String,
    val amount: Long,
    val billingCycle: String = "Monthly", // "Monthly", "Yearly"
    val nextRenewalDate: String = "05 Nov",
    val category: String = "Entertainment",
    val paymentMethod: String = "UPI",
    val isActive: Boolean = true,
    val reminderEnabled: Boolean = true
)

data class EmiItem(
    val id: String,
    val title: String,
    val monthlyEmi: Long,
    val remainingMonths: Int,
    val totalPrincipal: Long
)

data class WalletBalanceItem(
    val walletName: String, // "UPI", "Bank Account", "Cash", "Card"
    val manualBalance: Long,
    val isManualEntry: Boolean = true
)

data class GoalContributionItem(
    val dateLabel: String,
    val amount: Long,
    val note: String = "Vault Deposit"
)

data class KharcheeStreakLevelInfo(
    val levelName: String, // "Bronze", "Silver", "Gold", "Platinum", "Diamond"
    val minDays: Int,
    val nextLevelName: String,
    val nextLevelTargetDays: Int,
    val progressRatio: Float
)

object KharcheeProfileAndToolsStore {
    private const val PREFS_NAME = "kharchee_profile_and_tools_store"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun computeStreakLevel(streakDays: Int, snapCount: Int = 0): KharcheeStreakLevelInfo {
        val effectivePoints = streakDays + (snapCount / 2)
        return when {
            effectivePoints >= 60 -> KharcheeStreakLevelInfo(
                levelName = "Diamond",
                minDays = 60,
                nextLevelName = "Diamond Sovereign",
                nextLevelTargetDays = 90,
                progressRatio = 1f
            )
            effectivePoints >= 30 -> KharcheeStreakLevelInfo(
                levelName = "Platinum",
                minDays = 30,
                nextLevelName = "Diamond",
                nextLevelTargetDays = 60,
                progressRatio = ((effectivePoints - 30) / 30f).coerceIn(0f, 1f)
            )
            effectivePoints >= 14 -> KharcheeStreakLevelInfo(
                levelName = "Gold",
                minDays = 14,
                nextLevelName = "Platinum",
                nextLevelTargetDays = 30,
                progressRatio = ((effectivePoints - 14) / 16f).coerceIn(0f, 1f)
            )
            effectivePoints >= 7 -> KharcheeStreakLevelInfo(
                levelName = "Silver",
                minDays = 7,
                nextLevelName = "Gold",
                nextLevelTargetDays = 14,
                progressRatio = ((effectivePoints - 7) / 7f).coerceIn(0f, 1f)
            )
            else -> KharcheeStreakLevelInfo(
                levelName = "Bronze",
                minDays = 0,
                nextLevelName = "Silver",
                nextLevelTargetDays = 7,
                progressRatio = (effectivePoints / 7f).coerceIn(0.08f, 1f)
            )
        }
    }

    fun getProfileMetadata(
        context: Context,
        userId: String,
        defaultName: String,
        defaultEmail: String = "",
        defaultPhone: String = "",
        dailyLimit: Long = 500L,
        creationTimestampMillis: Long = 0L
    ): UserProfileMetadata {
        val p = prefs(context)
        val savedName = p.getString("${userId}_display_name", null)?.takeIf { it.isNotBlank() } ?: defaultName
        val savedEmail = p.getString("${userId}_email", null)?.takeIf { it.isNotBlank() } ?: defaultEmail
        val savedPhone = p.getString("${userId}_phone", null)?.takeIf { it.isNotBlank() } ?: defaultPhone
        val savedPhoto = p.getString("${userId}_photo_uri", "") ?: ""
        val savedMonthlyBudget = p.getLong("${userId}_monthly_budget", (dailyLimit * 30L).coerceAtLeast(3000L))
        val savedWallet = p.getString("${userId}_primary_wallet", "UPI") ?: "UPI"

        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val existingCreatedDate = p.getString("${userId}_created_date", null)
        val resolvedCreatedDate = when {
            !existingCreatedDate.isNullOrBlank() -> existingCreatedDate
            creationTimestampMillis > 0L -> dateFmt.format(Date(creationTimestampMillis))
            else -> dateFmt.format(Date(System.currentTimeMillis()))
        }
        if (existingCreatedDate.isNullOrBlank()) {
            p.edit().putString("${userId}_created_date", resolvedCreatedDate).apply()
        }

        val pinEnabled = p.getBoolean("${userId}_vault_pin_enabled", true)
        val hideBalancesOnShare = p.getBoolean("${userId}_share_hide_balances", true)

        return UserProfileMetadata(
            userId = userId,
            customDisplayName = savedName,
            email = savedEmail,
            phone = savedPhone,
            profilePhotoUri = savedPhoto,
            monthlyBudget = savedMonthlyBudget,
            primaryWallet = savedWallet,
            accountCreatedDate = resolvedCreatedDate,
            vaultPinProtectionEnabled = pinEnabled,
            sharePrivacyHideBalances = hideBalancesOnShare
        )
    }

    fun saveProfileMetadata(
        context: Context,
        userId: String,
        displayName: String,
        phone: String,
        profilePhotoUri: String,
        monthlyBudget: Long,
        primaryWallet: String,
        vaultPinProtectionEnabled: Boolean = true,
        sharePrivacyHideBalances: Boolean = true
    ) {
        prefs(context).edit()
            .putString("${userId}_display_name", displayName.trim())
            .putString("${userId}_phone", phone.trim())
            .putString("${userId}_photo_uri", profilePhotoUri.trim())
            .putLong("${userId}_monthly_budget", monthlyBudget.coerceAtLeast(1000L))
            .putString("${userId}_primary_wallet", primaryWallet)
            .putBoolean("${userId}_vault_pin_enabled", vaultPinProtectionEnabled)
            .putBoolean("${userId}_share_hide_balances", sharePrivacyHideBalances)
            .apply()
    }

    // SNAP & TRACK scan count & Streak Freeze
    fun getSnapScanCount(context: Context, userId: String): Int {
        return prefs(context).getInt("${userId}_snap_scan_count", 0)
    }

    fun incrementSnapScanCount(context: Context, userId: String): Int {
        val next = getSnapScanCount(context, userId) + 1
        prefs(context).edit().putInt("${userId}_snap_scan_count", next).apply()
        return next
    }

    fun isStreakFreezeEnabled(context: Context, userId: String): Boolean {
        return prefs(context).getBoolean("${userId}_streak_freeze_enabled", true)
    }

    fun setStreakFreezeEnabled(context: Context, userId: String, enabled: Boolean) {
        prefs(context).edit().putBoolean("${userId}_streak_freeze_enabled", enabled).apply()
    }

    // Category Budgets (for Smart Budget Coach)
    fun getCategoryBudgets(context: Context, userId: String): Map<String, Long> {
        val raw = prefs(context).getString("${userId}_category_budgets", null)
        val defaults = mutableMapOf(
            "Food" to 4500L,
            "Chai & Snacks" to 1200L,
            "Transport" to 2500L,
            "Grocery" to 3500L,
            "Entertainment" to 1500L,
            "Bills" to 3000L,
            "Shopping" to 2500L
        )
        if (raw.isNullOrBlank()) return defaults
        return try {
            val obj = JSONObject(raw)
            EXPENSE_CATEGORIES.forEach { cat ->
                if (obj.has(cat)) {
                    defaults[cat] = obj.optLong(cat, defaults[cat] ?: 2000L)
                }
            }
            defaults
        } catch (_: Exception) {
            defaults
        }
    }

    fun saveCategoryBudget(context: Context, userId: String, category: String, amount: Long): Map<String, Long> {
        val current = getCategoryBudgets(context, userId).toMutableMap()
        current[category] = amount.coerceAtLeast(100L)
        val obj = JSONObject()
        current.forEach { (k, v) -> obj.put(k, v) }
        prefs(context).edit().putString("${userId}_category_budgets", obj.toString()).apply()
        return current
    }

    // Subscriptions (Subscription Radar)
    fun getSubscriptions(context: Context, userId: String): List<SubscriptionItem> {
        val raw = prefs(context).getString("${userId}_subscriptions", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        SubscriptionItem(
                            id = o.optString("id"),
                            name = o.optString("name"),
                            amount = o.optLong("amount", 199L),
                            billingCycle = o.optString("billingCycle", "Monthly"),
                            nextRenewalDate = o.optString("nextRenewalDate", "05 Nov"),
                            category = o.optString("category", "Entertainment"),
                            paymentMethod = o.optString("paymentMethod", "UPI"),
                            isActive = o.optBoolean("isActive", true),
                            reminderEnabled = o.optBoolean("reminderEnabled", true)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addSubscription(context: Context, userId: String, item: SubscriptionItem): List<SubscriptionItem> {
        val updated = getSubscriptions(context, userId) + item
        saveSubscriptions(context, userId, updated)
        return updated
    }

    fun toggleSubscriptionActive(context: Context, userId: String, id: String): List<SubscriptionItem> {
        val updated = getSubscriptions(context, userId).map {
            if (it.id == id) it.copy(isActive = !it.isActive) else it
        }
        saveSubscriptions(context, userId, updated)
        return updated
    }

    fun toggleSubscriptionReminder(context: Context, userId: String, id: String): List<SubscriptionItem> {
        val updated = getSubscriptions(context, userId).map {
            if (it.id == id) it.copy(reminderEnabled = !it.reminderEnabled) else it
        }
        saveSubscriptions(context, userId, updated)
        return updated
    }

    fun removeSubscription(context: Context, userId: String, id: String): List<SubscriptionItem> {
        val updated = getSubscriptions(context, userId).filterNot { it.id == id }
        saveSubscriptions(context, userId, updated)
        return updated
    }

    private fun saveSubscriptions(context: Context, userId: String, list: List<SubscriptionItem>) {
        val arr = JSONArray()
        list.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("amount", item.amount)
                    put("billingCycle", item.billingCycle)
                    put("nextRenewalDate", item.nextRenewalDate)
                    put("category", item.category)
                    put("paymentMethod", item.paymentMethod)
                    put("isActive", item.isActive)
                    put("reminderEnabled", item.reminderEnabled)
                }
            )
        }
        prefs(context).edit().putString("${userId}_subscriptions", arr.toString()).apply()
    }

    // EMIs
    fun getEmis(context: Context, userId: String): List<EmiItem> {
        val raw = prefs(context).getString("${userId}_emis", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        EmiItem(
                            id = o.optString("id"),
                            title = o.optString("title"),
                            monthlyEmi = o.optLong("monthlyEmi", 2500L),
                            remainingMonths = o.optInt("remainingMonths", 6),
                            totalPrincipal = o.optLong("totalPrincipal", 15000L)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addEmi(context: Context, userId: String, item: EmiItem): List<EmiItem> {
        val updated = getEmis(context, userId) + item
        saveEmis(context, userId, updated)
        return updated
    }

    fun removeEmi(context: Context, userId: String, id: String): List<EmiItem> {
        val updated = getEmis(context, userId).filterNot { it.id == id }
        saveEmis(context, userId, updated)
        return updated
    }

    private fun saveEmis(context: Context, userId: String, list: List<EmiItem>) {
        val arr = JSONArray()
        list.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("monthlyEmi", item.monthlyEmi)
                    put("remainingMonths", item.remainingMonths)
                    put("totalPrincipal", item.totalPrincipal)
                }
            )
        }
        prefs(context).edit().putString("${userId}_emis", arr.toString()).apply()
    }

    // Multi-Wallet Manual Balances & Emergency Fund Saved
    fun getManualWallets(context: Context, userId: String): List<WalletBalanceItem> {
        val p = prefs(context)
        return listOf(
            WalletBalanceItem("UPI Wallet", p.getLong("${userId}_wallet_UPI", 0L)),
            WalletBalanceItem("Bank Account", p.getLong("${userId}_wallet_Bank", 0L)),
            WalletBalanceItem("Cash Reserve", p.getLong("${userId}_wallet_Cash", 0L)),
            WalletBalanceItem("Card Limit / Credit", p.getLong("${userId}_wallet_Card", 0L))
        )
    }

    fun updateManualWallet(context: Context, userId: String, key: String, amount: Long) {
        prefs(context).edit().putLong("${userId}_wallet_$key", amount.coerceAtLeast(0L)).apply()
    }

    fun getEmergencyFundSaved(context: Context, userId: String): Long {
        return prefs(context).getLong("${userId}_emergency_fund_saved", 0L)
    }

    fun setEmergencyFundSaved(context: Context, userId: String, amount: Long) {
        prefs(context).edit().putLong("${userId}_emergency_fund_saved", amount.coerceAtLeast(0L)).apply()
    }

    // Goal Target Dates & Contribution History for Dream Vault
    fun getGoalTargetDate(context: Context, userId: String, goalTitle: String): String {
        return prefs(context).getString("${userId}_goal_date_${goalTitle.lowercase()}", "31 Dec 2026") ?: "31 Dec 2026"
    }

    fun saveGoalTargetDate(context: Context, userId: String, goalTitle: String, targetDate: String) {
        prefs(context).edit()
            .putString("${userId}_goal_date_${goalTitle.lowercase()}", targetDate.trim().ifBlank { "31 Dec 2026" })
            .apply()
    }

    fun getGoalContributions(context: Context, userId: String, goalTitle: String): List<GoalContributionItem> {
        val key = "${userId}_goal_contrib_${goalTitle.lowercase()}"
        val raw = prefs(context).getString(key, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        GoalContributionItem(
                            dateLabel = o.optString("dateLabel", "Today"),
                            amount = o.optLong("amount", 500L),
                            note = o.optString("note", "Vault Deposit")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun recordGoalContribution(
        context: Context,
        userId: String,
        goalTitle: String,
        amount: Long,
        note: String = "Dream Vault Contribution"
    ): List<GoalContributionItem> {
        val key = "${userId}_goal_contrib_${goalTitle.lowercase()}"
        val dateFmt = SimpleDateFormat("dd MMM · hh:mm a", Locale.ENGLISH)
        val item = GoalContributionItem(
            dateLabel = dateFmt.format(Date(System.currentTimeMillis())),
            amount = amount,
            note = note
        )
        val updated = listOf(item) + getGoalContributions(context, userId, goalTitle).take(14)
        val arr = JSONArray()
        updated.forEach { c ->
            arr.put(
                JSONObject().apply {
                    put("dateLabel", c.dateLabel)
                    put("amount", c.amount)
                    put("note", c.note)
                }
            )
        }
        prefs(context).edit().putString(key, arr.toString()).apply()
        return updated
    }

    fun clearAllForUser(context: Context, userId: String) {
        val p = prefs(context)
        val keysToRemove = p.all.keys.filter { it.startsWith("${userId}_") }
        val editor = p.edit()
        keysToRemove.forEach { editor.remove(it) }
        editor.apply()
    }
}
