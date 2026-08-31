package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.YapRenderFullStackedLogo
import com.example.ui.components.YapNavyDark
import com.example.ui.components.tapBounce
import com.example.ui.theme.*

private enum class AuthMode { SIGN_IN, SIGN_UP }

private val ROLE_OPTIONS = listOf("Student", "Educator", "Interpreter", "Family / Friend", "Researcher", "Other")
private val GOAL_OPTIONS = listOf(
    "Learn ISL", "Teach ISL", "Everyday accessibility", "Talk with family", "Research & study"
)

@Composable
fun AuthScreen(
    isSubmitting: Boolean,
    error: String?,
    onClearError: () -> Unit,
    onSignIn: (email: String, password: String) -> Unit,
    onSignUp: (name: String, email: String, password: String, role: String, org: String, goal: String) -> Unit,
    onGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scroll = rememberScrollState()

    var mode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var org by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(ROLE_OPTIONS.first()) }
    var goal by remember { mutableStateOf(GOAL_OPTIONS.first()) }
    var showPassword by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val shownError = localError ?: error

    // Error shake
    val shakeX = remember { Animatable(0f) }
    LaunchedEffect(shownError) {
        if (shownError != null) {
            shakeX.snapTo(0f)
            val path = listOf(0f, -14f, 12f, -8f, 5f, 0f)
            for (p in path) shakeX.animateTo(p, tween(46))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(YapCreamBackground)
    ) {
        AuroraBackdrop(Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            var logoShown by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { logoShown = true }
            AnimatedVisibility(
                visible = logoShown,
                enter = fadeIn(tween(500)) + slideInVertically(tween(560)) { it / 4 }
            ) {
                YapRenderFullStackedLogo(iconSize = 84.dp)
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = "Indian Sign Language, rendered live.",
                fontSize = 13.sp,
                color = YapSubtextGray,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(22.dp))

            // Lime underlay + mac-window auth card
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 6.dp, y = 6.dp)
                        .background(YapLimeAccent, RoundedCornerShape(14.dp))
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.2.dp, YapHeadingBlack, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        MacHeader()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { translationX = shakeX.value }
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            SegmentedToggle(
                                mode = mode,
                                onSelect = {
                                    if (it != mode) {
                                        mode = it
                                        localError = null
                                        onClearError()
                                    }
                                }
                            )

                            AnimatedContent(
                                targetState = mode,
                                transitionSpec = {
                                    (fadeIn(tween(220)) togetherWith fadeOut(tween(160)))
                                },
                                label = "auth_mode"
                            ) { m ->
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text(
                                        text = if (m == AuthMode.SIGN_IN) "Welcome back" else "Create your account",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        color = YapHeadingBlack
                                    )

                                    if (m == AuthMode.SIGN_UP) {
                                        AuthField(
                                            label = "Full name",
                                            value = name,
                                            onValueChange = { name = it; localError = null },
                                            testTag = "auth_name"
                                        )
                                    }

                                    AuthField(
                                        label = "Email",
                                        value = email,
                                        onValueChange = { email = it; localError = null },
                                        keyboardType = KeyboardType.Email,
                                        testTag = "auth_email"
                                    )

                                    AuthField(
                                        label = "Password",
                                        value = password,
                                        onValueChange = { password = it; localError = null },
                                        keyboardType = KeyboardType.Password,
                                        visual = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailing = {
                                            IconButton(onClick = { showPassword = !showPassword }) {
                                                Icon(
                                                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                                    contentDescription = "Toggle password",
                                                    tint = YapMutedGray,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        },
                                        testTag = "auth_password"
                                    )

                                    if (m == AuthMode.SIGN_UP) {
                                        AuthField(
                                            label = "Confirm password",
                                            value = confirm,
                                            onValueChange = { confirm = it; localError = null },
                                            keyboardType = KeyboardType.Password,
                                            visual = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                            testTag = "auth_confirm"
                                        )

                                        ChipRow(
                                            title = "I am a",
                                            options = ROLE_OPTIONS,
                                            selected = role,
                                            onSelect = { role = it }
                                        )
                                        ChipRow(
                                            title = "Here to",
                                            options = GOAL_OPTIONS,
                                            selected = goal,
                                            onSelect = { goal = it }
                                        )
                                        AuthField(
                                            label = "Organization / Institute (optional)",
                                            value = org,
                                            onValueChange = { org = it },
                                            testTag = "auth_org"
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(
                                visible = shownError != null,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(YapOrangeBg)
                                        .border(1.dp, YapOrangeAccent.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = shownError.orEmpty(),
                                        fontSize = 12.5.sp,
                                        color = YapOrangeDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    localError = validate(mode, name, email, password, confirm)
                                    if (localError != null) return@Button
                                    if (mode == AuthMode.SIGN_IN) {
                                        onSignIn(email, password)
                                    } else {
                                        onSignUp(name, email, password, role, org, goal)
                                    }
                                },
                                enabled = !isSubmitting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("auth_submit"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = YapNavyDark,
                                    contentColor = Color.White
                                )
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = if (mode == AuthMode.SIGN_IN) "Sign in" else "Create account",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (mode == AuthMode.SIGN_IN) "New here?" else "Already have an account?",
                                    fontSize = 12.5.sp,
                                    color = YapSubtextGray
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (mode == AuthMode.SIGN_IN) "Create one" else "Sign in",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = YapOrangeAccent,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            mode = if (mode == AuthMode.SIGN_IN) AuthMode.SIGN_UP else AuthMode.SIGN_IN
                                            localError = null
                                            onClearError()
                                        }
                                        .padding(4.dp)
                                        .testTag("auth_switch_mode")
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            OutlinedButton(
                onClick = onGuest,
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("auth_guest"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = YapHeadingBlack),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(YapBorderColor)
                )
            ) {
                Text("Continue as guest", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = "SIH 2026 Prototype  •  100% on-device  •  No data leaves your phone",
                fontSize = 10.5.sp,
                color = YapMutedGray,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun validate(
    mode: AuthMode,
    name: String,
    email: String,
    password: String,
    confirm: String
): String? {
    if (mode == AuthMode.SIGN_UP && name.trim().length < 2) return "Enter your full name."
    if (!email.trim().matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))) return "That email address looks off."
    if (password.length < 6) return "Password needs at least 6 characters."
    if (mode == AuthMode.SIGN_UP && password != confirm) return "Passwords don't match."
    return null
}

@Composable
private fun MacHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(YapWindowHeader)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Box(Modifier.size(11.dp).clip(CircleShape).background(DotRed))
            Box(Modifier.size(11.dp).clip(CircleShape).background(DotYellow))
            Box(Modifier.size(11.dp).clip(CircleShape).background(DotGreen))
        }
        Text(
            text = "account.access",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = YapHeadingBlack,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.width(1.dp))
    }
}

@Composable
private fun SegmentedToggle(mode: AuthMode, onSelect: (AuthMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(YapWindowHeader)
            .border(1.dp, YapBorderColor, RoundedCornerShape(10.dp))
            .padding(3.dp)
    ) {
        SegmentButton("Sign in", mode == AuthMode.SIGN_IN, Modifier.weight(1f)) { onSelect(AuthMode.SIGN_IN) }
        SegmentButton("Sign up", mode == AuthMode.SIGN_UP, Modifier.weight(1f)) { onSelect(AuthMode.SIGN_UP) }
    }
}

@Composable
private fun SegmentButton(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) YapHeadingBlack else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else YapSubtextGray
        )
    }
}

