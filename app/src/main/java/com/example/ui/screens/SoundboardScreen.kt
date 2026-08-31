package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SoundEffect
import com.example.ui.theme.*
import com.example.ui.viewmodel.YapViewModel

@Composable
fun SoundboardScreen(
    viewModel: YapViewModel,
    modifier: Modifier = Modifier
) {
    var lastPlayedEffect by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Yap Soundboard 🔊",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Instant real-time sound effects for live streaming & clips",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = PrimaryLight)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Status Card
        lastPlayedEffect?.let { effectName ->
            Surface(
                color = DarkSurfaceElevated,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡ Triggered:", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(effectName, color = PrimaryLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Soundboard Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(SoundEffect.entries) { sfx ->
                val padBrush = when (sfx) {
                    SoundEffect.VINE_BOOM -> Brush.verticalGradient(listOf(Color(0xFFDC2626), Color(0xFF991B1B)))
                    SoundEffect.DING -> Brush.verticalGradient(listOf(Color(0xFFEAB308), Color(0xFFCA8A04)))
                    SoundEffect.GLITCH -> Brush.verticalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))
                    SoundEffect.WHOOSH -> Brush.verticalGradient(listOf(Color(0xFF06B6D4), Color(0xFF0891B2)))
                    SoundEffect.CASH_REGISTER -> Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                    SoundEffect.AIR_HORN -> Brush.verticalGradient(listOf(Color(0xFFF97316), Color(0xFFEA580C)))
                    SoundEffect.APPLAUSE -> Brush.verticalGradient(listOf(Color(0xFFEC4899), Color(0xFFDB2777)))
                    SoundEffect.CORRECT -> Brush.verticalGradient(listOf(Color(0xFF14B8A6), Color(0xFF0D9488)))
                    SoundEffect.ERROR_BUZZ -> Brush.verticalGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C)))
                    SoundEffect.LASER_BEAM -> Brush.verticalGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))
                }

                Box(
                    modifier = Modifier
                        .height(115.dp)
                        .shadow(8.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(padBrush)
                        .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .clickable {
                            viewModel.playSoundEffect(sfx)
                            lastPlayedEffect = sfx.title
                        }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = sfx.emoji, fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sfx.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        Text(
                            text = "${sfx.durationMs}ms",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
