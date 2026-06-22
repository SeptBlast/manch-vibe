import SwiftUI

// ---------------------------------------------------------------------------
// PrimaryButton — full-width teal CTA used throughout Solace
// ---------------------------------------------------------------------------

public struct PrimaryButton: View {
    let label: String
    let action: () -> Void
    var isEnabled: Bool = true
    var containerColor: Color = DesignTokens.Color.solaceTeal
    var contentColor: Color = DesignTokens.Color.textOnPrimary
    var leadingSystemImage: String? = nil

    public var body: some View {
        Button(action: action) {
            HStack(spacing: DesignTokens.Spacing.sm) {
                if let icon = leadingSystemImage {
                    Image(systemName: icon)
                        .font(.system(size: 18, weight: .medium))
                }
                Text(label)
                    .font(DesignTokens.Font.labelLarge)
            }
            .foregroundStyle(contentColor)
            .frame(maxWidth: .infinity)
            .frame(height: DesignTokens.Size.buttonHeight)
            .background(isEnabled ? containerColor : containerColor.opacity(0.4))
            .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.button))
        }
        .disabled(!isEnabled)
    }
}

// ---------------------------------------------------------------------------
// OutlinedAuthButton — bordered button for OAuth providers
// ---------------------------------------------------------------------------

public struct OutlinedAuthButton: View {
    let label: String
    let action: () -> Void
    var leadingSystemImage: String? = nil

    public var body: some View {
        Button(action: action) {
            HStack(spacing: DesignTokens.Spacing.sm) {
                if let icon = leadingSystemImage {
                    Image(systemName: icon)
                        .font(.system(size: 18, weight: .medium))
                }
                Text(label)
                    .font(DesignTokens.Font.labelLarge)
            }
            .foregroundStyle(DesignTokens.Color.textPrimary)
            .frame(maxWidth: .infinity)
            .frame(height: DesignTokens.Size.buttonHeight)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.button))
            .overlay(
                RoundedRectangle(cornerRadius: DesignTokens.Radius.button)
                    .stroke(DesignTokens.Color.inputBorder, lineWidth: 1)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

#Preview {
    VStack(spacing: 12) {
        PrimaryButton(label: "Continue with Email", action: {})
        OutlinedAuthButton(label: "Continue with Google", leadingSystemImage: "globe", action: {})
        OutlinedAuthButton(label: "Continue with Apple", leadingSystemImage: "applelogo", action: {})
        PrimaryButton(label: "Disabled", action: {}, isEnabled: false)
    }
    .padding()
}
