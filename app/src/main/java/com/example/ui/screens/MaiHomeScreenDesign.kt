package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.ExpenseRecord
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.ProactiveFinancialInsight
import com.example.data.model.isIncomeTransaction
import com.example.ui.components.KharcheeCompactHeaderBranding
import com.example.ui.components.KharcheeSymbolLogo
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.theme.getCategoryColor
import com.example.ui.theme.getCategoryIcon
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

// ============================================================================
// KHARCHEE DARK NAVY + PURPLE/VIOLET DASHBOARD PALETTE
// ============================================================================
val MaiDeepCanvas = Color(0xFF070510)
val MaiCardSurface = Color(0xFF110C22)
val MaiCardSurfaceElevated = Color(0xFF181130)
val MaiCardBorder = Color(0xFF31215C)
val MaiCardBorderBright = Color(0xFF8B5CF6)
val MaiPrimaryPurpleStart = Color(0xFFA855F7)
val MaiPrimaryPurpleEnd = Color(0xFF6D28D9)
val MaiLavenderSubtext = Color(0xFFB8B0D4)
val MaiMutedViolet = Color(0xFF8E84AD)
val MaiPositiveEmerald = Color(0xFF2EE59D)
val MaiExpenseRose = Color(0xFFFF4D8D)
val MaiNotificationRed = Color(0xFFFF4769)
val MaiWarningAmber = Color(0xFFFBBF24)

fun formatIndianAmount(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    return formatter.format(abs(amount))
}

fun resolveTimeOfDayGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good Morning,"
        in 12..16 -> "Good Afternoon,"
        else -> "Good Evening,"
    }
}

