package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * One-shot celebratory overlay shown right after a successful sign-in.
 * Fades a greeting card in over a dimmed scrim, throws a confetti burst,
 * then dismisses itself. [nonce] changing (to non-zero) replays it.
 */
@Composable
fun WelcomeOverlay(
    nonce: Long,
    name: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(nonce) {
        if (nonce == 0L) return@LaunchedEffect
        visible = true
        delay(2600)
        visible = false
        delay(320)
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)),
        exit = fadeOut(tween(300)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x66101012)),
            contentAlignment = Alignment.Center
        ) {
            Confetti(running = visible, modifier = Modifier.fillMaxSize())

            val pop = remember { Animatable(0.7f) }
            LaunchedEffect(nonce) {
                pop.snapTo(0.7f)
                pop.animateTo(1f, tween(360))
            }

            Column(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = pop.value
                        scaleY = pop.value
                        alpha = ((pop.value - 0.7f) / 0.3f).coerceIn(0f, 1f)
                    }
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .padding(horizontal = 30.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("👋", fontSize = 46.sp)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "NAMASTE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    color = YapOrangeAccent
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Welcome, ${name.trim().ifBlank { "friend" }}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = YapHeadingBlack
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "The avatar is saying hello to you.",
                    fontSize = 12.5.sp,
                    color = YapSubtextGray
                )
            }
        }
    }
}

@Composable
private fun Confetti(running: Boolean, modifier: Modifier = Modifier) {
    val colors = listOf(
        YapOrangeAccent, YapLimeAccent, AvatarTealBase, YapNavyDark, YapLimeUnderlay, YapOrangeLight
    )
    val pieces = remember {
        List(90) {
            ConfettiPiece(
                startX = Random.nextFloat(),
                angle = Random.nextFloat() * 6.2832f,
                speed = 0.35f + Random.nextFloat() * 0.75f,
                spin = (Random.nextFloat() - 0.5f) * 18f,
                size = 6f + Random.nextFloat() * 8f,
                color = colors[it % colors.size],
                delay = Random.nextFloat() * 0.25f
            )
        }
    }

    val progress = remember { Animatable(0f) }
    LaunchedEffect(running) {
        if (running) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(2400, easing = LinearEasing))
        }
    }

    Canvas(modifier = modifier) {
        val t = progress.value
        if (t <= 0f) return@Canvas
        pieces.forEach { p ->
            val local = ((t - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
            if (local <= 0f) return@forEach
            val originX = size.width * p.startX
            val originY = size.height * 0.32f
            val dist = local * p.speed * size.height
            val x = originX + cos(p.angle) * dist * 0.6f
            val y = originY + sin(p.angle) * dist * 0.25f + local * local * size.height * 0.9f
            val rot = p.spin * local * 12f
            rotate(rot, pivot = Offset(x, y)) {
                drawRect(
                    color = p.color.copy(alpha = (1f - local).coerceIn(0f, 1f)),
                    topLeft = Offset(x - p.size / 2f, y - p.size / 2f),
                    size = Size(p.size, p.size * 0.5f)
                )
            }
        }
    }
}

private data class ConfettiPiece(
    val startX: Float,
    val angle: Float,
    val speed: Float,
    val spin: Float,
    val size: Float,
    val color: Color,
    val delay: Float
)
