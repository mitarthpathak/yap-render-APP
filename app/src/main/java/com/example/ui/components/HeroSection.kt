package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.InfoModalType

@Composable
fun HeroSection(
    onSeeInActionClick: () -> Unit,
    onExploreIdeaClick: () -> Unit,
    onOpenDictionary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Tagline Pill: ● LANGUAGE, WITHOUT LIMITS
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(YapOrangeAccent)
            )
            Text(
                text = "LANGUAGE, WITHOUT LIMITS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = YapHeadingBlack
            )
        }

        // Editorial Display Headline:
        // Say it.
        // See it. (in coral terracotta orange italic)
        // Share
        // it.
        Column(
            verticalArrangement = Arrangement.spacedBy((-6).dp)
        ) {
            Text(
                text = "Say it.",
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = YapHeadingBlack,
                lineHeight = 58.sp,
                letterSpacing = (-1.5).sp
            )

            Text(
                text = "See it.",
                fontSize = 54.sp,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Serif,
                color = YapOrangeAccent,
                lineHeight = 58.sp,
                letterSpacing = (-1.5).sp
            )

            Text(
                text = "Share",
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = YapHeadingBlack,
                lineHeight = 58.sp,
                letterSpacing = (-1.5).sp
            )

            Text(
                text = "it.",
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = YapHeadingBlack,
                lineHeight = 58.sp,
                letterSpacing = (-1.5).sp
            )
        }

        // Subtitle text
        Text(
            text = "Yap & Render translates spoken language into Indian Sign Language, so more people can be part of the conversation.",
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = YapSubtextGray,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.padding(top = 6.dp)
        )

        // Action Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // See it in action ↘
            Button(
                onClick = onSeeInActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = YapHeadingBlack,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
                modifier = Modifier.tapBounce().testTag("btn_see_in_action")
            ) {
                Text(
                    text = "See it in action",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "↘",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Explore the idea →
            OutlinedButton(
                onClick = onExploreIdeaClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = YapHeadingBlack
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.2.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(YapBorderColor)
                ),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                modifier = Modifier.tapBounce().testTag("btn_explore_idea")
            ) {
                Text(
                    text = "Explore the idea",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "→",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Feature Badges: [ ISL-FIRST ] [ REAL-TIME SIGNS ] [ HUMAN CONNECTION ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FeaturePill(label = "ISL-FIRST", onClick = onOpenDictionary)
            FeaturePill(label = "REAL-TIME SIGNS", onClick = onSeeInActionClick)
            FeaturePill(label = "HUMAN CONNECTION", onClick = onExploreIdeaClick)
        }
    }
}

@Composable
private fun FeaturePill(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .tapBounce()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, YapPillBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = YapHeadingBlack
        )
    }
}