// ============================================================================
// SECTION A. HEADER (KHARCHEE BRANDING + PROFILE/AVATAR + GREETING + NOTIFICATIONS + SETTINGS)
// ============================================================================
@Composable
fun MaiScreenTopHeader(
    userDisplayName: String,
    profilePhotoUri: String,
    hasUnreadReminders: Boolean,
    stealthMode: Boolean,
    isLightMode: Boolean,
    onOpenMyProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onToggleThemeMode: () -> Unit,
    onToggleStealth: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    val greeting = remember { resolveTimeOfDayGreeting() }
    val displayTitle = remember(userDisplayName) {
        userDisplayName.trim().ifBlank { "Kharchee" }
    }

    val profileBitmap = remember(profilePhotoUri) {
        if (profilePhotoUri.isBlank()) null
        else try {
            context.contentResolver.openInputStream(Uri.parse(profilePhotoUri))?.use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        } catch (_: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF130B29), MaiDeepCanvas)
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Avatar + KHARCHEE Branding + Personalized Greeting
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenMyProfile() }
                    .testTag("open_my_profile_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .shadow(12.dp, CircleShape, spotColor = MaiPrimaryPurpleStart)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF4C2885), Color(0xFF1C1036))
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFC4B5FD), Color(0xFF7C3AED))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap,
                            contentDescription = "Profile Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Text(
                            text = displayTitle.firstOrNull()?.uppercaseChar()?.toString() ?: "K",
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.dp.value.sp,
                            color = Color.White
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        KharcheeCompactHeaderBranding()
                        Text(
                            text = greeting,
                            fontSize = 12.sp,
                            color = MaiLavenderSubtext,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = displayTitle,
                        fontFamily = headingFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right: Notification Bell + Settings + Theme + SignOut
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Notification Bell Box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaiCardSurfaceElevated)
                        .border(1.dp, MaiCardBorder, RoundedCornerShape(14.dp))
                        .clickable { onOpenNotifications() }
                        .testTag("header_notifications_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "Notifications & Bill Reminders",
                        tint = Color(0xFFD5CEE8),
                        modifier = Modifier.size(21.dp)
                    )
                    if (hasUnreadReminders) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 9.dp, end = 9.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaiNotificationRed)
                        )
                    }
                }

                // Settings Gear Box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaiCardSurfaceElevated)
                        .border(1.dp, MaiCardBorder, RoundedCornerShape(14.dp))
                        .clickable { onOpenMyProfile() }
                        .testTag("header_settings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings & Profile",
                        tint = Color(0xFFD5CEE8),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Theme Switcher
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaiCardSurface)
                        .border(0.8.dp, MaiCardBorder, RoundedCornerShape(12.dp))
                        .clickable { onToggleThemeMode() }
                        .testTag("toggle_theme_mode_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLightMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = "Switch Theme",
                        tint = MaiLavenderSubtext,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Sign Out
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaiCardSurface)
                        .border(0.8.dp, MaiCardBorder, RoundedCornerShape(12.dp))
                        .clickable { onSignOut() }
                        .testTag("sign_out_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign Out",
                        tint = MaiLavenderSubtext,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// SECTION B. AVAILABLE BALANCE (AVAILABLE BALANCE + TOTAL INCOME + TOTAL EXPENSES + VISIBILITY TOGGLE)
// ============================================================================
@Composable
fun MaiScreenTotalBalanceHeroCard(
    totalBalance: Long,
    totalIncome: Long,
    totalExpense: Long,
    monthlyNetDelta: Long,
    stealthMode: Boolean,
    onToggleStealth: () -> Unit,
    onRequestClicked: () -> Unit,
    onTransferClicked: () -> Unit,
    onMoreClicked: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val wholeFormatted = remember(totalBalance) { formatIndianAmount(totalBalance) }
    val deltaFormatted = remember(monthlyNetDelta) { formatIndianAmount(monthlyNetDelta) }
    val isPositiveDelta = monthlyNetDelta >= 0L

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(26.dp), spotColor = Color(0xFF6B3DF0))
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2B185E),
                        Color(0xFF190F3A),
                        Color(0xFF0E0822)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(900f, 650f)
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFB794F4).copy(alpha = 0.78f),
                        Color(0xFF5D34C2).copy(alpha = 0.35f),
                        Color(0xFF9F7AEA).copy(alpha = 0.65f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Row: Available Balance + Visibility Toggle + Sparkline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1.3f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Available Balance",
                            fontSize = 13.sp,
                            color = Color(0xFFD6CEEC),
                            fontWeight = FontWeight.Medium
                        )
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF28194D))
                                .clickable { onToggleStealth() }
                                .testTag("toggle_stealth_mode_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (stealthMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (stealthMode) "Show Balance" else "Hide Balance",
                                tint = Color(0xFFD8B4FE),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = if (stealthMode) "₹••••••" else "${if (totalBalance < 0L) "-" else ""}₹$wholeFormatted",
                        fontFamily = headingFont,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (stealthMode) {
                                "↑ +₹••••"
                            } else if (isPositiveDelta) {
                                "↑ +₹$deltaFormatted"
                            } else {
                                "↓ -₹$deltaFormatted"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositiveDelta) MaiPositiveEmerald else MaiExpenseRose
                        )
                        Text(
                            text = "Net This Month",
                            fontSize = 12.sp,
                            color = MaiLavenderSubtext
                        )
                    }
                }

                // Right Sparkline Curve with Glowing Endpoint Orb
                Box(
                    modifier = Modifier
                        .weight(0.85f)
                        .height(82.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val pts = listOf(
                            Offset(w * 0.04f, h * 0.86f),
                            Offset(w * 0.22f, h * 0.72f),
                            Offset(w * 0.42f, h * 0.56f),
                            Offset(w * 0.64f, h * 0.46f),
                            Offset(w * 0.82f, h * 0.34f),
                            Offset(w * 0.94f, h * 0.16f)
                        )
                        val curvePath = Path().apply {
                            moveTo(pts.first().x, pts.first().y)
                            for (i in 0 until pts.size - 1) {
                                val p0 = pts[i]
                                val p1 = pts[i + 1]
                                val cx = (p0.x + p1.x) / 2f
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                        }
                        val fillPath = Path().apply {
                            addPath(curvePath)
                            lineTo(pts.last().x, h)
                            lineTo(pts.first().x, h)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF9F7AEA).copy(alpha = 0.42f),
                                    Color(0xFF6B3DF0).copy(alpha = 0.0f)
                                )
                            )
                        )
                        drawPath(
                            path = curvePath,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF6E48AA),
                                    Color(0xFFB794F4),
                                    Color(0xFFF3E8FF)
                                )
                            ),
                            style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                        val endPt = pts.last()
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFE9D5FF).copy(alpha = 0.9f),
                                    Color(0xFFA855F7).copy(alpha = 0.4f),
                                    Color.Transparent
                                ),
                                center = endPt,
                                radius = 14.dp.toPx()
                            ),
                            radius = 14.dp.toPx(),
                            center = endPt
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = endPt
                        )
                    }
                }
            }

            // Bottom Row inside Available Balance Card: Total Income vs Total Expenses Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Income Pill
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF140D2B).copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFF2EE59D).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .clickable { onRequestClicked() }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2EE59D).copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Total Income",
                            tint = MaiPositiveEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "TOTAL INCOME",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaiLavenderSubtext,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = if (stealthMode) "₹••••" else "+₹${formatIndianAmount(totalIncome)}",
                            fontFamily = headingFont,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaiPositiveEmerald,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Total Expenses Pill
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF140D2B).copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFFFF4D8D).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .clickable { onTransferClicked() }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF4D8D).copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = "Total Expenses",
                            tint = MaiExpenseRose,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "TOTAL EXPENSES",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaiLavenderSubtext,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = if (stealthMode) "₹••••" else "-₹${formatIndianAmount(totalExpense)}",
                            fontFamily = headingFont,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaiExpenseRose,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// SECTION C. QUICK ACTIONS (ADD EXPENSE, ADD INCOME, ADD BILL, VIEW KHATA)
