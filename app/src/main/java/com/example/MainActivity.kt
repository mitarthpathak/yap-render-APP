package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AvatarType
import com.example.ui.components.*
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.YapCreamBackground
import com.example.ui.viewmodel.AuthGate
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.InfoModalType
import com.example.ui.viewmodel.YapRenderViewModel
import kotlinx.coroutines.launch

enum class AppScreenState {
    HOME,
    SIGN,
    ACCOUNT
}

class MainActivity : ComponentActivity() {
    private val viewModel: YapRenderViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                RootGate(authViewModel = authViewModel, yapViewModel = viewModel)
            }
        }
    }
}

@Composable
fun RootGate(authViewModel: AuthViewModel, yapViewModel: YapRenderViewModel) {
    val auth by authViewModel.uiState.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = auth.gate,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
        label = "auth_gate"
    ) { gate ->
        when (gate) {
            AuthGate.LOADING -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(YapCreamBackground),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = YapNavyDark, strokeWidth = 2.dp)
                }
            }

            AuthGate.UNAUTHENTICATED -> {
                AuthScreen(
                    isSubmitting = auth.isSubmitting,
                    error = auth.error,
                    onClearError = authViewModel::clearError,
                    onSignIn = authViewModel::signIn,
                    onSignUp = authViewModel::signUp,
                    onGuest = authViewModel::continueAsGuest
                )
            }

            AuthGate.ONBOARDING -> {
                OnboardingScreen(onFinish = authViewModel::finishOnboarding)
            }

            AuthGate.AUTHENTICATED -> {
                YapRenderApp(viewModel = yapViewModel, authViewModel = authViewModel)
            }
        }
    }
}

