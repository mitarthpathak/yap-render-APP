package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatioOption
import com.example.data.model.AvatarMascot
import com.example.data.model.BackgroundTheme
import com.example.data.model.CaptionStyle
import com.example.data.model.TimedWord
import com.example.data.model.AvatarType

@Composable
fun VideoCanvas(
    aspectRatio: AspectRatioOption,
    backgroundTheme: BackgroundTheme,
    avatarMascot: AvatarMascot,
    captionStyle: CaptionStyle,
    timedWords: List<TimedWord>,
    currentWordIndex: Int,
    positionMs: Long,
    totalDurationMs: Long,
    isPlaying: Boolean,
    amplitudes: FloatArray,
    captionFontSize: Int,
    captionColor: Color,
    captionHighlightColor: Color,
    captionPosition: String,
    isUppercase: Boolean,
    showWatermark: Boolean,
    onCanvasClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatarType: AvatarType = AvatarType.DEFAULT
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Aspect ratio container card
        Box(
            modifier = Modifier
                .aspectRatio(aspectRatio.ratioWidth / aspectRatio.ratioHeight)
                .fillMaxHeight(0.85f)
                .shadow(16.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))), RoundedCornerShape(16.dp))
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onCanvasClick()
                }
        ) {
            // 1. Dynamic Background Visualizer Canvas
            VisualizerCanvas(
                theme = backgroundTheme,
                positionMs = positionMs,
                isPlaying = isPlaying,
                amplitudes = amplitudes
            )

            // 2. Avatar / Mascot Overlay (3D Robot)
            if (avatarMascot != AvatarMascot.NONE) {
                Box(
                    modifier = Modifier
                        .align(
                            if (captionPosition == "Top") Alignment.BottomCenter
                            else Alignment.Center
                        )
                        .padding(bottom = if (captionPosition == "Top") 40.dp else 0.dp)
                        .size(if (aspectRatio == AspectRatioOption.RATIO_9_16) 280.dp else 220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Use the 3D WebGL Avatar for the high-fidelity robot look as seen in the image
                    Avatar3DWebView(
                        avatarType = if (avatarMascot == AvatarMascot.STREAMER_DUDE) AvatarType.HUMAN else avatarType,
                        phraseToPlay = timedWords.joinToString(" ") { it.word },
                        playTrigger = if (isPlaying) System.currentTimeMillis() else 0L,
                        stopTrigger = if (!isPlaying) System.currentTimeMillis() else 0L,
                        replayTrigger = 0L,
                        speed = 1.0f,
                        onAvatarStateChange = {},
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // 3. Dynamic Captions Overlay
            CaptionsOverlay(
                timedWords = timedWords,
                currentWordIndex = currentWordIndex,
                captionStyle = captionStyle,
                fontSizeSp = captionFontSize,
                textColor = captionColor,
                highlightColor = captionHighlightColor,
                isUppercase = isUppercase,
                position = captionPosition
            )

            // 4. Watermark / Brand Badge
            if (showWatermark) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF000000).copy(alpha = 0.65f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "⚡", fontSize = 11.sp)
                    Text(
                        text = "YAP RENDER",
                        color = Color(0xFFF8FAFC),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // 5. Timecode Overlay in top right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF000000).copy(alpha = 0.65f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                val currentSec = positionMs / 1000
                val currentMillis = (positionMs % 1000) / 100
                val totalSec = totalDurationMs / 1000
                Text(
                    text = String.format("%02d.%d / %02ds", currentSec, currentMillis, totalSec),
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 6. Big Play overlay button when paused
            if (!isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(Color(0xFF000000).copy(alpha = 0.6f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(27.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