// ============================================================================
@Composable
fun MaiScreenQuickActionsSection(
    onScanAndPay: () -> Unit,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenSavingsGoals: () -> Unit,
    onOpenBills: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenSettings: () -> Unit,
    onViewAllTools: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Actions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaiCardSurfaceElevated,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, MaiCardBorder),
                    modifier = Modifier
                        .clickable { onScanAndPay() }
                        .testTag("mai_snap_and_track_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Receipt",
                            tint = Color(0xFFD8B4FE),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Scan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD8B4FE)
                        )
                    }
                }

                Surface(
                    color = MaiCardSurfaceElevated,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, MaiCardBorder),
                    modifier = Modifier
                        .clickable { onViewAllTools() }
                        .testTag("mai_open_finance_tools_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Tools Hub",
                            tint = MaiLavenderSubtext,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tools",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaiLavenderSubtext
                        )
                    }
                }
            }
        }

        // 4 Primary Compact Action Buttons: Add Expense | Add Income | Add Bill | View Khata
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MaiCompactQuickActionTile(
                icon = Icons.Default.AddCircleOutline,
                label = "Add Expense",
                accentColor = Color(0xFFFF4D8D),
                testTag = "mai_add_expense_button",
                onClick = onAddExpense,
                modifier = Modifier.weight(1f)
            )
            MaiCompactQuickActionTile(
                icon = Icons.Default.Download,
                label = "Add Income",
                accentColor = Color(0xFF2EE59D),
                testTag = "mai_add_income_button",
                onClick = onAddIncome,
                modifier = Modifier.weight(1f)
            )
            MaiCompactQuickActionTile(
                icon = Icons.Default.CalendarToday,
                label = "Add Bill",
                accentColor = Color(0xFFA855F7),
                testTag = "mai_open_smart_reminders_button",
                onClick = onOpenBills,
                modifier = Modifier.weight(1f)
            )
            MaiCompactQuickActionTile(
                icon = Icons.Default.MenuBook,
                label = "View Khata",
                accentColor = Color(0xFF38BDF8),
                testTag = "quick_action_view_khata",
                onClick = onOpenVault,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MaiCompactQuickActionTile(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(82.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(MaiCardSurfaceElevated, MaiCardSurface)
                )
            )
            .border(1.dp, MaiCardBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 10.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.16f))
                .border(0.8.dp, accentColor.copy(alpha = 0.55f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFEAE4F8),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

// ============================================================================
// SECTION D. FINANCIAL HEALTH SCORE (PREMIUM METALLIC SILVER CARD)
// ============================================================================
@Composable
fun MaiScreenMetallicSilverHealthScoreCard(
    disciplineScore: Int,
    monthSpent: Long,
    monthlyBudgetTarget: Long,
    onCardClick: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val clampedScore = disciplineScore.coerceIn(0, 100)
    val healthCategory = when {
        clampedScore >= 80 -> "EXCELLENT"
        clampedScore >= 65 -> "GOOD"
        clampedScore >= 45 -> "FAIR"
        else -> "NEEDS ATTENTION"
    }
    val scoreDate = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date()).uppercase(Locale.ENGLISH)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF94A3B8))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF1F5F9), // Bright Platinum highlight
                        Color(0xFFCBD5E1), // Brushed Sterling Silver
                        Color(0xFF94A3B8), // Deep Titanium Satin
                        Color(0xFFE2E8F0), // Metallic Sheen reflection
                        Color(0xFFABB6C8)  // Engraved Silver base
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(900f, 500f)
                )
            )
            .border(
                width = 1.4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFF64748B),
                        Color(0xFFF8FAFC)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onCardClick() }
            .padding(20.dp)
            .testTag("financial_health_silver_card")
    ) {
        // Subtle metallic brushed lines & embossed watermark effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.82f, h * 0.25f),
                    radius = w * 0.45f
                ),
                center = Offset(w * 0.82f, h * 0.25f),
                radius = w * 0.45f
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Engraved Row: KHARCHEE Branding + Score Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Embossed Metallic Chip Emblem
                    Box(
                        modifier = Modifier
                            .size(width = 32.dp, height = 24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF475569), Color(0xFF1E293B))
                                )
                            )
                            .border(0.8.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        KharcheeSymbolLogo(height = 13.dp)
                    }
                    Column {
                        Text(
                            text = "KHARCHEE",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "FINANCIAL HEALTH SCORE",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF334155)
                        )
                    }
                }

                Surface(
                    color = Color(0xFF0F172A).copy(alpha = 0.10f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, Color(0xFF334155).copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "UPDATED $scoreDate",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Center Row: Embossed Score + Category Pill + Precision Ring Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$clampedScore",
                            fontFamily = headingFont,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "/ 100",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFF1E1B4B),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = healthCategory,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.1.sp,
                                color = Color(0xFFE9D5FF),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "Tap for full breakdown →",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }
                }

                // Circular Engraved Gauge on the Right
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 7.dp.toPx()
                        drawArc(
                            color = Color(0xFF64748B).copy(alpha = 0.28f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF4C1D95),
                                    Color(0xFF7C3AED),
                                    Color(0xFF0F172A),
                                    Color(0xFF4C1D95)
                                )
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * (clampedScore / 100f),
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "$clampedScore%",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }
    }
}

