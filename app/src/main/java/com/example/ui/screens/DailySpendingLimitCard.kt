package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.util.DailyLimitAlertTier
import com.example.util.DailyLimitTrackerStatus
import com.example.util.PerformancePreferences
import kotlin.math.roundToLong

private val DAILY_LIMIT_PRESETS = listOf(300L, 500L, 1000L, 1500L, 2500L)

/**
 * Main-screen Daily Spending Limit Tracker & Progress Indicator Card.
 *
 * Monitors today's personal spending against the user's configured daily budget,
 * visualizes live utilization with an animated progress bar + 80% warning marker,
 * and allows inline 1-tap adjustment of the daily limit.
 */
@Composable
fun DailySpendingLimitTrackerCard(
    status: DailyLimitTrackerStatus,
    stealthMode: Boolean,
    onUpdateDailyLimit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val headingFont = LocalAppFontFamily.current

    val midnightBlack = Color(0xFF080B08)
    val surfaceDark = Color(0xFF101610)
    val pureWhite = Color(0xFFFFFFFF)
    val mutedSilver = Color(0xFFA8B5A8)
    val limeGreen = Color(0xFFB7F000)
    val amberAlert = Color(0xFFFFB020)
    val crimsonAlert = Color(0xFFFF4D6D)

    val safeLimit = status.dailyLimit.coerceAtLeast(1L)
    val rawRatio = (status.todaySpent.toFloat() / safeLimit.toFloat()).coerceIn(0f, 1f)
    val animatedFill by animateFloatAsState(
        targetValue = rawRatio,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "daily_spending_limit_progress"
    )

    val accentColor = when (status.statusTier) {
        DailyLimitAlertTier.SAFE -> limeGreen
        DailyLimitAlertTier.APPROACHING_LIMIT -> amberAlert
        DailyLimitAlertTier.EXCEEDED_LIMIT -> crimsonAlert
    }

    val statusBadgeText = when (status.statusTier) {
        DailyLimitAlertTier.SAFE -> "ON TRACK · ${status.usagePercentage}%"
        DailyLimitAlertTier.APPROACHING_LIMIT -> "NEAR LIMIT · ${status.usagePercentage}%"
        DailyLimitAlertTier.EXCEEDED_LIMIT -> "EXCEEDED · ${status.usagePercentage}%"
    }

    val overageAmount = (status.todaySpent - safeLimit).coerceAtLeast(0L)

    var isEditorExpanded by remember { mutableStateOf(false) }
    var sliderValue by remember(safeLimit) {
        mutableFloatStateOf(safeLimit.toFloat().coerceIn(50f, 10_000f))
    }
    var customInputText by remember(safeLimit) {
        mutableStateOf(safeLimit.toString())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(surfaceDark)
            .border(
                width = 1.dp,
                color = if (status.statusTier == DailyLimitAlertTier.SAFE) {
                    Color(0xFF233023)
                } else {
                    accentColor.copy(alpha = 0.72f)
                },
                shape = RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
            .testTag("daily_spending_limit_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Row: Title + Live Tier Pill + Edit Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(midnightBlack)
                        .border(0.9.dp, accentColor.copy(alpha = 0.75f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (status.statusTier == DailyLimitAlertTier.SAFE) {
                            Icons.Default.Speed
                        } else {
                            Icons.Default.Warning
                        },
                        contentDescription = "Daily Spending Limit Status",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "DAILY SPENDING LIMIT",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        color = mutedSilver
                    )
                    Surface(
                        color = accentColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.7.dp, accentColor.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .testTag("daily_limit_status_badge")
                    ) {
                        Text(
                            text = statusBadgeText,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Surface(
                color = midnightBlack,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.8.dp, Color(0xFF2B3A2B)),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isEditorExpanded = !isEditorExpanded }
                    .testTag("daily_limit_edit_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Daily Spending Limit",
                        tint = limeGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isEditorExpanded) "Close" else "Set Limit",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = pureWhite
                    )
                }
            }
        }

        // Core Monetary Readout: Today Spent vs Daily Cap + Remaining/Exceeded
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "SPENT TODAY",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = mutedSilver
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = if (stealthMode) "₹••••" else PerformancePreferences.preFormatInr(status.todaySpent),
                        fontFamily = headingFont,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = pureWhite,
                        modifier = Modifier.testTag("daily_limit_spent_value")
                    )
                    Text(
                        text = if (stealthMode) "/ ₹••••" else "/ ${PerformancePreferences.preFormatInr(safeLimit)}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = mutedSilver,
                        modifier = Modifier
                            .padding(bottom = 3.dp)
                            .testTag("daily_limit_target_value")
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (overageAmount > 0L) "OVER LIMIT" else "REMAINING",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = mutedSilver
                )
                Text(
                    text = when {
                        stealthMode -> "₹••••"
                        overageAmount > 0L -> "+${PerformancePreferences.preFormatInr(overageAmount)}"
                        else -> PerformancePreferences.preFormatInr(status.remainingBudget)
                    },
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    modifier = Modifier.testTag("daily_limit_remaining_value")
                )
            }
        }

        // High-Contrast Progress Indicator Track with 80% Alert Threshold Marker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(50))
                .background(midnightBlack)
                .border(0.8.dp, Color(0xFF233023), RoundedCornerShape(50))
                .padding(2.dp)
                .semantics {
                    contentDescription = "Daily spending progress ${status.usagePercentage} percent"
                    progressBarRangeInfo = ProgressBarRangeInfo(
                        current = rawRatio,
                        range = 0f..1f
                    )
                }
                .testTag("daily_spending_progress_bar")
        ) {
            if (animatedFill > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFill.coerceIn(0.02f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(accentColor)
                )
            }
            // Subtle 80% warning threshold notch
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.80f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .fillMaxHeight()
                        .background(pureWhite.copy(alpha = 0.45f))
                )
            }
        }

        // Status Footer Guidance
        Text(
            text = when (status.statusTier) {
                DailyLimitAlertTier.EXCEEDED_LIMIT ->
                    "You crossed today's daily budget by ${if (stealthMode) "₹•••" else PerformancePreferences.preFormatInr(overageAmount)}. Pause non-essential spending."
                DailyLimitAlertTier.APPROACHING_LIMIT ->
                    "Approaching daily cap (${status.usagePercentage}% used). ${if (stealthMode) "₹•••" else PerformancePreferences.preFormatInr(status.remainingBudget)} left for today."
                DailyLimitAlertTier.SAFE ->
                    "Healthy pace — ${status.usagePercentage}% of today's budget used (80% alert threshold armed)."
            },
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = if (status.statusTier == DailyLimitAlertTier.SAFE) mutedSilver else accentColor
        )

        // Expandable Inline Daily Budget Editor
        AnimatedVisibility(
            visible = isEditorExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(midnightBlack)
                    .border(0.8.dp, Color(0xFF263326), RoundedCornerShape(16.dp))
                    .padding(12.dp)
                    .testTag("daily_limit_editor_panel"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ADJUST DAILY BUDGET",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = limeGreen
                )

                // 1-Tap Quick Limit Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DAILY_LIMIT_PRESETS.forEach { preset ->
                        val selected = customInputText.toLongOrNull() == preset
                        Surface(
                            color = if (selected) limeGreen else surfaceDark,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                0.8.dp,
                                if (selected) limeGreen else Color(0xFF2D3C2D)
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    sliderValue = preset.toFloat().coerceIn(50f, 10_000f)
                                    customInputText = preset.toString()
                                }
                                .testTag("daily_limit_preset_$preset")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "₹$preset",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) midnightBlack else pureWhite
                                )
                            }
                        }
                    }
                }

                Slider(
                    value = sliderValue,
                    onValueChange = { raw ->
                        val snapped = (raw / 50f).roundToLong() * 50L
                        val clamped = snapped.coerceIn(50L, 10_000L)
                        sliderValue = clamped.toFloat()
                        customInputText = clamped.toString()
                    },
                    valueRange = 50f..10_000f,
                    colors = SliderDefaults.colors(
                        thumbColor = limeGreen,
                        activeTrackColor = limeGreen,
                        inactiveTrackColor = Color(0xFF263326)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customInputText,
                        onValueChange = { text ->
                            val digits = text.filter { it.isDigit() }.take(6)
                            customInputText = digits
                            digits.toLongOrNull()?.let { parsed ->
                                sliderValue = parsed.toFloat().coerceIn(50f, 10_000f)
                            }
                        },
                        label = { Text("Daily Limit (₹)", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = limeGreen,
                            unfocusedBorderColor = Color(0xFF2B3A2B),
                            focusedTextColor = pureWhite,
                            unfocusedTextColor = pureWhite,
                            focusedLabelColor = limeGreen,
                            unfocusedLabelColor = mutedSilver
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("daily_limit_custom_input")
                    )

                    Button(
                        onClick = {
                            val parsed = customInputText.toLongOrNull()?.coerceIn(50L, 500_000L)
                                ?: sliderValue.roundToLong().coerceIn(50L, 500_000L)
                            onUpdateDailyLimit(parsed)
                            isEditorExpanded = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = limeGreen,
                            contentColor = midnightBlack
                        ),
                        modifier = Modifier
                            .heightIn(min = 52.dp)
                            .testTag("daily_limit_save_button")
                    ) {
                        Text(
                            text = "Save",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
