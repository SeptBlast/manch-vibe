import Foundation
import FirebaseFirestore

@MainActor
final class JournalViewModel: ObservableObject {

    @Published var entries: [JournalEntry] = []
    @Published var isLoading: Bool = true
    @Published var isSaving: Bool = false
    @Published var showCompose: Bool = false
    @Published var draftEmoji: String = "😐"
    @Published var draftText: String = ""
    @Published var error: String? = nil

    private var currentUid: String = ""
    private var listener: ListenerRegistration?
    private let repo = JournalRepository.shared

    func initialize(uid: String) {
        guard uid != currentUid else { return }
        currentUid = uid
        listener?.remove()
        listener = repo.observeEntries(uid: uid) { [weak self] entries in
            guard let self else { return }
            self.entries = entries
            self.isLoading = false
        }
    }

    func openCompose() {
        draftEmoji = "😐"
        draftText = ""
        showCompose = true
    }

    func dismissCompose() { showCompose = false }

    func saveEntry() {
        let text = draftText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty else { return }
        isSaving = true
        Task {
            do {
                _ = try await repo.addEntry(uid: currentUid, emoji: draftEmoji, text: text, moodColor: "CALM_RELAXED")
                showCompose = false
            } catch {
                self.error = error.localizedDescription
            }
            isSaving = false
        }
    }

    func deleteEntry(_ entry: JournalEntry) {
        Task {
            try? await repo.deleteEntry(uid: currentUid, entryId: entry.id)
        }
    }

    deinit { listener?.remove() }
}
