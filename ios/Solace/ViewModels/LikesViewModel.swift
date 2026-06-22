import Foundation
import FirebaseFirestore

@MainActor
final class LikesViewModel: ObservableObject {

    @Published var likers: [UserProfile] = []
    @Published var isLoading: Bool = true
    @Published var likedBack: Set<String> = []
    @Published var error: String? = nil

    private var currentUid: String = ""
    private var listener: ListenerRegistration?
    private let userRepo = UserRepository.shared

    func initialize(uid: String) {
        guard uid != currentUid else { return }
        currentUid = uid
        listener?.remove()
        listener = userRepo.observeLikerIds(uid: uid) { [weak self] ids in
            guard let self else { return }
            Task { await self.resolveProfiles(ids: ids) }
        }
    }

    private func resolveProfiles(ids: [String]) async {
        isLoading = true
        var resolved: [UserProfile] = []
        for id in ids {
            if let profile = try? await userRepo.getProfile(uid: id) {
                resolved.append(profile)
            }
        }
        likers = resolved
        isLoading = false
    }

    func likeBack(targetUid: String) {
        Task {
            do {
                try await userRepo.sendLike(from: currentUid, to: targetUid)
                likedBack.insert(targetUid)
            } catch {
                self.error = error.localizedDescription
            }
        }
    }

    deinit { listener?.remove() }
}
