package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.YapViewModel
import kotlinx.coroutines.delay

@Composable
fun TeleprompterScreen(
    viewModel: YapViewModel,
    modifier: Modifier = Modifier
) {
    val studioState by viewModel.studioState.collectAsState()
    val scrollState = rememberScrollState()

    var isAutoScrolling by remember { mutableStateOf(false) }
    var scrollSpeed by remember { mutableStateOf(2f) } // pixels per tick
    var fontSize by remember { mutableStateOf(32) }
    var isMirrored by remember { mutableStateOf(false) }

    // Auto-scroll loop
    LaunchedEffect(isAutoScrolling, scrollSpeed) {
        while (isAutoScrolling) {
            val next = scrollState.value + scrollSpeed.toInt().coerceAtLeast(1)
            if (next >= scrollState.maxValue) {
                isAutoScrolling = false
                break
            }
            scrollState.scrollTo(next)
            delay(30)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Control Top Bar
        Surface(
            color = DarkSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Teleprompter Mode 🎙️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Speed: ${scrollSpeed.toInt()}x • Size: ${fontSize}sp",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Mirror Horizontal Toggle
                    FilledIconToggleButton(
                        checked = isMirrored,
                        onCheckedChange = { isMirrored = it },
                        modifier = Modifier.size(38.dp),
                        colors = IconButtonDefaults.filledIconToggleButtonColors(
                            containerColor = DarkSurfaceElevated,
                            checkedContainerColor = PrimaryNeon
                        )
                    ) {
                        Icon(Icons.Default.Flip, contentDescription = "Mirror", modifier = Modifier.size(18.dp))
                    }

                    // Reset Scroll
                    IconButton(
                        onClick = {
                            isAutoScrolling = false
                            // scroll back to top
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset", tint = TextPrimary)
                    }
                }
            }
        }

        // Adjustable Sliders Row
        Surface(
            color = DarkSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Speed", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(40.dp))
                Slider(
                    value = scrollSpeed,
                    onValueChange = { scrollSpeed = it },
                    valueRange = 1f..8f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    colors = SliderDefaults.colors(thumbColor = PrimaryLight, activeTrackColor = PrimaryNeon)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Text", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(30.dp))
                Slider(
                    value = fontSize.toFloat(),
                    onValueChange = { fontSize = it.toInt() },
                    valueRange = 22f..48f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    colors = SliderDefaults.colors(thumbColor = SecondaryLight, activeTrackColor = SecondaryAqua)
                )
            }
        }

        // Main Prompter Canvas Stage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
        ) {
            // Horizontal Eye-Level Reading Guide Line
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(PrimaryNeon.copy(alpha = 0.45f))
            )

            // Scrolling Text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scaleX = if (isMirrored) -1f else 1f, scaleY = 1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 220.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = studioState.scriptText,
                    color = Color(0xFFF8FAFC),
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.45).sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }

            // Floating Transport Play/Pause Pill
            FloatingActionButton(
                onClick = { isAutoScrolling = !isAutoScrolling },
                containerColor = PrimaryNeon,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .size(60.dp)
            ) {
                Icon(
                    imageVector = if (isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isAutoScrolling) "Pause" else "Scroll",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
