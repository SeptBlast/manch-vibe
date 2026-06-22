import Foundation
import FirebaseFirestore

// ---------------------------------------------------------------------------
// Firestore schema  →  moods/{uid}/logs/{logId}
//   emoji, moodColorKey, note, createdAt
// ---------------------------------------------------------------------------

public struct MoodLog: Identifiable {
    public var id: String
    public var emoji: String
    public var moodColorKey: String
    public var note: String
    public var createdAt: Date
}

@MainActor
public final class MoodRepository {

    public static let shared = MoodRepository()
    private let db = Firestore.firestore()

    private func logsCol(uid: String) -> CollectionReference {
        db.collection("moods").document(uid).collection("logs")
    }

    public func observeLogs(uid: String, limit: Int = 30, onChange: @escaping ([MoodLog]) -> Void) -> ListenerRegistration {
        logsCol(uid: uid)
            .order(by: "createdAt", descending: true)
            .limit(to: limit)
            .addSnapshotListener { snap, _ in
                let logs = snap?.documents.map { doc -> MoodLog in
                    MoodLog(
                        id: doc.documentID,
                        emoji: doc["emoji"] as? String ?? "",
                        moodColorKey: doc["moodColorKey"] as? String ?? "",
                        note: doc["note"] as? String ?? "",
                        createdAt: (doc["createdAt"] as? Timestamp)?.dateValue() ?? Date()
                    )
                } ?? []
                onChange(logs)
            }
    }

    public func addLog(uid: String, emoji: String, moodColorKey: String, note: String) async throws {
        try await logsCol(uid: uid).addDocument(data: [
            "emoji":        emoji,
            "moodColorKey": moodColorKey,
            "note":         note,
            "createdAt":    FieldValue.serverTimestamp(),
        ])
    }
}
