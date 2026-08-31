package com.example.data.repository

import com.example.data.local.YapProjectDao
import com.example.data.local.YapProjectEntity
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class YapRepository(private val dao: YapProjectDao) {

    val allProjects: Flow<List<YapProjectEntity>> = dao.getAllProjects()
    val favoriteProjects: Flow<List<YapProjectEntity>> = dao.getFavoriteProjects()

    suspend fun getProjectById(id: Long): YapProjectEntity? = dao.getProjectById(id)

    suspend fun saveProject(project: YapProjectEntity): Long = dao.insertProject(project)

    suspend fun updateProject(project: YapProjectEntity) = dao.updateProject(project)

    suspend fun deleteProject(id: Long) = dao.deleteById(id)

    suspend fun toggleFavorite(id: Long) = dao.toggleFavorite(id)

    fun getTrendingTemplates(): List<YapTemplate> {
        return listOf(
            YapTemplate(
                id = "template_1",
                title = "Side Project Trap 💻",
                category = "Tech & Coding",
                script = "Why do 99% of programmers start twenty side projects and never finish any of them? Because writing the architecture is fun, but fixing CSS in production at 3 AM is pure pain!",
                backgroundTheme = BackgroundTheme.CYBERPUNK_GRID,
                captionStyle = CaptionStyle.HORMOZI_POP,
                mascot = AvatarMascot.CYBER_BOT,
                bgm = BgmTrack.SYNTHWAVE,
                speechRate = 1.1f
            ),
            YapTemplate(
                id = "template_2",
                title = "Time Travel Tourist Paradox ⏳",
                category = "Shower Thoughts",
                script = "If time travel is eventually invented in the future, where are all the time tourists? Either nobody wants to visit our era, or time travel is so expensive only billionaires can afford it.",
                backgroundTheme = BackgroundTheme.STARRY_NEBULA,
                captionStyle = CaptionStyle.CYBER_NEON,
                mascot = AvatarMascot.BRAINIAC,
                bgm = BgmTrack.DRAMATIC_PULSE,
                speechRate = 1.0f
            ),
            YapTemplate(
                id = "template_3",
                title = "The 8-Hour Workday Myth ⏰",
                category = "Mindset & Hustle",
                script = "The eight hour workday was designed in 1926 for assembly line factory workers. Why are we still using a century-old schedule for creative knowledge work in 2026?",
                backgroundTheme = BackgroundTheme.MOTION_WARP,
                captionStyle = CaptionStyle.HORMOZI_POP,
                mascot = AvatarMascot.STREAMER_DUDE,
                bgm = BgmTrack.LOFI_BEATS,
                speechRate = 1.15f
            ),
            YapTemplate(
                id = "template_4",
                title = "Coffee Conquered The World ☕",
                category = "Fascinating Facts",
                script = "Did you know that before coffee became popular in Europe, people drank beer for breakfast? The Enlightenment literally happened because humanity switched from alcohol to caffeine!",
                backgroundTheme = BackgroundTheme.SUNSET_GRADIENT,
                captionStyle = CaptionStyle.KARAOKE_BOUNCE,
                mascot = AvatarMascot.PIXEL_DOGE,
                bgm = BgmTrack.LOFI_BEATS,
                speechRate = 1.05f
            ),
            YapTemplate(
                id = "template_5",
                title = "AI Replaced My Sleep Schedule 🤖",
                category = "Humor & Creator",
                script = "AI was supposed to do my dishes and laundry so I could make art. Instead AI is writing poetry and coding apps while I'm still doing dishes at midnight!",
                backgroundTheme = BackgroundTheme.AUDIO_SPECTRUM,
                captionStyle = CaptionStyle.HORMOZI_POP,
                mascot = AvatarMascot.AI_ORB,
                bgm = BgmTrack.ARCADE_8BIT,
                speechRate = 1.2f
            ),
            YapTemplate(
                id = "template_6",
                title = "Simulation Glitch in Plain Sight 🕶️",
                category = "Conspiracy & Sci-Fi",
                script = "Have you ever walked into a room and instantly forgot what you went in there for? That is not bad memory. That is the simulation running out of RAM and reloading your task queue!",
                backgroundTheme = BackgroundTheme.DEEP_OBSIDIAN,
                captionStyle = CaptionStyle.TYPEWRITER,
                mascot = AvatarMascot.SKULL_VIBE,
                bgm = BgmTrack.DRAMATIC_PULSE,
                speechRate = 1.0f
            )
        )
    }

    fun generateQuickYap(topic: String, style: String): String {
        val cleanTopic = if (topic.isBlank()) "modern technology" else topic.trim()
        return when (style) {
            "Controversial Take" -> 
                "Hot take on $cleanTopic: Everyone pretends they understand it, but 95% of people are just repeating what they saw on Twitter. If you actually look at the data, the reality is completely upside down."
            "Storytime / Drama" -> 
                "You won't believe what happened when I decided to test $cleanTopic for 24 hours straight. What started as a harmless experiment turned into the wildest chain of events of my entire life."
            "Mind-Blowing Fact" -> 
                "Most people have no idea that $cleanTopic is secretly based on a fundamental flaw. Once you notice this one single detail, you will never look at it the same way again."
            "Grindset / Motivation" -> 
                "Stop wasting time overcomplicating $cleanTopic. The winners in this game aren't the smartest; they are the ones who show up every single day while everyone else is still making excuses."
            else -> 
                "Here is the unfiltered truth about $cleanTopic. If you want to master this in 2026, you only need to focus on three key rules that nobody is talking about."
        }
    }
}
