package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CaptionStyle
import com.example.data.model.TimedWord

@Composable
fun CaptionsOverlay(
    timedWords: List<TimedWord>,
    currentWordIndex: Int,
    captionStyle: CaptionStyle,
    fontSizeSp: Int,
    textColor: Color,
    highlightColor: Color,
    isUppercase: Boolean,
    position: String,
    modifier: Modifier = Modifier
) {
    if (timedWords.isEmpty()) return

    // Show a sliding window of ~5-7 words centered around the active word
    val windowSize = 5
    val activeIdx = currentWordIndex.coerceAtLeast(0)
    val startIndex = (activeIdx - 2).coerceAtLeast(0)
    val endIndex = (startIndex + windowSize).coerceAtMost(timedWords.size)
    val visibleWords = timedWords.subList(startIndex, endIndex)

    val alignment = when (position) {
        "Top" -> Alignment.TopCenter
        "Center" -> Alignment.Center
        else -> Alignment.BottomCenter
    }

    val verticalPadding = when (position) {
        "Top" -> 48.dp
        "Center" -> 0.dp
        else -> 56.dp
    }

    val activeWord = if (activeIdx in timedWords.indices) timedWords[activeIdx] else null

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = verticalPadding),
        contentAlignment = alignment
    ) {
        when (captionStyle) {
            CaptionStyle.HORMOZI_POP -> {
                HormoziPopCaptionView(
                    visibleWords = visibleWords,
                    activeWordIndex = activeIdx,
                    fontSizeSp = fontSizeSp,
                    defaultColor = textColor,
                    highlightColor = highlightColor,
                    isUppercase = isUppercase
                )
            }
            CaptionStyle.CYBER_NEON -> {
                CyberNeonCaptionView(
                    visibleWords = visibleWords,
                    activeWordIndex = activeIdx,
                    fontSizeSp = fontSizeSp,
                    defaultColor = textColor,
                    highlightColor = highlightColor,
                    isUppercase = isUppercase
                )
            }
            CaptionStyle.MINIMAL_BOX -> {
                MinimalBoxCaptionView(
                    visibleWords = visibleWords,
                    activeWordIndex = activeIdx,
                    fontSizeSp = fontSizeSp,
                    defaultColor = textColor,
                    highlightColor = highlightColor,
                    isUppercase = isUppercase
                )
            }
            CaptionStyle.KARAOKE_BOUNCE -> {
                KaraokeBounceCaptionView(
                    visibleWords = visibleWords,
                    activeWordIndex = activeIdx,
                    fontSizeSp = fontSizeSp,
                    defaultColor = textColor,
                    highlightColor = highlightColor,
                    isUppercase = isUppercase
                )
            }
            CaptionStyle.TYPEWRITER -> {
                TypewriterCaptionView(
                    visibleWords = visibleWords,
                    activeWordIndex = activeIdx,
                    fontSizeSp = fontSizeSp,
                    defaultColor = textColor,
                    highlightColor = highlightColor,
                    isUppercase = isUppercase
                )
            }
        }
    }
}

