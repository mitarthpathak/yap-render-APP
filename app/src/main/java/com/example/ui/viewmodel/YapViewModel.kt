package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.YapDatabase
import com.example.data.local.YapProjectEntity
import com.example.data.model.*
import com.example.data.repository.YapRepository
import com.example.engine.AudioEngine
import com.example.engine.YapPlaybackEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class YapStudioState(
    val currentProjectId: Long = 0L,
    val projectTitle: String = "Viral Yap #1",
    val scriptText: String = "Why do 99% of programmers start twenty side projects and never finish any of them? Because writing the architecture is fun, but fixing CSS in production at 3 AM is pure pain!",
    val aspectRatio: AspectRatioOption = AspectRatioOption.RATIO_9_16,
    val backgroundTheme: BackgroundTheme = BackgroundTheme.CYBERPUNK_GRID,
    val captionStyle: CaptionStyle = CaptionStyle.HORMOZI_POP,
    val avatarMascot: AvatarMascot = AvatarMascot.CYBER_BOT,
    val voicePreset: String = "Standard",
    val speechPitch: Float = 1.0f,
    val speechRate: Float = 1.1f,
    val bgmTrack: BgmTrack = BgmTrack.SYNTHWAVE,
    val bgmVolume: Float = 0.35f,
    val captionFontSize: Int = 26,
    val captionColorHex: String = "#FFFFFF",
    val captionHighlightColorHex: String = "#FACC15",
    val captionPosition: String = "Bottom",
    val isUppercase: Boolean = true,
    val showWatermark: Boolean = true,
    val isFavorite: Boolean = false,
    val isRendering: Boolean = false,
    val renderProgress: Float = 0f,
    val renderedExportSuccess: Boolean = false,
    val selectedStudioTab: Int = 0, // 0: Script, 1: Visuals, 2: Audio, 3: Export
    val currentPose: JointPose = com.example.data.repository.IslSignDictionary.IDLE_POSE
)

class YapViewModel(application: Application) : AndroidViewModel(application) {

    private val database = YapDatabase.getDatabase(application)
    private val repository = YapRepository(database.yapProjectDao())
    val audioEngine = AudioEngine(application)
    val playbackEngine = YapPlaybackEngine()

    private val _studioState = MutableStateFlow(YapStudioState())
    val studioState: StateFlow<YapStudioState> = _studioState.asStateFlow()

    val allProjects: StateFlow<List<YapProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteProjects: StateFlow<List<YapProjectEntity>> = repository.favoriteProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trendingTemplates: List<YapTemplate> = repository.getTrendingTemplates()

    private var playbackJob: Job? = null
    private var renderJob: Job? = null

    init {
        // Initial parse of default script
        playbackEngine.parseScriptToTimedWords(
            script = _studioState.value.scriptText,
            speechRate = _studioState.value.speechRate
        )

        audioEngine.onSpeechComplete = {
            // Speech finished callback
        }
    }

    fun setStudioTab(tabIndex: Int) {
        _studioState.update { it.copy(selectedStudioTab = tabIndex) }
    }

    fun updateTitle(newTitle: String) {
        _studioState.update { it.copy(projectTitle = newTitle) }
    }

    fun updateScriptText(newText: String) {
        _studioState.update { it.copy(scriptText = newText) }
        playbackEngine.parseScriptToTimedWords(newText, _studioState.value.speechRate)
    }

    fun updateAspectRatio(ratio: AspectRatioOption) {
        _studioState.update { it.copy(aspectRatio = ratio) }
    }

    fun updateBackgroundTheme(theme: BackgroundTheme) {
        _studioState.update { it.copy(backgroundTheme = theme) }
    }

    fun updateCaptionStyle(style: CaptionStyle) {
        _studioState.update { it.copy(captionStyle = style) }
    }

    fun updateAvatarMascot(mascot: AvatarMascot) {
        _studioState.update { it.copy(avatarMascot = mascot) }
    }

    fun updateSpeechPitch(pitch: Float) {
        _studioState.update { it.copy(speechPitch = pitch) }
    }

    fun updateSpeechRate(rate: Float) {
        _studioState.update { it.copy(speechRate = rate) }
        playbackEngine.parseScriptToTimedWords(_studioState.value.scriptText, rate)
    }

    fun updateBgmTrack(track: BgmTrack) {
        _studioState.update { it.copy(bgmTrack = track) }
        if (playbackEngine.isPlaying.value) {
            audioEngine.startBgm(track, _studioState.value.bgmVolume)
        }
    }

    fun updateBgmVolume(volume: Float) {
        _studioState.update { it.copy(bgmVolume = volume) }
        audioEngine.updateBgmVolume(volume)
    }

    fun updateCaptionFontSize(size: Int) {
        _studioState.update { it.copy(captionFontSize = size) }
    }

    fun updateCaptionColor(hex: String) {
        _studioState.update { it.copy(captionColorHex = hex) }
    }

    fun updateCaptionHighlightColor(hex: String) {
        _studioState.update { it.copy(captionHighlightColorHex = hex) }
    }