@Composable
private fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    visual: VisualTransformation = VisualTransformation.None,
    trailing: @Composable (() -> Unit)? = null,
    testTag: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        visualTransformation = visual,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(),
        trailingIcon = trailing,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = YapHeadingBlack,
            unfocusedBorderColor = YapBorderColor,
            focusedLabelColor = YapHeadingBlack,
            cursorColor = YapOrangeAccent,
            focusedContainerColor = Color(0xFFFCFBF8),
            unfocusedContainerColor = Color(0xFFFCFBF8)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    )
}

@Composable
private fun ChipRow(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            text = title.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = YapSubtextGray
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            options.forEach { opt ->
                val isSel = opt == selected
                Box(
                    modifier = Modifier
                        .tapBounce()
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSel) YapHeadingBlack else Color(0xFFF7F5EE))
                        .border(
                            1.dp,
                            if (isSel) YapHeadingBlack else YapBorderColor,
                            RoundedCornerShape(9.dp)
                        )
                        .clickable { onSelect(opt) }
                        .padding(horizontal = 11.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = opt,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSel) Color.White else YapSubtextGray
                    )
                }
            }
        }
    }
}

/** Slow-drifting soft colour blooms behind the card — no blur API, minSdk-safe. */
@Composable
private fun AuroraBackdrop(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "aurora")
    val p by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "aurora_p"
    )
    val q by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(13000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "aurora_q"
    )

    Box(modifier = modifier) {
        Box(
            Modifier
                .size(320.dp)
                .offset(x = (-70 + p * 40).dp, y = (40 + q * 60).dp)
                .background(
                    Brush.radialGradient(
                        listOf(YapLimeAccent.copy(alpha = 0.30f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(300.dp)
                .offset(x = (60 - q * 40).dp, y = (-40 + p * 50).dp)
                .background(
                    Brush.radialGradient(
                        listOf(YapOrangeAccent.copy(alpha = 0.16f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .size(360.dp)
                .offset(x = (p * 50 - 25).dp, y = (40 - q * 40).dp)
                .background(
                    Brush.radialGradient(
                        listOf(AvatarTealBase.copy(alpha = 0.16f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
    }
}
