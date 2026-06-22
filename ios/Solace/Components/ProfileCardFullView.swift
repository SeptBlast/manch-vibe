import SwiftUI

// ---------------------------------------------------------------------------
// ProfileCardFrontView — Figma "front" frame
// ---------------------------------------------------------------------------

struct ProfileCardFrontView: View {
    let profile: UserProfile
    var onEditClick: (() -> Void)? = nil
    var onConnectClick: () -> Void = {}
    var imageOverride: UIImage? = nil

    private var moodColor: Color {
        defaultMoodPalette.first { $0.key == profile.moodColor }?.color ?? DesignTokens.Color.solaceTeal
    }

    var body: some View {
        cardContent
    }

    private var cardContent: some View {
        VStack(alignment: .leading, spacing: 0) {
            CardHeaderView(onEditClick: onEditClick)

            AvatarSectionView(avatarUrl: profile.avatarUrl, username: profile.username, imageOverride: imageOverride)

            VStack(alignment: .leading, spacing: DesignTokens.Spacing.sm) {
                // Mood descriptor chips
                if !profile.vibes.isEmpty {
                    HStack(spacing: DesignTokens.Spacing.xs) {
                        ForEach(profile.vibes.prefix(2), id: \.self) { tag in
                            MoodChipView(label: tag, color: moodColor, filled: false)
                        }
                    }
                }

                // Username + dot
                HStack {
                    Text(profile.username)
                        .font(DesignTokens.Font.displayMedium)
                    Spacer()
                    Circle()
                        .fill(moodColor)
                        .frame(width: 8, height: 8)
                }

                // Mood label
                Text(profile.moodLabel)
                    .font(DesignTokens.Font.bodySmall)
                    .foregroundStyle(DesignTokens.Color.textSecondary)

                Divider()

                // Bio
                Text(profile.bio)
                    .font(DesignTokens.Font.bodyMedium)
                    .lineLimit(3)

                // Vibe chips
                HStack(spacing: DesignTokens.Spacing.xs) {
                    ForEach(profile.vibes.prefix(3), id: \.self) { vibe in
                        SurfaceChipView(label: vibe)
                    }
                }

                // Footer tagline
                Text("You're all ❤️  Tailored to connect your thoughts")
                    .font(DesignTokens.Font.bodySmall)
                    .italic()
                    .foregroundStyle(DesignTokens.Color.textSecondary)

                PrimaryButton(label: "Connect ahead", action: onConnectClick)
                    .padding(.top, DesignTokens.Spacing.xs)
            }
            .padding(DesignTokens.Spacing.md)
        }
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.card))
        .shadow(color: .black.opacity(0.08), radius: 8, y: 4)
    }
}

// ---------------------------------------------------------------------------
// ProfileCardBackView — Figma "back" frame
// ---------------------------------------------------------------------------

struct ProfileCardBackView: View {
    let profile: UserProfile
    var soughtSupport: String = ""
    var openness: String = ""
    var feelingNow: [String] = []
    var onEditClick: (() -> Void)? = nil
    var onConnectClick: () -> Void = {}
    var imageOverride: UIImage? = nil

    private var moodColor: Color {
        defaultMoodPalette.first { $0.key == profile.moodColor }?.color ?? DesignTokens.Color.solaceTeal
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            CardHeaderView(onEditClick: onEditClick)

            VStack(alignment: .leading, spacing: DesignTokens.Spacing.sm) {
                // Large username
                Text(profile.username)
                    .font(DesignTokens.Font.displayLarge)

                // Short bio tagline
                Text(String(profile.bio.prefix(60)) + (profile.bio.count > 60 ? "…" : ""))
                    .font(DesignTokens.Font.bodyMedium)
                    .foregroundStyle(DesignTokens.Color.textSecondary)

                Divider()

                // Feeling now
                if !feelingNow.isEmpty {
                    VStack(alignment: .leading, spacing: 4) {
                        ForEach(feelingNow.prefix(2), id: \.self) { f in
                            Text(f)
                                .font(DesignTokens.Font.bodySmall)
                                .foregroundStyle(DesignTokens.Color.textSecondary)
                        }
                    }
                }

                // Mood chip filled
                MoodChipView(label: profile.moodLabel, color: moodColor, filled: true)

                Divider()

                // Full bio
                Text(profile.bio)
                    .font(DesignTokens.Font.bodyMedium)
                    .lineLimit(3)

                // Sought support
                if !soughtSupport.isEmpty {
                    BackInfoRow(text: soughtSupport)
                }

                // Openness
                if !openness.isEmpty {
                    BackInfoRow(text: openness)
                }

                Divider()

                // Vibe chips
                HStack(spacing: DesignTokens.Spacing.xs) {
                    ForEach(profile.vibes.prefix(3), id: \.self) { vibe in
                        SurfaceChipView(label: vibe)
                    }
                }

                Text("You're all ❤️  Tailored to connect your thoughts")
                    .font(DesignTokens.Font.bodySmall)
                    .italic()
                    .foregroundStyle(DesignTokens.Color.textSecondary)

                PrimaryButton(label: "Connect ahead", action: onConnectClick)
                    .padding(.top, DesignTokens.Spacing.xs)
            }
            .padding(DesignTokens.Spacing.md)
        }
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.card))
        .shadow(color: .black.opacity(0.08), radius: 8, y: 4)
    }
}

