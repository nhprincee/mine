package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.example.MainActivity
import com.example.R
import com.example.data.model.ParsedSmsTransaction
import com.example.receiver.AutoExpenseDetectionStore
import com.example.util.PerformancePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Interactive Obsidian Dark Android Home-Screen Widget (`KharcheeQuickWidgetProvider`).
 *
 * - Displays live Remaining Monthly Budget and Today's Spend directly on the user's phone home screen.
 * - Includes 3 instant action buttons right on the widget:
 *   1. `[+ ₹20 Chai]` — logs ₹20 Chai in the background without opening the app.
 *   2. `[+ ₹50 Auto]` — logs ₹50 Auto in the background without opening the app.
 *   3. `[+ Custom]` — opens Kharchee directly to the Custom Add Expense modal.
 */
class KharcheeQuickWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateSingleWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_QUICK_LOG_CHAI_20 -> {
                handleBackgroundQuickExpense(
                    context = context,
                    title = "Chai",
                    category = "Chai & Snacks",
                    amount = 20L,
                    toastText = "☕ Logged ₹20 Chai from Kharchee Widget"
                )
            }
            ACTION_QUICK_LOG_AUTO_50 -> {
                handleBackgroundQuickExpense(
                    context = context,
                    title = "Auto",
                    category = "Transport",
                    amount = 50L,
                    toastText = "🛺 Logged ₹50 Auto from Kharchee Widget"
                )
            }
        }
    }

    private fun handleBackgroundQuickExpense(
        context: Context,
        title: String,
        category: String,
        amount: Long,
        toastText: String
    ) {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentRemaining = prefs.getLong(KEY_REMAINING_BUDGET, 25_000L)
        val currentTodaySpend = prefs.getLong(KEY_TODAY_SPEND, 0L)
        val updatedRemaining = (currentRemaining - amount).coerceAtLeast(0L)
        val updatedTodaySpend = currentTodaySpend + amount

        prefs.edit()
            .putLong(KEY_REMAINING_BUDGET, updatedRemaining)
            .putLong(KEY_TODAY_SPEND, updatedTodaySpend)
            .putString(KEY_LAST_ACTION, "Logged +₹$amount $title ✓")
            .apply()

        refreshAllWidgets(appContext)
        Toast.makeText(appContext, toastText, Toast.LENGTH_SHORT).show()

        val pendingResult = goAsync()
        widgetScope.launch {
            try {
                AutoExpenseDetectionStore.ensureLoaded(appContext)
                val syntheticTx = ParsedSmsTransaction(
                    rawText = "1-Tap Home Screen Widget ($title ₹$amount)",
                    amount = amount,
                    merchant = title,
                    category = category,
                    paymentMode = "UPI",
                    confidence = "high",
                    isIgnoredSecurityMessage = false
                )
                AutoExpenseDetectionStore.recordAutoLoggedSmsExpense(
                    context = appContext,
                    parsed = syntheticTx,
                    senderAddress = "HOME-WIDGET"
                )
            } catch (_: Exception) {
            } finally {
                runCatching { pendingResult.finish() }
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "kharchee_quick_widget_prefs"
        private const val KEY_REMAINING_BUDGET = "remaining_monthly_budget"
        private const val KEY_TODAY_SPEND = "today_spend"
        private const val KEY_LAST_ACTION = "last_action_label"

        const val ACTION_QUICK_LOG_CHAI_20 = "com.example.widget.ACTION_QUICK_LOG_CHAI_20"
        const val ACTION_QUICK_LOG_AUTO_50 = "com.example.widget.ACTION_QUICK_LOG_AUTO_50"
        const val EXTRA_WIDGET_OPEN_ADD_EXPENSE = "extra_widget_open_add_expense"

        private val _pendingOpenAddExpenseFlow = kotlinx.coroutines.flow.MutableStateFlow(false)
        val pendingOpenAddExpenseFlow: kotlinx.coroutines.flow.StateFlow<Boolean> = _pendingOpenAddExpenseFlow

        fun setPendingOpenAddExpense(pending: Boolean) {
            _pendingOpenAddExpenseFlow.value = pending
        }

        private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /**
         * Syncs live Remaining Monthly Budget and Today's Spend from the active app session
         * and pushes an immediate update to all pinned home-screen widgets.
         */
        fun syncWidgetState(
            context: Context,
            remainingMonthlyBudget: Long,
            todaySpent: Long,
            lastActionLabel: String? = null
        ) {
            val appContext = context.applicationContext
            val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
                .putLong(KEY_REMAINING_BUDGET, remainingMonthlyBudget.coerceAtLeast(0L))
                .putLong(KEY_TODAY_SPEND, todaySpent.coerceAtLeast(0L))
            if (!lastActionLabel.isNullOrBlank()) {
                editor.putString(KEY_LAST_ACTION, lastActionLabel)
            }
            editor.apply()
            refreshAllWidgets(appContext)
        }

        fun refreshAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, KharcheeQuickWidgetProvider::class.java)
                val ids = appWidgetManager.getAppWidgetIds(componentName)
                for (id in ids) {
                    updateSingleWidget(context, appWidgetManager, id)
                }
            } catch (_: Exception) {
            }
        }

        private fun updateSingleWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val remainingBudget = prefs.getLong(KEY_REMAINING_BUDGET, 25_000L)
            val todaySpend = prefs.getLong(KEY_TODAY_SPEND, 0L)
            val lastAction = prefs.getString(KEY_LAST_ACTION, "1-Tap Background Log") ?: "1-Tap Background Log"

            val views = RemoteViews(context.packageName, R.layout.widget_kharchee_quick)
            views.setTextViewText(
                R.id.widget_remaining_budget_text,
                PerformancePreferences.preFormatInr(remainingBudget)
            )
            views.setTextViewText(
                R.id.widget_today_spend_text,
                PerformancePreferences.preFormatInr(todaySpend)
            )
            views.setTextViewText(R.id.widget_last_action_text, lastAction)

            // 1. [+ ₹20 Chai] Background Broadcast PendingIntent
            val chaiIntent = Intent(context, KharcheeQuickWidgetProvider::class.java).apply {
                action = ACTION_QUICK_LOG_CHAI_20
            }
            val chaiPendingIntent = PendingIntent.getBroadcast(
                context,
                101,
                chaiIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_chai_20, chaiPendingIntent)

            // 2. [+ ₹50 Auto] Background Broadcast PendingIntent
            val autoIntent = Intent(context, KharcheeQuickWidgetProvider::class.java).apply {
                action = ACTION_QUICK_LOG_AUTO_50
            }
            val autoPendingIntent = PendingIntent.getBroadcast(
                context,
                102,
                autoIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_auto_50, autoPendingIntent)

            // 3. [+ Custom] Activity PendingIntent opening Add Expense Sheet directly
            val customIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_WIDGET_OPEN_ADD_EXPENSE, true)
            }
            val customPendingIntent = PendingIntent.getActivity(
                context,
                103,
                customIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_custom, customPendingIntent)

            // Tapping the widget root container opens Kharchee Dashboard
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                104,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root_container, openAppPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
