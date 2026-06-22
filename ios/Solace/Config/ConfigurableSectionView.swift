import SwiftUI

// ---------------------------------------------------------------------------
// ConfigurableSectionView — renders one remotely-configured home section.
// Add new HomeSectionType cases here as the product grows.
// ---------------------------------------------------------------------------

public struct ConfigurableSectionView: View {
    let config: HomeSectionConfig
    let profiles: [UserProfile]

    public var body: some View {
        switch config.type {
        case .profileFeed:
            ProfileFeedSection(config: config, profiles: profiles)
        case .likesGrid:
            LikesGridSection(config: config, profiles: profiles)
        case .journalPreview:
            JournalPreviewSection(config: config)
        case .emotionPrompt:
            EmotionPromptSection(config: config)
        case .unknown:
            EmptyView() // graceful no-op for unknown future types
        }
    }
}

// ---------------------------------------------------------------------------
// Section renderers
// ---------------------------------------------------------------------------

private struct ProfileFeedSection: View {
    let config: HomeSectionConfig
    let profiles: [UserProfile]

    var body: some View {
        VStack(alignment: .leading, spacing: DesignTokens.Spacing.md) {
            if !config.title.isEmpty {
                Text(config.title)
                    .font(DesignTokens.Font.headlineMedium)
                    .padding(.horizontal, DesignTokens.Spacing.md)
            }
            ScrollView {
                LazyVStack(spacing: DesignTokens.Spacing.md) {
                    ForEach(profiles) { profile in
                        ProfileCardView(profile: profile)
                            .padding(.horizontal, DesignTokens.Spacing.md)
                    }
                }
                .padding(.bottom, DesignTokens.Spacing.md)
            }
        }
    }
}

private struct LikesGridSection: View {
    let config: HomeSectionConfig
    let profiles: [UserProfile]

    private let columns = [
        GridItem(.flexible(), spacing: DesignTokens.Spacing.sm),
        GridItem(.flexible(), spacing: DesignTokens.Spacing.sm),
        GridItem(.flexible(), spacing: DesignTokens.Spacing.sm),
    ]

    var body: some View {
        VStack(alignment: .leading, spacing: DesignTokens.Spacing.sm) {
            if !config.title.isEmpty {
                Text(config.title)
                    .font(DesignTokens.Font.headlineMedium)
                    .padding(.horizontal, DesignTokens.Spacing.md)
            }
            ScrollView {
                LazyVGrid(columns: columns, spacing: DesignTokens.Spacing.sm) {
                    ForEach(profiles) { profile in
                        ProfileGridItemView(profile: profile)
                    }
                }
                .padding(DesignTokens.Spacing.md)
            }
        }
    }
}

private struct JournalPreviewSection: View {
    let config: HomeSectionConfig

    var body: some View {
        // TODO: wire to JournalViewModel
        VStack(alignment: .leading, spacing: DesignTokens.Spacing.sm) {
            Text(config.title)
                .font(DesignTokens.Font.headlineMedium)
            Text("Your journal entries will appear here.")
                .font(DesignTokens.Font.bodyMedium)
                .foregroundStyle(DesignTokens.Color.textSecondary)
        }
        .padding(DesignTokens.Spacing.md)
    }
}

private struct EmotionPromptSection: View {
    let config: HomeSectionConfig

    var body: some View {
        // TODO: wire to EmotionViewModel
        VStack(alignment: .leading, spacing: DesignTokens.Spacing.sm) {
            Text(config.title)
                .font(DesignTokens.Font.headlineMedium)
            Text("How are you feeling today?")
                .font(DesignTokens.Font.bodyMedium)
                .foregroundStyle(DesignTokens.Color.textSecondary)
        }
        .padding(DesignTokens.Spacing.md)
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

#Preview("Profile Feed Section") {
    ConfigurableSectionView(
        config: HomeSectionConfig(id: "profile_feed", type: .profileFeed, title: "Discover"),
        profiles: [
            UserProfile(
                id: "1", username: "CalmWaves27", avatarUrl: nil,
                moodColor: .calmRelaxed, moodLabel: "Calm and reflective",
                bio: "There's a part of me that wants to stay in bed all day.",
                vibes: ["Relationships", "Calm and reflective"]
            ),
        ]
    )
    .solaceTheme()
}
