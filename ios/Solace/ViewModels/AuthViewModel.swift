import SwiftUI
import FirebaseAuth
import AuthenticationServices

public enum AuthState: Equatable {
    case unauthenticated
    case loading
    case authenticated
    case error(String)

    public static func == (lhs: AuthState, rhs: AuthState) -> Bool {
        switch (lhs, rhs) {
        case (.unauthenticated, .unauthenticated),
             (.loading, .loading),
             (.authenticated, .authenticated): return true
        case (.error(let a), .error(let b)): return a == b
        default: return false
        }
    }
}

@MainActor
public final class AuthViewModel: ObservableObject {

    @Published public private(set) var state: AuthState = .unauthenticated
    @Published public private(set) var currentUserId: String? = nil

    private let repo = AuthRepository.shared

    public init() {
        // Reflect existing session immediately
        if let user = repo.currentUser {
            state = .authenticated
            currentUserId = user.uid
        }
        // Observe changes (repo publishes)
        repo.$currentUser
            .receive(on: RunLoop.main)
            .sink { [weak self] user in
                if let user {
                    self?.state = .authenticated
                    self?.currentUserId = user.uid
                } else {
                    self?.state = .unauthenticated
                    self?.currentUserId = nil
                }
            }
            .store(in: &cancellables)
    }

    private var cancellables = Set<AnyCancellable>()

    // ── Email ─────────────────────────────────────────────────────────────

    public func signInWithEmail(email: String, password: String) {
        state = .loading
        Task {
            do {
                try await repo.signInWithEmail(email: email, password: password)
                // state updated via listener
            } catch {
                state = .error(error.localizedDescription)
            }
        }
    }

    public func signUpWithEmail(email: String, password: String) {
        state = .loading
        Task {
            do {
                try await repo.signUpWithEmail(email: email, password: password)
            } catch {
                state = .error(error.localizedDescription)
            }
        }
    }

    // ── Google ────────────────────────────────────────────────────────────

    public func signInWithGoogle() async {
        state = .loading
        do {
            try await repo.signInWithGoogle()
        } catch {
            state = .error(error.localizedDescription)
        }
    }

    // ── Apple ─────────────────────────────────────────────────────────────

    public func prepareAppleRequest(_ request: ASAuthorizationAppleIDRequest) {
        repo.prepareAppleRequest(request)
    }

    public func handleAppleAuth(_ result: Result<ASAuthorization, Error>) {
        state = .loading
        Task {
            do {
                try await repo.handleAppleAuth(result)
            } catch {
                state = .error(error.localizedDescription)
            }
        }
    }

    // ── Sign out ──────────────────────────────────────────────────────────

    public func signOut() {
        try? repo.signOut()
    }

    public func clearError() {
        if case .error = state { state = .unauthenticated }
    }
}

// MARK: - Combine import shim
import Combine
