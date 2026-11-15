package com.example.util

import android.content.Context
import com.example.data.model.SavingsGoalRecord
import org.json.JSONArray
import org.json.JSONObject

data class SpareChangeSweepEvent(
    val id: String,
    val expenseTitle: String,
    val expenseAmount: Long,
    val roundedToAmount: Long,
    val sweptDifference: Long,
    val targetGoalId: String,
    val targetGoalTitle: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

object SavingsGoalsManager {
    private const val PREFS_NAME = "kharchee_savings_goals_auto_sweep_prefs"
    const val FREE_TIER_MAX_SAVINGS_GOALS = 3

    /**
     * Sovereign Pro users have UNLIMITED savings goals (iPhone Fund, Trip, Emergency, MacBook, etc.).
     * Free tier supports up to 3 active goals.
     */
    fun canCreateNewSavingsGoal(currentGoalsCount: Int, isSovereignPro: Boolean): Boolean {
        if (isSovereignPro) return true
        return currentGoalsCount < FREE_TIER_MAX_SAVINGS_GOALS
    }

    fun getMaxGoalsAllowedLabel(isSovereignPro: Boolean): String {
        return if (isSovereignPro) "∞ Unlimited Sovereign Goals" else "Up to $FREE_TIER_MAX_SAVINGS_GOALS Free Goals"
    }

    /**
     * Computes the spare-change round-up difference for a logged expense.
     * Example:
     * - Expense = ₹120, roundToNearest = 50 -> rounds up to ₹150 -> sweeps ₹30
     * - Expense = ₹120, roundToNearest = 100 -> rounds up to ₹200 -> sweeps ₹80
     * - Expense = ₹35, roundToNearest = 50 -> rounds up to ₹50 -> sweeps ₹15
     * - If expense is already an exact multiple (e.g. ₹100 with nearest ₹50), sweeps a ₹10 micro-save boost or 0 based on strictRoundUp.
     */
    fun computeSpareChangeRoundUp(
        expenseAmount: Long,
        roundToNearest: Long = 50L,
        sweepFixedBoostWhenExactMultiple: Boolean = false
    ): Long {
        if (expenseAmount <= 0L) return 0L
        val step = if (roundToNearest == 100L) 100L else 50L
        val remainder = expenseAmount % step
        return if (remainder == 0L) {
            if (sweepFixedBoostWhenExactMultiple) 10L else 0L
        } else {
            step - remainder
        }
    }

    fun isSpareChangeAutoSweepEnabled(context: Context, userId: String = "default"): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("auto_sweep_enabled_$userId", false)
    }

    fun setSpareChangeAutoSweepEnabled(
        context: Context,
        userId: String = "default",
        enabled: Boolean
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("auto_sweep_enabled_$userId", enabled)
            .apply()
    }

    fun getRoundUpIncrement(context: Context, userId: String = "default"): Long {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong("auto_sweep_increment_$userId", 50L)
        return if (raw == 100L) 100L else 50L
    }

    fun setRoundUpIncrement(
        context: Context,
        userId: String = "default",
        roundToNearest: Long
    ) {
        val clean = if (roundToNearest == 100L) 100L else 50L
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong("auto_sweep_increment_$userId", clean)
            .apply()
    }

    fun getActiveSweepGoalId(context: Context, userId: String = "default"): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("active_sweep_goal_id_$userId", "") ?: ""
    }

    fun setActiveSweepGoalId(context: Context, userId: String = "default", goalId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("active_sweep_goal_id_$userId", goalId)
            .apply()
    }

    fun resolveTargetSweepGoal(
        context: Context,
        userId: String,
        goals: List<SavingsGoalRecord>
    ): SavingsGoalRecord? {
        if (goals.isEmpty()) return null
        val preferredId = getActiveSweepGoalId(context, userId)
        return goals.firstOrNull { it.id == preferredId }
            ?: goals.firstOrNull { it.savedAmount < it.targetAmount }
            ?: goals.firstOrNull()
    }

    fun recordSpareChangeSweep(
        context: Context,
        userId: String,
        expenseTitle: String,
        expenseAmount: Long,
        sweptAmount: Long,
        targetGoal: SavingsGoalRecord
    ): SpareChangeSweepEvent {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val prevTotal = prefs.getLong("total_swept_inr_$userId", 0L)
        val prevCount = prefs.getInt("total_swept_count_$userId", 0)
        val event = SpareChangeSweepEvent(
            id = "sweep_${System.currentTimeMillis()}",
            expenseTitle = expenseTitle,
            expenseAmount = expenseAmount,
            roundedToAmount = expenseAmount + sweptAmount,
            sweptDifference = sweptAmount,
            targetGoalId = targetGoal.id,
            targetGoalTitle = targetGoal.title
        )

        val history = getRecentSweepHistory(context, userId).toMutableList()
        history.add(0, event)
        val trimmed = history.take(25)
        val arr = JSONArray()
        trimmed.forEach { ev ->
            arr.put(
                JSONObject().apply {
                    put("id", ev.id)
                    put("expenseTitle", ev.expenseTitle)
                    put("expenseAmount", ev.expenseAmount)
                    put("roundedToAmount", ev.roundedToAmount)
                    put("sweptDifference", ev.sweptDifference)
                    put("targetGoalId", ev.targetGoalId)
                    put("targetGoalTitle", ev.targetGoalTitle)
                    put("timestampMillis", ev.timestampMillis)
                }
            )
        }

        prefs.edit()
            .putLong("total_swept_inr_$userId", prevTotal + sweptAmount)
            .putInt("total_swept_count_$userId", prevCount + 1)
            .putString("sweep_history_json_$userId", arr.toString())
            .apply()

        return event
    }

    fun getTotalSpareChangeSweptInr(context: Context, userId: String = "default"): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong("total_swept_inr_$userId", 0L)
    }

    fun getTotalSpareChangeSweptCount(context: Context, userId: String = "default"): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt("total_swept_count_$userId", 0)
    }

    fun getRecentSweepHistory(context: Context, userId: String = "default"): List<SpareChangeSweepEvent> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("sweep_history_json_$userId", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<SpareChangeSweepEvent>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                list.add(
                    SpareChangeSweepEvent(
                        id = obj.optString("id", "sweep_$i"),
                        expenseTitle = obj.optString("expenseTitle", "Spend"),
                        expenseAmount = obj.optLong("expenseAmount", 0L),
                        roundedToAmount = obj.optLong("roundedToAmount", 0L),
                        sweptDifference = obj.optLong("sweptDifference", 0L),
                        targetGoalId = obj.optString("targetGoalId", ""),
                        targetGoalTitle = obj.optString("targetGoalTitle", "Goal"),
                        timestampMillis = obj.optLong("timestampMillis", 0L)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }
}