@Composable
fun YapRenderApp(viewModel: YapRenderViewModel, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val auth by authViewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(AppScreenState.HOME) }
    var currentDockTab by remember { mutableStateOf(DockTab.HOME) }

    val account = auth.account
    val displayName = account?.name?.takeIf { it.isNotBlank() } ?: "You"

    // Restore this account's saved avatar + speed preferences once on entry.
    LaunchedEffect(Unit) {
        viewModel.onPhrasePlayed = { authViewModel.recordTranslation(it) }
        account?.let {
            runCatching { AvatarType.valueOf(it.avatarType) }.getOrNull()?.let(viewModel::setAvatarType)
            viewModel.setSpeed(it.speed)
        }
    }

    // Greet the user: the avatar signs HELLO whenever a fresh session begins.
    LaunchedEffect(auth.welcomeNonce) {
        if (auth.welcomeNonce != 0L) {
            scrollState.animateScrollTo(0)
            viewModel.playSignSequence("HELLO")
        }
    }

    val persistPrefs: () -> Unit = {
        authViewModel.savePreferences(uiState.avatarType.name, uiState.speed)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceListening()
        }
    }

    val requestVoiceInput = {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            viewModel.startVoiceListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Handle system back press
    BackHandler(enabled = currentScreen == AppScreenState.ACCOUNT) {
        currentScreen = AppScreenState.HOME
        currentDockTab = DockTab.HOME
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(YapCreamBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = YapCreamBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(YapCreamBackground)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    val forward = targetState != AppScreenState.HOME
                    val dir = if (forward) 1 else -1
                    (slideInHorizontally(tween(320)) { w -> dir * w / 3 } + fadeIn(tween(240)))
                        .togetherWith(
                            slideOutHorizontally(tween(320)) { w -> -dir * w / 3 } + fadeOut(tween(200))
                        )
                },
                label = "screen_transition"
            ) { screen ->
            when (screen) {
                AppScreenState.SIGN -> {
                    // Camera Sign → Text, the web app's "You Sign. We Speak." stage.
                    SignCameraWebView(
                        onClose = {
                            currentScreen = AppScreenState.HOME
                            currentDockTab = DockTab.HOME
                        }
                    )
                }

                AppScreenState.ACCOUNT -> {
                    AccountScreen(
                        account = account,
                        stats = auth.stats,
                        history = auth.history,
                        onNavigateBack = {
                            currentScreen = AppScreenState.HOME
                            currentDockTab = DockTab.HOME
                        },
                        onOpenTranslator = {
                            currentScreen = AppScreenState.HOME
                            currentDockTab = DockTab.HOME
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0)
                            }
                            viewModel.playSignSequence()
                        },
                        onOpenDictionary = {
                            viewModel.openModal(InfoModalType.ISL_DICTIONARY)
                        },
                        onReplayPhrase = { phrase ->
                            currentScreen = AppScreenState.HOME
                            currentDockTab = DockTab.HOME
                            coroutineScope.launch { scrollState.animateScrollTo(0) }
                            viewModel.onInputTextChanged(phrase)
                            viewModel.playSignSequence(phrase)
                        },
                        onClearHistory = authViewModel::clearHistory,
                        onUpdateProfile = authViewModel::updateProfile,
                        onSignOut = authViewModel::signOut
                    )
                }

                AppScreenState.HOME -> {
                    // Main Scrollable Landing Page Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(bottom = 96.dp) // Room for bottom floating dock
                    ) {
                        // Top Brand & Navigation Bar (Logo + signed-in username)
                        TopNavBar(
                            isLoggedIn = true,
                            username = displayName,
                            onAccountClick = {
                                currentScreen = AppScreenState.ACCOUNT
                                currentDockTab = DockTab.ACCOUNT
                            },
                            onLogoClick = {
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(0)
                                }
                            }
                        )

                        // 1. Prominent Mac Window 3D Avatar Viewport right on the Landing Page!
                        Reveal(delayMillis = 40) {
                        AvatarStudioWidget(
                            uiState = uiState,
                            onInputChanged = { viewModel.onInputTextChanged(it) },
                            onPlay = { viewModel.playSignSequence() },
                            onStop = { viewModel.stopSigning() },
                            onReplay = { viewModel.replayCurrent() },
                            onVoiceClick = { requestVoiceInput() },
                            onLiveToggle = {
                                val permissionCheck = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                )
                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                    viewModel.toggleLiveMode()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            onAvatarTypeChanged = {
                                viewModel.setAvatarType(it)
                                persistPrefs()
                            },
                            onSpeedChanged = {
                                viewModel.setSpeed(it)
                                persistPrefs()
                            },
                            onChipSelected = { viewModel.onChipSelected(it) },
                            onToggleFullscreen = { viewModel.toggleFullscreen() },
                            onAvatarStateChange = { viewModel.onAvatarStateChange(it) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. Hero Mission & Headline Section
                        Reveal(delayMillis = 140) {
                        HeroSection(
                            onSeeInActionClick = {
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(0)
                                }
                                viewModel.playSignSequence("YOU HOME")
                            },
                            onExploreIdeaClick = {
                                viewModel.openModal(InfoModalType.HELP_GUIDE)
                            },
                            onOpenDictionary = {
                                viewModel.openModal(InfoModalType.ISL_DICTIONARY)
                            }
                        )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. "Say it. See it. Share it." Detailed Pipeline & Interactive Vocabulary
                        Reveal(delayMillis = 240) {
                        SayItSeeItShareItSection(
                            onSignWordSelected = { word ->
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(0)
                                }
                                viewModel.onInputTextChanged(word)
                                viewModel.playSignSequence(word)
                            },
                            onExploreTheoryClick = {
                                viewModel.openModal(InfoModalType.HELP_GUIDE)
                            }
                        )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
            }

            // Floating Bottom Dock positioned just above navigation bar.
            // The camera stage is full screen with its own close button.
            if (currentScreen != AppScreenState.SIGN) BottomFloatingDock(
                currentTab = currentDockTab,
                onTabSelected = { tab ->
                    currentDockTab = tab
                    when (tab) {
                        DockTab.HOME -> {
                            currentScreen = AppScreenState.HOME
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0)
                            }
                        }
                        DockTab.SIGN -> {
                            currentScreen = AppScreenState.SIGN
                        }
                        DockTab.HELP -> {
                            viewModel.openModal(InfoModalType.HELP_GUIDE)
                        }
                        DockTab.ACCOUNT -> {
                            currentScreen = AppScreenState.ACCOUNT
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Info Modal Sheet (for Help Guide & Dictionary)
            uiState.activeInfoModal?.let { modal ->
                InfoModalsSheet(
                    modalType = modal,
                    onDismiss = {
                        viewModel.closeModal()
                    },
                    onSignWordSelected = { word ->
                        viewModel.closeModal()
                        currentScreen = AppScreenState.HOME
                        currentDockTab = DockTab.HOME
                        coroutineScope.launch {
                            scrollState.animateScrollTo(0)
                        }
                        viewModel.onInputTextChanged(word)
                        viewModel.playSignSequence(word)
                    },
                    onOpenTranslator = {
                        viewModel.closeModal()
                        currentScreen = AppScreenState.HOME
                        currentDockTab = DockTab.HOME
                        coroutineScope.launch {
                            scrollState.animateScrollTo(0)
                        }
                        viewModel.playSignSequence()
                    }
                )
            }

            // Post sign-in celebration + greeting (avatar signs HELLO alongside)
            WelcomeOverlay(
                nonce = auth.welcomeNonce,
                name = displayName,
                onDismiss = authViewModel::consumeWelcome,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