// ============================================================================
// SECTION E. SPENDING OVERVIEW (MONTHLY SPENDING CHART + INCOME VS EXPENSE + MONTH SELECTOR)
// ============================================================================
@Composable
fun MaiScreenSpendingOverviewCard(
    expenses: List<ExpenseRecord>,
    stealthMode: Boolean,
    onOpenAnalytics: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    var monthOffset by remember { mutableIntStateOf(0) } // 0 = current month, -1 = prev month, etc.
    var selectedWeekIndex by remember { mutableIntStateOf(-1) }

    val selectedMonthCal = remember(monthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
        }
    }
    val monthLabel = remember(monthOffset) {
        SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(selectedMonthCal.time)
    }

    val monthTransactions = remember(expenses, monthOffset) {
        val targetYear = selectedMonthCal.get(Calendar.YEAR)
        val targetMonth = selectedMonthCal.get(Calendar.MONTH)
        expenses.filter { tx ->
            val c = Calendar.getInstance().apply { timeInMillis = tx.txEpochMillis }
            c.get(Calendar.YEAR) == targetYear && c.get(Calendar.MONTH) == targetMonth
        }
    }

    val monthIncome = remember(monthTransactions) {
        monthTransactions.filter { isIncomeTransaction(it) }.sumOf { it.amount }
    }
    val monthExpense = remember(monthTransactions) {
        monthTransactions.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
    }

    // 4 Weekly buckets (W1: days 1-7, W2: days 8-14, W3: days 15-21, W4: days 22-31)
    val weeklyData = remember(monthTransactions) {
        val incomeBuckets = LongArray(4)
        val expenseBuckets = LongArray(4)
        monthTransactions.forEach { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.txEpochMillis }
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val bucket = ((day - 1) / 7).coerceIn(0, 3)
            if (isIncomeTransaction(tx)) {
                incomeBuckets[bucket] += tx.amount
            } else {
                expenseBuckets[bucket] += tx.amount
            }
        }
        (0..3).map { idx ->
            Triple("W${idx + 1}", incomeBuckets[idx], expenseBuckets[idx])
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaiCardSurface)
            .border(1.dp, MaiCardBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
            .testTag("spending_overview_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header + Month Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Spending Overview",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Income vs Expense by week",
                    fontSize = 11.sp,
                    color = MaiLavenderSubtext
                )
            }

            // Month Selector Pill (< Month YYYY >)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaiCardSurfaceElevated)
                    .border(0.8.dp, MaiCardBorder, RoundedCornerShape(50))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                IconButton(
                    onClick = {
                        monthOffset -= 1
                        selectedWeekIndex = -1
                    },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Previous Month",
                        tint = Color(0xFFD8B4FE),
                        modifier = Modifier.size(12.dp)
                    )
                }
                Text(
                    text = monthLabel,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                IconButton(
                    onClick = {
                        if (monthOffset < 0) {
                            monthOffset += 1
                            selectedWeekIndex = -1
                        }
                    },
                    enabled = monthOffset < 0,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Next Month",
                        tint = if (monthOffset < 0) Color(0xFFD8B4FE) else MaiMutedViolet.copy(alpha = 0.4f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // Income vs Expense Summary Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaiCardSurfaceElevated)
                    .border(0.8.dp, MaiCardBorder, RoundedCornerShape(14.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "INCOME",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = MaiLavenderSubtext
                )
                Text(
                    text = if (stealthMode) "₹••••" else "+₹${formatIndianAmount(monthIncome)}",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaiPositiveEmerald
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaiCardSurfaceElevated)
                    .border(0.8.dp, MaiCardBorder, RoundedCornerShape(14.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "EXPENSES",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = MaiLavenderSubtext
                )
                Text(
                    text = if (stealthMode) "₹••••" else "-₹${formatIndianAmount(monthExpense)}",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaiExpenseRose
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaiCardSurfaceElevated)
                    .border(0.8.dp, MaiCardBorder, RoundedCornerShape(14.dp))
                    .padding(10.dp)
            ) {
                val net = monthIncome - monthExpense
                Text(
                    text = "NET FLOW",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = MaiLavenderSubtext
                )
                Text(
                    text = if (stealthMode) "₹••••" else "${if (net >= 0) "+" else "-"}₹${formatIndianAmount(net)}",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = if (net >= 0) Color(0xFFD8B4FE) else MaiExpenseRose
                )
            }
        }

        // Interactive Weekly Bar Chart (Income vs Expense)
        val maxVal = remember(weeklyData) {
            weeklyData.maxOfOrNull { max(it.second, it.third) }?.coerceAtLeast(100L) ?: 100L
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(126.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0C081A))
                .border(0.8.dp, MaiCardBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            weeklyData.forEachIndexed { idx, (weekLabel, incAmt, expAmt) ->
                val isSelected = selectedWeekIndex == idx
                val incRatio = (incAmt.toFloat() / maxVal.toFloat()).coerceIn(0.06f, 1f)
                val expRatio = (expAmt.toFloat() / maxVal.toFloat()).coerceIn(0.06f, 1f)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) MaiCardSurfaceElevated else Color.Transparent)
                        .clickable {
                            selectedWeekIndex = if (selectedWeekIndex == idx) -1 else idx
                        }
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Income Bar
                        Box(
                            modifier = Modifier
                                .width(11.dp)
                                .fillMaxHeight(incRatio)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(MaiPositiveEmerald, Color(0xFF059669))
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        // Expense Bar
                        Box(
                            modifier = Modifier
                                .width(11.dp)
                                .fillMaxHeight(expRatio)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFFC084FC), Color(0xFF7C3AED))
                                    )
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = weekLabel,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaiLavenderSubtext
                    )
                }
            }
        }

        if (selectedWeekIndex in 0..3) {
            val (wLabel, wInc, wExp) = weeklyData[selectedWeekIndex]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaiCardSurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$wLabel Breakdown ($monthLabel)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = if (stealthMode) "₹••••" else "In: +₹${formatIndianAmount(wInc)} · Out: -₹${formatIndianAmount(wExp)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD8B4FE)
                )
            }
        }
    }
}