    fun updateCaptionPosition(pos: String) {
        _studioState.update { it.copy(captionPosition = pos) }
    }

    fun toggleUppercase() {
        _studioState.update { it.copy(isUppercase = !it.isUppercase) }
    }

    fun toggleWatermark() {
        _studioState.update { it.copy(showWatermark = !it.showWatermark) }
    }

    fun togglePlayPause() {
        val state = _studioState.value
        val isNowPlaying = !playbackEngine.isPlaying.value
        playbackEngine.togglePlayPause(onComplete = {
            audioEngine.stopBgm()
            audioEngine.stopSpeaking()
            stopSigning()
        })

        if (isNowPlaying) {
            audioEngine.speak(
                text = state.scriptText,
                pitch = state.speechPitch,
                speechRate = state.speechRate
            )
            audioEngine.startBgm(state.bgmTrack, state.bgmVolume)
            playSignSequence(state.scriptText)
        } else {
            audioEngine.stopSpeaking()
            audioEngine.stopBgm()
            stopSigning()
        }
    }

    private fun playSignSequence(text: String) {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val tokens = text.trim().split("\\s+".toRegex())
            for (token in tokens) {
                if (!isActive) break
                val upperToken = token.uppercase(java.util.Locale.getDefault())

                val directSign = com.example.data.repository.IslSignDictionary.SIGN_DICTIONARY[upperToken]
                if (directSign != null) {
                    for (kf in directSign.keyframes) {
                        if (!isActive) break
                        animateToPose(kf.pose, (kf.durationMs / _studioState.value.speechRate).toLong())
                    }
                    delay((150L / _studioState.value.speechRate).toLong())
                } else {
                    for (char in upperToken) {
                        if (!isActive) break
                        if (char.isLetter()) {
                            val pose = com.example.data.repository.IslSignDictionary.getLetterPose(char)
                            animateToPose(pose, (280L / _studioState.value.speechRate).toLong())
                            delay((60L / _studioState.value.speechRate).toLong())
                        }
                    }
                }
            }
            animateToPose(com.example.data.repository.IslSignDictionary.IDLE_POSE, 400L)
        }
    }

    private fun stopSigning() {
        playbackJob?.cancel()
        playbackJob = null
        _studioState.update { it.copy(currentPose = com.example.data.repository.IslSignDictionary.IDLE_POSE) }
    }

    private suspend fun animateToPose(target: JointPose, durationMs: Long) {
        val startPose = _studioState.value.currentPose
        val steps = (durationMs / 16).coerceAtLeast(8).toInt()
        val stepDelay = durationMs / steps

        for (i in 1..steps) {
            val progress = (i.toFloat() / steps).coerceIn(0f, 1f)
            val eased = (1f - kotlin.math.cos(progress * Math.PI.toFloat())) / 2f
            val interpolated = interpolatePose(startPose, target, eased)
            _studioState.update { it.copy(currentPose = interpolated) }
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

    private fun lerp(start: Float, end: Float, fraction: Float): Float = start + (end - start) * fraction

    fun seekTo(positionMs: Long) {
        playbackEngine.seekTo(positionMs)
    }

    fun playSoundEffect(effect: SoundEffect) {
        audioEngine.playSoundEffect(effect)
        Toast.makeText(getApplication(), "Playing ${effect.title}", Toast.LENGTH_SHORT).show()
    }

    fun applyTemplate(template: YapTemplate) {
        _studioState.update {
            it.copy(
                projectTitle = template.title,
                scriptText = template.script,
                backgroundTheme = template.backgroundTheme,
                captionStyle = template.captionStyle,
                avatarMascot = template.mascot,
                bgmTrack = template.bgm,
                speechRate = template.speechRate
            )
        }
        playbackEngine.parseScriptToTimedWords(template.script, template.speechRate)
        Toast.makeText(getApplication(), "Loaded Template: ${template.title}", Toast.LENGTH_SHORT).show()
    }

    fun generateYapFromPrompt(topic: String, style: String) {
        val generated = repository.generateQuickYap(topic, style)
        updateScriptText(generated)
        updateTitle("Yap: ${topic.ifBlank { "Modern Takes" }}")
        Toast.makeText(getApplication(), "Generated new script!", Toast.LENGTH_SHORT).show()
    }

    fun saveCurrentProject() {
        viewModelScope.launch {
            val state = _studioState.value
            val entity = YapProjectEntity(
                id = state.currentProjectId,
                title = state.projectTitle,
                scriptText = state.scriptText,
                aspectRatio = state.aspectRatio.label,
                captionStyle = state.captionStyle.displayName,
                backgroundTheme = state.backgroundTheme.displayName,
                avatarMascot = state.avatarMascot.displayName,
                speechPitch = state.speechPitch,
                speechRate = state.speechRate,
                bgmTrack = state.bgmTrack.title,
                bgmVolume = state.bgmVolume,
                captionFontSize = state.captionFontSize,
                captionColorHex = state.captionColorHex,
                captionHighlightColorHex = state.captionHighlightColorHex,
                captionPosition = state.captionPosition,
                isUppercase = state.isUppercase,
                showWatermark = state.showWatermark,
                isFavorite = state.isFavorite,
                lastModified = System.currentTimeMillis(),
                estimatedDurationMs = playbackEngine.totalDurationMs.value
            )

            val newId = repository.saveProject(entity)
            if (state.currentProjectId == 0L) {
                _studioState.update { it.copy(currentProjectId = newId) }
            }
            Toast.makeText(getApplication(), "Project Saved to Device 💾", Toast.LENGTH_SHORT).show()
        }
    }

    fun loadProject(entity: YapProjectEntity) {
        val ratio = AspectRatioOption.entries.find { it.label == entity.aspectRatio } ?: AspectRatioOption.RATIO_9_16
        val theme = BackgroundTheme.entries.find { it.displayName == entity.backgroundTheme } ?: BackgroundTheme.CYBERPUNK_GRID
        val style = CaptionStyle.entries.find { it.displayName == entity.captionStyle } ?: CaptionStyle.HORMOZI_POP
        val mascot = AvatarMascot.entries.find { it.displayName == entity.avatarMascot } ?: AvatarMascot.CYBER_BOT
        val bgm = BgmTrack.entries.find { it.title == entity.bgmTrack } ?: BgmTrack.SYNTHWAVE

        _studioState.update {
            it.copy(
                currentProjectId = entity.id,
                projectTitle = entity.title,
                scriptText = entity.scriptText,
                aspectRatio = ratio,
                backgroundTheme = theme,
                captionStyle = style,
                avatarMascot = mascot,
                speechPitch = entity.speechPitch,
                speechRate = entity.speechRate,
                bgmTrack = bgm,
                bgmVolume = entity.bgmVolume,
                captionFontSize = entity.captionFontSize,
                captionColorHex = entity.captionColorHex,
                captionHighlightColorHex = entity.captionHighlightColorHex,
                captionPosition = entity.captionPosition,
                isUppercase = entity.isUppercase,
                showWatermark = entity.showWatermark,
                isFavorite = entity.isFavorite
            )
        }
        playbackEngine.parseScriptToTimedWords(entity.scriptText, entity.speechRate)
        Toast.makeText(getApplication(), "Loaded: ${entity.title}", Toast.LENGTH_SHORT).show()
    }

    fun createNewProject() {
        _studioState.update {
            YapStudioState(
                currentProjectId = 0L,
                projectTitle = "New Yap Project",
                scriptText = "Enter your viral script here or generate one using the AI prompt assistant."
            )
        }
        playbackEngine.parseScriptToTimedWords(_studioState.value.scriptText, 1.0f)
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
            Toast.makeText(getApplication(), "Project deleted", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleProjectFavorite(id: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
        }
    }

    fun startRenderExport() {
        if (_studioState.value.isRendering) return
        _studioState.update { it.copy(isRendering = true, renderProgress = 0f, renderedExportSuccess = false) }

        renderJob = viewModelScope.launch {
            // High-speed multi-stage rendering simulator:
            // 1. Audio rendering & synthesis
            // 2. Subtitle keyframe rasterization
            // 3. Background canvas composition
            // 4. Video packaging
            for (progress in 1..100) {
                delay(25)
                _studioState.update { it.copy(renderProgress = progress / 100f) }
            }
            _studioState.update { it.copy(isRendering = false, renderedExportSuccess = true) }
            Toast.makeText(getApplication(), "Render Complete! Ready to Share 🎉", Toast.LENGTH_LONG).show()
        }
    }

    fun shareTranscript(context: Context) {
        val script = _studioState.value.scriptText
        val title = _studioState.value.projectTitle
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$title - Yap Render Transcript")
            putExtra(Intent.EXTRA_TEXT, "$title\n\n$script\n\nRendered with Yap Render Studio ⚡")
        }
        context.startActivity(Intent.createChooser(intent, "Share Yap Transcript"))
    }

    fun exportSrtSubtitles(context: Context) {
        val timed = playbackEngine.timedWords.value
        if (timed.isEmpty()) return

        val srtBuilder = StringBuilder()
        var chunkIndex = 1
        val chunkSize = 4
        for (i in timed.indices step chunkSize) {
            val chunk = timed.subList(i, (i + chunkSize).coerceAtMost(timed.size))
            val start = formatSrtTime(chunk.first().startMs)
            val end = formatSrtTime(chunk.last().endMs)
            val text = chunk.joinToString(" ") { it.word }

            srtBuilder.append("$chunkIndex\n")
            srtBuilder.append("$start --> $end\n")
            srtBuilder.append("$text\n\n")
            chunkIndex++
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "${_studioState.value.projectTitle}.srt")
            putExtra(Intent.EXTRA_TEXT, srtBuilder.toString())
        }
        context.startActivity(Intent.createChooser(intent, "Export SRT Subtitles"))
    }

    private fun formatSrtTime(ms: Long): String {
        val hours = ms / 3600000
        val mins = (ms % 3600000) / 60000
        val secs = (ms % 60000) / 1000
        val millis = ms % 1000
        return String.format("%02d:%02d:%02d,%03d", hours, mins, secs, millis)
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
        playbackEngine.release()
    }
}
