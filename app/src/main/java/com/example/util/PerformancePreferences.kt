package com.example.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Pre-formatted immutable UI model for an expense/income item so that
 * currency & date strings are never computed inside @Composable draw phases.
 */
data class PreFormattedTransactionUiModel(
    val id: String,
    val title: String,
    val category: String,
    val amount: Long,
    val formattedInrAmount: String,
    val signedFormattedAmount: String,
    val formattedDateLabel: String,
    val isIncome: Boolean,
    val paymentMode: String,
    val txEpochMillis: Long
)

/**
 * Manages lifecycle-aware animation pausing (ON_PAUSE / background) and the
 * user-configurable "Battery Saver / Reduced Motion" mode for 60fps scrolling
 * and minimal battery drain.
 */
object PerformancePreferences {
    private const val PREFS_NAME = "kharchee_performance_prefs"
    private const val KEY_BATTERY_SAVER_REDUCED_MOTION = "battery_saver_reduced_motion"
    const val DEFAULT_DASHBOARD_PAGE_SIZE = 20

    private val _isBatterySaverReducedMotionFlow = MutableStateFlow(false)
    val isBatterySaverReducedMotionFlow: StateFlow<Boolean> = _isBatterySaverReducedMotionFlow.asStateFlow()

    private val _isAppInForegroundFlow = MutableStateFlow(true)
    val isAppInForegroundFlow: StateFlow<Boolean> = _isAppInForegroundFlow.asStateFlow()

    // Thread-safe LRU-bounded cache for pre-calculated INR currency strings
    private val inrCache = ConcurrentHashMap<Long, String>(256)
    private val threadLocalInrFormat = object : ThreadLocal<NumberFormat>() {
        override fun initialValue(): NumberFormat = NumberFormat.getIntegerInstance(Locale("en", "IN"))
    }
    private val threadLocalDateFormat = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH)
    }

    fun ensureInitialized(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isBatterySaverReducedMotionFlow.value = prefs.getBoolean(KEY_BATTERY_SAVER_REDUCED_MOTION, false)
    }

    fun setBatterySaverReducedMotion(context: Context, enabled: Boolean) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_BATTERY_SAVER_REDUCED_MOTION, enabled).apply()
        _isBatterySaverReducedMotionFlow.value = enabled
    }

    fun setAppInForeground(inForeground: Boolean) {
        _isAppInForegroundFlow.value = inForeground
    }

    /**
     * Composable helper that returns true whenever infinite animations or heavy
     * gradient blurs should pause (either app is in ON_PAUSE/background OR
     * Battery Saver / Reduced Motion is enabled).
     */
    @Composable
    fun shouldPauseAnimations(): Boolean {
        val reducedMotion by isBatterySaverReducedMotionFlow.collectAsStateWithLifecycle()
        val inForeground by isAppInForegroundFlow.collectAsStateWithLifecycle()
        return reducedMotion || !inForeground
    }

    fun preFormatInr(amount: Long): String {
        val absVal = abs(amount)
        inrCache[absVal]?.let { return it }
        val formatted = "₹${threadLocalInrFormat.get()?.format(absVal) ?: absVal.toString()}"
        if (inrCache.size < 2000) {
            inrCache[absVal] = formatted
        }
        return formatted
    }

    fun preFormatDate(epochMillis: Long): String {
        return threadLocalDateFormat.get()?.format(Date(epochMillis)) ?: ""
    }
}
