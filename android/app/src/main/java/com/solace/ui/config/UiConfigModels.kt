package com.solace.ui.config

import androidx.compose.ui.graphics.Color
import com.solace.ui.theme.*

// ---------------------------------------------------------------------------
// Root config — deserialized from backend JSON (e.g. via Gson / Moshi / kotlinx)
// TODO: fetch from remote config endpoint on app start; cache locally for offline
// ---------------------------------------------------------------------------

data class SolaceUiConfig(
    val schemaVersion: Int = 1,
    val themeVariant: ThemeVariant = ThemeVariant.DEFAULT,
    val experimentFlags: ExperimentFlags = ExperimentFlags(),
    val loginScreen: LoginScreenConfig = LoginScreenConfig(),
    val homeScreen: HomeScreenConfig = HomeScreenConfig(),
)

// ---------------------------------------------------------------------------
// Theme
// ---------------------------------------------------------------------------

enum class ThemeVariant { DEFAULT, DARK }

// ---------------------------------------------------------------------------
// Experiment flags
// ---------------------------------------------------------------------------

data class ExperimentFlags(
    val enableChat: Boolean = true,
    val enableJournal: Boolean = true,
    val enableLikes: Boolean = true,
    val showMoodColorPicker: Boolean = true,
)

// ---------------------------------------------------------------------------
// Login screen
// ---------------------------------------------------------------------------

enum class AuthProvider { EMAIL, GOOGLE, APPLE }

data class LoginScreenConfig(
    val visible: Boolean = true,
    val tagline: String = "Discover yourself. We'll help guide you.",
    val logoUrl: String? = null,
    val providers: List<AuthProvider> = listOf(
        AuthProvider.EMAIL,
        AuthProvider.GOOGLE,
        AuthProvider.APPLE,
    ),
    val emailCtaLabel: String = "Continue with Email",
    val googleCtaLabel: String = "Continue with Google",
    val appleCtaLabel: String = "Continue with Apple",
)

// ---------------------------------------------------------------------------
// Home screen
// ---------------------------------------------------------------------------

enum class HomeSectionType {
    PROFILE_FEED,
    LIKES_GRID,
    JOURNAL_PREVIEW,
    EMOTION_PROMPT,
    UNKNOWN,
}

data class HomeSectionConfig(
    val id: String,
    val type: HomeSectionType,
    val visible: Boolean = true,
    val order: Int = 0,
    val title: String = "",
    val ctaLabel: String? = null,
    val imageUrl: String? = null,
)

data class BottomNavTabConfig(
    val id: String,
    val label: String,
    val icon: String,   // icon name resolved natively on each platform
    val visible: Boolean = true,
)

data class HomeScreenConfig(
    val sections: List<HomeSectionConfig> = defaultSections(),
    val bottomNav: List<BottomNavTabConfig> = defaultTabs(),
) {
    val visibleSections: List<HomeSectionConfig>
        get() = sections.filter { it.visible }.sortedBy { it.order }
}

// ---------------------------------------------------------------------------
// Mood palette — backend drives which colours are enabled/ordered
// ---------------------------------------------------------------------------

enum class MoodColorKey {
    CALM_RELAXED,
    ENERGETIC_PASSIONATE,
    HAPPY_OPTIMISTIC,
    NORTH_REFLECTIVE,
    PEACEFUL_GROUNDED,
    CONFUSED_OVERWHELMED,
    NEUTRAL_NUMB,
    WARM_HOPEFUL,
}

data class MoodColorOption(
    val key: MoodColorKey,
    val label: String,
    val color: Color,
)

val DefaultMoodPalette: List<MoodColorOption> = listOf(
    MoodColorOption(MoodColorKey.CALM_RELAXED,           "Calm & Relaxed",             MoodCalmRelaxed),
    MoodColorOption(MoodColorKey.ENERGETIC_PASSIONATE,   "Energetic & Passionate",     MoodEnergeticPassionate),
    MoodColorOption(MoodColorKey.HAPPY_OPTIMISTIC,       "Happy & Optimistic",         MoodHappyOptimistic),
    MoodColorOption(MoodColorKey.NORTH_REFLECTIVE,       "North Reflective",           MoodNorthReflective),
    MoodColorOption(MoodColorKey.PEACEFUL_GROUNDED,      "Peaceful & Grounded",        MoodPeacefulGrounded),
    MoodColorOption(MoodColorKey.CONFUSED_OVERWHELMED,   "Confused & Overwhelmed",     MoodConfusedOverwhelmed),
    MoodColorOption(MoodColorKey.NEUTRAL_NUMB,           "Neutral & Numb",             MoodNeutralNumb),
    MoodColorOption(MoodColorKey.WARM_HOPEFUL,           "Warm & Hopeful",             MoodWarmHopeful),
)

// ---------------------------------------------------------------------------
// Profile / user data models (display only — not persisted here)
// ---------------------------------------------------------------------------

data class UserProfile(
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val moodColor: MoodColorKey,
    val moodLabel: String,
    val bio: String,
    val vibes: List<String>,
    val minutesPerDay: String = "2 minutes a day",
)

// ---------------------------------------------------------------------------
// Defaults
// ---------------------------------------------------------------------------

private fun defaultSections() = listOf(
    HomeSectionConfig(id = "profile_feed", type = HomeSectionType.PROFILE_FEED, order = 0, title = "Discover"),
    HomeSectionConfig(id = "likes_grid",   type = HomeSectionType.LIKES_GRID,   order = 1, title = "Likes you"),
)

private fun defaultTabs() = listOf(
    BottomNavTabConfig(id = "home",    label = "Home",    icon = "home"),
    BottomNavTabConfig(id = "journal", label = "Journal", icon = "book_open"),
    BottomNavTabConfig(id = "emotion", label = "Emotion", icon = "emoji_emotions"),
    BottomNavTabConfig(id = "likes",   label = "Likes",   icon = "favorite"),
    BottomNavTabConfig(id = "chat",    label = "Chat",    icon = "chat_bubble"),
)