// ============================================================================
// SECTION F. BUDGET OVERVIEW (MONTHLY BUDGET + SPENT + REMAINING + PROGRESS + CATEGORY SUMMARY)
// ============================================================================
@Composable
fun MaiScreenBudgetOverviewCard(
    monthSpent: Long,
    monthlyBudgetTarget: Long,
    expenses: List<ExpenseRecord>,
    stealthMode: Boolean,
    onEditBudgetClick: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val safeTarget = max(1L, monthlyBudgetTarget)
    val remainingBudget = max(0L, monthlyBudgetTarget - monthSpent)
    val budgetRatio = (monthSpent.toFloat() / safeTarget.toFloat()).coerceIn(0f, 1f)
    val budgetPct = ((monthSpent.toDouble() / safeTarget.toDouble()) * 100.0).roundToInt().coerceIn(0, 999)

    val nowMillis = System.currentTimeMillis()
    val categoryBreakdown = remember(expenses, nowMillis) {
        val currentCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val monthExpenses = expenses.filter { tx ->
            !isIncomeTransaction(tx) && run {
                val c = Calendar.getInstance().apply { timeInMillis = tx.txEpochMillis }
                c.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
                    c.get(Calendar.MONTH) == currentCal.get(Calendar.MONTH)
            }
        }
        val sourceList = if (monthExpenses.isNotEmpty()) monthExpenses else expenses.filter { !isIncomeTransaction(it) }
        sourceList
            .groupBy { it.category.ifBlank { "General" } }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
            .take(4)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaiCardSurface)
            .border(1.dp, MaiCardBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
            .testTag("budget_overview_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFA855F7), Color(0xFF6D28D9))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Budget Overview",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Budget Overview",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "$budgetPct% of monthly budget used",
                        fontSize = 11.sp,
                        color = MaiLavenderSubtext
                    )
                }
            }

            Surface(
                color = MaiCardSurfaceElevated,
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, MaiCardBorderBright.copy(alpha = 0.6f)),
                modifier = Modifier
                    .clickable { onEditBudgetClick() }
                    .testTag("mai_edit_budget_chip")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Budget",
                        tint = Color(0xFFD8B4FE),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Adjust Limit",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD8B4FE)
                    )
                }
            }
        }

        // 3 Metric Tiles: Monthly Budget | Spent | Remaining
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaiCardSurfaceElevated)
                    .padding(10.dp)
            ) {
                Text(
                    text = "MONTHLY BUDGET",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 8.sp,
                    color = MaiLavenderSubtext
                )
                Text(
                    text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(monthlyBudgetTarget)}",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaiCardSurfaceElevated)
                    .padding(10.dp)
            ) {
                Text(
                    text = "AMOUNT SPENT",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 8.sp,
                    color = MaiLavenderSubtext
                )
                Text(
                    text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(monthSpent)}",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = if (monthSpent > monthlyBudgetTarget) MaiExpenseRose else Color(0xFFD8B4FE)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaiCardSurfaceElevated)
                    .padding(10.dp)
            ) {
                Text(
                    text = "REMAINING",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 8.sp,
                    color = MaiLavenderSubtext
                )
                Text(
                    text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(remainingBudget)}",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = MaiPositiveEmerald
                )
            }
        }

        // Overall Monthly Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF231842))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(budgetRatio)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            colors = if (monthSpent > monthlyBudgetTarget) {
                                listOf(MaiWarningAmber, MaiExpenseRose)
                            } else {
                                listOf(Color(0xFF7C3AED), Color(0xFFC084FC))
                            }
                        )
                    )
            )
        }

        // Category-wise Budget Summary
        if (categoryBreakdown.isNotEmpty()) {
            HorizontalDivider(color = MaiCardBorder, thickness = 0.8.dp)
            Text(
                text = "CATEGORY-WISE SPENDING BREAKDOWN",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = MaiLavenderSubtext
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categoryBreakdown.forEach { (categoryName, catAmount) ->
                    val catRatio = (catAmount.toFloat() / safeTarget.toFloat()).coerceIn(0.03f, 1f)
                    val catPct = ((catAmount.toDouble() / safeTarget.toDouble()) * 100.0).roundToInt()
                    val catColor = getCategoryColor(categoryName)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(categoryName),
                                    contentDescription = categoryName,
                                    tint = catColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = categoryName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(catAmount)} ($catPct%)",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD8B4FE)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { catRatio },
                            color = catColor,
                            trackColor = Color(0xFF1E1438),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// SECTION G. JARVIS INSIGHT (COMPACT CONTEXTUAL INSIGHT + RECOMMENDATION + OPEN JARVIS BUTTON)
// ============================================================================
@Composable
fun MaiScreenJarvisInsightBanner(
    monthSpent: Long,
    monthlyBudgetTarget: Long,
    proactiveInsights: List<ProactiveFinancialInsight>,
    onOpenJarvis: () -> Unit
) {
    val insightCopy = remember(monthSpent, monthlyBudgetTarget, proactiveInsights) {
        when {
            proactiveInsights.isNotEmpty() -> proactiveInsights.first().message
            monthSpent == 0L -> "Your ledger is clean this month. Log expenses or scan a UPI receipt for live AI wealth insights."
            monthSpent <= monthlyBudgetTarget -> {
                val saved = monthlyBudgetTarget - monthSpent
                "You are ₹${formatIndianAmount(saved)} within this month's budget target. Recommendation: move 20% of your remaining buffer into your primary Savings Goal."
            }
            else -> {
                val over = monthSpent - monthlyBudgetTarget
                "Monthly spending is ₹${formatIndianAmount(over)} above your target budget. Recommendation: pause non-essential late-night spends for the next 3 days."
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFF7B3FE4))
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF28165A),
                        Color(0xFF140D2E),
                        Color(0xFF1F1147)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFA97BFF).copy(alpha = 0.7f),
                        Color(0xFF472D8C).copy(alpha = 0.4f),
                        Color(0xFF8B5CF6).copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onOpenJarvis() }
            .padding(16.dp)
            .testTag("mai_jarvis_insight_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Circular Pinterest J.A.R.V.I.S. Visual Emblem
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF05030F))
                    .border(1.4.dp, Color(0xFFC4B5FD), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.jarvis_reactor_core),
                    contentDescription = "JARVIS Core",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFD8B4FE),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "JARVIS INSIGHT",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.1.sp,
                        color = Color(0xFFD8B4FE)
                    )
                }
                Text(
                    text = insightCopy,
                    fontSize = 12.sp,
                    color = Color(0xFFEAE4F8),
                    lineHeight = 17.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    color = Color(0xFF7C3AED),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.clickable { onOpenJarvis() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Open JARVIS Command Center",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open JARVIS",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// SECTION H. UPCOMING BILLS (ADD, EDIT, DELETE & MARK-AS-PAID FUNCTIONALITY)
// ============================================================================
@Composable
fun MaiScreenUpcomingBillsCard(
    paymentReminders: List<PaymentReminderRecord>,
    stealthMode: Boolean,
    onOpenBillsManager: () -> Unit,
    onCreateBill: ((title: String, amount: Long, category: String, dueEpochMillis: Long) -> Unit)? = null,
    onToggleBillPaid: ((PaymentReminderRecord, Boolean) -> Unit)? = null,
    onDeleteBill: ((PaymentReminderRecord) -> Unit)? = null
) {
    val headingFont = LocalAppFontFamily.current
    val nowMillis = System.currentTimeMillis()
    val sortedBills = remember(paymentReminders) {
        paymentReminders.sortedWith(compareBy<PaymentReminderRecord> { it.isPaid }.thenBy { it.dueEpochMillis })
    }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) }

    var showInlineBillDialog by remember { mutableStateOf(false) }
    var editingBill by remember { mutableStateOf<PaymentReminderRecord?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaiCardSurface)
            .border(1.dp, MaiCardBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
            .testTag("upcoming_bills_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Upcoming Bills",
                    tint = Color(0xFFD6CEEC),
                    modifier = Modifier.size(19.dp)
                )
                Text(
                    text = "Upcoming Bills",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFF6D28D9),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .clickable {
                            if (onCreateBill != null) {
                                editingBill = null
                                showInlineBillDialog = true
                            } else {
                                onOpenBillsManager()
                            }
                        }
                        .testTag("dashboard_add_bill_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Bill",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Add Bill",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = "Manage →",
                    fontSize = 12.sp,
                    color = MaiLavenderSubtext,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenBillsManager() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        if (sortedBills.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaiCardSurfaceElevated)
                    .border(1.dp, MaiCardBorder, RoundedCornerShape(14.dp))
                    .clickable {
                        if (onCreateBill != null) {
                            editingBill = null
                            showInlineBillDialog = true
                        } else {
                            onOpenBillsManager()
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "No upcoming bills scheduled",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "Tap to add rent, electricity, subscriptions or EMI reminders",
                        fontSize = 11.sp,
                        color = MaiLavenderSubtext
                    )
                }
                Text(
                    text = "+ Add Bill",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD8B4FE)
                )
            }
        } else {
            sortedBills.take(4).forEach { bill ->
                val daysRemaining = ((bill.dueEpochMillis - nowMillis) / (24L * 60L * 60L * 1000L)).toInt()
                val dueLabel = when {
                    bill.isPaid -> "Paid · ${dateFormatter.format(Date(bill.dueEpochMillis))}"
                    daysRemaining < 0 -> "Overdue by ${abs(daysRemaining)}d · ${dateFormatter.format(Date(bill.dueEpochMillis))}"
                    daysRemaining == 0 -> "Due today · ${dateFormatter.format(Date(bill.dueEpochMillis))}"
                    daysRemaining == 1 -> "Due tomorrow · ${dateFormatter.format(Date(bill.dueEpochMillis))}"
                    else -> "Due in $daysRemaining days · ${dateFormatter.format(Date(bill.dueEpochMillis))}"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaiCardSurfaceElevated)
                        .border(1.dp, MaiCardBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Mark Paid / Unpaid Icon Toggle
                        IconButton(
                            onClick = {
                                if (onToggleBillPaid != null) {
                                    onToggleBillPaid(bill, !bill.isPaid)
                                } else {
                                    onOpenBillsManager()
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (bill.isPaid) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                contentDescription = if (bill.isPaid) "Mark as Pending" else "Mark as Paid",
                                tint = if (bill.isPaid) MaiPositiveEmerald else Color(0xFFA78BFA),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = bill.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Surface(
                                    color = if (bill.isPaid) MaiPositiveEmerald.copy(alpha = 0.16f) else MaiWarningAmber.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = if (bill.isPaid) "PAID" else "PENDING",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (bill.isPaid) MaiPositiveEmerald else MaiWarningAmber,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = dueLabel,
                                fontSize = 11.sp,
                                color = MaiLavenderSubtext
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (stealthMode) "₹•••" else "₹${formatIndianAmount(bill.amount)}",
                            fontFamily = headingFont,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (bill.isPaid) MaiPositiveEmerald else Color.White
                        )

                        if (onCreateBill != null) {
                            IconButton(
                                onClick = {
                                    editingBill = bill
                                    showInlineBillDialog = true
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Bill",
                                    tint = MaiLavenderSubtext,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        if (onDeleteBill != null) {
                            IconButton(
                                onClick = { onDeleteBill(bill) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Bill",
                                    tint = MaiExpenseRose,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInlineBillDialog && onCreateBill != null) {
        val currentEditing = editingBill
        var billTitle by remember(currentEditing) { mutableStateOf(currentEditing?.title.orEmpty()) }
        var billAmountText by remember(currentEditing) {
            mutableStateOf(currentEditing?.amount?.toString().orEmpty())
        }
        var daysUntilDueText by remember(currentEditing) {
            val defaultDays = if (currentEditing != null) {
                max(0L, (currentEditing.dueEpochMillis - nowMillis) / 86_400_000L).toString()
            } else "3"
            mutableStateOf(defaultDays)
        }

        Dialog(onDismissRequest = { showInlineBillDialog = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaiCardSurface)
                    .border(1.2.dp, MaiCardBorderBright, RoundedCornerShape(22.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (currentEditing != null) "Edit Bill Reminder" else "Add Upcoming Bill",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color.White
                )

                OutlinedTextField(
                    value = billTitle,
                    onValueChange = { billTitle = it },
                    label = { Text("Bill Name (e.g. Electricity, Rent, Netflix)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaiCardBorderBright,
                        unfocusedBorderColor = MaiCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = billAmountText,
                    onValueChange = { billAmountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaiCardBorderBright,
                        unfocusedBorderColor = MaiCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = daysUntilDueText,
                    onValueChange = { daysUntilDueText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Due in how many days?") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaiCardBorderBright,
                        unfocusedBorderColor = MaiCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showInlineBillDialog = false },
                        border = BorderStroke(1.dp, MaiCardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = MaiLavenderSubtext)
                    }
                    Button(
                        onClick = {
                            val cleanTitle = billTitle.trim()
                            val amt = billAmountText.toLongOrNull() ?: 0L
                            val days = (daysUntilDueText.toLongOrNull() ?: 3L).coerceIn(0L, 365L)
                            if (cleanTitle.isNotEmpty() && amt > 0L) {
                                if (currentEditing != null && onDeleteBill != null) {
                                    onDeleteBill(currentEditing)
                                }
                                val dueMillis = System.currentTimeMillis() + (days * 86_400_000L)
                                onCreateBill(cleanTitle, amt, "Bills", dueMillis)
                                showInlineBillDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaiPrimaryPurpleEnd),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (currentEditing != null) "Save Changes" else "Save Bill", color = Color.White)
                    }
                }
            }
        }
    }
}

// ============================================================================
// SECTION I. RECENT TRANSACTIONS (MERCHANT, CATEGORY, DATE, AMOUNT + VIEW ALL -> KHATA)
// ============================================================================
@Composable
fun MaiScreenRecentTransactionsCard(
    expenses: List<ExpenseRecord>,
    stealthMode: Boolean,
    onSeeAllPassbook: () -> Unit,
    onEditTransaction: (ExpenseRecord) -> Unit,
    onOpenAddExpense: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.ENGLISH) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM, h:mm a", Locale.ENGLISH) }
    val nowMillis = System.currentTimeMillis()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaiCardSurface)
            .border(1.dp, MaiCardBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
            .testTag("recent_transactions_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Recent Transactions",
                    tint = Color(0xFFD6CEEC),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Recent Transactions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSeeAllPassbook() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .testTag("recent_transactions_view_all_khata")
            ) {
                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD8B4FE)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View All in Khata",
                    tint = Color(0xFFD8B4FE),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        if (expenses.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaiCardSurfaceElevated)
                    .border(1.dp, MaiCardBorder, RoundedCornerShape(14.dp))
                    .clickable { onOpenAddExpense() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "No transactions recorded yet",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "Log your first expense or income to see real-time insights",
                        fontSize = 11.sp,
                        color = MaiLavenderSubtext
                    )
                }
                Text(
                    text = "+ Log Now",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA78BFA)
                )
            }
        } else {
            expenses.take(5).forEach { tx ->
                val isInc = isIncomeTransaction(tx)
                val isToday = remember(tx.txEpochMillis) {
                    val c1 = Calendar.getInstance().apply { timeInMillis = nowMillis }
                    val c2 = Calendar.getInstance().apply { timeInMillis = tx.txEpochMillis }
                    c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                        c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
                }
                val timeLabel = if (isToday) {
                    "Today, ${timeFormatter.format(Date(tx.txEpochMillis))}"
                } else {
                    dateFormatter.format(Date(tx.txEpochMillis))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onEditTransaction(tx) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isInc) Color(0xFF0E2F2C) else Color(0xFF23153C)
                                )
                                .border(
                                    0.8.dp,
                                    if (isInc) MaiPositiveEmerald.copy(alpha = 0.4f) else MaiCardBorder,
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isInc) Icons.Default.AccountBalanceWallet else Icons.Default.ShoppingBag,
                                contentDescription = tx.title,
                                tint = if (isInc) MaiPositiveEmerald else Color(0xFFD8B4FE),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = tx.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${tx.category.ifBlank { if (isInc) "Income" else "Expense" }} · $timeLabel",
                                fontSize = 11.sp,
                                color = MaiMutedViolet
                            )
                        }
                    }

                    Text(
                        text = if (stealthMode) {
                            "₹••••"
                        } else if (isInc) {
                            "+ ₹${formatIndianAmount(tx.amount)}"
                        } else {
                            "- ₹${formatIndianAmount(tx.amount)}"
                        },
                        fontFamily = headingFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isInc) MaiPositiveEmerald else MaiExpenseRose
                    )
                }
            }
        }
    }
}

