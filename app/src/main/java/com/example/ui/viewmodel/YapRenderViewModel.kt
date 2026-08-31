package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AvatarType
import com.example.data.model.JointPose
import com.example.data.model.SignKeyframe
import com.example.data.repository.IslSignDictionary
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

data class YapRenderUiState(
    val currentPose: JointPose = IslSignDictionary.IDLE_POSE,
    val avatarType: AvatarType = AvatarType.DEFAULT,
    val inputText: String = "YOU HOME",
    val phraseToPlay: String = "YOU HOME",
    val playTrigger: Long = 0L,
    val stopTrigger: Long = 0L,
    val replayTrigger: Long = 0L,
    val isSigning: Boolean = false,
    val currentWord: String = "",
    val currentKeyframeLabel: String = "",
    val statusText: String = "ready — words or A-Z fingerspelling",
    val speed: Float = 1.0f,
    val isLiveListening: Boolean = false,
    val isListeningVoice: Boolean = false,
    val isFullscreen: Boolean = false,
    val activeInfoModal: InfoModalType? = null
)

enum class InfoModalType {
    YOUR_ACCOUNT,
    HELP_GUIDE,
    HOW_IT_WORKS,
    WHY_IT_MATTERS,
    ABOUT,
    MITARTH_PROFILE,
    ISL_DICTIONARY
}

class YapRenderViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(YapRenderUiState())
    val uiState: StateFlow<YapRenderUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private var speechRecognizer: SpeechRecognizer? = null

    /** Set by the host so each played phrase is logged to the signed-in account. */
    var onPhrasePlayed: ((String) -> Unit)? = null

    init {
        initSpeechRecognizer()
    }

    private fun initSpeechRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(getApplication())) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplication()).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _uiState.update { it.copy(statusText = "Listening for speech...") }
                        }
                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            _uiState.update { it.copy(isListeningVoice = false) }
                        }
                        override fun onError(error: Int) {
                            _uiState.update {
                                it.copy(
                                    isListeningVoice = false,
                                    statusText = "Voice ready — tap Voice to speak"
                                )
                            }
                        }
                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                val spoken = matches[0].uppercase(Locale.getDefault())
                                onInputTextChanged(spoken)
                                playSignSequence(spoken)
                            }
                            _uiState.update { it.copy(isListeningVoice = false) }
                        }
                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                val partial = matches[0].uppercase(Locale.getDefault())
                                _uiState.update { it.copy(inputText = partial) }
                            }
                        }
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            }
        } catch (e: Exception) {
            // Speech recognizer fallback
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun setAvatarType(type: AvatarType) {
        _uiState.update { it.copy(avatarType = type) }
    }

    fun setSpeed(speed: Float) {
        _uiState.update { it.copy(speed = speed) }
    }

    fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    fun openModal(modal: InfoModalType) {
        _uiState.update { it.copy(activeInfoModal = modal) }
    }

    fun closeModal() {
        _uiState.update { it.copy(activeInfoModal = null) }
    }

    fun toggleLiveMode() {
        val newLive = !_uiState.value.isLiveListening
        _uiState.update {
            it.copy(
                isLiveListening = newLive,
                statusText = if (newLive) "● Live translation mode active" else "ready — words or A-Z fingerspelling"
            )
        }
        if (newLive) {
            startVoiceListening()
        } else {
            stopVoiceListening()
        }
    }

    fun startVoiceListening() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to translate into ISL")
            }
            _uiState.update { it.copy(isListeningVoice = true, statusText = "Listening for speech...") }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _uiState.update { it.copy(isListeningVoice = false, statusText = "Voice unavailable") }
        }
    }

    fun stopVoiceListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // ignore
        }
        _uiState.update { it.copy(isListeningVoice = false) }
    }

    fun playSignSequence(rawText: String? = null) {
        val textToPlay = rawText ?: _uiState.value.inputText
        if (textToPlay.isBlank()) return

        val triggerTime = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                phraseToPlay = textToPlay,
                playTrigger = triggerTime,
                isSigning = true,
                statusText = "signing: $textToPlay"
            )
        }
        onPhrasePlayed?.invoke(textToPlay)

        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val tokens = textToPlay.trim().split("\\s+".toRegex())
            for (token in tokens) {
                if (!isActive) break
                val upperToken = token.uppercase(Locale.getDefault())

                val directSign = IslSignDictionary.SIGN_DICTIONARY[upperToken]
                if (directSign != null) {
                    _uiState.update {
                        it.copy(
                            currentWord = upperToken,
                            statusText = "signing: $upperToken"
                        )
                    }
                    for (kf in directSign.keyframes) {
                        if (!isActive) break
                        _uiState.update { it.copy(currentKeyframeLabel = kf.label) }
                        animateToPose(kf.pose, (kf.durationMs / _uiState.value.speed).toLong())
                    }
                    delay((200L / _uiState.value.speed).toLong())
                } else {
                    _uiState.update {
                        it.copy(
                            currentWord = upperToken,
                            statusText = "fingerspelling: $upperToken"
                        )
                    }
                    for (char in upperToken) {
                        if (!isActive) break
                        if (char.isLetter()) {
                            val pose = IslSignDictionary.getLetterPose(char)
                            _uiState.update { it.copy(currentKeyframeLabel = "Letter '$char'") }
                            animateToPose(pose, (320L / _uiState.value.speed).toLong())
                            delay((80L / _uiState.value.speed).toLong())
                        }
                    }
                }
            }

            animateToPose(IslSignDictionary.IDLE_POSE, 400L)
            _uiState.update {
                it.copy(
                    isSigning = false,
                    currentWord = "",
                    currentKeyframeLabel = "",
                    statusText = "ready — words or A-Z fingerspelling"
                )
            }
        }
    }

    fun onAvatarStateChange(state: String) {
        when (state) {
            "loading" -> {
                _uiState.update { it.copy(statusText = "loading 3D avatar...") }
            }
            "ready" -> {
                _uiState.update {
                    it.copy(
                        isSigning = false,
                        statusText = "ready — words or A-Z fingerspelling"
                    )
                }
            }
            "signing" -> {
                _uiState.update {
                    it.copy(isSigning = true)
                }
            }
        }
    }

    fun stopSigning() {
        playbackJob?.cancel()
        playbackJob = null
        _uiState.update {
            it.copy(
                stopTrigger = System.currentTimeMillis(),
                isSigning = false,
                currentWord = "",
                currentKeyframeLabel = "",
                currentPose = IslSignDictionary.IDLE_POSE,
                statusText = "ready — words or A-Z fingerspelling"
            )
        }
    }

    fun replayCurrent() {
        _uiState.update {
            it.copy(
                replayTrigger = System.currentTimeMillis(),
                isSigning = true
            )
        }
        playSignSequence(_uiState.value.inputText)
    }

    fun onChipSelected(word: String) {
        val current = _uiState.value.inputText.trim()
        val next = if (current.isEmpty()) word else "$current $word"
        _uiState.update { it.copy(inputText = next) }
        playSignSequence(word)
    }

    private suspend fun animateToPose(target: JointPose, durationMs: Long) {
        val startPose = _uiState.value.currentPose
        val steps = (durationMs / 16).coerceAtLeast(10).toInt()
        val stepDelay = durationMs / steps

        for (i in 1..steps) {
            val progress = (i.toFloat() / steps).coerceIn(0f, 1f)
            // Cosine easing for ultra-smooth organic human joint motion
            val eased = (1f - kotlin.math.cos(progress * Math.PI.toFloat())) / 2f

            val interpolated = interpolatePose(startPose, target, eased)
            _uiState.update { it.copy(currentPose = interpolated) }
            delay(stepDelay)
        }
    }

    private fun interpolatePose(start: JointPose, end: JointPose, t: Float): JointPose {
        return JointPose(
            leftArmPitch = lerp(start.leftArmPitch, end.leftArmPitch, t),
            leftArmRoll = lerp(start.leftArmRoll, end.leftArmRoll, t),
            leftArmYaw = lerp(start.leftArmYaw, end.leftArmYaw, t),
            leftElbowBend = lerp(start.leftElbowBend, end.leftElbowBend, t),
            leftWristPitch = lerp(start.leftWristPitch, end.leftWristPitch, t),
            leftHandOpen = lerp(start.leftHandOpen, end.leftHandOpen, t),
            leftHandGesture = if (t < 0.5f) start.leftHandGesture else end.leftHandGesture,

            rightArmPitch = lerp(start.rightArmPitch, end.rightArmPitch, t),
            rightArmRoll = lerp(start.rightArmRoll, end.rightArmRoll, t),
            rightArmYaw = lerp(start.rightArmYaw, end.rightArmYaw, t),
            rightElbowBend = lerp(start.rightElbowBend, end.rightElbowBend, t),
            rightWristPitch = lerp(start.rightWristPitch, end.rightWristPitch, t),
            rightHandOpen = lerp(start.rightHandOpen, end.rightHandOpen, t),
            rightHandGesture = if (t < 0.5f) start.rightHandGesture else end.rightHandGesture,

            headPitch = lerp(start.headPitch, end.headPitch, t),
            headYaw = lerp(start.headYaw, end.headYaw, t),
            headRoll = lerp(start.headRoll, end.headRoll, t),
            chestLean = lerp(start.chestLean, end.chestLean, t),
            chestTwist = lerp(start.chestTwist, end.chestTwist, t)
        )
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + (end - start) * fraction
    }

    override fun onCleared() {
        super.onCleared()
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // ignore
        }
    }
}
