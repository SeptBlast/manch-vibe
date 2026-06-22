import Foundation
import FirebaseFirestore

@MainActor
final class EmotionViewModel: ObservableObject {

    @Published var logs: [MoodLog] = []
    @Published var isLoading: Bool = true
    @Published var isSaving: Bool = false
    @Published var savedToday: Bool = false
    @Published var selectedEmoji: String = "😐"
    @Published var selectedMoodKey: MoodColorKey = .calmRelaxed
    @Published var note: String = ""
    @Published var error: String? = nil

    private var currentUid: String = ""
    private var listener: ListenerRegistration?
    private let repo = MoodRepository.shared

    func initialize(uid: String) {
        guard uid != currentUid else { return }
        currentUid = uid
        listener?.remove()
        listener = repo.observeLogs(uid: uid) { [weak self] logs in
            guard let self else { return }
            self.logs = logs
            self.isLoading = false
        }
    }

    func logMood() {
        isSaving = true
        Task {
            do {
                try await repo.addLog(
                    uid: currentUid,
                    emoji: selectedEmoji,
                    moodColorKey: selectedMoodKey.rawValue,
                    note: note.trimmingCharacters(in: .whitespacesAndNewlines)
                )
                note = ""
                savedToday = true
            } catch {
                self.error = error.localizedDescription
            }
            isSaving = false
        }
    }

    func dismissConfirmation() { savedToday = false }

    deinit { listener?.remove() }
}
