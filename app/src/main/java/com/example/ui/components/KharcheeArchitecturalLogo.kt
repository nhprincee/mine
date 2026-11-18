package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily

/**
 * Builds the official KHARCHEE Architectural "A" monolith vector path:
 * - Angled 45° left ramp
 * - Flat top horizontal peak
 * - Vertical inner negative cut with rounded arch
 * - Solid right horizontal step and base foot
 */
fun buildKharcheeArchitecturalAPath(w: Float, h: Float): Path {
    return Path().apply {
        // 1. Start at bottom-left vertical edge
        moveTo(w * 0.06f, h * 0.82f)
        lineTo(w * 0.06f, h * 0.64f)
        quadraticTo(w * 0.06f, h * 0.59f, w * 0.09f, h * 0.55f)

        // 2. Angled left ramp rising to flat top peak
        lineTo(w * 0.43f, h * 0.14f)
        quadraticTo(w * 0.46f, h * 0.10f, w * 0.51f, h * 0.10f)

        // 3. Flat top horizontal shelf
        lineTo(w * 0.61f, h * 0.10f)
        quadraticTo(w * 0.66f, h * 0.10f, w * 0.66f, h * 0.16f)

        // 4. Vertical step down on right side of peak
        lineTo(w * 0.66f, h * 0.45f)
        quadraticTo(w * 0.66f, h * 0.51f, w * 0.72f, h * 0.51f)

        // 5. Right horizontal wing shelf
        lineTo(w * 0.89f, h * 0.51f)
        quadraticTo(w * 0.94f, h * 0.51f, w * 0.94f, h * 0.57f)

        // 6. Solid right vertical outer edge
        lineTo(w * 0.94f, h * 0.84f)
        quadraticTo(w * 0.94f, h * 0.90f, w * 0.89f, h * 0.90f)

        // 7. Solid right base foot with rounded inner-left heel
        lineTo(w * 0.68f, h * 0.90f)
        quadraticTo(w * 0.58f, h * 0.90f, w * 0.58f, h * 0.78f)

        // 8. Vertical inner negative cut & arch
        lineTo(w * 0.58f, h * 0.63f)
        cubicTo(
            w * 0.58f, h * 0.53f,
            w * 0.47f, h * 0.53f,
            w * 0.42f, h * 0.60f
        )

        // 9. Inner diagonal ramp descending to left base foot
        lineTo(w * 0.25f, h * 0.86f)
        quadraticTo(w * 0.22f, h * 0.90f, w * 0.17f, h * 0.90f)

        // 10. Bottom-left base foot closing back to start
        lineTo(w * 0.11f, h * 0.90f)
        quadraticTo(w * 0.06f, h * 0.90f, w * 0.06f, h * 0.82f)
        close()
    }
}

/**
 * Renders the standalone official KHARCHEE Architectural "A" Monolith Mark
 * in a clean pure white (#FFFFFF) / brushed titanium silver finish.
 */
@Composable
fun KharcheeArchitecturalAMark(
    size: Dp = 28.dp,
    modifier: Modifier = Modifier,
    brush: Brush? = null
) {
    Canvas(
        modifier = modifier.size(width = size * 1.18f, height = size)
    ) {
        val w = this.size.width
        val h = this.size.height
        val monolithPath = buildKharcheeArchitecturalAPath(w, h)
        val fillBrush = brush ?: Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF8FAFC),
                Color(0xFFE2E8F0),
                Color(0xFFFFFFFF)
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
        drawPath(
            path = monolithPath,
            brush = fillBrush
        )
    }
}

/**
 * Official KHARCHEE Architectural Brand Logo & Wordmark Component.
 * Combines the official architectural "A" block geometry alongside bold tracking "KHARCHEE" text.
 */
@Composable
fun KharcheeArchitecturalLogo(
    subtitle: String? = null,
    symbolSize: Dp = 36.dp,
    wordmarkFontSizeSp: Int = 15,
    compact: Boolean = false,
    usePurpleAccentDot: Boolean = false,
    modifier: Modifier = Modifier
) {
    val headingFont = LocalAppFontFamily.current
    val effectiveSymbolBox = if (compact) 32.dp else symbolSize
    val effectiveGlyphHeight = effectiveSymbolBox * 0.56f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp),
        modifier = modifier.testTag("kharchee_architectural_logo")
    ) {
        // Midnight Black Squircle Emblem housing the Pure White "A" monolith with subtle Lime Green accent
        Box(
            modifier = Modifier
                .size(effectiveSymbolBox)
                .clip(RoundedCornerShape(effectiveSymbolBox * 0.28f))
                .background(Color(0xFF101510))
                .border(
                    width = 1.dp,
                    color = Color(0xFFB7F000).copy(alpha = 0.65f),
                    shape = RoundedCornerShape(effectiveSymbolBox * 0.28f)
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            KharcheeArchitecturalAMark(size = effectiveGlyphHeight)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = "KHARCHEE",
                fontFamily = headingFont,
                fontSize = wordmarkFontSizeSp.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = if (compact) 2.2.sp else 3.0.sp,
                color = Color(0xFFFFFFFF),
                maxLines = 1,
                softWrap = false,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    color = Color(0xFFB7F000),
                    maxLines = 1,
                    softWrap = false,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

