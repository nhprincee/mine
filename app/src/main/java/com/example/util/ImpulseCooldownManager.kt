package com.example.util

import android.content.Context
import java.util.Calendar
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

data class PendingImpulseFreezeItem(
    val id: String,
    val title: String,
    val category: String,
    val amount: Long,
    val paymentMode: String,
    val note: String,
    val createdAtMillis: Long,
    val unlockAtMillis: Long,
    val jarvisWarningMessage: String
) {
    fun remainingSeconds(nowMillis: Long = System.currentTimeMillis()): Long {
        return ((unlockAtMillis - nowMillis) / 1000L).coerceAtLeast(0L)
    }

    fun isCooldownExpired(nowMillis: Long = System.currentTimeMillis()): Boolean {
        return nowMillis >= unlockAtMillis
    }

    fun formattedCountdown(nowMillis: Long = System.currentTimeMillis()): String {
        val remSec = remainingSeconds(nowMillis)
        val mins = remSec / 60L
        val secs = remSec % 60L
        return String.format(Locale.ENGLISH, "%02d:%02d", mins, secs)
    }
}

/**
 * Impulse-Buy Cooldown Lock ("The Shame Vault"):
 * Protects users from late-night (>10 PM) or high-ticket (>₹500) impulse buys in Shopping & Entertainment
 * by holding the transaction in a 15-minute "Pending Freeze" state with a live countdown timer and Jarvis warning.
 */
object ImpulseCooldownManager {

    private const val PREFS_NAME = "kharchee_impulse_shame_vault_prefs"
    private const val KEY_FROZEN_ITEMS_JSON = "frozen_impulse_items_json"
    private const val KEY_SAVED_BY_COOLDOWN_INR = "total_saved_by_cooldown_inr"
    const val COOLDOWN_DURATION_MILLIS = 15 * 60 * 1000L // 15 Minutes

    /**
     * Checks whether the category is a target impulse category (Shopping or Entertainment) and amount > ₹500.
     */
    fun isImpulseTargetCategoryAndAmount(amount: Long, category: String): Boolean {
        if (amount <= 500L) return false
        return category.equals("Shopping", ignoreCase = true) ||
            category.equals("Entertainment", ignoreCase = true)
    }

    /**
     * Checks whether the given 24-hour clock hour is in the late-night impulse window (after 10 PM / 22:00..04:59).
     */
    fun isLateNightImpulseHour(hourOfDay24: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): Boolean {
        return hourOfDay24 >= 22 || hourOfDay24 < 5
    }

    /**
     * Generates a sharp, contextual Jarvis Shame Vault warning message for the pending freeze state.
     */
    fun buildJarvisShameVaultWarning(
        title: String,
        category: String,
        amount: Long,
        isAfter10Pm: Boolean
    ): String {
        val cleanTitle = title.trim().ifBlank { category }
        val timeContext = if (isAfter10Pm) "after 10 PM" else "right now"
        return "🤖 JARVIS SHAME VAULT: Hold on! Spending ₹$amount on '$cleanTitle' ($category) $timeContext has an 87% probability of being a dopamine impulse buy. This transaction is frozen in Pending Freeze for 15 minutes — walk away, drink water, and cancel if you don't truly need it."
    }

    fun freezeImpulseTransaction(
        context: Context,
        title: String,
        category: String,
        amount: Long,
        paymentMode: String,
        note: String,
        nowMillis: Long = System.currentTimeMillis(),
        cooldownDurationMillis: Long = COOLDOWN_DURATION_MILLIS
    ): PendingImpulseFreezeItem {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val isLateNight = isLateNightImpulseHour(cal.get(Calendar.HOUR_OF_DAY))
        val item = PendingImpulseFreezeItem(
            id = "freeze_$nowMillis",
            title = title.trim().ifBlank { category },
            category = category,
            amount = amount,
            paymentMode = paymentMode,
            note = note,
            createdAtMillis = nowMillis,
            unlockAtMillis = nowMillis + cooldownDurationMillis,
            jarvisWarningMessage = buildJarvisShameVaultWarning(
                title = title,
                category = category,
                amount = amount,
                isAfter10Pm = isLateNight
            )
        )
        val updated = (listOf(item) + getPendingFrozenItems(context)).take(20)
        savePendingFrozenItems(context, updated)
        return item
    }

    fun getPendingFrozenItems(context: Context): List<PendingImpulseFreezeItem> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FROZEN_ITEMS_JSON, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<PendingImpulseFreezeItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                list.add(
                    PendingImpulseFreezeItem(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        category = obj.optString("category", "Shopping"),
                        amount = obj.optLong("amount", 0L),
                        paymentMode = obj.optString("paymentMode", "UPI"),
                        note = obj.optString("note", ""),
                        createdAtMillis = obj.optLong("createdAtMillis", 0L),
                        unlockAtMillis = obj.optLong("unlockAtMillis", 0L),
                        jarvisWarningMessage = obj.optString("jarvisWarningMessage", "")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun cancelFrozenImpulseAndRecordSavings(
        context: Context,
        freezeId: String
    ): Long {
        val current = getPendingFrozenItems(context)
        val target = current.firstOrNull { it.id == freezeId }
        val remaining = current.filterNot { it.id == freezeId }
        savePendingFrozenItems(context, remaining)
        val savedAmount = target?.amount ?: 0L
        if (savedAmount > 0L) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val prev = prefs.getLong(KEY_SAVED_BY_COOLDOWN_INR, 0L)
            prefs.edit().putLong(KEY_SAVED_BY_COOLDOWN_INR, prev + savedAmount).apply()
        }
        return savedAmount
    }

    fun removeFrozenItemAfterConfirm(context: Context, freezeId: String) {
        val remaining = getPendingFrozenItems(context).filterNot { it.id == freezeId }
        savePendingFrozenItems(context, remaining)
    }

    fun getTotalSavedByCooldownInr(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_SAVED_BY_COOLDOWN_INR, 0L)
    }

    private fun savePendingFrozenItems(context: Context, list: List<PendingImpulseFreezeItem>) {
        val arr = JSONArray()
        list.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("category", item.category)
                    put("amount", item.amount)
                    put("paymentMode", item.paymentMode)
                    put("note", item.note)
                    put("createdAtMillis", item.createdAtMillis)
                    put("unlockAtMillis", item.unlockAtMillis)
                    put("jarvisWarningMessage", item.jarvisWarningMessage)
                }
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FROZEN_ITEMS_JSON, arr.toString())
            .apply()
    }
}
