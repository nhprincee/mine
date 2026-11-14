package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily

/**
 * Centralized KHARCHEE Full Logo Component (Official Architectural "A" Monolith + Bold "KHARCHEE" Wordmark).
 */
@Composable
fun KharcheeFullLogo(
    height: Dp = 36.dp,
    modifier: Modifier = Modifier,
    testTag: String = "kharchee_full_logo"
) {
    val headingFont = LocalAppFontFamily.current
    val glyphHeight = (height * 0.62f).coerceAtLeast(16.dp)
    val textSp = (height.value * 0.44f).coerceIn(13f, 22f).sp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .height(height)
            .testTag(testTag)
    ) {
        KharcheeArchitecturalAMark(size = glyphHeight)
        Text(
            text = "KHARCHEE",
            fontFamily = headingFont,
            fontSize = textSp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.2.sp,
            color = Color.White
        )
    }
}

/**
 * Centralized KHARCHEE Architectural "A" Symbol Component for compact placements.
 */
@Composable
fun KharcheeSymbolLogo(
    height: Dp = 22.dp,
    modifier: Modifier = Modifier,
    testTag: String = "kharchee_symbol_logo"
) {
    KharcheeArchitecturalAMark(
        size = height,
        modifier = modifier.testTag(testTag)
    )
}

/**
 * Compact header branding component combining the official KHARCHEE architectural "A" symbol
 * with bold tracking "KHARCHEE" wordmark and optional section or PRO badge.
 */
@Composable
fun KharcheeCompactHeaderBranding(
    sectionLabel: String? = null,
    isPro: Boolean = false,
    symbolHeight: Dp = 14.dp,
    modifier: Modifier = Modifier,
    testTag: String = "kharchee_compact_header_branding"
) {
    Surface(
        color = Color(0xFF151329),
        shape = RoundedCornerShape(50),
        border = BorderStroke(0.8.dp, Color(0xFFA855F7).copy(alpha = 0.65f)),
        modifier = modifier.testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            KharcheeSymbolLogo(height = symbolHeight)
            Text(
                text = buildString {
                    append("KHARCHEE")
                    if (isPro) append(" PRO")
                    if (!sectionLabel.isNullOrBlank()) append(" · $sectionLabel")
                },
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.4.sp,
                color = Color.White
            )
        }
    }
}

/**
 * Subtle logo-based loading indicator featuring the official KHARCHEE architectural "A" monolith.
 */
@Composable
fun KharcheeLogoLoadingIndicator(
    message: String = "Synchronizing KHARCHEE...",
    modifier: Modifier = Modifier
) {
    val pauseAnimations = com.example.util.PerformancePreferences.shouldPauseAnimations()
    val pulseScale: Float
    val pulseAlpha: Float

    if (pauseAnimations) {
        pulseScale = 1.0f
        pulseAlpha = 1.0f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "kharcheeLogoPulse")
        val animScale by infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
        val animAlpha by infiniteTransition.animateFloat(
            initialValue = 0.78f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
        pulseScale = animScale
        pulseAlpha = animAlpha
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF05050A))
            .testTag("kharchee_logo_loading_indicator"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF141126),
                            Color(0xFF0D0E17),
                            Color(0xFF05050A)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFFA855F7),
                strokeWidth = 2.dp,
                modifier = Modifier.size(82.dp)
            )
            KharcheeSymbolLogo(
                height = 28.dp,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .alpha(pulseAlpha)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        KharcheeFullLogo(height = 26.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 11.sp,
            color = Color(0xFFB8B0D4)
        )
    }
}
