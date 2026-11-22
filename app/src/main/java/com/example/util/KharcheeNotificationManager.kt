package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.ExpenseRecord
import com.example.data.model.NightGuardSchedule
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.UserSettings
import com.example.data.model.isIncomeTransaction
import com.example.data.model.isSameDay
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.roundToInt

enum class DailyLimitAlertTier {
    SAFE,
    APPROACHING_LIMIT,
    EXCEEDED_LIMIT
}

data class DailyLimitTrackerStatus(
    val todaySpent: Long,
    val dailyLimit: Long,
    val remainingBudget: Long,
    val usagePercentage: Int,
    val statusTier: DailyLimitAlertTier,
    val alertTriggered: Boolean
)

object KharcheeNotificationManager {

    private const val CHANNEL_PAYMENT_REMINDERS = "kharchee_payment_reminders"
    private const val CHANNEL_NIGHT_GUARD = "kharchee_night_guard"
    private const val CHANNEL_MORNING_REVIEW = "kharchee_morning_review"
    private const val CHANNEL_DAILY_LIMIT = "kharchee_daily_limit_alerts"
    private const val TYPE_DAILY_LIMIT_ALERT = "daily_limit_alert"
    private const val PREFS_DEDUPE = "kharchee_notification_dedupe"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val paymentChannel = NotificationChannel(
                CHANNEL_PAYMENT_REMINDERS,
                "Payment & Bill Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Scheduled bill reminders and upcoming payment alerts"
            }

            val nightGuardChannel = NotificationChannel(
                CHANNEL_NIGHT_GUARD,
                "Night Guard Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Night Guard activation and impulse protection reminders"
            }

            val morningReviewChannel = NotificationChannel(
                CHANNEL_MORNING_REVIEW,
                "Morning Financial Review",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily morning review of postponed purchases and budget reserve"
            }

            val dailyLimitChannel = NotificationChannel(
                CHANNEL_DAILY_LIMIT,
                "Daily Spending Limit Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alerts when approaching or exceeding your daily spending threshold"
            }

