package com.example.engine

import com.example.data.model.TimedWord
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sin

class YapPlaybackEngine {

    private val playbackScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tickerJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(10000L)
    val totalDurationMs: StateFlow<Long> = _totalDurationMs.asStateFlow()

    private val _currentWordIndex = MutableStateFlow(-1)
    val currentWordIndex: StateFlow<Int> = _currentWordIndex.asStateFlow()

    private val _timedWords = MutableStateFlow<List<TimedWord>>(emptyList())
    val timedWords: StateFlow<List<TimedWord>> = _timedWords.asStateFlow()

    private val _audioAmplitudes = MutableStateFlow(FloatArray(16) { 0.1f })
    val audioAmplitudes: StateFlow<FloatArray> = _audioAmplitudes.asStateFlow()

    var isLooping: Boolean = true
    var playbackSpeed: Float = 1.0f

    fun parseScriptToTimedWords(script: String, speechRate: Float = 1.0f) {
        val words = script.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) {
            _timedWords.value = emptyList()
            _totalDurationMs.value = 5000L
            return
        }

        // Estimate ~280ms per word at 1.0x rate + punctuation pauses
        val baseMsPerWord = (280f / speechRate.coerceIn(0.5f, 2.5f)).toLong()
        val list = mutableListOf<TimedWord>()
        var currentMs = 300L // 300ms pre-roll

        words.forEachIndexed { index, rawWord ->
            val cleanWord = rawWord.trim()
            val extraPause = when {
                cleanWord.endsWith(".") || cleanWord.endsWith("!") || cleanWord.endsWith("?") -> 250L
                cleanWord.endsWith(",") || cleanWord.endsWith(";") || cleanWord.endsWith(":") -> 140L
                else -> 0L
            }
            val wordDuration = (baseMsPerWord * (cleanWord.length.coerceIn(3, 10) / 5f)).toLong().coerceIn(160L, 700L)
            val endMs = currentMs + wordDuration
            list.add(TimedWord(word = cleanWord, startMs = currentMs, endMs = endMs, index = index))
            currentMs = endMs + 40L + extraPause
        }

        _timedWords.value = list
        _totalDurationMs.value = (currentMs + 600L).coerceAtLeast(3000L)
        if (_currentPositionMs.value > _totalDurationMs.value) {
            _currentPositionMs.value = 0L
        }
    }

    fun play(onComplete: (() -> Unit)? = null) {
        if (_isPlaying.value) return
        _isPlaying.value = true
        startTicker(onComplete)
    }

    fun pause() {
        _isPlaying.value = false
        tickerJob?.cancel()
        tickerJob = null
        // Reset amplitudes
        _audioAmplitudes.value = FloatArray(16) { 0.08f }
    }

    fun seekTo(positionMs: Long) {
        val bounded = positionMs.coerceIn(0L, _totalDurationMs.value)
        _currentPositionMs.value = bounded
        updateActiveWordIndex(bounded)
    }

    fun togglePlayPause(onComplete: (() -> Unit)? = null) {
        if (_isPlaying.value) {
            pause()
        } else {
            if (_currentPositionMs.value >= _totalDurationMs.value) {
                seekTo(0L)
            }
            play(onComplete)
        }
    }

    private fun startTicker(onComplete: (() -> Unit)?) {
        tickerJob?.cancel()
        tickerJob = playbackScope.launch {
            val tickInterval = 33L // ~30 FPS
            while (isActive && _isPlaying.value) {
                val nextPos = _currentPositionMs.value + (tickInterval * playbackSpeed).toLong()
                if (nextPos >= _totalDurationMs.value) {
                    if (isLooping) {
                        _currentPositionMs.value = 0L
                        updateActiveWordIndex(0L)
                    } else {
                        _currentPositionMs.value = _totalDurationMs.value
                        _isPlaying.value = false
                        onComplete?.invoke()
                        break
                    }
                } else {
                    _currentPositionMs.value = nextPos
                    updateActiveWordIndex(nextPos)
                }

                // Generate live dynamic spectrum data
                generateSpectrumAmplitudes(nextPos)
                delay(tickInterval)
            }
        }
    }

    private fun updateActiveWordIndex(posMs: Long) {
        val words = _timedWords.value
        val found = words.indexOfFirst { posMs in it.startMs..it.endMs }
        _currentWordIndex.value = if (found != -1) {
            found
        } else {
            // Find closest previous word
            words.indexOfLast { it.endMs < posMs }
        }
    }

    private fun generateSpectrumAmplitudes(posMs: Long) {
        val t = posMs / 1000.0
        val isWordActive = _currentWordIndex.value >= 0
        val baseMultiplier = if (isWordActive) 0.85f else 0.25f
        val amps = FloatArray(16) { index ->
            val harmonic1 = sin(t * 8.0 + index * 0.4)
            val harmonic2 = sin(t * 14.0 + index * 0.9)
            val v = (harmonic1 * 0.5 + harmonic2 * 0.5).toFloat()
            (0.15f + (v + 1f) * 0.4f * baseMultiplier).coerceIn(0.08f, 1.0f)
        }
        _audioAmplitudes.value = amps
    }

    fun release() {
        tickerJob?.cancel()
        playbackScope.cancel()
    }
}
