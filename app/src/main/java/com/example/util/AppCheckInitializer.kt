package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

private const val APP_CHECK_PREFS = "com.google.firebase.appcheck.debug.store"
private const val DEBUG_SECRET_KEY =
    "com.google.firebase.appcheck.debug.store.FIREBASE_APP_CHECK_DEBUG_SECRET"

fun configureAppCheck(context: Context, intent: Intent?) {
    val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    if (!isDebuggable) {
        val siteKey = context.getString(R.string.recaptcha_site_key)
        Firebase.appCheck.installAppCheckProviderFactory(
            RecaptchaAppCheckProviderFactory.getInstance(siteKey)
        )
        return
    }

    val token = intent?.getStringExtra("FIREBASE_APPCHECK_DEBUG_TOKEN")
    if (!token.isNullOrEmpty()) {
        context.getSharedPreferences(APP_CHECK_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(DEBUG_SECRET_KEY, token)
            .commit()
    }

    Firebase.appCheck.installAppCheckProviderFactory(
        DebugAppCheckProviderFactory.getInstance()
    )
}
