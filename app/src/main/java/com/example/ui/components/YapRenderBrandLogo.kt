package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YapHeadingBlack

val YapNavyDark = Color(0xFF0E2238)
val YapNavyBlue = Color(0xFF1B3B5F)
val YapCyanGlow = Color(0xFF00C6FF)
val YapTaglineGray = Color(0xFF5E6D82)

/**
 * Custom vector logo based directly on the Yap & Render visual brand identity
 * (Hand holding rising equalizer soundwaves with cyan voice nexus).
 */
@Composable
fun YapRenderBrandLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 40.dp,
    showTagline: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // High-fidelity vector symbol of Hand + Equalizer Speech Wave + Cyan Node
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawBrandIcon(size.width, size.height)
            }
        }

        // Typography
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = "Yap & Render",
                fontSize = if (showTagline) 18.sp else 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = YapNavyDark,
                letterSpacing = (-0.4).sp
            )
            if (showTagline) {
                Text(
                    text = "You Speak. We Sign.",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    color = YapTaglineGray,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

/**
 * Full stacked version of the Logo (like the user uploaded image)
 */
@Composable
fun YapRenderFullStackedLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 100.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(iconSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawBrandIcon(size.width, size.height)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Yap & Render",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = YapNavyDark,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "You Speak. We Sign.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                color = YapTaglineGray,
                letterSpacing = 0.5.sp
            )
        }
    }
}

private fun DrawScope.drawBrandIcon(w: Float, h: Float) {
    val navyColor = YapNavyDark
    val glowColor = YapCyanGlow

    // 1. Equalizer vertical bars in the upper soundwave area
    val barColor = YapNavyDark
    val strokeW = w * 0.042f

    val bars = listOf(
        Pair(0.20f, Pair(0.40f, 0.48f)),
        Pair(0.26f, Pair(0.32f, 0.52f)),
        Pair(0.32f, Pair(0.25f, 0.55f)),
        Pair(0.38f, Pair(0.18f, 0.56f)),
        Pair(0.44f, Pair(0.14f, 0.52f)),
        Pair(0.50f, Pair(0.10f, 0.48f)),
        Pair(0.56f, Pair(0.16f, 0.53f)),
        Pair(0.62f, Pair(0.22f, 0.55f)),
        Pair(0.68f, Pair(0.28f, 0.52f)),
        Pair(0.74f, Pair(0.36f, 0.48f)),
        Pair(0.80f, Pair(0.42f, 0.46f))
    )

    bars.forEach { (xRatio, yRange) ->
        drawLine(
            color = barColor.copy(alpha = 0.85f),
            start = Offset(w * xRatio, h * yRange.first),
            end = Offset(w * xRatio, h * yRange.second),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }

    // 2. Intertwined central speech / question-sign curve
    val wavePath = Path().apply {
        moveTo(w * 0.50f, h * 0.58f)
        cubicTo(w * 0.45f, h * 0.45f, w * 0.35f, h * 0.38f, w * 0.38f, h * 0.28f)
        cubicTo(w * 0.40f, h * 0.20f, w * 0.54f, h * 0.20f, w * 0.56f, h * 0.30f)
        cubicTo(w * 0.58f, h * 0.42f, w * 0.50f, h * 0.50f, w * 0.50f, h * 0.58f)
    }
    drawPath(
        path = wavePath,
        color = navyColor,
        style = Stroke(width = w * 0.05f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Inner second loop
    val innerWavePath = Path().apply {
        moveTo(w * 0.50f, h * 0.58f)
        cubicTo(w * 0.48f, h * 0.50f, w * 0.42f, h * 0.44f, w * 0.44f, h * 0.35f)
        cubicTo(w * 0.46f, h * 0.28f, w * 0.52f, h * 0.28f, w * 0.52f, h * 0.34f)
    }
    drawPath(
        path = innerWavePath,
        color = navyColor,
        style = Stroke(width = w * 0.038f, cap = StrokeCap.Round)
    )

    // 3. Cyan Glowing Origin Node on the palm
    drawCircle(
        color = glowColor.copy(alpha = 0.35f),
        radius = w * 0.12f,
        center = Offset(w * 0.50f, h * 0.60f)
    )
    drawCircle(
        color = glowColor,
        radius = w * 0.055f,
        center = Offset(w * 0.50f, h * 0.60f)
    )

    // 4. Stylized Hand Silhouette supporting the voice from below
    // Angled palm base reaching right with thumb and finger contours
    val handPath = Path().apply {
        // Wrist / base
        moveTo(w * 0.18f, h * 0.68f)
        lineTo(w * 0.24f, h * 0.82f)
        lineTo(w * 0.36f, h * 0.76f)

        // Palm bottom curve
        cubicTo(w * 0.48f, h * 0.77f, w * 0.62f, h * 0.72f, w * 0.82f, h * 0.62f)
        // Little finger tip
        cubicTo(w * 0.85f, h * 0.60f, w * 0.83f, h * 0.56f, w * 0.78f, h * 0.58f)
        // Finger lines
        lineTo(w * 0.65f, h * 0.64f)
        // Index / Middle fingers extending
        cubicTo(w * 0.74f, h * 0.58f, w * 0.78f, h * 0.54f, w * 0.74f, h * 0.52f)
        cubicTo(w * 0.70f, h * 0.51f, w * 0.60f, h * 0.58f, w * 0.52f, h * 0.62f)

        // Thumb extending left-center
        cubicTo(w * 0.42f, h * 0.62f, w * 0.30f, h * 0.63f, w * 0.18f, h * 0.68f)
        close()
    }
    drawPath(
        path = handPath,
        color = navyColor,
        style = Fill
    )

    // Highlight line on fingers
    drawLine(
        color = Color.White.copy(alpha = 0.3f),
        start = Offset(w * 0.54f, h * 0.64f),
        end = Offset(w * 0.76f, h * 0.57f),
        strokeWidth = w * 0.02f,
        cap = StrokeCap.Round
    )
}
