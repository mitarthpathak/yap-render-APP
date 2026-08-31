package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.IslSignDictionary
import com.example.ui.theme.*
import com.example.ui.viewmodel.InfoModalType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoModalsSheet(
    modalType: InfoModalType,
    onDismiss: () -> Unit,
    onSignWordSelected: (String) -> Unit,
    onOpenTranslator: () -> Unit = onDismiss
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.Transparent,
        dragHandle = null,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (modalType) {
                InfoModalType.YOUR_ACCOUNT, InfoModalType.MITARTH_PROFILE -> {
                    YourAccountDashboardCard(
                        onDismiss = onDismiss,
                        onOpenTranslator = {
                            onDismiss()
                            onOpenTranslator()
                        }
                    )
                }
                InfoModalType.HELP_GUIDE -> {
                    HelpTheoryModalCard(
                        onDismiss = onDismiss,
                        onOpenDictionary = {
                            onDismiss()
                        }
                    )
                }
                InfoModalType.HOW_IT_WORKS -> {
                    StandardModalContainer(title = "How Yap & Render Works", onDismiss = onDismiss) {
                        HowItWorksContent()
                    }
                }
                InfoModalType.WHY_IT_MATTERS -> {
                    StandardModalContainer(title = "Why It Matters", onDismiss = onDismiss) {
                        WhyItMattersContent()
                    }
                }
                InfoModalType.ABOUT -> {
                    StandardModalContainer(title = "About Yap & Render", onDismiss = onDismiss) {
                        AboutContent()
                    }
                }
                InfoModalType.ISL_DICTIONARY -> {
                    StandardModalContainer(title = "ISL Sign Dictionary", onDismiss = onDismiss) {
                        IslDictionaryContent(onSignWordSelected = onSignWordSelected, onDismiss = onDismiss)
                    }
                }
            }
        }
    }
}

/**
 * Exact replica of the Dashboard in user's image.png:
 * - Lime yellow underlay layer
 * - Warm paper/cream dialog card
 * - Grid icon badge + YOUR DASHBOARD uppercase subhead + "Hello, Mitarth." serif headline
 * - Coral M avatar profile card
 * - 3-column stats box (ACCOUNT: Active, TRANSLATOR: Ready, VOICE LANGUAGE: English (IN))
 * - ACCOUNT DETAILS list (NAME, EMAIL, ENGINE, RIG, VOCABULARY)
 * - Black button "Open translator ↘"
 */
@Composable
fun YourAccountDashboardCard(
    onDismiss: () -> Unit,
    onOpenTranslator: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        // Neon Lime Green Accent Underlay Box (Exact shadow styling from screenshot)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 6.dp)
                .background(YapLimeAccent, RoundedCornerShape(2.dp))
        )

        // Main Dashboard Cream Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.2.dp, YapHeadingBlack, RoundedCornerShape(2.dp)),
            shape = RoundedCornerShape(2.dp),
            colors = CardDefaults.cardColors(containerColor = YapCardWarm)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Top row with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Dark icon square with 4-box grid
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(YapHeadingBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Dashboard",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "YOUR DASHBOARD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.SansSerif,
                                color = YapSubtextGray
                            )
                            Text(
                                text = "Hello, Mitarth.",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = YapHeadingBlack,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    }

                    // Close Button ✕
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_close_dashboard")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = YapHeadingBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Profile Card Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YapCardTint)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Circular Orange 'M' avatar
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(YapOrangeAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "M",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Mitarth",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = YapHeadingBlack
                            )
                            Text(
                                text = "mpathak6207@gmail.com",
                                fontSize = 13.sp,
                                color = YapSubtextGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3-Column Stats Grid Container
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFDFD9CC))
                ) {
                    // Column 1: ACCOUNT
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "ACCOUNT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = YapSubtextGray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Active",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = YapHeadingBlack
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color(0xFFDFD9CC))
                    )

                    // Column 2: TRANSLATOR
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "TRANSLATOR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = YapSubtextGray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ready",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = YapHeadingBlack
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color(0xFFDFD9CC))
                    )

                    // Column 3: VOICE LANGUAGE
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "VOICE LANGUAGE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = YapSubtextGray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "English (IN)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = YapHeadingBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Section: ACCOUNT DETAILS
                Text(
                    text = "ACCOUNT DETAILS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = YapSubtextGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Detail Rows
                AccountDetailRow(label = "NAME", value = "Mitarth")
                AccountDetailRow(label = "EMAIL", value = "mpathak6207@gmail.com")
                AccountDetailRow(label = "ENGINE", value = "Three.js r185 (WebGL)")
                AccountDetailRow(label = "AVATAR RIG", value = "3D Mixamo Kinematics")
                AccountDetailRow(label = "DICTIONARY", value = "200+ Words & A-Z Fingerspelling")
                AccountDetailRow(label = "SPEECH NLP", value = "ISL SOV Rule Engine")

                Spacer(modifier = Modifier.height(20.dp))

                // Button: Open translator ↘
                Button(
                    onClick = onOpenTranslator,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YapHeadingBlack,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(2.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    modifier = Modifier.testTag("btn_dashboard_open_translator")
                ) {
                    Text(
                        text = "Open translator",
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
            }
        }
    }
}

