import SwiftUI

struct LikesView: View {
    let currentUid: String
    var onConnect: (String) -> Void = { _ in }

    @StateObject private var vm = LikesViewModel()

    private let columns = Array(repeating: GridItem(.flexible(), spacing: DesignTokens.Spacing.sm), count: 2)

    var body: some View {
        NavigationStack {
            Group {
                if vm.isLoading {
                    ProgressView().tint(DesignTokens.Color.solaceTeal)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if vm.likers.isEmpty {
                    EmptyLikesView()
                } else {
                    ScrollView {
                        LazyVGrid(columns: columns, spacing: DesignTokens.Spacing.sm) {
                            ForEach(vm.likers) { profile in
                                LikerCard(
                                    profile: profile,
                                    isLikedBack: vm.likedBack.contains(profile.id),
                                    onLikeBack: { vm.likeBack(targetUid: profile.id) },
                                    onConnect: { onConnect(profile.id) }
                                )
                            }
                        }
                        .padding(DesignTokens.Spacing.md)
                    }
                }
            }
            .navigationTitle("Likes you")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                if !vm.isLoading && !vm.likers.isEmpty {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Text("\(vm.likers.count) \(vm.likers.count == 1 ? "person" : "people")")
                            .font(DesignTokens.Font.bodySmall)
                            .foregroundStyle(DesignTokens.Color.textSecondary)
                    }
                }
            }
        }
        .onAppear { vm.initialize(uid: currentUid) }
        .alert("Error", isPresented: .constant(vm.error != nil)) {
            Button("OK") { vm.error = nil }
        } message: {
            Text(vm.error ?? "")
        }
    }
}

// ---------------------------------------------------------------------------
// Liker card
// ---------------------------------------------------------------------------

private struct LikerCard: View {
    let profile: UserProfile
    let isLikedBack: Bool
    let onLikeBack: () -> Void
    let onConnect: () -> Void

    private var moodColor: Color {
        defaultMoodPalette.first { $0.key == profile.moodColor }?.color
            ?? DesignTokens.Color.solaceTeal
    }

    var body: some View {
        VStack(spacing: 0) {
            // Avatar
            ZStack(alignment: .topLeading) {
                avatarView
                    .frame(maxWidth: .infinity)
                    .frame(height: 140)
                    .clipped()

                if isLikedBack {
                    Text("Match ✨")
                        .font(DesignTokens.Font.labelSmall)
                        .foregroundStyle(.white)
                        .padding(.horizontal, DesignTokens.Spacing.sm)
                        .padding(.vertical, 3)
                        .background(DesignTokens.Color.solaceTeal)
                        .clipShape(UnevenRoundedRectangle(
                            topLeadingRadius: DesignTokens.Radius.card,
                            bottomLeadingRadius: 0,
                            bottomTrailingRadius: DesignTokens.Radius.card,
                            topTrailingRadius: 0
                        ))
                }
            }

            // Info + actions
            VStack(alignment: .leading, spacing: 4) {
                Text(profile.username)
                    .font(DesignTokens.Font.labelLarge)
                    .lineLimit(1)

                HStack(spacing: 4) {
                    Circle().fill(moodColor).frame(width: 6, height: 6)
                    Text(profile.moodLabel)
                        .font(DesignTokens.Font.labelSmall)
                        .foregroundStyle(moodColor)
                        .lineLimit(1)
                }

                HStack(spacing: DesignTokens.Spacing.xs) {
                    // Like back
                    Button(action: onLikeBack) {
                        Image(systemName: isLikedBack ? "heart.fill" : "heart")
                            .font(.system(size: 15))
                            .foregroundStyle(isLikedBack ? DesignTokens.Color.solaceTeal : DesignTokens.Color.textSecondary)
                            .frame(width: 36, height: 32)
                            .background(isLikedBack
                                ? DesignTokens.Color.solaceTeal.opacity(0.1)
                                : DesignTokens.Color.chipUnselectedBg)
                            .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.button))
                    }
                    .buttonStyle(.plain)

                    // Connect
                    Button(action: onConnect) {
                        Text("Connect")
                            .font(DesignTokens.Font.labelSmall)
                            .foregroundStyle(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 32)
                            .background(DesignTokens.Color.solaceTeal)
                            .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.button))
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(DesignTokens.Spacing.sm)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.card))
        .shadow(color: .black.opacity(0.06), radius: 4, y: 2)
    }

    @ViewBuilder
    private var avatarView: some View {
        if let urlStr = profile.avatarUrl, let url = URL(string: urlStr) {
            AsyncImage(url: url) { img in img.resizable().scaledToFill() }
                placeholder: { moodColor.opacity(0.15) }
        } else {
            ZStack {
                moodColor.opacity(0.15)
                Text(String(profile.username.prefix(2)).uppercased())
                    .font(DesignTokens.Font.displayMedium)
                    .foregroundStyle(moodColor)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Empty state
// ---------------------------------------------------------------------------

private struct EmptyLikesView: View {
    var body: some View {
        VStack(spacing: DesignTokens.Spacing.md) {
            Spacer()
            Text("💝")
                .font(.system(size: 56))
            Text("No likes yet")
                .font(DesignTokens.Font.headlineSmall)
                .foregroundStyle(DesignTokens.Color.textSecondary)
            Text("Keep your profile active to attract connections")
                .font(DesignTokens.Font.bodySmall)
                .foregroundStyle(DesignTokens.Color.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, DesignTokens.Spacing.xl)
            Spacer()
        }
        .frame(maxWidth: .infinity)
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

#Preview {
    LikesView(currentUid: "preview_uid")
        .solaceTheme()
}
