import Foundation
import FirebaseFirestore

// ---------------------------------------------------------------------------
// Firestore schema  →  journals/{uid}/entries/{entryId}
//   emoji, text, moodColor, createdAt
// ---------------------------------------------------------------------------

public struct JournalEntry: Identifiable, Codable {
    public var id: String
    public var emoji: String
    public var text: String
    public var moodColor: String
    public var createdAt: Date
}

@MainActor
public final class JournalRepository {

    public static let shared = JournalRepository()
    private let db = Firestore.firestore()

    private func entriesCol(uid: String) -> CollectionReference {
        db.collection("journals").document(uid).collection("entries")
    }

    public func observeEntries(uid: String, onChange: @escaping ([JournalEntry]) -> Void) -> ListenerRegistration {
        entriesCol(uid: uid)
            .order(by: "createdAt", descending: true)
            .limit(to: 100)
            .addSnapshotListener { snap, _ in
                let entries = snap?.documents.map { doc -> JournalEntry in
                    JournalEntry(
                        id: doc.documentID,
                        emoji: doc["emoji"] as? String ?? "",
                        text: doc["text"] as? String ?? "",
                        moodColor: doc["moodColor"] as? String ?? "",
                        createdAt: (doc["createdAt"] as? Timestamp)?.dateValue() ?? Date()
                    )
                } ?? []
                onChange(entries)
            }
    }

    public func addEntry(uid: String, emoji: String, text: String, moodColor: String) async throws -> String {
        let ref = entriesCol(uid: uid).document()
        try await ref.setData([
            "emoji":     emoji,
            "text":      text,
            "moodColor": moodColor,
            "createdAt": FieldValue.serverTimestamp(),
        ])
        return ref.documentID
    }

    public func deleteEntry(uid: String, entryId: String) async throws {
        try await entriesCol(uid: uid).document(entryId).delete()
    }
}