// ============================================================================
// SECTION 2. BOTTOM NAVIGATION (EXACTLY 4 TABS: HOME | KHATA | INSIGHT | PRO)
// ============================================================================
@Composable
fun MaiScreenBottomNavigationBar(
    activeTabId: String,
    onSelectHome: () -> Unit,
    onSelectPassbook: () -> Unit,
    onOpenCenterScanner: () -> Unit,
    onSelectAnalytics: () -> Unit,
    onOpenProfile: () -> Unit,
    onSelectVault: () -> Unit,
    onSelectJarvis: () -> Unit,
    onSelectPro: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF090614))
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF150E2B), Color(0xFF090614))
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF34235E),
                            Color(0xFF8B5CF6),
                            Color(0xFF34235E)
                        )
                    ),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MaiBottomNavSlot(
                    label = "Home",
                    icon = Icons.Default.Home,
                    selected = activeTabId == "home",
                    testTag = "nav_tab_home",
                    onClick = onSelectHome,
                    modifier = Modifier.weight(1f)
                )

                MaiBottomNavSlot(
                    label = "Khata",
                    icon = Icons.Default.MenuBook,
                    selected = activeTabId == "history" || activeTabId == "vault",
                    testTag = "nav_tab_history",
                    onClick = onSelectPassbook,
                    modifier = Modifier.weight(1f)
                )

                MaiBottomNavSlot(
                    label = "Insight",
                    icon = Icons.Default.BarChart,
                    selected = activeTabId == "insights",
                    testTag = "nav_tab_insights",
                    onClick = onSelectAnalytics,
                    modifier = Modifier.weight(1f)
                )

                MaiBottomNavSlot(
                    label = "Pro",
                    icon = Icons.Default.WorkspacePremium,
                    selected = activeTabId == "pro",
                    testTag = "nav_tab_pro",
                    onClick = onSelectPro,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MaiBottomNavSlot(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color(0xFFD8B4FE) else Color(0xFF8378A6),
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (selected) Color.White else Color(0xFF8378A6)
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .width(26.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFA855F7))
            )
        } else {
            Spacer(modifier = Modifier.height(3.dp))
        }
    }
}
