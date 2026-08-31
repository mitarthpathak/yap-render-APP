package com.example.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.BgmTrack
import com.example.data.model.SoundEffect
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.sin

class AudioEngine(private val context: Context) {

    private var tts: TextToSpeech? = null
    var isTtsReady: Boolean = false
        private set

    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var bgmJob: Job? = null
    private var isBgmPlaying = false
    private var bgmVolume = 0.3f
    private var currentBgmTrack: BgmTrack = BgmTrack.NONE

    var onWordSpoken: ((word: String, startMs: Long) -> Unit)? = null
    var onSpeechComplete: (() -> Unit)? = null

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsReady = true
                }
                setupTtsListener()
            }
        }
    }

    private fun setupTtsListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // Speech started
            }

            override fun onDone(utteranceId: String?) {
                onSpeechComplete?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onSpeechComplete?.invoke()
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                super.onRangeStart(utteranceId, start, end, frame)
            }
        })
    }

    fun speak(text: String, pitch: Float = 1.0f, speechRate: Float = 1.0f, utteranceId: String = "yap_speech") {
        if (!isTtsReady || text.isBlank()) return
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
        tts?.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    fun playSoundEffect(effect: SoundEffect) {
        engineScope.launch {
            try {
                synthesizeSoundEffect(effect)
            } catch (e: Exception) {
                Log.e("AudioEngine", "Error playing sound effect: ${e.message}")
            }
        }
    }

    private fun synthesizeSoundEffect(effect: SoundEffect) {
        val sampleRate = 44100
        val durationMs = effect.durationMs
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(100)
        val generatedSnd = ShortArray(numSamples)

        when (effect.type) {
            "boom" -> {
                // Low pitch exponential drop with distortion
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = 120.0 * Math.exp(-3.5 * t) + 35.0
                    val envelope = Math.exp(-2.5 * t)
                    val raw = sin(2.0 * Math.PI * freq * t)
                    val softClip = Math.tanh(raw * 2.2)
                    generatedSnd[i] = (softClip * Short.MAX_VALUE * 0.9 * envelope).toInt().toShort()
                }
            }
            "bell", "chime" -> {
                // Bell chime with dual harmonics
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val envelope = Math.exp(-6.0 * t)
                    val f1 = effect.frequencyHz.toDouble()
                    val f2 = f1 * 2.04
                    val f3 = f1 * 3.01
                    val sample = 0.6 * sin(2.0 * Math.PI * f1 * t) +
                            0.3 * sin(2.0 * Math.PI * f2 * t) +
                            0.1 * sin(2.0 * Math.PI * f3 * t)
                    generatedSnd[i] = (sample * Short.MAX_VALUE * 0.7 * envelope).toInt().toShort()
                }
            }
            "glitch" -> {
                // Rapid frequency modulation + noise
                val random = java.util.Random()
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val noise = (random.nextDouble() * 2.0 - 1.0) * 0.4
                    val tone = sin(2.0 * Math.PI * (effect.frequencyHz + ((i % 100) * 15)) * t)
                    val sample = (tone * 0.6 + noise) * Math.exp(-4.0 * t)
                    generatedSnd[i] = (sample * Short.MAX_VALUE * 0.6).toInt().toShort()
                }
            }
            "whoosh" -> {
                // Filtered noise sweep
                val random = java.util.Random()
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / numSamples
                    val curve = sin(Math.PI * progress)
                    val whiteNoise = (random.nextDouble() * 2.0 - 1.0)
                    generatedSnd[i] = (whiteNoise * curve * Short.MAX_VALUE * 0.6).toInt().toShort()
                }
            }
            "cash" -> {
                // Dual high-pitch bell ding Ka-Ching
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val tOffset = if (i > numSamples / 3) t - (numSamples / 3.0 / sampleRate) else t
                    val baseFreq = if (i < numSamples / 3) 980.0 else 1480.0
                    val env = Math.exp(-7.0 * tOffset)
                    val sample = sin(2.0 * Math.PI * baseFreq * t) * env
                    generatedSnd[i] = (sample * Short.MAX_VALUE * 0.8).toInt().toShort()
                }
            }
            "horn" -> {
                // Airhorn chord (Root + Fifth + Octave)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = if (i > numSamples * 0.8) Math.exp(-12.0 * (t - (numSamples * 0.8 / sampleRate))) else 1.0
                    val f1 = 466.16 // Bb4
                    val f2 = 587.33 // D5
                    val f3 = 698.46 // F5
                    val sample = (sin(2.0 * Math.PI * f1 * t) +
                            sin(2.0 * Math.PI * f2 * t) +
                            sin(2.0 * Math.PI * f3 * t)) / 3.0
                    generatedSnd[i] = (sample * Short.MAX_VALUE * 0.85 * env).toInt().toShort()
                }
            }
            "laser" -> {
                // Fast downward laser chirp
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = 2400.0 * Math.exp(-14.0 * t) + 200.0
                    val env = Math.exp(-5.0 * t)
                    val sample = sin(2.0 * Math.PI * freq * t) * env
                    generatedSnd[i] = (sample * Short.MAX_VALUE * 0.75).toInt().toShort()
                }
            }
            else -> {
                // Default clean tone
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = Math.exp(-4.0 * t)
                    val sample = sin(2.0 * Math.PI * effect.frequencyHz * t) * env
                    generatedSnd[i] = (sample * Short.MAX_VALUE * 0.6).toInt().toShort()
                }
            }
        }

        playAudioTrack(generatedSnd, sampleRate)
    }

    private fun playAudioTrack(pcmData: ShortArray, sampleRate: Int) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(pcmData.size * 2.coerceAtLeast(minBufferSize))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(pcmData, 0, pcmData.size)
        audioTrack.play()
        engineScope.launch {
            delay((pcmData.size * 1000L / sampleRate) + 200)
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }

    fun startBgm(track: BgmTrack, volume: Float = 0.3f) {
        currentBgmTrack = track
        bgmVolume = volume.coerceIn(0f, 1f)
        if (track == BgmTrack.NONE || volume <= 0f) {
            stopBgm()
            return
        }
        stopBgm()
        isBgmPlaying = true

        bgmJob = engineScope.launch {
            val sampleRate = 22050
            val chordProgressions = when (track) {
                BgmTrack.LOFI_BEATS -> listOf(
                    doubleArrayOf(261.63, 329.63, 392.00, 493.88), // Cmaj7
                    doubleArrayOf(220.00, 261.63, 329.63, 392.00), // Am7
                    doubleArrayOf(174.61, 220.00, 261.63, 329.63), // Fmaj7
                    doubleArrayOf(196.00, 246.94, 293.66, 349.23)  // G7
                )
                BgmTrack.SYNTHWAVE -> listOf(
                    doubleArrayOf(220.00, 261.63, 329.63), // Am
                    doubleArrayOf(174.61, 220.00, 261.63), // F
                    doubleArrayOf(261.63, 329.63, 392.00), // C
                    doubleArrayOf(196.00, 246.94, 293.66)  // G
                )
                BgmTrack.DRAMATIC_PULSE -> listOf(
                    doubleArrayOf(110.00, 164.81), // A2/E3
                    doubleArrayOf(110.00, 164.81),
                    doubleArrayOf(98.00, 146.83),  // G2/D3
                    doubleArrayOf(87.31, 130.81)   // F2/C3
                )
                BgmTrack.ARCADE_8BIT -> listOf(
                    doubleArrayOf(523.25, 659.25), // C5/E5
                    doubleArrayOf(587.33, 698.46), // D5/F5
                    doubleArrayOf(659.25, 783.99), // E5/G5
                    doubleArrayOf(783.99, 1046.50) // G5/C6
                )
                else -> listOf(
                    doubleArrayOf(130.81, 196.00, 261.63)
                )
            }

            var chordIndex = 0
            while (isActive && isBgmPlaying) {
                val chord = chordProgressions[chordIndex % chordProgressions.size]
                chordIndex++
                val chordDurationMs = 1800
                val numSamples = (sampleRate * (chordDurationMs / 1000.0)).toInt()
                val pcm = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    var sum = 0.0
                    for (freq in chord) {
                        sum += sin(2.0 * Math.PI * freq * t)
                    }
                    val normalized = sum / chord.size
                    // Soft envelope
                    val env = when {
                        t < 0.1 -> t / 0.1
                        t > 1.6 -> (1.8 - t) / 0.2
                        else -> 1.0
                    }
                    pcm[i] = (normalized * Short.MAX_VALUE * bgmVolume * 0.3 * env).toInt().toShort()
                }

                playAudioTrack(pcm, sampleRate)
                delay(chordDurationMs.toLong() - 50)
            }
        }
    }

    fun stopBgm() {
        isBgmPlaying = false
        bgmJob?.cancel()
        bgmJob = null
    }

    fun updateBgmVolume(volume: Float) {
        bgmVolume = volume.coerceIn(0f, 1f)
    }

    fun release() {
        stopBgm()
        stopSpeaking()
        tts?.shutdown()
        tts = null
        engineScope.cancel()
    }
}
