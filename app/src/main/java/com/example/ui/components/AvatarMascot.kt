package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AvatarMascot

@Composable
fun AvatarMascotComponent(
    mascot: AvatarMascot,
    isPlaying: Boolean,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    if (mascot == AvatarMascot.NONE) return

    val infiniteTransition = rememberInfiniteTransition(label = "avatar_anim")
    
    val bounceScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isPlaying && isSpeaking) 1.08f else if (isPlaying) 1.03f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 180 else 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isSpeaking) 0.9f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .scale(bounceScale),
        contentAlignment = Alignment.Center
    ) {
        // Outer Glowing Halo
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF818CF8).copy(alpha = glowAlpha),
                            Color(0xFF06B6D4).copy(alpha = glowAlpha * 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Mascot Badge Frame
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(12.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                )
                .border(2.5.dp, Brush.linearGradient(listOf(Color(0xFF818CF8), Color(0xFF38BDF8))), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = mascot.emoji,
                    fontSize = 34.sp
                )
            }
        }

        // Live Speaking Sound Wave Dot
        if (isPlaying && isSpeaking) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
                    .border(2.dp, Color(0xFF0F172A), CircleShape)
            )
        }
    }
}
