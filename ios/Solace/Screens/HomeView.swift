import SwiftUI

// ---------------------------------------------------------------------------
// HomeView — 5-tab root driven by HomeScreenConfig from remote config
// Tab visibility, order, and labels are all remotely controllable.
// TODO: inject real ViewModels per tab
// ---------------------------------------------------------------------------

public struct HomeView: View {
    let config: HomeScreenConfig
    var profiles: [UserProfile] = []
    var currentUid: String = ""

    @State private var selectedTabId: String

    public init(config: HomeScreenConfig = .init(), profiles: [UserProfile] = [], currentUid: String = "") {
        self.config = config
        self.profiles = profiles
        self.currentUid = currentUid
        _selectedTabId = State(initialValue: config.visibleTabs.first?.id ?? "home")
    }

    public var body: some View {
        TabView(selection: $selectedTabId) {
            ForEach(config.visibleTabs) { tab in
                tabBody(for: tab)
                    .tabItem {
                        Label(tab.label, systemImage: systemImage(for: tab.icon))
                    }
                    .tag(tab.id)
            }
        }
        .tint(DesignTokens.Color.navActive)
    }

    // MARK: - Tab bodies

    @ViewBuilder
    private func tabBody(for tab: BottomNavTabConfig) -> some View {
        switch tab.id {
        case "home":
            homeTabContent
        case "journal":
            journalTabContent
        case "emotion":
            emotionTabContent
        case "likes":
            likesTabContent
        case "chat":
            chatTabContent
        default:
            homeTabContent
        }
    }

    private var homeTabContent: some View {
        ScrollView {
            VStack(spacing: DesignTokens.Spacing.lg) {
                ForEach(config.visibleSections) { section in
                    ConfigurableSectionView(config: section, profiles: profiles)
                }
            }
        }
        .background(DesignTokens.Color.backgroundLight)
    }

    private var journalTabContent: some View {
        JournalView(currentUid: currentUid)
    }

    private var emotionTabContent: some View {
        EmotionView(currentUid: currentUid)
    }

    private var likesTabContent: some View {
        LikesView(currentUid: currentUid)
    }

    private var chatTabContent: some View {
        ChatView(currentUid: currentUid)
    }

    // MARK: - Icon mapping

    private func systemImage(for icon: String) -> String {
        switch icon {
        case "house":       return "house"
        case "book":        return "book"
        case "face.smiling":return "face.smiling"
        case "heart":       return "heart"
        case "bubble.left": return "bubble.left"
        default:            return "circle"
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

#Preview("Full nav") {
    HomeView(
        config: HomeScreenConfig(),
        profiles: [
            UserProfile(
                id: "1", username: "CalmWaves27", avatarUrl: nil,
                moodColor: .calmRelaxed, moodLabel: "Calm and reflective",
                bio: "There's a part of me that wants to stay in bed all day, but I'm making an effort.",
                vibes: ["Relationships"]
            ),
            UserProfile(
                id: "2", username: "kittycorner42", avatarUrl: nil,
                moodColor: .happyOptimistic, moodLabel: "Happy & Optimistic",
                bio: "Feeling good today!",
                vibes: ["Calm and reflective"]
            ),
        ]
    )
    .solaceTheme()
}

#Preview("Chat + Home only") {
    HomeView(
        config: HomeScreenConfig(
            bottomNav: [
                BottomNavTabConfig(id: "home", label: "Home", icon: "house"),
                BottomNavTabConfig(id: "chat", label: "Chat", icon: "bubble.left"),
            ]
        )
    )
    .solaceTheme()
}
