package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.model.BackgroundTheme
import kotlin.math.*

@Composable
fun VisualizerCanvas(
    theme: BackgroundTheme,
    positionMs: Long,
    isPlaying: Boolean,
    amplitudes: FloatArray,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val timeSec = positionMs / 1000f

        when (theme) {
            BackgroundTheme.CYBERPUNK_GRID -> {
                drawCyberpunkGrid(width, height, timeSec, isPlaying)
            }
            BackgroundTheme.LOFI_CHILL -> {
                drawLofiChill(width, height, timeSec, isPlaying)
            }
            BackgroundTheme.MOTION_WARP -> {
                drawMotionWarp(width, height, timeSec, isPlaying)
            }
            BackgroundTheme.STARRY_NEBULA -> {
                drawStarryNebula(width, height, timeSec, isPlaying)
            }
            BackgroundTheme.SUNSET_GRADIENT -> {
                drawSunsetGradient(width, height, timeSec, isPlaying)
            }
            BackgroundTheme.AUDIO_SPECTRUM -> {
                drawAudioSpectrumTheme(width, height, timeSec, amplitudes)
            }
            BackgroundTheme.DEEP_OBSIDIAN -> {
                drawDeepObsidian(width, height, timeSec)
            }
        }
    }
}

private fun DrawScope.drawCyberpunkGrid(w: Float, h: Float, t: Float, isPlaying: Boolean) {
    // Deep dark background
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0D0221), Color(0xFF0F0826), Color(0xFF1D0B3D)),
            startY = 0f,
            endY = h
        )
    )

    // Glowing synthwave sun
    val sunCenter = Offset(w * 0.5f, h * 0.42f)
    val sunRadius = min(w, h) * 0.22f
    drawCircle(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFF007A), Color(0xFFFF8A00), Color(0xFFFFDE00)),
            startY = sunCenter.y - sunRadius,
            endY = sunCenter.y + sunRadius
        ),
        radius = sunRadius,
        center = sunCenter
    )

    // Sun horizontal scanline cuts
    for (i in 1..6) {
        val cutY = sunCenter.y + (i * sunRadius * 0.14f)
        val cutH = i * 2.5f
        if (cutY < sunCenter.y + sunRadius) {
            drawRect(
                color = Color(0xFF0F0826),
                topLeft = Offset(sunCenter.x - sunRadius, cutY),
                size = Size(sunRadius * 2, cutH)
            )
        }
    }

    // Perspective 3D Grid Floor
    val horizonY = h * 0.55f
    val gridColor = Color(0xFF00F0FF).copy(alpha = 0.55f)
    val vanishPoint = Offset(w * 0.5f, horizonY)

    // Perspective lines
    val numLines = 14
    for (i in -numLines / 2..numLines / 2) {
        val bottomX = w * 0.5f + (i * w * 0.12f)
        drawLine(
            color = gridColor,
            start = vanishPoint,
            end = Offset(bottomX, h),
            strokeWidth = 1.5f
        )
    }

    // Moving horizontal grid lines
    val speedOffset = if (isPlaying) (t * 0.8f) % 1.0f else 0.2f
    for (j in 1..10) {
        val progress = (j.toFloat() / 10f + speedOffset) % 1.0f
        val expProgress = progress * progress
        val lineY = horizonY + (h - horizonY) * expProgress
        val alpha = expProgress.coerceIn(0.1f, 0.9f)
        drawLine(
            color = Color(0xFFE000FF).copy(alpha = alpha * 0.7f),
            start = Offset(0f, lineY),
            end = Offset(w, lineY),
            strokeWidth = 1.8f * expProgress + 0.8f
        )
    }
}

private fun DrawScope.drawLofiChill(w: Float, h: Float, t: Float, isPlaying: Boolean) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1E1B4B), Color(0xFF311042), Color(0xFF4C1D95)),
            startY = 0f,
            endY = h
        )
    )

    // Soft moon
    val moonCenter = Offset(w * 0.78f, h * 0.22f)
    drawCircle(
        color = Color(0xFFFDE68A).copy(alpha = 0.2f),
        radius = 45f,
        center = moonCenter
    )
    drawCircle(
        color = Color(0xFFFEF3C7),
        radius = 35f,
        center = moonCenter
    )

    // Floating dust / particles
    for (i in 0..25) {
        val seedX = (sin((i * 99 + 12).toFloat()) * 0.5f + 0.5f) * w
        val seedY = (cos((i * 47 + 5).toFloat()) * 0.5f + 0.5f) * h
        val driftY = if (isPlaying) (seedY - (t * 20f * (1 + i % 3))) % h else seedY
        val finalY = if (driftY < 0) driftY + h else driftY
        drawCircle(
            color = Color(0xFFDDD6FE).copy(alpha = 0.35f + 0.3f * sin(t * 2f + i)),
            radius = 1.5f + (i % 3),
            center = Offset(seedX, finalY)
        )
    }

    // Skyline silhouette
    val cityY = h * 0.75f
    val bldgWidth = w / 8f
    for (b in 0..8) {
        val bH = ((b * 7 + 13) % 9) * 20f + 50f
        drawRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(b * bldgWidth, h - bH),
            size = Size(bldgWidth + 2f, bH)
        )
        // Little yellow windows
        for (win in 0..3) {
            if ((b + win) % 2 == 0) {
                drawRect(
                    color = Color(0xFFFBBF24).copy(alpha = 0.6f),
                    topLeft = Offset(b * bldgWidth + 10f, h - bH + 15f + win * 20f),
                    size = Size(6f, 10f)
                )
            }
        }
    }
}

