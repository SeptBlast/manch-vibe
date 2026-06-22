import SwiftUI
import FirebaseCore
import GoogleSignIn

@main
struct SolaceApp: App {

    @StateObject private var authVM = AuthViewModel()
    @StateObject private var homeVM = HomeViewModel()

    init() {
        // TODO: add GoogleService-Info.plist to the Xcode target before building.
        // File is at ios/Solace/GoogleService-Info.plist — drag it into Xcode and
        // tick "Add to target: Solace" in the file-add dialog.
        FirebaseApp.configure()
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(authVM)
                .environmentObject(homeVM)
                .onOpenURL { url in GIDSignIn.sharedInstance.handle(url) }
        }
    }
}

// ---------------------------------------------------------------------------
// RootView — drives the Login → Onboarding → Profile Card → Home gate
// ---------------------------------------------------------------------------

struct RootView: View {
    @EnvironmentObject var authVM: AuthViewModel
    @EnvironmentObject var homeVM: HomeViewModel

    @StateObject private var onboardingVM = OnboardingViewModel()
    @State private var needsOnboarding: Bool = false
    @State private var needsProfileCard: Bool = false
    @State private var checkingOnboarding: Bool = false
    @State private var pendingAnswers: OnboardingAnswers? = nil

    var body: some View {
        Group {
            switch authVM.state {

            case .unauthenticated, .error:
                LoginView(
                    config: homeVM.uiConfig.loginScreen,
                    onEmailLogin: authVM.signInWithEmail,
                    onGoogleLogin: { Task { await authVM.signInWithGoogle() } },
                    onAppleLogin:  authVM.handleAppleAuth
                )

            case .loading:
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(DesignTokens.Color.backgroundDark)

            case .authenticated:
                if checkingOnboarding {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .background(DesignTokens.Color.backgroundLight)

                } else if needsOnboarding, let uid = authVM.currentUserId {
                    OnboardingView(uid: uid) { answers in
                        pendingAnswers = answers
                        needsOnboarding = false
                        needsProfileCard = true
                    }

                } else if needsProfileCard, let uid = authVM.currentUserId {
                    ProfileCardCreationView(
                        uid: uid,
                        answers: pendingAnswers ?? OnboardingAnswers()
                    ) {
                        pendingAnswers = nil
                        needsProfileCard = false
                        homeVM.startObservingProfiles()
                    }

                } else {
                    HomeView(
                        config: homeVM.uiConfig.homeScreen,
                        profiles: homeVM.profiles,
                        currentUid: authVM.currentUserId ?? ""
                    )
                    .onAppear { homeVM.startObservingProfiles() }
                }
            }
        }
        .solaceTheme(homeVM.uiConfig.themeVariant)
        .task { await homeVM.loadConfig() }
        .onChange(of: authVM.state) { _, newState in
            if case .authenticated = newState, let uid = authVM.currentUserId {
                Task { await checkOnboardingStatus(uid: uid) }
            }
        }
    }

    private func checkOnboardingStatus(uid: String) async {
        checkingOnboarding = true
        let complete = await OnboardingRepository.shared.isOnboardingComplete(uid: uid)
        needsOnboarding = !complete
        checkingOnboarding = false
    }
}