@Composable
private fun AccountDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = YapSubtextGray
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = YapHeadingBlack
        )
    }
}

/**
 * Help & Comprehensive ISL Theory Guide
 */
@Composable
fun HelpTheoryModalCard(
    onDismiss: () -> Unit,
    onOpenDictionary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.2.dp, YapHeadingBlack, RoundedCornerShape(4.dp)),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = YapCardWarm)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "ISL KNOWLEDGE & THEORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = YapOrangeAccent
                    )
                    Text(
                        text = "Indian Sign Language Guide",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = YapHeadingBlack
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Theory Card 1: Linguistic Syntax & SOV Grammar
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, YapBorderColor, RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. ISL Grammar & Sentence Structure", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = YapHeadingBlack)
                    Text(
                        text = "Unlike English which follows Subject-Verb-Object (SVO), Indian Sign Language naturally follows Subject-Object-Verb (SOV) order with strict temporal markers:",
                        fontSize = 13.sp,
                        color = YapSubtextGray,
                        lineHeight = 18.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(YapCreamBackground)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "• English: \"I am going home tomorrow.\"\n• ISL Gloss: TOMORROW YOU HOME GO\n• English: \"What is your name?\"\n• ISL Gloss: YOU NAME WHAT",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = YapHeadingBlack,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Theory Card 2: A-Z Fingerspelling
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, YapBorderColor, RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("2. Procedural A to Z Fingerspelling", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = YapHeadingBlack)
                    Text(
                        text = "When a spoken word or proper noun (like a person's name or city) does not exist in the whole-word dictionary, the engine automatically decomposes the word into letters A through Z. Each letter triggers precise individual finger joint, thumb, and wrist keyframe angles.",
                        fontSize = 13.sp,
                        color = YapSubtextGray,
                        lineHeight = 18.sp
                    )
                }
            }

            // Theory Card 3: 3D Avatar Kinematics
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, YapBorderColor, RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("3. Mixamo Bone Rigging & Euler Mathematics", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = YapHeadingBlack)
                    Text(
                        text = "The avatar uses 3D Three.js WebGL. Each authored gesture is a delta rotation composed onto the bone's captured rest quaternion, so every finger and arm arc lands exactly where the sign table specifies. Both the Default and Human avatars run on the same Mixamo ybot rig — the Human look is a procedural face and warm per-vertex armour applied at runtime — so ISL playback is identical on both.",
                        fontSize = 13.sp,
                        color = YapSubtextGray,
                        lineHeight = 18.sp
                    )
                }
            }

            // Theory Card 4: Assigned Core Vocabulary
            Card(
                colors = CardDefaults.cardColors(containerColor = YapOrangeBg),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, YapOrangeAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("4. Assigned Vocabulary Words", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = YapOrangeDark)
                    Text(
                        text = "TIME, HOME, PERSON, YOU, HELLO, HELP, THANK YOU, DRINK, EAT, FOOD, WATER, YES, NO, PLEASE, SORRY, WELCOME, GOOD, BAD, STOP, WAIT, COME, GO, WANT, NEED, LIKE, KNOW, UNDERSTAND, ASK, DOCTOR, HOSPITAL, SCHOOL.",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = YapHeadingBlack,
                        lineHeight = 18.sp
                    )
                }
            }

            // Theory Card 5: Inclusivity
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, YapBorderColor, RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("5. 18+ Million Deaf & Hard-of-Hearing Indians", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = YapHeadingBlack)
                    Text(
                        text = "Indian Sign Language is a complete natural language. Yap & Render runs 100% locally and offline in the browser/app so communication is always accessible without high bandwidth requirements.",
                        fontSize = 13.sp,
                        color = YapSubtextGray,
                        lineHeight = 18.sp
                    )
                }
            }

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = YapHeadingBlack),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Got it", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StandardModalContainer(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, YapBorderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = YapCreamBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = YapHeadingBlack
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            content()
        }
    }
}

