import SwiftUI

// ---------------------------------------------------------------------------
// ChatView — root of the chat tab; switches between list and thread
// ---------------------------------------------------------------------------

struct ChatView: View {
    let currentUid: String

    @StateObject private var vm = ChatViewModel()

    var body: some View {
        NavigationStack {
            ChatListView(
                previews: vm.previews,
                onOpenChat: { vm.openChat(otherUserId: $0) }
            )
            .navigationTitle("Messages")
            .navigationBarTitleDisplayMode(.large)
            .navigationDestination(isPresented: Binding(
                get: { vm.activeChatPartner != nil },
                set: { if !$0 { vm.closeChat() } }
            )) {
                if let partner = vm.activeChatPartner {
                    ChatThreadView(
                        currentUid: currentUid,
                        partner: partner,
                        messages: vm.messages,
                        isLoading: vm.isLoadingMessages,
                        isSending: vm.isSending,
                        onSend: { vm.sendMessage(text: $0) },
                        onBack: { vm.closeChat() }
                    )
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
// Chat list
// ---------------------------------------------------------------------------

private struct ChatListView: View {
    let previews: [ChatPreviewUiItem]
    let onOpenChat: (String) -> Void

    var body: some View {
        if previews.isEmpty {
            VStack(spacing: DesignTokens.Spacing.sm) {
                Spacer()
                Text("No messages yet")
                    .font(DesignTokens.Font.headlineSmall)
                    .foregroundStyle(DesignTokens.Color.textSecondary)
                Text("Connect with someone to start chatting")
                    .font(DesignTokens.Font.bodySmall)
                    .foregroundStyle(DesignTokens.Color.textSecondary)
                Spacer()
            }
            .frame(maxWidth: .infinity)
        } else {
            List(previews) { item in
                ChatPreviewRow(item: item)
                    .contentShape(Rectangle())
                    .onTapGesture { onOpenChat(item.otherUserId) }
                    .listRowInsets(EdgeInsets(top: 0, leading: DesignTokens.Spacing.md, bottom: 0, trailing: DesignTokens.Spacing.md))
                    .listRowSeparatorTint(DesignTokens.Color.divider)
            }
            .listStyle(.plain)
        }
    }
}

private struct ChatPreviewRow: View {
    let item: ChatPreviewUiItem

    var body: some View {
        HStack(spacing: DesignTokens.Spacing.md) {
            AvatarCircleView(avatarUrl: item.otherAvatarUrl, username: item.otherUsername, size: 48)

            VStack(alignment: .leading, spacing: 2) {
                Text(item.otherUsername)
                    .font(DesignTokens.Font.labelLarge)
                Text(item.lastMessage)
                    .font(DesignTokens.Font.bodySmall)
                    .foregroundStyle(DesignTokens.Color.textSecondary)
                    .lineLimit(1)
            }

            Spacer()

            Text(item.lastMessageAt.chatTimestamp())
                .font(DesignTokens.Font.labelSmall)
                .foregroundStyle(DesignTokens.Color.textSecondary)
        }
        .padding(.vertical, DesignTokens.Spacing.sm)
    }
}

// ---------------------------------------------------------------------------
// Chat thread
// ---------------------------------------------------------------------------

struct ChatThreadView: View {
    let currentUid: String
    let partner: UserProfile
    let messages: [ChatMessage]
    let isLoading: Bool
    let isSending: Bool
    let onSend: (String) -> Void
    let onBack: () -> Void

    @State private var draft: String = ""
    @FocusState private var composerFocused: Bool

    var body: some View {
        VStack(spacing: 0) {
            if isLoading {
                Spacer()
                ProgressView().tint(DesignTokens.Color.solaceTeal)
                Spacer()
            } else {
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(spacing: DesignTokens.Spacing.sm) {
                            ForEach(messages) { msg in
                                MessageBubbleView(msg: msg, isMine: msg.senderId == currentUid)
                                    .id(msg.id)
                            }
                        }
                        .padding(DesignTokens.Spacing.md)
                    }
                    .onChange(of: messages.count) { _ in
                        if let last = messages.last {
                            withAnimation { proxy.scrollTo(last.id, anchor: .bottom) }
                        }
                    }
                    .onAppear {
                        if let last = messages.last {
                            proxy.scrollTo(last.id, anchor: .bottom)
                        }
                    }
                }
            }

            ComposerBarView(
                draft: $draft,
                focused: $composerFocused,
                isSending: isSending,
                onSend: {
                    onSend(draft)
                    draft = ""
                }
            )
        }
        .navigationTitle(partner.username)
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onBack) {
                    HStack(spacing: 4) {
                        Image(systemName: "chevron.left")
                        AvatarCircleView(avatarUrl: partner.avatarUrl, username: partner.username, size: 28)
                        Text(partner.username)
                            .font(DesignTokens.Font.labelLarge)
                    }
                }
            }
        }
        .background(DesignTokens.Color.backgroundLight)
    }
}

