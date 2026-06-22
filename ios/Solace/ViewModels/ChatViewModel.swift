import Foundation
import FirebaseFirestore

struct ChatPreviewUiItem: Identifiable {
    var id: String          // chatId
    var otherUserId: String
    var otherUsername: String
    var otherAvatarUrl: String?
    var lastMessage: String
    var lastMessageAt: Date
}

@MainActor
final class ChatViewModel: ObservableObject {

    @Published var previews: [ChatPreviewUiItem] = []
    @Published var messages: [ChatMessage] = []
    @Published var activeChatPartner: UserProfile? = nil
    @Published var isLoadingMessages: Bool = false
    @Published var isSending: Bool = false
    @Published var error: String? = nil

    private var currentUid: String = ""
    private var previewsListener: ListenerRegistration?
    private var messagesListener: ListenerRegistration?

    private let chatRepo = ChatRepository.shared
    private let userRepo = UserRepository.shared

    func initialize(uid: String) {
        guard uid != currentUid else { return }
        currentUid = uid
        observePreviews()
    }

    private func observePreviews() {
        previewsListener?.remove()
        previewsListener = chatRepo.observePreviews(uid: currentUid) { [weak self] rawPreviews in
            guard let self else { return }
            Task { await self.resolvePreviews(rawPreviews) }
        }
    }

    private func resolvePreviews(_ raw: [ChatPreview]) async {
        var items: [ChatPreviewUiItem] = []
        for p in raw {
            let profile = try? await userRepo.getProfile(uid: p.otherUserId)
            items.append(ChatPreviewUiItem(
                id: p.id,
                otherUserId: p.otherUserId,
                otherUsername: profile?.username ?? p.otherUserId,
                otherAvatarUrl: profile?.avatarUrl,
                lastMessage: p.lastMessage,
                lastMessageAt: p.lastMessageAt
            ))
        }
        previews = items
    }

    func openChat(otherUserId: String) {
        isLoadingMessages = true
        Task {
            activeChatPartner = try? await userRepo.getProfile(uid: otherUserId)
        }
        messagesListener?.remove()
        messagesListener = chatRepo.observeMessages(uid1: currentUid, uid2: otherUserId) { [weak self] msgs in
            guard let self else { return }
            self.messages = msgs
            self.isLoadingMessages = false
        }
    }

    func closeChat() {
        messagesListener?.remove()
        messagesListener = nil
        activeChatPartner = nil
        messages = []
    }

    func sendMessage(text: String) {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty, let partner = activeChatPartner else { return }
        isSending = true
        Task {
            do {
                try await chatRepo.sendMessage(from: currentUid, to: partner.id, text: trimmed)
            } catch {
                self.error = error.localizedDescription
            }
            isSending = false
        }
    }

    deinit {
        previewsListener?.remove()
        messagesListener?.remove()
    }
}