@Composable
private fun HowItWorksContent() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        val steps = listOf(
            Triple("1. Voice & Text Ingestion", "Real-time speech recognition and direct text entry capture natural conversation effortlessly.", "🎙"),
            Triple("2. ISL Syntax Engine", "Translates phrases into Indian Sign Language linguistic structures and word-level gestural markers.", "⚡"),
            Triple("3. 3D Kinematic Avatar", "A high-performance skeletal engine renders accurate hand gestures, fingerspelling, and body dynamics.", "🤖")
        )

        steps.forEach { (title, desc, icon) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, YapBorderColor, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(icon, fontSize = 22.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = YapHeadingBlack)
                        Text(desc, fontSize = 12.sp, color = YapSubtextGray, lineHeight = 17.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun WhyItMattersContent() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = YapOrangeBg),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, YapOrangeAccent.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "18+ Million Deaf & Hard-of-Hearing Individuals in India",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = YapOrangeDark
                )
                Text(
                    text = "Sign language is not just a translation; it is a primary, complete, and rich natural language with its own grammar and spatial semantics.",
                    fontSize = 12.sp,
                    color = YapHeadingBlack,
                    lineHeight = 17.sp
                )
            }
        }

        val points = listOf(
            "Equal Access" to "Empowers deaf students, workers, and citizens to access real-time audio information in public, educational, and digital spaces.",
            "Digital Inclusivity" to "Offline-ready rendering ensures that accessibility tools work without requiring high-speed data connections.",
            "Human Connection" to "Eliminates social barriers by making two-way communication between hearing and deaf individuals effortless."
        )

        points.forEach { (heading, desc) ->
            Column(modifier = Modifier.padding(vertical = 2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(heading, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = YapHeadingBlack)
                Text(desc, fontSize = 12.sp, color = YapSubtextGray, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun AboutContent() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Yap & Render is designed to make spoken and written language instantly visual in Indian Sign Language (ISL).",
            fontSize = 13.sp,
            color = YapHeadingBlack,
            lineHeight = 19.sp
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, YapBorderColor, RoundedCornerShape(10.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Key Capabilities", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = YapHeadingBlack)
                Text("• 3D Articulated Mannequin Avatar (Default Cyan & Human modes)", fontSize = 12.sp, color = YapSubtextGray)
                Text("• Real-time Speech-to-Sign translation", fontSize = 12.sp, color = YapSubtextGray)
                Text("• Full A-Z Fingerspelling fallback for any custom sentence", fontSize = 12.sp, color = YapSubtextGray)
                Text("• Variable playback speed control (0.5x to 2.0x)", fontSize = 12.sp, color = YapSubtextGray)
                Text("• 100% offline responsive client architecture", fontSize = 12.sp, color = YapSubtextGray)
            }
        }
    }
}

@Composable
private fun IslDictionaryContent(
    onSignWordSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Tap any sign below to watch the 3D avatar perform it:",
            fontSize = 13.sp,
            color = YapSubtextGray
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.height(340.dp)
        ) {
            items(IslSignDictionary.SIGN_DICTIONARY.values.toList()) { sign ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, YapBorderColor, RoundedCornerShape(8.dp))
                        .clickable {
                            onSignWordSelected(sign.word)
                            onDismiss()
                        }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(sign.word, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = YapHeadingBlack)
                            Text(sign.category, fontSize = 9.sp, color = YapOrangeAccent, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(sign.description, fontSize = 11.sp, color = YapSubtextGray, maxLines = 2)
                    }
                }
            }
        }
    }
}
