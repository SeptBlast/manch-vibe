import SwiftUI

// ---------------------------------------------------------------------------
// Root config — decoded from backend JSON
// TODO: fetch from remote config endpoint on app launch; persist in UserDefaults for offline
// ---------------------------------------------------------------------------

public struct SolaceUiConfig: Codable {
    public var schemaVersion: Int = 1
    public var themeVariant: SolaceThemeVariant = .default
    public var experimentFlags: ExperimentFlags = .init()
    public var loginScreen: LoginScreenConfig = .init()
    public var homeScreen: HomeScreenConfig = .init()

    public static let `default` = SolaceUiConfig()
}

// ---------------------------------------------------------------------------
// Experiment flags
// ---------------------------------------------------------------------------

public struct ExperimentFlags: Codable {
    public var enableChat: Bool = true
    public var enableJournal: Bool = true
    public var enableLikes: Bool = true
    public var showMoodColorPicker: Bool = true
}

// ---------------------------------------------------------------------------
// Login screen
// ---------------------------------------------------------------------------

public enum AuthProvider: String, Codable, CaseIterable {
    case email  = "EMAIL"
    case google = "GOOGLE"
    case apple  = "APPLE"
    // NOTE: phone / OTP / SMS intentionally excluded per product spec
}

public struct LoginScreenConfig: Codable {
    public var visible: Bool = true
    public var tagline: String = "Discover yourself. We'll help guide you."
    public var logoUrl: String? = nil
    public var providers: [AuthProvider] = [.email, .google, .apple]
    public var emailCtaLabel: String = "Continue with Email"
    public var googleCtaLabel: String = "Continue with Google"
    public var appleCtaLabel: String = "Continue with Apple"
}

// ---------------------------------------------------------------------------
// Home screen
// ---------------------------------------------------------------------------

public enum HomeSectionType: String, Codable {
    case profileFeed    = "PROFILE_FEED"
    case likesGrid      = "LIKES_GRID"
    case journalPreview = "JOURNAL_PREVIEW"
    case emotionPrompt  = "EMOTION_PROMPT"
    case unknown        = "UNKNOWN"
}

public struct HomeSectionConfig: Codable, Identifiable {
    public var id: String
    public var type: HomeSectionType
    public var visible: Bool = true
    public var order: Int = 0
    public var title: String = ""
    public var ctaLabel: String? = nil
    public var imageUrl: String? = nil
}

public struct BottomNavTabConfig: Codable, Identifiable {
    public var id: String
    public var label: String
    public var icon: String    // system image name resolved in SolaceTabIcon
    public var visible: Bool = true
}

public struct HomeScreenConfig: Codable {
    public var sections: [HomeSectionConfig] = HomeSectionConfig.defaults
    public var bottomNav: [BottomNavTabConfig] = BottomNavTabConfig.defaults

    public init(sections: [HomeSectionConfig] = HomeSectionConfig.defaults,
                bottomNav: [BottomNavTabConfig] = BottomNavTabConfig.defaults) {
        self.sections = sections
        self.bottomNav = bottomNav
    }

    public var visibleSections: [HomeSectionConfig] {
        sections.filter { $0.visible }.sorted { $0.order < $1.order }
    }
    public var visibleTabs: [BottomNavTabConfig] {
        bottomNav.filter { $0.visible }
    }
}

// ---------------------------------------------------------------------------
// Mood palette
// ---------------------------------------------------------------------------

public enum MoodColorKey: String, Codable, CaseIterable {
    case calmRelaxed          = "CALM_RELAXED"
    case energeticPassionate  = "ENERGETIC_PASSIONATE"
    case happyOptimistic      = "HAPPY_OPTIMISTIC"
    case northReflective      = "NORTH_REFLECTIVE"
    case peacefulGrounded     = "PEACEFUL_GROUNDED"
    case confusedOverwhelmed  = "CONFUSED_OVERWHELMED"
    case neutralNumb          = "NEUTRAL_NUMB"
    case warmHopeful          = "WARM_HOPEFUL"
}

public struct MoodColorOption: Identifiable {
    public var id: MoodColorKey { key }
    public let key: MoodColorKey
    public let label: String
    public let color: Color
}

public let defaultMoodPalette: [MoodColorOption] = [
    .init(key: .calmRelaxed,         label: "Calm & Relaxed",           color: DesignTokens.Color.moodCalmRelaxed),
    .init(key: .energeticPassionate, label: "Energetic & Passionate",   color: DesignTokens.Color.moodEnergeticPassionate),
    .init(key: .happyOptimistic,     label: "Happy & Optimistic",       color: DesignTokens.Color.moodHappyOptimistic),
    .init(key: .northReflective,     label: "North Reflective",         color: DesignTokens.Color.moodNorthReflective),
    .init(key: .peacefulGrounded,    label: "Peaceful & Grounded",      color: DesignTokens.Color.moodPeacefulGrounded),
    .init(key: .confusedOverwhelmed, label: "Confused & Overwhelmed",   color: DesignTokens.Color.moodConfusedOverwhelmed),
    .init(key: .neutralNumb,         label: "Neutral & Numb",           color: DesignTokens.Color.moodNeutralNumb),
    .init(key: .warmHopeful,         label: "Warm & Hopeful",           color: DesignTokens.Color.moodWarmHopeful),
]

public func moodColor(for key: MoodColorKey) -> Color {
    defaultMoodPalette.first { $0.key == key }?.color ?? DesignTokens.Color.solaceTeal
}

// ---------------------------------------------------------------------------
// User profile (display model)
// ---------------------------------------------------------------------------

public struct UserProfile: Identifiable, Codable {
    public var id: String
    public var username: String
    public var avatarUrl: String?
    public var moodColor: MoodColorKey
    public var moodLabel: String
    public var bio: String
    public var vibes: [String]
    public var minutesPerDay: String = "2 minutes a day"
}

// ---------------------------------------------------------------------------
// Defaults
// ---------------------------------------------------------------------------

extension HomeSectionConfig {
    public static let defaults: [HomeSectionConfig] = [
        .init(id: "profile_feed", type: .profileFeed, order: 0, title: "Discover"),
        .init(id: "likes_grid",   type: .likesGrid,   order: 1, title: "Likes you"),
    ]
}

extension BottomNavTabConfig {
    public static let defaults: [BottomNavTabConfig] = [
        .init(id: "home",    label: "Home",    icon: "house"),
        .init(id: "journal", label: "Journal", icon: "book"),
        .init(id: "emotion", label: "Emotion", icon: "face.smiling"),
        .init(id: "likes",   label: "Likes",   icon: "heart"),
        .init(id: "chat",    label: "Chat",    icon: "bubble.left"),
    ]
}