// ---------------------------------------------------------------------------
// Shared sub-views
// ---------------------------------------------------------------------------

private struct CardHeaderView: View {
    var onEditClick: (() -> Void)?

    var body: some View {
        HStack {
            Text("Profile Card")
                .font(DesignTokens.Font.labelLarge)
                .foregroundStyle(DesignTokens.Color.textSecondary)
            Spacer()
            if let edit = onEditClick {
                Button(action: edit) {
                    Label("Edit", systemImage: "pencil")
                        .font(DesignTokens.Font.labelSmall)
                        .foregroundStyle(DesignTokens.Color.textSecondary)
                }
            }
        }
        .padding(.horizontal, DesignTokens.Spacing.md)
        .padding(.vertical, DesignTokens.Spacing.sm)
    }
}

private struct AvatarSectionView: View {
    let avatarUrl: String?
    let username: String
    var imageOverride: UIImage? = nil

    var body: some View {
        if let img = imageOverride {
            Image(uiImage: img)
                .resizable()
                .scaledToFill()
                .frame(maxWidth: .infinity)
                .frame(height: DesignTokens.Size.profileImageHeight)
                .clipped()
        } else if let url = avatarUrl, let parsed = URL(string: url) {
            AsyncImage(url: parsed) { image in
                image.resizable().scaledToFill()
            } placeholder: {
                Color(DesignTokens.Color.chipUnselectedBg)
            }
            .frame(maxWidth: .infinity)
            .frame(height: DesignTokens.Size.profileImageHeight)
            .clipped()
        } else {
            ZStack {
                DesignTokens.Color.chipUnselectedBg
                Text(String(username.prefix(2)).uppercased())
                    .font(DesignTokens.Font.displayLarge)
                    .foregroundStyle(DesignTokens.Color.textSecondary)
            }
            .frame(maxWidth: .infinity)
            .frame(height: DesignTokens.Size.profileImageHeight)
        }
    }
}

struct MoodChipView: View {
    let label: String
    let color: Color
    var filled: Bool = false

    var body: some View {
        HStack(spacing: 4) {
            Circle().fill(color).frame(width: 8, height: 8)
            Text(label)
                .font(DesignTokens.Font.labelSmall)
                .foregroundStyle(filled ? .white : color)
        }
        .padding(.horizontal, DesignTokens.Spacing.sm)
        .padding(.vertical, 4)
        .background(filled ? DesignTokens.Color.solaceTeal : color.opacity(0.12))
        .clipShape(Capsule())
        .overlay(filled ? nil : Capsule().stroke(color, lineWidth: 1))
    }
}

private struct BackInfoRow: View {
    let text: String
    var body: some View {
        Text("• \(text)")
            .font(DesignTokens.Font.bodySmall)
            .foregroundStyle(DesignTokens.Color.textSecondary)
            .lineLimit(2)
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

#Preview("Card Front") {
    let sample = UserProfile(
        id: "1", username: "CalmWaves27",
        moodColor: .calmRelaxed, moodLabel: "Calm and reflective",
        bio: "There's a part of me that wants to stay in bed all day, but I'm making an effort.",
        vibes: ["Relationships", "Calm and reflective", "Growth"]
    )
    ProfileCardFrontView(profile: sample)
        .padding()
        .background(DesignTokens.Color.backgroundDark)
}

#Preview("Card Back") {
    let sample = UserProfile(
        id: "1", username: "CalmWaves27",
        moodColor: .calmRelaxed, moodLabel: "Calm and reflective",
        bio: "There's a part of me that wants to stay in bed all day, but I'm making an effort.",
        vibes: ["Relationships", "Calm and reflective", "Growth"]
    )
    ProfileCardBackView(
        profile: sample,
        soughtSupport: "Yes, I'm currently in therapy",
        openness: "I'd like to connect with a few like-minded people",
        feelingNow: ["I'm feeling numb and disconnected"]
    )
    .padding()
    .background(DesignTokens.Color.backgroundDark)
}
