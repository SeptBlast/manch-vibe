package com.solace.data.config

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.solace.ui.config.SolaceUiConfig
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// Remote Config key — backend publishes the full SolaceUiConfig JSON here
private const val KEY_UI_CONFIG = "ui_config_json"
// Minimum fetch interval (seconds); use 0 in debug, 3600 in production
private const val FETCH_INTERVAL_SECONDS = 3600L

@Singleton
class RemoteConfigRepository @Inject constructor() {

    private val rc: FirebaseRemoteConfig = Firebase.remoteConfig

    init {
        rc.setConfigSettingsAsync(
            remoteConfigSettings { minimumFetchIntervalInSeconds = FETCH_INTERVAL_SECONDS }
        )
        // Embedded fallback so the UI works even if the network call fails
        rc.setDefaultsAsync(
            mapOf(KEY_UI_CONFIG to DEFAULT_CONFIG_JSON)
        )
    }

    /**
     * Fetch + activate Remote Config, then decode [SolaceUiConfig].
     * Falls back to the embedded default JSON on any error.
     */
    suspend fun fetchUiConfig(): SolaceUiConfig {
        return runCatching {
            rc.fetchAndActivate().await()
            val json = rc.getString(KEY_UI_CONFIG).takeIf { it.isNotBlank() }
                ?: DEFAULT_CONFIG_JSON
            Gson().fromJson(json, SolaceUiConfig::class.java)
        }.getOrDefault(SolaceUiConfig())
    }
}

// Matches the shared JSON schema documented in CLAUDE.md
private val DEFAULT_CONFIG_JSON = """
{
  "schemaVersion": 1,
  "themeVariant": "DEFAULT",
  "experimentFlags": {
    "enableChat": true,
    "enableJournal": true,
    "enableLikes": true,
    "showMoodColorPicker": true
  },
  "loginScreen": {
    "visible": true,
    "tagline": "Discover yourself. We'll help guide you.",
    "logoUrl": null,
    "providers": ["EMAIL", "GOOGLE", "APPLE"],
    "emailCtaLabel": "Continue with Email",
    "googleCtaLabel": "Continue with Google",
    "appleCtaLabel": "Continue with Apple"
  },
  "homeScreen": {
    "sections": [
      { "id": "profile_feed", "type": "PROFILE_FEED", "visible": true, "order": 0, "title": "Discover" },
      { "id": "likes_grid",   "type": "LIKES_GRID",   "visible": true, "order": 1, "title": "Likes you" }
    ],
    "bottomNav": [
      { "id": "home",    "label": "Home",    "icon": "home",          "visible": true },
      { "id": "journal", "label": "Journal", "icon": "book_open",     "visible": true },
      { "id": "emotion", "label": "Emotion", "icon": "emoji_emotions","visible": true },
      { "id": "likes",   "label": "Likes",   "icon": "favorite",      "visible": true },
      { "id": "chat",    "label": "Chat",    "icon": "chat_bubble",   "visible": true }
    ]
  }
}
""".trimIndent()