private fun DrawScope.drawMotionWarp(w: Float, h: Float, t: Float, isPlaying: Boolean) {
    drawRect(color = Color(0xFF030712))

    val center = Offset(w * 0.5f, h * 0.5f)
    val numRays = 36
    val animOffset = if (isPlaying) t * 2.5f else 0f

    for (r in 0 until numRays) {
        val angle = (r.toDouble() / numRays * 2 * Math.PI) + (t * 0.15f)
        val dirX = cos(angle).toFloat()
        val dirY = sin(angle).toFloat()

        for (seg in 1..4) {
            val distProgress = ((seg * 0.25f + (animOffset * 0.2f)) % 1.0f)
            val startDist = distProgress * min(w, h) * 0.65f
            val len = 25f + distProgress * 80f
            val p1 = Offset(center.x + dirX * startDist, center.y + dirY * startDist)
            val p2 = Offset(center.x + dirX * (startDist + len), center.y + dirY * (startDist + len))

            val color = if (r % 2 == 0) Color(0xFF38BDF8) else Color(0xFFA855F7)
            drawLine(
                color = color.copy(alpha = distProgress.coerceIn(0.1f, 0.9f)),
                start = p1,
                end = p2,
                strokeWidth = 2.0f + distProgress * 3.5f,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawStarryNebula(w: Float, h: Float, t: Float, isPlaying: Boolean) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF2E1065), Color(0xFF090514), Color(0xFF000000)),
            center = Offset(w * 0.4f, h * 0.35f),
            radius = max(w, h) * 0.8f
        )
    )

    // Nebula cloud glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF9333EA).copy(alpha = 0.25f), Color.Transparent),
            center = Offset(w * 0.65f, h * 0.45f),
            radius = w * 0.55f
        ),
        radius = w * 0.55f,
        center = Offset(w * 0.65f, h * 0.45f)
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.2f), Color.Transparent),
            center = Offset(w * 0.3f, h * 0.7f),
            radius = w * 0.45f
        ),
        radius = w * 0.45f,
        center = Offset(w * 0.3f, h * 0.7f)
    )

    // Twinkling stars
    for (i in 0..40) {
        val sx = (sin(i * 12.3f) * 0.5f + 0.5f) * w
        val sy = (cos(i * 7.7f) * 0.5f + 0.5f) * h
        val twinkle = (sin(t * 3f + i * 1.5f) * 0.5f + 0.5f)
        drawCircle(
            color = Color.White.copy(alpha = 0.3f + twinkle * 0.7f),
            radius = 1.2f + twinkle * 1.5f,
            center = Offset(sx, sy)
        )
    }
}

private fun DrawScope.drawSunsetGradient(w: Float, h: Float, t: Float, isPlaying: Boolean) {
    val shift = sin(t * 0.5f) * 0.1f
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF4C0519),
                Color(0xFFBE123C),
                Color(0xFFF97316),
                Color(0xFFFDE047)
            ),
            startY = 0f,
            endY = h * (1.1f + shift)
        )
    )

    // Ambient circular glow ring
    drawCircle(
        color = Color(0xFFFFFBEB).copy(alpha = 0.15f),
        radius = min(w, h) * 0.35f,
        center = Offset(w * 0.5f, h * 0.4f),
        style = Stroke(width = 4f)
    )
}

private fun DrawScope.drawAudioSpectrumTheme(w: Float, h: Float, t: Float, amps: FloatArray) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF020617)),
            startY = 0f,
            endY = h
        )
    )

    // Animated central circular audiogram
    val center = Offset(w * 0.5f, h * 0.38f)
    val baseRadius = min(w, h) * 0.18f

    drawCircle(
        color = Color(0xFF1E293B),
        radius = baseRadius * 0.85f,
        center = center
    )

    val count = amps.size
    for (i in 0 until count) {
        val angle = (i.toDouble() / count * 2 * Math.PI) - Math.PI / 2
        val amp = amps[i]
        val barLen = baseRadius * (0.3f + amp * 0.85f)
        val dirX = cos(angle).toFloat()
        val dirY = sin(angle).toFloat()

        val start = Offset(center.x + dirX * baseRadius, center.y + dirY * baseRadius)
        val end = Offset(center.x + dirX * (baseRadius + barLen), center.y + dirY * (baseRadius + barLen))

        val barColor = Color(
            red = (0.2f + (i.toFloat() / count) * 0.8f).coerceIn(0f, 1f),
            green = 0.7f,
            blue = 1.0f - (i.toFloat() / count) * 0.5f,
            alpha = 0.85f
        )

        drawLine(
            color = barColor,
            start = start,
            end = end,
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawDeepObsidian(w: Float, h: Float, t: Float) {
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF0B0D13), Color(0xFF111622), Color(0xFF08090C)),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
    )

    // Minimal elegant corner lines
    val lineCol = Color(0xFF334155).copy(alpha = 0.4f)
    val margin = 20f
    val len = 40f
    // Top-left
    drawLine(lineCol, Offset(margin, margin), Offset(margin + len, margin), 2f)
    drawLine(lineCol, Offset(margin, margin), Offset(margin, margin + len), 2f)
    // Top-right
    drawLine(lineCol, Offset(w - margin, margin), Offset(w - margin - len, margin), 2f)
    drawLine(lineCol, Offset(w - margin, margin), Offset(w - margin, margin + len), 2f)
    // Bottom-left
    drawLine(lineCol, Offset(margin, h - margin), Offset(margin + len, h - margin), 2f)
    drawLine(lineCol, Offset(margin, h - margin), Offset(margin, h - margin - len), 2f)
    // Bottom-right
    drawLine(lineCol, Offset(w - margin, h - margin), Offset(w - margin - len, h - margin), 2f)
    drawLine(lineCol, Offset(w - margin, h - margin), Offset(w - margin, h - margin - len), 2f)
}
