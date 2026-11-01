package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PaymentReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE) ?: TYPE_PAYMENT_REMINDER
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Kharchee Payment Reminder"
        val body = intent.getStringExtra(EXTRA_BODY) ?: "You have a scheduled payment reminder."
        val dedupeKey = intent.getStringExtra(EXTRA_DEDUPE_KEY) ?: "default_${System.currentTimeMillis() / 3600_000L}"
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 2001)

        KharcheeNotificationManager.showNotificationIfPermitted(
            context = context,
            type = type,
            notificationId = notificationId,
            title = title,
            body = body,
            dedupeKey = dedupeKey
        )
    }

    companion object {
        const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_BODY = "extra_body"
        const val EXTRA_DEDUPE_KEY = "extra_dedupe_key"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

        const val TYPE_PAYMENT_REMINDER = "payment_reminder"
        const val TYPE_NIGHT_GUARD_ALERT = "night_guard_alert"
        const val TYPE_MORNING_REVIEW = "morning_review"
    }
}
