import Foundation
import FirebaseAuth
import GoogleSignIn
import AuthenticationServices
import CryptoKit

// ---------------------------------------------------------------------------
// AuthRepository — wraps Firebase Auth for Email / Google / Apple
// NO phone, OTP, or SMS providers
// ---------------------------------------------------------------------------

public enum AuthError: LocalizedError {
    case unknown
    case missingCredential
    case wrap(Error)

    public var errorDescription: String? {
        switch self {
        case .unknown:            return "An unknown error occurred."
        case .missingCredential:  return "Could not retrieve sign-in credential."
        case .wrap(let e):        return e.localizedDescription
        }
    }
}

@MainActor
public final class AuthRepository: ObservableObject {

    public static let shared = AuthRepository()
    private let auth = Auth.auth()

    // Current user — updated by the Firebase listener
    @Published public private(set) var currentUser: User? = Auth.auth().currentUser

    private var stateHandle: AuthStateDidChangeListenerHandle?

    public init() {
        stateHandle = auth.addStateDidChangeListener { [weak self] _, user in
            self?.currentUser = user
        }
    }

    deinit { if let h = stateHandle { auth.removeStateDidChangeListener(h) } }

    // ── Email / password ──────────────────────────────────────────────────

    public func signInWithEmail(email: String, password: String) async throws {
        do {
            try await auth.signIn(withEmail: email, password: password)
        } catch { throw AuthError.wrap(error) }
    }

    public func signUpWithEmail(email: String, password: String) async throws {
        do {
            try await auth.createUser(withEmail: email, password: password)
        } catch { throw AuthError.wrap(error) }
    }

    public func sendPasswordReset(email: String) async throws {
        try await auth.sendPasswordReset(withEmail: email)
    }

    // ── Google ────────────────────────────────────────────────────────────

    // TODO: replace with your CLIENT_ID from GoogleService-Info.plist
    private let googleClientId = "414230295497-vp0r007feu5feeag8b29g1n9k8if4haa.apps.googleusercontent.com"

    public func signInWithGoogle() async throws {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootVC = windowScene.windows.first?.rootViewController
        else { throw AuthError.unknown }

        let config = GIDConfiguration(clientID: googleClientId)
        GIDSignIn.sharedInstance.configuration = config

        let result = try await GIDSignIn.sharedInstance.signIn(withPresenting: rootVC)
        guard let idToken = result.user.idToken?.tokenString else { throw AuthError.missingCredential }
        let credential = GoogleAuthProvider.credential(
            withIDToken: idToken,
            accessToken: result.user.accessToken.tokenString
        )
        do {
            try await auth.signIn(with: credential)
        } catch { throw AuthError.wrap(error) }
    }

    // ── Apple ─────────────────────────────────────────────────────────────

    private var currentNonce: String?

    /// Call this from the onRequest handler of SignInWithAppleButton.
    public func prepareAppleRequest(_ request: ASAuthorizationAppleIDRequest) {
        let nonce = randomNonceString()
        currentNonce = nonce
        request.requestedScopes = [.fullName, .email]
        request.nonce = sha256(nonce)
    }

    /// Call this from the onCompletion handler of SignInWithAppleButton.
    public func handleAppleAuth(_ result: Result<ASAuthorization, Error>) async throws {
        switch result {
        case .failure(let e): throw AuthError.wrap(e)
        case .success(let auth):
            guard
                let appleCredential = auth.credential as? ASAuthorizationAppleIDCredential,
                let tokenData = appleCredential.identityToken,
                let tokenString = String(data: tokenData, encoding: .utf8),
                let nonce = currentNonce
            else { throw AuthError.missingCredential }

            let firebaseCredential = OAuthProvider.appleCredential(
                withIDToken: tokenString,
                rawNonce: nonce,
                fullName: appleCredential.fullName
            )
            do {
                try await self.auth.signIn(with: firebaseCredential)
            } catch { throw AuthError.wrap(error) }
        }
    }

    // ── Sign out ──────────────────────────────────────────────────────────

    public func signOut() throws {
        try auth.signOut()
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private func randomNonceString(length: Int = 32) -> String {
        var randomBytes = [UInt8](repeating: 0, count: length)
        _ = SecRandomCopyBytes(kSecRandomDefault, length, &randomBytes)
        return randomBytes.map { String(format: "%02x", $0) }.joined()
    }

    private func sha256(_ input: String) -> String {
        let data = Data(input.utf8)
        let hash = SHA256.hash(data: data)
        return hash.compactMap { String(format: "%02x", $0) }.joined()
    }
}
