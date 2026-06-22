import Foundation
import FirebaseRemoteConfig

// ---------------------------------------------------------------------------
// RemoteConfigRepository — fetches SolaceUiConfig from Firebase Remote Config
// Remote Config key: "ui_config_json"
// ---------------------------------------------------------------------------

public final class RemoteConfigRepository {

    public static let shared = RemoteConfigRepository()

    private let rc = RemoteConfig.remoteConfig()
    private let configKey = "ui_config_json"

    public init() {
        let settings = RemoteConfigSettings()
        // 0s in debug so every launch gets a fresh fetch; 3600s in release
        #if DEBUG
        settings.minimumFetchInterval = 0
        #else
        settings.minimumFetchInterval = 3600
        #endif
        rc.configSettings = settings
        rc.setDefaults([configKey: defaultConfigJson as NSString])
    }

    /// Fetch + activate, then decode into SolaceUiConfig.
    /// Falls back to compiled-in default on any error.
    public func fetchUiConfig() async -> SolaceUiConfig {
        do {
            let status = try await rc.fetchAndActivate()
            _ = status // .successFetchedFromRemote / .successUsingPreFetchedData
            let json = rc[configKey].stringValue ?? defaultConfigJson
            return decode(json: json)
        } catch {
            return decode(json: defaultConfigJson)
        }
    }

    private func decode(json: String) -> SolaceUiConfig {
        guard
            let data = json.data(using: .utf8),
            let config = try? JSONDecoder().decode(SolaceUiConfig.self, from: data)
        else { return SolaceUiConfig() }
        return config
    }
}

// Matches the shared JSON schema in CLAUDE.md
private let defaultConfigJson = """
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
      { "id": "home",    "label": "Home",    "icon": "house",        "visible": true },
      { "id": "journal", "label": "Journal", "icon": "book",         "visible": true },
      { "id": "emotion", "label": "Emotion", "icon": "face.smiling", "visible": true },
      { "id": "likes",   "label": "Likes",   "icon": "heart",        "visible": true },
      { "id": "chat",    "label": "Chat",    "icon": "bubble.left",  "visible": true }
    ]
  }
}
"""
