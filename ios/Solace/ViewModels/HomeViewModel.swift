import SwiftUI
import FirebaseFirestore

@MainActor
public final class HomeViewModel: ObservableObject {

    @Published public private(set) var uiConfig: SolaceUiConfig = .default
    @Published public private(set) var profiles: [UserProfile] = []
    @Published public private(set) var isLoading: Bool = true
    @Published public private(set) var error: String? = nil

    private let userRepo = UserRepository.shared
    private let configRepo = RemoteConfigRepository.shared
    private var profilesListener: ListenerRegistration?

    public init() {}

    deinit { profilesListener?.remove() }

    // ── Config ────────────────────────────────────────────────────────────

    public func loadConfig() async {
        uiConfig = await configRepo.fetchUiConfig()
    }

    // ── Profiles ──────────────────────────────────────────────────────────

    public func startObservingProfiles() {
        profilesListener?.remove()
        profilesListener = userRepo.observeProfiles { [weak self] profiles in
            Task { @MainActor in
                self?.profiles = profiles
                self?.isLoading = false
            }
        }
    }

    public func stopObservingProfiles() {
        profilesListener?.remove()
        profilesListener = nil
    }

    // ── Actions ───────────────────────────────────────────────────────────

    public func sendLike(from currentUid: String, to targetUid: String) {
        Task {
            do { try await userRepo.sendLike(from: currentUid, to: targetUid) }
            catch { self.error = error.localizedDescription }
        }
    }

    public func dismissError() { error = nil }
}
