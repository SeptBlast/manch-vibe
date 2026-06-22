import Foundation
import FirebaseFirestore

// ---------------------------------------------------------------------------
// Firestore: users/{uid} — saves all onboarding answers and marks complete
// ---------------------------------------------------------------------------

@MainActor
public final class OnboardingRepository {

    public static let shared = OnboardingRepository()
    private let db = Firestore.firestore()

    public func isOnboardingComplete(uid: String) async -> Bool {
        guard let doc = try? await db.collection("users").document(uid).getDocument() else { return false }
        return doc["onboardingComplete"] as? Bool == true
    }

    public func saveOnboardingAnswers(uid: String, answers: OnboardingAnswers) async throws {
        try await db.collection("users").document(uid).setData([
            "username":           answers.username,
            "moodColor":          answers.moodColor.rawValue,
            "moodLabel":          answers.moodLabel,
            "bio":                answers.feelingSentence,
            "vibes":              answers.vibes,
            "minutesPerDay":      "2 minutes a day",
            "moodDescriptors":    answers.moodDescriptors,
            "feelingNow":         answers.feelingNow,
            "feelingLike":        answers.feelingLike,
            "strugglingWith":     answers.strugglingWith,
            "soughtSupport":      answers.soughtSupport,
            "openness":           answers.openness,
            "hopeToGain":         answers.hopeToGain,
            "feelingSentence":    answers.feelingSentence,
            "isVisible":          true,
            "onboardingComplete": true,
            "createdAt":          FieldValue.serverTimestamp(),
            "updatedAt":          FieldValue.serverTimestamp(),
        ], merge: true)
    }
}
