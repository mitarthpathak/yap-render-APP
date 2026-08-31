package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "yap_projects")
data class YapProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val scriptText: String,
    val aspectRatio: String = "9:16",
    val captionStyle: String = "Viral Pop",
    val backgroundTheme: String = "Cyber Grid",
    val avatarMascot: String = "Cyber Bot",
    val voicePreset: String = "Standard",
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val bgmTrack: String = "Midnight Lo-Fi",
    val bgmVolume: Float = 0.35f,
    val captionFontSize: Int = 26,
    val captionColorHex: String = "#FFFFFF",
    val captionHighlightColorHex: String = "#FACC15",
    val captionPosition: String = "Bottom",
    val isUppercase: Boolean = true,
    val showWatermark: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val estimatedDurationMs: Long = 10000L
)
