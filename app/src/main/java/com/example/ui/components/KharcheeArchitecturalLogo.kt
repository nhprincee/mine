package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
 * Approved KHARCHEE Geometric Brand Logo & Wordmark Component.
 * Uses the exact uploaded KHARCHEE geometric symbol and full logo assets
 * without stretching, cropping, or distorting proportions.
 */
@Composable
fun KharcheeArchitecturalLogo(
    subtitle: String? = null,
    symbolSize: Dp = 38.dp,
    wordmarkFontSizeSp: Int = 15,
    compact: Boolean = false,
    usePurpleAccentDot: Boolean = false,
    modifier: Modifier = Modifier
) {
    val matteBlack = Color(0xFF0A0714)
    val metallicSilver = Color(0xFFC4CBD4)
    val subtlePurple = Color(0xFF9D4EDD)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
        modifier = modifier.testTag("kharchee_architectural_logo")
    ) {
        if (subtitle.isNullOrBlank()) {
            // Render the full approved logo (symbol + KHARCHEE wordmark) preserving exact aspect ratio
            Image(
                painter = painterResource(id = R.drawable.kharchee_logo_full),
                contentDescription = "KHARCHEE Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(if (compact) 28.dp else symbolSize.coerceAtLeast(30.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(symbolSize)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                matteBlack,
                                Color(0xFF140E26),
                                Color(0xFF1F1438)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE9D5FF).copy(alpha = 0.75f),
                                subtlePurple.copy(alpha = 0.55f),
                                metallicSilver.copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kharchee_logo_symbol),
                    contentDescription = "KHARCHEE Symbol",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(symbolSize * 0.62f)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.kharchee_logo_full),
                    contentDescription = "KHARCHEE Wordmark",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height((wordmarkFontSizeSp + 6).dp)
                )
                Text(
                    text = subtitle,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.9.sp,
                    color = Color(0xFFB8B0D4)
                )
            }
        }
    }
}
