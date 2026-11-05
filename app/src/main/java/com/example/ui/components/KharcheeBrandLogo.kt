package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.JetBrainsMonoFontFamily

/**
 * Centralized KHARCHEE Full Logo Component (Geometric Symbol + KHARCHEE Wordmark).
 * Preserves original proportions, colors, and transparency using ContentScale.Fit.
 */
@Composable
fun KharcheeFullLogo(
    height: Dp = 36.dp,
    modifier: Modifier = Modifier,
    testTag: String = "kharchee_full_logo"
) {
    Image(
        painter = painterResource(id = R.drawable.kharchee_logo_full),
        contentDescription = "KHARCHEE Logo",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .height(height)
            .testTag(testTag)
    )
}

/**
 * Centralized KHARCHEE Geometric Symbol Component for compact placements.
 * Preserves original proportions, colors, and transparency using ContentScale.Fit.
 */
@Composable
fun KharcheeSymbolLogo(
    height: Dp = 22.dp,
    modifier: Modifier = Modifier,
    testTag: String = "kharchee_symbol_logo"
) {
    Image(
        painter = painterResource(id = R.drawable.kharchee_logo_symbol),
        contentDescription = "KHARCHEE Symbol",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .height(height)
            .testTag(testTag)
    )
}

/**
 * Compact header branding component combining the approved KHARCHEE geometric symbol
 * with optional section or PRO badge in the Dark Navy + Purple/Violet theme.
 */
@Composable
fun KharcheeCompactHeaderBranding(
    sectionLabel: String? = null,
    isPro: Boolean = false,
    symbolHeight: Dp = 16.dp,
    modifier: Modifier = Modifier,
    testTag: String = "kharchee_compact_header_branding"
) {
    Surface(
        color = Color(0xFF1F133D),
        shape = RoundedCornerShape(50),
        border = BorderStroke(0.8.dp, Color(0xFF8B5CF6).copy(alpha = 0.7f)),
        modifier = modifier.testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                letterSpacing = 1.sp,
                color = Color(0xFFE9D5FF)
            )
        }
    }
}

/**
 * Subtle logo-based loading indicator for screen and app loading states.
 */
@Composable
fun KharcheeLogoLoadingIndicator(
    message: String = "Synchronizing KHARCHEE...",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "kharcheeLogoPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070510))
            .testTag("kharchee_logo_loading_indicator"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7C3AED).copy(alpha = 0.28f),
                            Color(0xFF150D2E),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFFA855F7),
                strokeWidth = 2.dp,
                modifier = Modifier.size(76.dp)
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
        Spacer(modifier = Modifier.height(14.dp))
        KharcheeFullLogo(height = 24.dp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 11.sp,
            color = Color(0xFFB8B0D4)
        )
    }
}
