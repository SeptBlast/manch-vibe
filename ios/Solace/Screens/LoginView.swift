import SwiftUI
import AuthenticationServices

// ---------------------------------------------------------------------------
// LoginView — driven entirely by LoginScreenConfig from remote config
// No phone/OTP/SMS fields — Email, Google, Apple only
// TODO: connect to AuthViewModel / Coordinator once auth layer is wired
// ---------------------------------------------------------------------------

public struct LoginView: View {
    let config: LoginScreenConfig
    var onEmailLogin: (String, String) -> Void = { _, _ in }
    var onGoogleLogin: () -> Void = {}
    var onAppleLogin: (ASAuthorization) -> Void = { _ in }
    var onNavigateToSignUp: () -> Void = {}

    @State private var email: String = ""
    @State private var password: String = ""
    @State private var passwordVisible: Bool = false
    @FocusState private var focusedField: LoginField?

    private enum LoginField { case email, password }

    private var showEmail:  Bool { config.providers.contains(.email) }
    private var showGoogle: Bool { config.providers.contains(.google) }
    private var showApple:  Bool { config.providers.contains(.apple) }
    private var canSubmit:  Bool { !email.isEmpty && !password.isEmpty }

    public var body: some View {
        if !config.visible { return AnyView(EmptyView()) }

        return AnyView(
            ZStack {
                DesignTokens.Color.backgroundDark.ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 0) {
                        Spacer().frame(height: DesignTokens.Spacing.xxl)

                        // Brand
                        Text("Solace")
                            .font(DesignTokens.Font.displayLarge)
                            .foregroundStyle(DesignTokens.Color.solaceTeal)

                        Spacer().frame(height: DesignTokens.Spacing.sm)

                        Text(config.tagline)
                            .font(DesignTokens.Font.bodyLarge)
                            .foregroundStyle(DesignTokens.Color.textOnDark.opacity(0.7))
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, DesignTokens.Spacing.xl)

                        Spacer().frame(height: DesignTokens.Spacing.xxl)

                        VStack(spacing: DesignTokens.Spacing.sm) {

                            // ── Email + password ──────────────────────────
                            if showEmail {
                                emailField
                                passwordField

                                Spacer().frame(height: DesignTokens.Spacing.xs)

                                PrimaryButton(
                                    label: config.emailCtaLabel,
                                    action: { onEmailLogin(email, password) },
                                    isEnabled: canSubmit
                                )

                                Button(action: onNavigateToSignUp) {
                                    Text("Don't have an account? Sign up")
                                        .font(DesignTokens.Font.bodyMedium)
                                        .foregroundStyle(DesignTokens.Color.solaceTeal)
                                }
                                .padding(.top, DesignTokens.Spacing.xs)
                            }

                            // ── Divider ───────────────────────────────────
                            if showEmail && (showGoogle || showApple) {
                                orDivider
                            }

                            // ── OAuth ─────────────────────────────────────
                            if showGoogle {
                                OutlinedAuthButton(
                                    label: config.googleCtaLabel,
                                    action: onGoogleLogin,
                                    leadingSystemImage: "globe"
                                )
                            }

                            if showApple {
                                SignInWithAppleButton(
                                    .signIn,
                                    onRequest: { request in
                                        request.requestedScopes = [.fullName, .email]
                                    },
                                    onCompletion: { result in
                                        if case .success(let auth) = result { onAppleLogin(auth) }
                                    }
                                )
                                .signInWithAppleButtonStyle(.white)
                                .frame(height: DesignTokens.Size.buttonHeight)
                                .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.button))
                            }
                        }
                        .padding(.horizontal, DesignTokens.Spacing.lg)

                        Spacer().frame(height: DesignTokens.Spacing.xxl)
                    }
                }
            }
        )
    }

    // MARK: - Sub-views

    private var emailField: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("Email")
                .font(DesignTokens.Font.labelSmall)
                .foregroundStyle(DesignTokens.Color.textOnDark.opacity(0.6))
            TextField("", text: $email)
                .keyboardType(.emailAddress)
                .autocapitalization(.none)
                .textContentType(.emailAddress)
                .focused($focusedField, equals: .email)
                .submitLabel(.next)
                .onSubmit { focusedField = .password }
                .foregroundStyle(DesignTokens.Color.textOnDark)
                .padding(DesignTokens.Spacing.md)
                .background(Color.white.opacity(0.08))
                .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.button))
                .overlay(
                    RoundedRectangle(cornerRadius: DesignTokens.Radius.button)
                        .stroke(
                            focusedField == .email
                                ? DesignTokens.Color.solaceTeal
                                : DesignTokens.Color.divider,
                            lineWidth: 1
                        )
                )
        }
    }

    private var passwordField: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("Password")
                .font(DesignTokens.Font.labelSmall)
                .foregroundStyle(DesignTokens.Color.textOnDark.opacity(0.6))
            HStack {
                Group {
                    if passwordVisible {
                        TextField("", text: $password)
                    } else {
                        SecureField("", text: $password)
                    }
                }
                .textContentType(.password)
                .focused($focusedField, equals: .password)
                .submitLabel(.done)
                .foregroundStyle(DesignTokens.Color.textOnDark)

                Button {
                    passwordVisible.toggle()
                } label: {
                    Text(passwordVisible ? "Hide" : "Show")
                        .font(DesignTokens.Font.labelSmall)
                        .foregroundStyle(DesignTokens.Color.solaceTeal)
                }
            }
            .padding(DesignTokens.Spacing.md)
            .background(Color.white.opacity(0.08))
            .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.button))
            .overlay(
                RoundedRectangle(cornerRadius: DesignTokens.Radius.button)
                    .stroke(
                        focusedField == .password
                            ? DesignTokens.Color.solaceTeal
                            : DesignTokens.Color.divider,
                        lineWidth: 1
                    )
            )
        }
    }

    private var orDivider: some View {
        HStack {
            Rectangle().fill(DesignTokens.Color.divider).frame(height: 1)
            Text("or")
                .font(DesignTokens.Font.bodySmall)
                .foregroundStyle(DesignTokens.Color.textOnDark.opacity(0.5))
                .padding(.horizontal, DesignTokens.Spacing.sm)
            Rectangle().fill(DesignTokens.Color.divider).frame(height: 1)
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

#Preview("All providers") {
    LoginView(config: LoginScreenConfig())
}

#Preview("Email only") {
    LoginView(config: LoginScreenConfig(providers: [.email]))
}

#Preview("OAuth only") {
    LoginView(config: LoginScreenConfig(providers: [.google, .apple]))
}
