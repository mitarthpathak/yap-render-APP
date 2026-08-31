package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.YapNavyDark
import com.example.ui.theme.*
import kotlinx.coroutines.launch

private data class Slide(
    val kicker: String,
    val title: String,
    val body: String,
    val glyph: String,
    val tint: Color
)

private val SLIDES = listOf(
    Slide(
        kicker = "STEP 01",
        title = "Speak or type, naturally",
        body = "Say a sentence or type it in. Yap & Render captures English (India) speech on-device — nothing is uploaded.",
        glyph = "🎙",
        tint = AvatarTealBase
    ),
    Slide(
        kicker = "STEP 02",
        title = "ISL grammar, not word-for-word",
        body = "A rule engine re-orders your words into Indian Sign Language's Subject-Object-Verb structure before rendering.",
        glyph = "⚡",
        tint = YapOrangeAccent
    ),
    Slide(
        kicker = "STEP 03",
        title = "A 3D avatar signs it back",
        body = "Known words play real gesture keyframes; anything else is fingerspelled A–Z. Switch between the robot and human avatar anytime.",
        glyph = "🤟",
        tint = YapLimeUnderlay
    )
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pager = rememberPagerState(pageCount = { SLIDES.size })
    val scope = rememberCoroutineScope()
    val isLast = pager.currentPage == SLIDES.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YapCreamBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GETTING STARTED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = YapSubtextGray
            )
            Text(
                text = "Skip",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = YapMutedGray,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onFinish)
                    .padding(6.dp)
                    .testTag("onboarding_skip")
            )
        }

        HorizontalPager(
            state = pager,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            val slide = SLIDES[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(slide.tint.copy(alpha = 0.18f))
                        .border(1.4.dp, YapHeadingBlack, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(slide.glyph, fontSize = 62.sp)
                }

                Spacer(Modifier.height(30.dp))

                Text(
                    text = slide.kicker,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = YapOrangeAccent
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = slide.title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = YapHeadingBlack,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = slide.body,
                    fontSize = 14.sp,
                    color = YapSubtextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(SLIDES.size) { i ->
                val active = i == pager.currentPage
                val w by animateFloatAsState(if (active) 22f else 7f, label = "dot_w")
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(7.dp)
                        .width(w.dp)
                        .clip(CircleShape)
                        .background(if (active) YapHeadingBlack else YapPillBorder)
                )
            }
        }

        Button(
            onClick = {
                if (isLast) onFinish()
                else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_next"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = YapNavyDark,
                contentColor = Color.White
            )
        ) {
            Text(
                text = if (isLast) "Enter Yap & Render" else "Next",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