private struct MessageBubbleView: View {
    let msg: ChatMessage
    let isMine: Bool

    var body: some View {
        HStack {
            if isMine { Spacer(minLength: 60) }
            Text(msg.text)
                .font(DesignTokens.Font.bodyMedium)
                .foregroundStyle(isMine ? .white : DesignTokens.Color.textPrimary)
                .padding(.horizontal, DesignTokens.Spacing.md)
                .padding(.vertical, DesignTokens.Spacing.sm)
                .background(isMine ? DesignTokens.Color.solaceTeal : Color.white)
                .clipShape(
                    UnevenRoundedRectangle(
                        topLeadingRadius: 16,
                        bottomLeadingRadius: isMine ? 16 : 4,
                        bottomTrailingRadius: isMine ? 4 : 16,
                        topTrailingRadius: 16
                    )
                )
                .shadow(color: .black.opacity(0.05), radius: 2, y: 1)
            if !isMine { Spacer(minLength: 60) }
        }
    }
}

private struct ComposerBarView: View {
    @Binding var draft: String
    var focused: FocusState<Bool>.Binding
    let isSending: Bool
    let onSend: () -> Void

    var body: some View {
        HStack(spacing: DesignTokens.Spacing.sm) {
            TextField("Write a message…", text: $draft, axis: .vertical)
                .font(DesignTokens.Font.bodyMedium)
                .lineLimit(1...4)
                .focused(focused)
                .padding(.horizontal, DesignTokens.Spacing.md)
                .padding(.vertical, DesignTokens.Spacing.sm)
                .background(DesignTokens.Color.chipUnselectedBg)
                .clipShape(Capsule())
                .onSubmit { if !draft.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty { onSend() } }

            Button(action: onSend) {
                Group {
                    if isSending {
                        ProgressView()
                            .tint(.white)
                            .scaleEffect(0.8)
                    } else {
                        Image(systemName: "arrow.up")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundStyle(.white)
                    }
                }
                .frame(width: 36, height: 36)
                .background(draft.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                    ? DesignTokens.Color.textSecondary.opacity(0.3)
                    : DesignTokens.Color.solaceTeal)
                .clipShape(Circle())
            }
            .disabled(draft.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || isSending)
        }
        .padding(.horizontal, DesignTokens.Spacing.md)
        .padding(.vertical, DesignTokens.Spacing.sm)
        .background(Color.white)
        .overlay(Divider(), alignment: .top)
        .padding(.bottom, DesignTokens.Spacing.xs)
    }
}

// ---------------------------------------------------------------------------
// Shared helper views
// ---------------------------------------------------------------------------

struct AvatarCircleView: View {
    let avatarUrl: String?
    let username: String
    let size: CGFloat

    var body: some View {
        if let url = avatarUrl, let parsed = URL(string: url) {
            AsyncImage(url: parsed) { img in img.resizable().scaledToFill() }
                placeholder: { DesignTokens.Color.solaceTeal.opacity(0.15) }
                .frame(width: size, height: size)
                .clipShape(Circle())
        } else {
            ZStack {
                Circle()
                    .fill(DesignTokens.Color.solaceTeal.opacity(0.15))
                Text(String(username.prefix(2)).uppercased())
                    .font(.system(size: size * 0.33, weight: .semibold))
                    .foregroundStyle(DesignTokens.Color.solaceTeal)
            }
            .frame(width: size, height: size)
        }
    }
}

// ---------------------------------------------------------------------------
// Date helpers
// ---------------------------------------------------------------------------

private extension Date {
    func chatTimestamp() -> String {
        let diff = Date().timeIntervalSince(self)
        if diff < 60 { return "now" }
        if diff < 3600 { return "\(Int(diff / 60))m" }
        if diff < 86400 {
            let f = DateFormatter(); f.dateFormat = "h:mm a"
            return f.string(from: self)
        }
        let f = DateFormatter(); f.dateFormat = "MMM d"
        return f.string(from: self)
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

#Preview("Chat list - empty") {
    ChatView(currentUid: "preview_uid")
        .solaceTheme()
}