            manager.createNotificationChannel(paymentChannel)
            manager.createNotificationChannel(nightGuardChannel)
            manager.createNotificationChannel(morningReviewChannel)
            manager.createNotificationChannel(dailyLimitChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        val runtimeGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return runtimeGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    @SuppressLint("MissingPermission")
    fun showNotificationIfPermitted(
        context: Context,
        type: String,
        notificationId: Int,
        title: String,
        body: String,
        dedupeKey: String
    ): Boolean {
        ensureChannels(context)
        if (!hasNotificationPermission(context)) return false

        val prefs = context.getSharedPreferences(PREFS_DEDUPE, Context.MODE_PRIVATE)
        if (dedupeKey.isNotBlank() && prefs.getBoolean(dedupeKey, false)) {
            return false // Already delivered for this cycle
        }

        val channelId = when (type) {
            PaymentReminderReceiver.TYPE_NIGHT_GUARD_ALERT -> CHANNEL_NIGHT_GUARD
            PaymentReminderReceiver.TYPE_MORNING_REVIEW -> CHANNEL_MORNING_REVIEW
            TYPE_DAILY_LIMIT_ALERT -> CHANNEL_DAILY_LIMIT
            else -> CHANNEL_PAYMENT_REMINDERS
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            if (dedupeKey.isNotBlank()) {
                prefs.edit().putBoolean(dedupeKey, true).apply()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun computeTriggerEpochMillis(reminder: PaymentReminderRecord): Long {
        val dayMs = 86_400_000L
        return when (reminder.intervalType) {
            "1_day_before" -> reminder.dueEpochMillis - dayMs
            "3_days_before" -> reminder.dueEpochMillis - (3L * dayMs)
            "weekly" -> reminder.dueEpochMillis - (7L * dayMs)
            else -> reminder.dueEpochMillis
        }
    }

    fun syncRemindersAndSchedules(
        context: Context,
        reminders: List<PaymentReminderRecord>,
        settings: UserSettings
    ) {
        ensureChannels(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()

        // 1. Sync payment reminders
        if (settings.notifyPaymentReminders) {
            reminders.forEach { reminder ->
                val notifId = 3000 + abs(reminder.id.hashCode() % 50000)
                if (reminder.isPaid) {
                    cancelScheduledAlarm(context, alarmManager, notifId)
                } else {
                    val triggerAt = computeTriggerEpochMillis(reminder)
                    val dedupeKey = "rem_${reminder.id}_${reminder.dueEpochMillis}_${reminder.intervalType}"
                    val title = "Upcoming Payment: ${reminder.title} (₹${reminder.amount})"
                    val body = "${reminder.category} · Due soon. Open Kharchee to review your daily reserve before paying."

                    if (triggerAt <= now && reminder.dueEpochMillis >= now - 86_400_000L) {
                        showNotificationIfPermitted(
                            context = context,
                            type = PaymentReminderReceiver.TYPE_PAYMENT_REMINDER,
                            notificationId = notifId,
                            title = title,
                            body = body,
                            dedupeKey = dedupeKey
                        )
                    } else if (triggerAt > now) {
                        scheduleWindowAlarm(
                            context = context,
                            alarmManager = alarmManager,
                            notificationId = notifId,
                            triggerAtMillis = triggerAt,
                            type = PaymentReminderReceiver.TYPE_PAYMENT_REMINDER,
                            title = title,
                            body = body,
                            dedupeKey = dedupeKey
                        )
                    }
                }
            }
        }

        // 2. Optional Night Guard activation reminder
        val nightGuardNotifId = 9001
        if (settings.nightGuardEnabled && settings.notifyNightGuardAlert) {
            val nextStart = computeNextOccurrenceMillis(settings.nightGuardStartHour, settings.nightGuardStartMinute, now)
            val windowStr = NightGuardSchedule.formatTimeWindow(
                settings.nightGuardStartHour,
                settings.nightGuardStartMinute,
                settings.nightGuardEndHour,
                settings.nightGuardEndMinute
            )
            val dayKey = nextStart / 86_400_000L
            scheduleWindowAlarm(
                context = context,
                alarmManager = alarmManager,
                notificationId = nightGuardNotifId,
                triggerAtMillis = nextStart,
                type = PaymentReminderReceiver.TYPE_NIGHT_GUARD_ALERT,
                title = "🛡️ Night Guard Active ($windowStr)",
                body = "Ruko Zara. Late-night discretionary spending protection is now armed.",
                dedupeKey = "ng_start_$dayKey"
            )
        } else {
            cancelScheduledAlarm(context, alarmManager, nightGuardNotifId)
        }

        // 3. Optional Morning Review reminder (8:30 AM local time)
        val morningNotifId = 9002
        if (settings.notifyMorningReview) {
            val nextMorning = computeNextOccurrenceMillis(8, 30, now)
            val dayKey = nextMorning / 86_400_000L
            scheduleWindowAlarm(
                context = context,
                alarmManager = alarmManager,
                notificationId = morningNotifId,
                triggerAtMillis = nextMorning,
                type = PaymentReminderReceiver.TYPE_MORNING_REVIEW,
                title = "☀️ Kharchee Morning Wealth Briefing",
                body = "Review yesterday's discipline score, postponed items, and today's ₹${settings.dailyLimit} reserve.",
                dedupeKey = "morning_review_$dayKey"
            )
        } else {
            cancelScheduledAlarm(context, alarmManager, morningNotifId)
        }
    }

    private fun computeNextOccurrenceMillis(hour: Int, minute: Int, nowMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= nowMillis) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun scheduleWindowAlarm(
        context: Context,
        alarmManager: AlarmManager,
        notificationId: Int,
        triggerAtMillis: Long,
        type: String,
        title: String,
        body: String,
        dedupeKey: String
    ) {
        val intent = Intent(context, PaymentReminderReceiver::class.java).apply {
            putExtra(PaymentReminderReceiver.EXTRA_NOTIFICATION_TYPE, type)
            putExtra(PaymentReminderReceiver.EXTRA_TITLE, title)
            putExtra(PaymentReminderReceiver.EXTRA_BODY, body)
            putExtra(PaymentReminderReceiver.EXTRA_DEDUPE_KEY, dedupeKey)
            putExtra(PaymentReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                10 * 60 * 1000L, // 10-minute delivery window
                pendingIntent
            )
        } catch (_: Exception) {
            // Fallback if alarm scheduling is restricted
        }
    }

    private fun cancelScheduledAlarm(
        context: Context,
        alarmManager: AlarmManager,
        notificationId: Int
    ) {
        val intent = Intent(context, PaymentReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    /**
     * Pure calculation of the current day's total personal expense against the defined daily threshold.
     * Excludes income and Khata/lending/borrowing money movements so personal daily limits stay accurate.
     */
    fun evaluateDailySpendingLimit(
        expenses: List<ExpenseRecord>,
        dailyLimit: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): DailyLimitTrackerStatus {
        val safeLimit = dailyLimit.coerceAtLeast(1L)
        val todaySpent = expenses
            .filter {
                com.example.data.local.LifetimeFinancialMemorySystem.isPersonalSpending(it) &&
                    isSameDay(it.txEpochMillis, nowMillis)
            }
            .sumOf { it.amount }
        val remaining = (safeLimit - todaySpent).coerceAtLeast(0L)
        val usagePct = ((todaySpent.toDouble() / safeLimit.toDouble()) * 100.0).roundToInt().coerceAtLeast(0)
        val tier = when {
            todaySpent > safeLimit -> DailyLimitAlertTier.EXCEEDED_LIMIT
            usagePct >= 80 -> DailyLimitAlertTier.APPROACHING_LIMIT
            else -> DailyLimitAlertTier.SAFE
        }
        return DailyLimitTrackerStatus(
            todaySpent = todaySpent,
            dailyLimit = safeLimit,
            remainingBudget = remaining,
            usagePercentage = usagePct,
            statusTier = tier,
            alertTriggered = false
        )
    }

    /**
     * Calculates today's total spending and triggers a deduplicated high-priority alert notification
     * whenever the user approaches (>=80%) or exceeds (>100%) their daily spending limit.
     */
    fun checkAndNotifyDailySpendingLimit(
        context: Context,
        expenses: List<ExpenseRecord>,
        dailyLimit: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): DailyLimitTrackerStatus {
        val status = evaluateDailySpendingLimit(expenses, dailyLimit, nowMillis)
        if (status.statusTier == DailyLimitAlertTier.SAFE) {
            return status
        }
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val dayKey = "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.DAY_OF_YEAR)}"
        val (title, body, dedupeKey, notifId) = when (status.statusTier) {
            DailyLimitAlertTier.EXCEEDED_LIMIT -> {
                val overBy = (status.todaySpent - status.dailyLimit).coerceAtLeast(0L)
                Quadruple(
                    "🚨 Daily Spending Limit Exceeded!",
                    "You spent ₹${status.todaySpent} today (${status.usagePercentage}% of ₹${status.dailyLimit} limit — over by ₹$overBy). Pause discretionary spends.",
                    "daily_limit_exceeded_${dayKey}_${status.dailyLimit}",
                    41002
                )
            }
            DailyLimitAlertTier.APPROACHING_LIMIT -> {
                Quadruple(
                    "⚠️ Approaching Daily Spending Limit (${status.usagePercentage}%)",
                    "You spent ₹${status.todaySpent} of ₹${status.dailyLimit} today. Only ₹${status.remainingBudget} remaining before your daily threshold.",
                    "daily_limit_approaching_${dayKey}_${status.dailyLimit}",
                    41001
                )
            }
            DailyLimitAlertTier.SAFE -> return status
        }
        val fired = showNotificationIfPermitted(
            context = context,
            type = TYPE_DAILY_LIMIT_ALERT,
            notificationId = notifId,
            title = title,
            body = body,
            dedupeKey = dedupeKey
        )
        return status.copy(alertTriggered = fired)
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