@Composable
private fun HormoziPopCaptionView(
    visibleWords: List<TimedWord>,
    activeWordIndex: Int,
    fontSizeSp: Int,
    defaultColor: Color,
    highlightColor: Color,
    isUppercase: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        visibleWords.forEach { wordItem ->
            val isActive = wordItem.index == activeWordIndex
            val wordText = if (isUppercase) wordItem.word.uppercase() else wordItem.word

            if (isActive) {
                // Word Pop Animation
                val transition = rememberInfiniteTransition(label = "pop")
                val popScale by transition.animateFloat(
                    initialValue = 1.15f,
                    targetValue = 1.25f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(150, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "popScale"
                )

                Box(
                    modifier = Modifier
                        .scale(popScale)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF000000).copy(alpha = 0.85f))
                        .border(2.dp, highlightColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = wordText,
                        color = highlightColor,
                        fontSize = (fontSizeSp + 3).sp,
                        fontWeight = FontWeight.Black,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = Shadow(
                                color = highlightColor.copy(alpha = 0.8f),
                                offset = Offset(0f, 0f),
                                blurRadius = 12f
                            )
                        )
                    )
                }
            } else {
                Text(
                    text = wordText,
                    color = defaultColor.copy(alpha = if (wordItem.index < activeWordIndex) 0.5f else 0.85f),
                    fontSize = fontSizeSp.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 3.dp),
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun CyberNeonCaptionView(
    visibleWords: List<TimedWord>,
    activeWordIndex: Int,
    fontSizeSp: Int,
    defaultColor: Color,
    highlightColor: Color,
    isUppercase: Boolean
) {
    val annotatedString = buildAnnotatedString {
        visibleWords.forEach { wordItem ->
            val isActive = wordItem.index == activeWordIndex
            val wordText = (if (isUppercase) wordItem.word.uppercase() else wordItem.word) + " "

            if (isActive) {
                withStyle(
                    SpanStyle(
                        color = highlightColor,
                        fontWeight = FontWeight.Black,
                        fontSize = (fontSizeSp + 2).sp,
                        shadow = Shadow(
                            color = highlightColor,
                            offset = Offset(0f, 0f),
                            blurRadius = 16f
                        )
                    )
                ) {
                    append(wordText)
                }
            } else {
                withStyle(
                    SpanStyle(
                        color = defaultColor.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSizeSp.sp,
                        shadow = Shadow(
                            color = Color(0xFF06B6D4).copy(alpha = 0.5f),
                            offset = Offset(0f, 0f),
                            blurRadius = 6f
                        )
                    )
                ) {
                    append(wordText)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF090D16).copy(alpha = 0.8f))
            .border(1.5.dp, Color(0xFF06B6D4).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = annotatedString,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MinimalBoxCaptionView(
    visibleWords: List<TimedWord>,
    activeWordIndex: Int,
    fontSizeSp: Int,
    defaultColor: Color,
    highlightColor: Color,
    isUppercase: Boolean
) {
    val annotatedString = buildAnnotatedString {
        visibleWords.forEach { wordItem ->
            val isActive = wordItem.index == activeWordIndex
            val wordText = (if (isUppercase) wordItem.word.uppercase() else wordItem.word) + " "

            if (isActive) {
                withStyle(
                    SpanStyle(
                        color = highlightColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = fontSizeSp.sp
                    )
                ) {
                    append(wordText)
                }
            } else {
                withStyle(
                    SpanStyle(
                        color = defaultColor,
                        fontWeight = FontWeight.Normal,
                        fontSize = fontSizeSp.sp
                    )
                ) {
                    append(wordText)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.92f))
            .border(1.dp, Color(0xFF475569), RoundedCornerShape(24.dp))
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = annotatedString,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun KaraokeBounceCaptionView(
    visibleWords: List<TimedWord>,
    activeWordIndex: Int,
    fontSizeSp: Int,
    defaultColor: Color,
    highlightColor: Color,
    isUppercase: Boolean
) {
    val annotatedString = buildAnnotatedString {
        visibleWords.forEach { wordItem ->
            val isPast = wordItem.index <= activeWordIndex
            val wordText = (if (isUppercase) wordItem.word.uppercase() else wordItem.word) + " "

            if (isPast) {
                withStyle(
                    SpanStyle(
                        color = highlightColor,
                        fontWeight = FontWeight.Black,
                        fontSize = fontSizeSp.sp
                    )
                ) {
                    append(wordText)
                }
            } else {
                withStyle(
                    SpanStyle(
                        color = defaultColor.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Medium,
                        fontSize = fontSizeSp.sp
                    )
                ) {
                    append(wordText)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = annotatedString,
            textAlign = TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(
                shadow = Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 4f)
            )
        )
    }
}

@Composable
private fun TypewriterCaptionView(
    visibleWords: List<TimedWord>,
    activeWordIndex: Int,
    fontSizeSp: Int,
    defaultColor: Color,
    highlightColor: Color,
    isUppercase: Boolean
) {
    val text = visibleWords.joinToString(" ") {
        if (isUppercase) it.word.uppercase() else it.word
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF000000).copy(alpha = 0.9f))
            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(4.dp))
            .padding(10.dp)
    ) {
        Text(
            text = "> $text _",
            color = Color(0xFF10B981),
            fontFamily = FontFamily.Monospace,
            fontSize = fontSizeSp.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Start
        )
    }
}
