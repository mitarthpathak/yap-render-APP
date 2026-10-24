package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.data.repository.IslSignDictionary
import com.example.ui.theme.*

@Composable
fun SayItSeeItShareItSection(
    onSignWordSelected: (String) -> Unit,
    onExploreTheoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Section Header
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(YapOrangeAccent)
                )
                Text(
                    text = "THE PIPELINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = YapSubtextGray
                )
            }

            Text(
                text = "Say it. See it. Share it.",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = YapHeadingBlack,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Explore the three foundational layers of Yap & Render's real-time Indian Sign Language engine.",
                fontSize = 14.sp,
                color = YapSubtextGray,
                lineHeight = 20.sp
            )
        }

        // 1. "Say it." Card with Lime Underlay
        FeaturePillarCard(
            stepNumber = "01",
            title = "Say it.",
            subtitle = "Spoken Voice & Natural Text Parsing",
            description = "Capture everyday spoken audio or typed text. Our NLP engine eliminates stopwords, rearranges English grammar into Indian Sign Language SOV (Subject-Object-Verb) structure, and tokenizes sign sequences.",
            pillText = "VOICE RECOGNITION + NLP",
            actionText = "Try saying \"YOU HOME\"",
            onActionClick = { onSignWordSelected("YOU HOME") }
        )

        // 2. "See it." Card with Lime Underlay
        FeaturePillarCard(
            stepNumber = "02",
            title = "See it.",
            subtitle = "3D Kinematic Motion Engine",
            description = "Three.js WebGL renders fully articulated skeletal avatars with Mixamo rigging. Features real-time fingerspelling keyframes, procedural finger bone Euler angles, and natural torso dynamics.",
            pillText = "WEBGL 3D KINEMATICS",
            actionText = "Watch sign for \"TIME\"",
            onActionClick = { onSignWordSelected("TIME") }
        )

        // 3. "Share it." Card with Lime Underlay
        FeaturePillarCard(
            stepNumber = "03",
            title = "Share it.",
            subtitle = "Zero-Barrier Universal Access",
            description = "Runs 100% locally and offline in the browser and mobile app. Designed to empower India's 18+ million deaf and hard-of-hearing community in classrooms, offices, and daily life.",
            pillText = "OFFLINE-READY & ACCESSIBLE",
            actionText = "Learn about ISL Theory",
            onActionClick = onExploreTheoryClick
        )

        // 4. Interactive A to Z Fingerspelling Strip
        FingerspellingStrip(onLetterSelected = onSignWordSelected)

        // 5. Assigned Core Vocabulary Grid
        AssignedVocabularyMatrix(onWordSelected = onSignWordSelected)
    }
}

@Composable
private fun FeaturePillarCard(
    stepNumber: String,
    title: String,
    subtitle: String,
    description: String,
    pillText: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        // Lime underlay shadow effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(YapLimeAccent, RoundedCornerShape(12.dp))
        )

        // Main Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, YapHeadingBlack, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Step Number + Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(YapHeadingBlack)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stepNumber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = YapHeadingBlack
                        )
                    }

                    // Tech Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(YapOrangeBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = pillText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = YapOrangeDark
                        )
                    }
                }

                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = YapHeadingBlack
                )

                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = YapSubtextGray,
                    lineHeight = 19.sp
                )

                // Action chip button
                Row(
                    modifier = Modifier
                        .tapBounce()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFAF8F2))
                        .border(1.dp, YapBorderColor, RoundedCornerShape(20.dp))
                        .clickable(onClick = onActionClick)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = YapOrangeAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = actionText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = YapHeadingBlack
                    )
                }
            }
        }
    }
}

@Composable
private fun FingerspellingStrip(
    onLetterSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, YapBorderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "A-Z Fingerspelling Ribbon",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = YapHeadingBlack
                )
                Text(
                    text = "Tap letter to sign",
                    fontSize = 11.sp,
                    color = YapSubtextGray
                )
            }

            Text(
                text = "Procedural joint angles automatically fingerspell letters A through Z:",
                fontSize = 12.sp,
                color = YapSubtextGray
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ('A'..'Z').forEach { char ->
                    val letter = char.toString()
                    Box(
                        modifier = Modifier
                            .tapBounce()
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFAF8F2))
                            .border(1.dp, YapBorderColor, RoundedCornerShape(8.dp))
                            .clickable { onLetterSelected(letter) }
                            .testTag("ribbon_letter_$letter"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = YapHeadingBlack
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignedVocabularyMatrix(
    onWordSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, YapBorderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Assigned Core Vocabulary",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = YapHeadingBlack
            )

            Text(
                text = "Tap any word below to immediately animate the 3D avatar:",
                fontSize = 12.sp,
                color = YapSubtextGray
            )

            val coreWords = listOf(
                "TIME", "YOU", "PERSON", "HELLO", "HOME",
                "DRINK", "EAT", "WATER", "HELP", "THANK YOU",
                "YES", "NO", "PLEASE", "SORRY", "WELCOME",
                "GOOD", "BAD", "STOP", "WAIT", "COME", "GO",
                "WANT", "NEED", "LIKE", "KNOW", "UNDERSTAND", "ASK",
                "I", "MY", "YOUR", "NAME", "LOVE", "MEET", "MORNING", "FRIEND"
            )

            // Flow row simulation using chunked rows
            coreWords.chunked(4).forEach { rowWords ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowWords.forEach { word ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .tapBounce()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFAF8F2))
                                .border(1.dp, YapBorderColor, RoundedCornerShape(8.dp))
                                .clickable { onWordSelected(word) }
                                .padding(vertical = 8.dp)
                                .testTag("matrix_word_$word"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = word,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = YapHeadingBlack,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
