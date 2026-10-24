package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AvatarType
import com.example.data.repository.IslSignDictionary
import com.example.ui.components.Mannequin3DCanvas
import com.example.ui.theme.*
import com.example.ui.viewmodel.YapRenderUiState

@Composable
fun AvatarStudioWidget(
    uiState: YapRenderUiState,
    onInputChanged: (String) -> Unit,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onReplay: () -> Unit,
    onVoiceClick: () -> Unit,
    onLiveToggle: () -> Unit,
    onAvatarTypeChanged: (AvatarType) -> Unit,
    onSpeedChanged: (Float) -> Unit,
    onChipSelected: (String) -> Unit,
    onToggleFullscreen: () -> Unit,
    onAvatarStateChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var showSpeedMenu by remember { mutableStateOf(false) }

    // Live blinking dot animation
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val liveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    ) {
        // Vibrant Lime Green Offset Base/Drop Accent Card (Exact styling signature from yap-render & dashboard)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 6.dp)
                .background(YapLimeAccent, RoundedCornerShape(14.dp))
        )

        // Main Mac Window Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.2.dp, YapHeadingBlack, RoundedCornerShape(14.dp))
                .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = Color(0x1F000000))
                .testTag("avatar_studio_widget"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. macOS Window Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YapWindowHeader)
                        .border(
                            width = 1.dp,
                            color = YapBorderSubtle,
                            shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Traffic Light Dots (Red, Yellow, Green)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(DotRed)
                                .border(0.8.dp, Color(0x33000000), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(DotYellow)
                                .border(0.8.dp, Color(0x33000000), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(DotGreen)
                                .border(0.8.dp, Color(0x33000000), CircleShape)
                        )
                    }

                    // Window Center Title & Tech Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "avatar.render",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = YapHeadingBlack,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "•",
                            fontSize = 10.sp,
                            color = YapMutedGray
                        )
                        Text(
                            text = "ISL 3D Engine",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = YapSubtextGray
                        )
                    }

                    // Right Side: Fullscreen expand icon
                    IconButton(
                        onClick = onToggleFullscreen,
                        modifier = Modifier.size(24.dp).testTag("btn_fullscreen")
                    ) {
                        Icon(
                            imageVector = if (uiState.isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Expand",
                            tint = YapHeadingBlack,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 2. 3D Avatar Stage Viewport (WebGL Canvas)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (uiState.isFullscreen) 360.dp else 270.dp)
                        .background(Color(0xFFFAF8F2)),
                    contentAlignment = Alignment.Center
                ) {
                    // Status Badge (Top-Left): ● ISL AVATAR • ready
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.95f))
                            .border(1.dp, YapBorderColor, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isSigning) YapOrangeAccent else StatusReadyGreen)
                        )
                        Text(
                            text = if (uiState.isSigning && uiState.currentWord.isNotEmpty()) {
                                "ISL AVATAR • signing '${uiState.currentWord}'"
                            } else {
                                "ISL AVATAR • ready"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = YapHeadingBlack
                        )
                    }

                    // Interactive 3D Three.js Avatar WebGL Viewport
                    Avatar3DWebView(
                        avatarType = uiState.avatarType,
                        phraseToPlay = uiState.phraseToPlay,
                        playTrigger = uiState.playTrigger,
                        stopTrigger = uiState.stopTrigger,
                        replayTrigger = uiState.replayTrigger,
                        speed = uiState.speed,
                        onAvatarStateChange = onAvatarStateChange,
                        modifier = Modifier.fillMaxSize(),
                        liveTranscript = uiState.liveTranscript,
                        liveIsFinal = uiState.liveIsFinal,
                        liveTrigger = uiState.liveTrigger
                    )

                    // Bottom-Right Avatar Switch: AVATAR [ Default ] [ Human ]
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.95f))
                            .border(1.dp, YapBorderColor, RoundedCornerShape(20.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "AVATAR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = YapSubtextGray,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Default Option Pill
                        Box(
                            modifier = Modifier
                                .tapBounce()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (uiState.avatarType == AvatarType.DEFAULT) YapHeadingBlack else Color.Transparent)
                                .clickable { onAvatarTypeChanged(AvatarType.DEFAULT) }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                .testTag("avatar_opt_default")
                        ) {
                            Text(
                                text = "Default",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (uiState.avatarType == AvatarType.DEFAULT) Color.White else YapSubtextGray
                            )
                        }

                        // Human Option Pill
                        Box(
                            modifier = Modifier
                                .tapBounce()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (uiState.avatarType == AvatarType.HUMAN) YapHeadingBlack else Color.Transparent)
                                .clickable { onAvatarTypeChanged(AvatarType.HUMAN) }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                .testTag("avatar_opt_human")
                        ) {
                            Text(
                                text = "Human",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (uiState.avatarType == AvatarType.HUMAN) Color.White else YapSubtextGray
                            )
                        }
                    }
                }

                Divider(color = YapBorderSubtle, thickness = 1.dp)

                // 3. Input Controls: [ Text Input ] [ 🎙 Voice ] [ ● Live ]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search / Text Input Field
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = onInputChanged,
                        placeholder = {
                            Text(
                                "Type words or letters (e.g. YOU HOME)",
                                fontSize = 13.sp,
                                color = YapMutedGray
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("isl_input_field"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YapHeadingBlack,
                            unfocusedBorderColor = YapBorderColor,
                            focusedContainerColor = Color(0xFFFCFBF8),
                            unfocusedContainerColor = Color(0xFFFCFBF8)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                focusManager.clearFocus()
                                onPlay()
                            }
                        ),
                        trailingIcon = {
                            if (uiState.inputText.isNotEmpty()) {
                                IconButton(onClick = onPlay) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Translate",
                                        tint = YapOrangeAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    )

                    // Voice Dictation Button
                    Button(
                        onClick = onVoiceClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isListeningVoice) YapOrangeAccent else Color.White,
                            contentColor = if (uiState.isListeningVoice) Color.White else YapHeadingBlack
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = if (uiState.isListeningVoice) null else ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(YapBorderColor)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                        modifier = Modifier.testTag("btn_voice")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Voice",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Live Listening Mode Button
                    Button(
                        onClick = onLiveToggle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isLiveListening) YapOrangeAccent else Color.White,
                            contentColor = if (uiState.isLiveListening) Color.White else YapHeadingBlack
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = if (uiState.isLiveListening) null else ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(YapBorderColor)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                        modifier = Modifier.testTag("btn_live")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.isLiveListening) Color.White.copy(alpha = liveAlpha)
                                    else DotRed
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Live",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // 4. Quick Suggestion Words Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "<",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = YapMutedGray,
                        modifier = Modifier.padding(end = 2.dp)
                    )

                    IslSignDictionary.QUICK_SUGGESTION_WORDS.forEach { word ->
                        Box(
                            modifier = Modifier
                                .tapBounce()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF7F5EE))
                                .border(
                                    width = 1.dp,
                                    color = YapBorderColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onChipSelected(word) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("chip_$word")
                        ) {
                            Text(
                                text = word,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = YapHeadingBlack
                            )
                        }
                    }

                    Text(
                        text = ">",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = YapMutedGray,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = YapBorderSubtle, thickness = 1.dp)

                // 5. Bottom Control Deck: [ ■ Stop ] [ ⟲ Replay ] [ status ] [ Speed ] [ ISL ]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAF8F2))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Terracotta Stop Button [ ■ ]
                    Box(
                        modifier = Modifier
                            .tapBounce()
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(YapOrangeAccent)
                            .clickable(onClick = onStop)
                            .testTag("btn_stop_sign"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color.White, RoundedCornerShape(2.dp))
                        )
                    }

                    // Terracotta Replay Button [ ⟲ ]
                    Box(
                        modifier = Modifier
                            .tapBounce()
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(YapOrangeAccent)
                            .clickable(onClick = onReplay)
                            .testTag("btn_replay_sign"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Replay",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Status Description text
                    Text(
                        text = uiState.statusText,
                        fontSize = 12.sp,
                        color = YapSubtextGray,
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    // Speed Selector Pill [ Speed 1x ]
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, YapBorderColor, RoundedCornerShape(8.dp))
                                .clickable { showSpeedMenu = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("btn_speed_menu"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Speed",
                                fontSize = 11.sp,
                                color = YapSubtextGray
                            )
                            Text(
                                text = "${uiState.speed}x",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = YapHeadingBlack
                            )
                        }

                        DropdownMenu(
                            expanded = showSpeedMenu,
                            onDismissRequest = { showSpeedMenu = false }
                        ) {
                            listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s}x speed") },
                                    onClick = {
                                        onSpeedChanged(s)
                                        showSpeedMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // ISL Tag Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(YapWindowHeader)
                            .border(1.dp, YapBorderColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ISL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = YapHeadingBlack
                        )
                    }
                }
            }
        }
    }
}
