package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class AspectRatioOption(val label: String, val ratioWidth: Float, val ratioHeight: Float, val badge: String) {
    RATIO_9_16("9:16", 9f, 16f, "TikTok / Reels"),
    RATIO_1_1("1:1", 1f, 1f, "Square / Post"),
    RATIO_16_9("16:9", 16f, 9f, "YouTube / Video")
}

enum class CaptionStyle(val displayName: String, val description: String) {
    HORMOZI_POP("Viral Pop", "High-energy bold active word bounce with glow"),
    CYBER_NEON("Cyber Neon", "Futuristic outline with neon glow shadow"),
    MINIMAL_BOX("Minimal Box", "Clean pill background with rounded edges"),
    KARAOKE_BOUNCE("Karaoke Flow", "Smooth color fill gradient transition"),
    TYPEWRITER("Typewriter", "Retro monospace terminal typography")
}

enum class BackgroundTheme(val displayName: String, val category: String) {
    CYBERPUNK_GRID("Cyber Grid", "Retro Wave"),
    LOFI_CHILL("Lo-Fi Twilight", "Aesthetic"),
    MOTION_WARP("Hyper Speed", "Motion"),
    STARRY_NEBULA("Cosmic Void", "Sci-Fi"),
    SUNSET_GRADIENT("Sunset Blaze", "Vibrant"),
    AUDIO_SPECTRUM("Spectrum Core", "Audiogram"),
    DEEP_OBSIDIAN("Minimal Dark", "Clean")
}

enum class AvatarMascot(val displayName: String, val emoji: String, val subtitle: String) {
    CYBER_BOT("Cyber Bot", "🤖", "AI Assistant"),
    PIXEL_DOGE("Pixel Doge", "🐕", "Good Boy"),
    STREAMER_DUDE("Streamer", "🎮", "Gaming Pro"),
    AI_ORB("Quantum Orb", "🔮", "Synth Core"),
    BRAINIAC("Brainiac", "🧠", "Big Brain Yap"),
    SKULL_VIBE("Retro Skull", "💀", "Dead Serious"),
    CHILL_CAT("Vibe Cat", "🐱", "Chill Mode"),
    NONE("No Mascot", "🚫", "Captions Only")
}

enum class SoundEffect(val title: String, val emoji: String, val frequencyHz: Int, val durationMs: Int, val type: String) {
    VINE_BOOM("Vine Boom", "💥", 80, 700, "boom"),
    DING("Ding Notification", "🔔", 1760, 400, "bell"),
    GLITCH("Cyber Glitch", "⚡", 350, 250, "glitch"),
    WHOOSH("Fast Whoosh", "💨", 440, 300, "whoosh"),
    CASH_REGISTER("Ka-Ching", "💰", 1200, 450, "cash"),
    AIR_HORN("Air Horn", "📢", 620, 600, "horn"),
    APPLAUSE("Applause Pop", "👏", 500, 500, "pop"),
    CORRECT("Success Chime", "✨", 987, 350, "chime"),
    ERROR_BUZZ("Error Buzz", "❌", 150, 400, "buzz"),
    LASER_BEAM("Laser Zap", "🔫", 1400, 300, "laser")
}

enum class BgmTrack(val title: String, val mood: String, val baseBpm: Int) {
    LOFI_BEATS("Midnight Lo-Fi", "Chill & Relaxed", 75),
    SYNTHWAVE("Cyber Synthwave", "80s Neon Drive", 115),
    DRAMATIC_PULSE("Dramatic Ticking", "Suspense & Mystery", 90),
    ARCADE_8BIT("Pixel Rush", "Fast & Energetic", 130),
    DEEP_AMBIENT("Deep Meditation", "Slow & Focused", 60),
    NONE("Silent / No Music", "Voice Only", 0)
}

data class TimedWord(
    val word: String,
    val startMs: Long,
    val endMs: Long,
    val index: Int
)

data class ScriptSegment(
    val id: String,
    val text: String,
    val startMs: Long,
    val durationMs: Long
)

data class YapTemplate(
    val id: String,
    val title: String,
    val category: String,
    val script: String,
    val backgroundTheme: BackgroundTheme,
    val captionStyle: CaptionStyle,
    val mascot: AvatarMascot,
    val bgm: BgmTrack,
    val speechRate: Float = 1.05f
)
