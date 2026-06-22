import Foundation
import FirebaseFirestore
import FirebaseStorage

// ---------------------------------------------------------------------------
// Firestore schema  →  users/{uid}
//   username, avatarUrl, moodColor, moodLabel, bio, vibes,
//   minutesPerDay, isVisible, createdAt, updatedAt
// ---------------------------------------------------------------------------

@MainActor
public final class UserRepository {

    public static let shared = UserRepository()

    private let db = Firestore.firestore()
    private let storage = Storage.storage()
    private var usersCol: CollectionReference { db.collection("users") }

    // ── Feed ──────────────────────────────────────────────────────────────

    /// Live listener — passes new profile list every time Firestore updates.
    public func observeProfiles(onChange: @escaping ([UserProfile]) -> Void) -> ListenerRegistration {
        usersCol
            .whereField("isVisible", isEqualTo: true)
            .order(by: "updatedAt", descending: true)
            .limit(to: 50)
            .addSnapshotListener { snapshot, _ in
                let profiles = snapshot?.documents.compactMap { doc -> UserProfile? in
                    guard let username = doc["username"] as? String else { return nil }
                    return UserProfile(
                        id: doc.documentID,
                        username: username,
                        avatarUrl: doc["avatarUrl"] as? String,
                        moodColor: MoodColorKey(rawValue: doc["moodColor"] as? String ?? "") ?? .calmRelaxed,
                        moodLabel: doc["moodLabel"] as? String ?? "",
                        bio: doc["bio"] as? String ?? "",
                        vibes: doc["vibes"] as? [String] ?? [],
                        minutesPerDay: doc["minutesPerDay"] as? String ?? "2 minutes a day"
                    )
                } ?? []
                onChange(profiles)
            }
    }

    public func getProfile(uid: String) async throws -> UserProfile? {
        let doc = try await usersCol.document(uid).getDocument()
        guard doc.exists, let username = doc["username"] as? String else { return nil }
        return UserProfile(
            id: uid,
            username: username,
            avatarUrl: doc["avatarUrl"] as? String,
            moodColor: MoodColorKey(rawValue: doc["moodColor"] as? String ?? "") ?? .calmRelaxed,
            moodLabel: doc["moodLabel"] as? String ?? "",
            bio: doc["bio"] as? String ?? "",
            vibes: doc["vibes"] as? [String] ?? [],
            minutesPerDay: doc["minutesPerDay"] as? String ?? ""
        )
    }

    // ── Write ─────────────────────────────────────────────────────────────

    public func saveProfile(uid: String, profile: UserProfile) async throws {
        try await usersCol.document(uid).setData([
            "username":      profile.username,
            "avatarUrl":     profile.avatarUrl as Any,
            "moodColor":     profile.moodColor.rawValue,
            "moodLabel":     profile.moodLabel,
            "bio":           profile.bio,
            "vibes":         profile.vibes,
            "minutesPerDay": profile.minutesPerDay,
            "isVisible":     true,
            "updatedAt":     FieldValue.serverTimestamp(),
        ], merge: true)
    }

    /// Upload JPEG avatar, returns download URL string.
    public func uploadAvatar(uid: String, imageData: Data) async throws -> String {
        let ref = storage.reference().child("avatars/\(uid).jpg")
        let meta = StorageMetadata()
        meta.contentType = "image/jpeg"
        _ = try await ref.putDataAsync(imageData, metadata: meta)
        let url = try await ref.downloadURL()
        return url.absoluteString
    }

    // ── Likes ─────────────────────────────────────────────────────────────

    public func sendLike(from fromUid: String, to toUid: String) async throws {
        try await db.collection("likes")
            .document(toUid)
            .collection("received")
            .document(fromUid)
            .setData(["fromUserId": fromUid, "createdAt": FieldValue.serverTimestamp()])
    }

    /// Observe UIDs of users who liked [uid], newest first.
    public func observeLikerIds(uid: String, onChange: @escaping ([String]) -> Void) -> ListenerRegistration {
        db.collection("likes").document(uid).collection("received")
            .order(by: "createdAt", descending: true)
            .addSnapshotListener { snap, _ in
                let ids = snap?.documents.compactMap { $0["fromUserId"] as? String } ?? []
                onChange(ids)
            }
    }
}
