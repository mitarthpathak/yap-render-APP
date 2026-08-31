package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AuthRepository
import com.example.ui.components.YapNavyDark
import com.example.ui.components.YapRenderBrandLogo
import com.example.ui.components.tapBounce
import com.example.ui.theme.*

private val ROLE_OPTIONS = listOf("Student", "Educator", "Interpreter", "Family / Friend", "Researcher", "Other")
private val GOAL_OPTIONS = listOf(
    "Learn ISL", "Teach ISL", "Everyday accessibility", "Talk with family", "Research & study"
)

@Composable
fun AccountScreen(
    account: AuthRepository.Account?,
    stats: AuthRepository.Stats,
    history: List<AuthRepository.HistoryEntry>,
    onNavigateBack: () -> Unit,
    onOpenTranslator: () -> Unit,
    onOpenDictionary: () -> Unit,
    onReplayPhrase: (String) -> Unit,
    onClearHistory: () -> Unit,
    onUpdateProfile: (name: String, role: String, org: String, goal: String) -> Boolean,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val name = account?.name?.takeIf { it.isNotBlank() } ?: "You"
    val isGuest = account?.isGuest ?: false

    var showEdit by remember { mutableStateOf(false) }
    var showSignOut by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(YapCreamBackground),
        containerColor = YapCreamBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, YapBorderColor, RoundedCornerShape(20.dp))
                        .clickable { onNavigateBack() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("account_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = YapHeadingBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Text("Back", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = YapHeadingBlack)
                }

                YapRenderBrandLogo(iconSize = 34.dp, showTagline = false)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Header
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "YOUR DASHBOARD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = YapSubtextGray
                )
                Text(
                    text = "Hello, ${name.split(" ").first()}.",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = YapHeadingBlack,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Manage your Yap & Render profile, track your signing progress, and jump back into the 3D translator.",
                    fontSize = 14.sp,
                    color = YapSubtextGray,
                    lineHeight = 20.sp
                )
            }

            // Guest nudge
            if (isGuest) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(YapOrangeBg)
                        .border(1.dp, YapOrangeAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = YapOrangeDark, modifier = Modifier.size(20.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Browsing as a guest", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = YapOrangeDark)
                        Text(
                            "Create a free account to keep your streak and history.",
                            fontSize = 11.5.sp,
                            color = YapHeadingBlack
                        )
                    }
                    Text(
                        text = "Sign up",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(YapOrangeAccent)
                            .clickable { onSignOut() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // 2. Profile card with lime underlay
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(YapLimeAccent, RoundedCornerShape(14.dp))
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, YapHeadingBlack, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(AvatarTealBase)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = account?.initial ?: "Y",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = YapHeadingBlack
                                )
                                Text(
                                    text = account?.email?.takeIf { it.isNotBlank() } ?: "Guest session",
                                    fontSize = 13.sp,
                                    color = YapSubtextGray
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2E7D32))
                                    )
                                    Text(
                                        text = "Active Session",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showEdit = true },
                                modifier = Modifier.testTag("account_edit_btn")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit profile", tint = YapHeadingBlack, modifier = Modifier.size(18.dp))
                            }
                        }

                        HorizontalDivider(color = YapBorderColor)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatusColumn("ROLE", account?.role ?: "Explorer", isGreen = false, modifier = Modifier.weight(1f))
                            StatusColumn("TRANSLATOR", "Ready", isGreen = true, modifier = Modifier.weight(1f))
                            StatusColumn("MEMBER", memberSince(account?.joinedAtMillis), isGreen = false, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // 3. Progress / gamification
            Text(
                text = "YOUR PROGRESS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = YapSubtextGray
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile("Day streak", stats.dayStreak, "🔥", YapOrangeAccent, Modifier.weight(1f))
                StatTile("Phrases", stats.phrasesTranslated, "💬", AvatarTealBase, Modifier.weight(1f))
                StatTile("Signs", stats.wordsSigned, "🤟", YapLimeUnderlay, Modifier.weight(1f))
            }
            if (stats.lettersFingerspelled > 0) {
                Text(
                    text = "+ ${stats.lettersFingerspelled} letters fingerspelled A–Z",
                    fontSize = 11.5.sp,
                    color = YapMutedGray,
                    fontWeight = FontWeight.Medium
                )
            }

            // 4. Recent translations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT TRANSLATIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = YapSubtextGray
                )
                if (history.isNotEmpty()) {
                    Text(
                        text = "Clear",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = YapOrangeAccent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onClearHistory() }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, YapBorderColor, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
                    if (history.isEmpty()) {
                        Text(
                            text = "Nothing yet. Head to the translator and sign your first phrase.",
                            fontSize = 12.5.sp,
                            color = YapMutedGray,
                            modifier = Modifier.padding(14.dp)
                        )
                    } else {
                        history.forEachIndexed { index, entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onReplayPhrase(entry.phrase) }
                                    .padding(horizontal = 12.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.Replay,
                                    contentDescription = "Replay",
                                    tint = YapOrangeAccent,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text(
                                    text = entry.phrase,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = YapHeadingBlack,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = relativeTime(entry.timestamp),
                                    fontSize = 10.5.sp,
                                    color = YapMutedGray
                                )
                            }
                            if (index != history.lastIndex) {
                                HorizontalDivider(color = YapBorderSubtle, modifier = Modifier.padding(horizontal = 12.dp))
                            }
                        }
                    }
                }
            }

            // 5. Engine specs
            Text(
                text = "SYSTEM & ENGINE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = YapSubtextGray
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(YapLimeAccent, RoundedCornerShape(14.dp))
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, YapHeadingBlack, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        SpecRow(Icons.Default.Bolt, AvatarTealMid, "Render engine", "Three.js WebGL · GPU accelerated")
                        SpecRow(Icons.Default.Accessibility, YapOrangeAccent, "Rig standard", "Mixamo ybot skeleton + procedural fingers")
                        SpecRow(Icons.Default.Translate, YapNavyDark, "Grammar engine", "ISL SOV grammar · rule-based gloss")
                        SpecRow(Icons.Default.MenuBook, AvatarTealMid, "Vocabulary", "3,500+ signs + A–Z fingerspelling")
                        SpecRow(Icons.Default.CloudOff, YapOrangeAccent, "Runtime", "100% on-device · works offline")
                        SpecRow(Icons.Default.Mic, YapNavyDark, "Speech input", "Android Speech API · English (India)")

                        HorizontalDivider(color = YapBorderColor)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "App version",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = YapSubtextGray
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(YapWindowHeader)
                                    .border(1.dp, YapBorderColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "v2.4.0",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = YapHeadingBlack
                                )
                            }
                        }
                    }
                }
            }

            // 6. Quick actions
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { onOpenTranslator() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("account_open_translator_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YapNavyDark,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Open 3D Translator", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowOutward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }

                OutlinedButton(
                    onClick = { onOpenDictionary() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("account_open_dictionary_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = YapHeadingBlack),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(YapBorderColor)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp), tint = YapOrangeAccent)
                        Text("Browse ISL Vocabulary & Dictionary", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                OutlinedButton(
                    onClick = { showSignOut = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("account_sign_out_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = YapOrangeDark),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(YapOrangeAccent.copy(alpha = 0.5f))
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(if (isGuest) "Exit guest session" else "Sign out", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showEdit) {
        EditProfileDialog(
            account = account,
            onDismiss = { showEdit = false },
            onSave = { n, r, o, g ->
                if (onUpdateProfile(n, r, o, g)) showEdit = false
            }
        )
    }

    if (showSignOut) {
        AlertDialog(
            onDismissRequest = { showSignOut = false },
            confirmButton = {
                TextButton(onClick = {
                    showSignOut = false
                    onSignOut()
                }) { Text("Sign out", color = YapOrangeDark, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showSignOut = false }) { Text("Cancel", color = YapSubtextGray) }
            },
            title = { Text("Sign out?", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif) },
            text = {
                Text(
                    if (isGuest) "Your guest streak and history will be cleared from this device."
                    else "You can sign back in anytime. Your progress stays on this device."
                )
            },
            containerColor = YapCardWarm
        )
    }
}

@Composable
private fun EditProfileDialog(
    account: AuthRepository.Account?,
    onDismiss: () -> Unit,
    onSave: (name: String, role: String, org: String, goal: String) -> Unit
) {
    var name by remember { mutableStateOf(account?.name?.takeIf { !it.equals("Guest", true) } ?: "") }
    var org by remember { mutableStateOf(account?.organization ?: "") }
    var role by remember { mutableStateOf(account?.role?.takeIf { it in ROLE_OPTIONS } ?: ROLE_OPTIONS.first()) }
    var goal by remember { mutableStateOf(account?.goal?.takeIf { it in GOAL_OPTIONS } ?: GOAL_OPTIONS.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(name, role, org, goal) }) {
                Text("Save", color = YapHeadingBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = YapSubtextGray) }
        },
        title = { Text("Edit profile", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif) },
        containerColor = YapCardWarm,
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full name") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                DialogChipRow("I am a", ROLE_OPTIONS, role) { role = it }
                DialogChipRow("Here to", GOAL_OPTIONS, goal) { goal = it }
                OutlinedTextField(
                    value = org,
                    onValueChange = { org = it },
                    label = { Text("Organization / Institute") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
private fun DialogChipRow(title: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = YapSubtextGray)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            options.forEach { opt ->
                val sel = opt == selected
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (sel) YapHeadingBlack else Color(0xFFF3EFE7))
                        .border(1.dp, if (sel) YapHeadingBlack else YapBorderColor, RoundedCornerShape(9.dp))
                        .clickable { onSelect(opt) }
                        .padding(horizontal = 11.dp, vertical = 7.dp)
                ) {
                    Text(opt, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (sel) Color.White else YapSubtextGray)
                }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, target: Int, glyph: String, tint: Color, modifier: Modifier = Modifier) {
    var start by remember { mutableStateOf(false) }
    LaunchedEffect(target) { start = true }
    val shown by animateIntAsState(
        targetValue = if (start) target else 0,
        animationSpec = tween(850),
        label = "count_$label"
    )
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, YapBorderColor, RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center
        ) {
            Text(glyph, fontSize = 15.sp)
        }
        Text(
            text = "$shown",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = YapHeadingBlack
        )
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = YapSubtextGray)
    }
}

@Composable
private fun StatusColumn(label: String, value: String, isGreen: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = YapSubtextGray)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            if (isGreen) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF2E7D32)))
            }
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = YapHeadingBlack, maxLines = 1)
        }
    }
}

@Composable
private fun SpecRow(icon: ImageVector, accent: Color, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(17.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label.uppercase(),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = YapMutedGray
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = YapHeadingBlack,
                lineHeight = 17.sp
            )
        }
    }
}

private fun memberSince(millis: Long?): String {
    if (millis == null || millis <= 0L) return "Today"
    val days = ((System.currentTimeMillis() - millis) / 86_400_000L).toInt()
    return when {
        days <= 0 -> "Today"
        days == 1 -> "1 day"
        days < 30 -> "$days days"
        days < 60 -> "1 month"
        else -> "${days / 30} months"
    }
}

private fun relativeTime(millis: Long): String {
    val diff = System.currentTimeMillis() - millis
    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000L -> "${diff / 3_600_000}h ago"
        diff < 2_592_000_000L -> "${diff / 86_400_000}d ago"
        else -> "a while ago"
    }
}
