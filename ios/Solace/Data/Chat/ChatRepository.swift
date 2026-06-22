import Foundation
import FirebaseFirestore

// ---------------------------------------------------------------------------
// Firestore schema
// chats/{chatId}   (chatId = [uid1, uid2].sorted().joined(separator: "_"))
//   participants, lastMessage, lastSenderId, lastMessageAt
// chats/{chatId}/messages/{msgId}
//   text, senderId, createdAt
// ---------------------------------------------------------------------------

public struct ChatPreview: Identifiable {
    public var id: String       // chatId
    public var otherUserId: String
    public var lastMessage: String
    public var lastMessageAt: Date
}

public struct ChatMessage: Identifiable {
    public var id: String
    public var text: String
    public var senderId: String
    public var createdAt: Date
}

@MainActor
public final class ChatRepository {

    public static let shared = ChatRepository()
    private let db = Firestore.firestore()
    private var chatsCol: CollectionReference { db.collection("chats") }

    private func chatId(_ uid1: String, _ uid2: String) -> String {
        [uid1, uid2].sorted().joined(separator: "_")
    }

    public func observePreviews(uid: String, onChange: @escaping ([ChatPreview]) -> Void) -> ListenerRegistration {
        chatsCol
            .whereField("participants", arrayContains: uid)
            .order(by: "lastMessageAt", descending: true)
            .addSnapshotListener { snap, _ in
                let previews = snap?.documents.compactMap { doc -> ChatPreview? in
                    guard let participants = doc["participants"] as? [String],
                          let other = participants.first(where: { $0 != uid })
                    else { return nil }
                    return ChatPreview(
                        id: doc.documentID,
                        otherUserId: other,
                        lastMessage: doc["lastMessage"] as? String ?? "",
                        lastMessageAt: (doc["lastMessageAt"] as? Timestamp)?.dateValue() ?? Date()
                    )
                } ?? []
                onChange(previews)
            }
    }

    public func observeMessages(uid1: String, uid2: String, onChange: @escaping ([ChatMessage]) -> Void) -> ListenerRegistration {
        chatsCol.document(chatId(uid1, uid2)).collection("messages")
            .order(by: "createdAt")
            .addSnapshotListener { snap, _ in
                let msgs = snap?.documents.map { doc -> ChatMessage in
                    ChatMessage(
                        id: doc.documentID,
                        text: doc["text"] as? String ?? "",
                        senderId: doc["senderId"] as? String ?? "",
                        createdAt: (doc["createdAt"] as? Timestamp)?.dateValue() ?? Date()
                    )
                } ?? []
                onChange(msgs)
            }
    }

    public func sendMessage(from senderUid: String, to recipientUid: String, text: String) async throws {
        let cid = chatId(senderUid, recipientUid)
        let chatDoc = chatsCol.document(cid)
        let now = FieldValue.serverTimestamp()

        try await chatDoc.setData([
            "participants":   [senderUid, recipientUid],
            "lastMessage":    text,
            "lastSenderId":   senderUid,
            "lastMessageAt":  now,
        ], merge: true)

        try await chatDoc.collection("messages").addDocument(data: [
            "text":      text,
            "senderId":  senderUid,
            "createdAt": now,
        ])
    }
}
