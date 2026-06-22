import SwiftUI

// ---------------------------------------------------------------------------
// ProfileCardView — full profile card (matches Figma "front" frame)
// ---------------------------------------------------------------------------

public struct ProfileCardView: View {
    let profile: UserProfile
    var onConnectTap: () -> Void = {}

    private var mood: MoodColorOption? {
        defaultMoodPalette.first { $0.key == profile.moodColor }
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Avatar
            avatarSection

            // Info
            VStack(alignment: .leading, spacing: DesignTokens.Spacing.sm) {
                // Username + mood dot
                HStack(spacing: DesignTokens.Spacing.sm) {
                    Circle()
                        .fill(mood?.color ?? DesignTokens.Color.solaceTeal)
                        .frame(width: 10, height: 10)
                    Text(profile.username)
                        .font(DesignTokens.Font.headlineSmall)
                        .lineLimit(1)
                }

                // Minutes badge
                if !profile.minutesPerDay.isEmpty {
                    SurfaceChipView(label: profile.minutesPerDay)
                }

                // Mood label
                Text(profile.moodLabel)
                    .font(DesignTokens.Font.bodySmall)
                    .foregroundStyle(DesignTokens.Color.textSecondary)

                // Bio
                Text(profile.bio)
                    .font(DesignTokens.Font.bodyMedium)
                    .lineLimit(3)
                    .foregroundStyle(DesignTokens.Color.textPrimary)

                // Vibe tags
                if !profile.vibes.isEmpty {
                    HStack(spacing: DesignTokens.Spacing.xs) {
                        ForEach(profile.vibes.prefix(3), id: \.self) { vibe in
                            SurfaceChipView(label: vibe)
                        }
                    }
                }

                // CTA
                PrimaryButton(label: "Connect ahead", action: onConnectTap)
            }
            .padding(DesignTokens.Spacing.md)
        }
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.card))
        .shadow(color: .black.opacity(0.06), radius: 6, x: 0, y: 2)
    }

    @ViewBuilder
    private var avatarSection: some View {
        if let urlStr = profile.avatarUrl, let url = URL(string: urlStr) {
            AsyncImage(url: url) { phase in
                switch phase {
                case .success(let image):
                    image.resizable().scaledToFill()
                default:
                    placeholderAvatar
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: DesignTokens.Size.profileImageHeight)
            .clipped()
            .clipShape(
                .rect(
                    topLeadingRadius: DesignTokens.Radius.card,
                    topTrailingRadius: DesignTokens.Radius.card
                )
            )
        } else {
            placeholderAvatar
                .clipShape(
                    .rect(
                        topLeadingRadius: DesignTokens.Radius.card,
                        topTrailingRadius: DesignTokens.Radius.card
                    )
                )
        }
    }

    private var placeholderAvatar: some View {
        ZStack {
            DesignTokens.Color.chipUnselectedBg
            Text(String(profile.username.prefix(2)).uppercased())
                .font(DesignTokens.Font.displayMedium)
                .foregroundStyle(DesignTokens.Color.textSecondary)
        }
        .frame(maxWidth: .infinity)
        .frame(height: DesignTokens.Size.profileImageHeight)
    }
}

// ---------------------------------------------------------------------------
// ProfileGridItemView — compact tile for the Likes 3-column grid
// ---------------------------------------------------------------------------

public struct ProfileGridItemView: View {
    let profile: UserProfile
    var onTap: () -> Void = {}

    private var moodColor: Color {
        defaultMoodPalette.first { $0.key == profile.moodColor }?.color ?? DesignTokens.Color.solaceTeal
    }

    public var body: some View {
        Button(action: onTap) {
            VStack(spacing: 0) {
                if let urlStr = profile.avatarUrl, let url = URL(string: urlStr) {
                    AsyncImage(url: url) { phase in
                        if case .success(let img) = phase { img.resizable().scaledToFill() }
                        else { moodColor.opacity(0.2) }
                    }
                    .frame(maxWidth: .infinity)
                    .aspectRatio(1, contentMode: .fill)
                    .clipped()
                    .clipShape(
                        .rect(
                            topLeadingRadius: DesignTokens.Radius.card,
                            topTrailingRadius: DesignTokens.Radius.card
                        )
                    )
                } else {
                    ZStack {
                        moodColor.opacity(0.2)
                        Circle().fill(moodColor).frame(width: 8, height: 8)
                    }
                    .frame(maxWidth: .infinity)
                    .aspectRatio(1, contentMode: .fill)
                    .clipShape(
                        .rect(
                            topLeadingRadius: DesignTokens.Radius.card,
                            topTrailingRadius: DesignTokens.Radius.card
                        )
                    )
                }

                Text(profile.username)
                    .font(DesignTokens.Font.labelSmall)
                    .lineLimit(1)
                    .foregroundStyle(DesignTokens.Color.textPrimary)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 4)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.card))
            .shadow(color: .black.opacity(0.04), radius: 4, x: 0, y: 1)
        }
        .buttonStyle(.plain)
    }
}

// ---------------------------------------------------------------------------
// SurfaceChipView — small non-interactive label chip
// ---------------------------------------------------------------------------

public struct SurfaceChipView: View {
    let label: String

    public var body: some View {
        Text(label)
            .font(DesignTokens.Font.labelSmall)
            .foregroundStyle(DesignTokens.Color.textSecondary)
            .padding(.horizontal, DesignTokens.Spacing.sm)
            .padding(.vertical, 4)
            .background(DesignTokens.Color.chipUnselectedBg)
            .clipShape(Capsule())
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

#Preview("Full card") {
    ProfileCardView(
        profile: UserProfile(
            id: "1", username: "CalmWaves27", avatarUrl: nil,
            moodColor: .calmRelaxed, moodLabel: "Calm and reflective",
            bio: "There's a part of me that wants to stay in bed all day, but I'm making an effort to keep going.",
            vibes: ["Relationships", "Calm and reflective"]
        )
    )
    .padding()
}
