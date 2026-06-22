import SwiftUI

// ---------------------------------------------------------------------------
// AppTheme — environment-injected theme variant
// TODO: backend drives variant via UiConfig.themeVariant
// ---------------------------------------------------------------------------

public enum SolaceThemeVariant: String, Codable {
    case `default` = "DEFAULT"
    case dark      = "DARK"
}

public struct AppTheme {
    public let variant: SolaceThemeVariant

    // Resolved tokens (allow overrides for future white-labelling)
    public var primary:            Color { DesignTokens.Color.solaceTeal }
    public var background:         Color { variant == .dark ? DesignTokens.Color.backgroundDark : DesignTokens.Color.backgroundLight }
    public var surface:            Color { variant == .dark ? DesignTokens.Color.backgroundDark : DesignTokens.Color.surfaceLight }
    public var textPrimary:        Color { variant == .dark ? DesignTokens.Color.textOnDark : DesignTokens.Color.textPrimary }
    public var textSecondary:      Color { DesignTokens.Color.textSecondary }
    public var navActive:          Color { DesignTokens.Color.navActive }
    public var navInactive:        Color { DesignTokens.Color.navInactive }
    public var chipSelectedBg:     Color { DesignTokens.Color.chipSelectedBg }
    public var chipUnselectedBg:   Color { DesignTokens.Color.chipUnselectedBg }

    public static let `default` = AppTheme(variant: .default)
    public static let dark      = AppTheme(variant: .dark)
}

// MARK: - Environment key

private struct AppThemeKey: EnvironmentKey {
    static let defaultValue = AppTheme.default
}

extension EnvironmentValues {
    public var appTheme: AppTheme {
        get { self[AppThemeKey.self] }
        set { self[AppThemeKey.self] = newValue }
    }
}

// MARK: - View modifier

public struct SolaceThemeModifier: ViewModifier {
    let variant: SolaceThemeVariant

    public func body(content: Content) -> some View {
        content
            .environment(\.appTheme, variant == .dark ? .dark : .default)
            .preferredColorScheme(variant == .dark ? .dark : .light)
    }
}

extension View {
    public func solaceTheme(_ variant: SolaceThemeVariant = .default) -> some View {
        modifier(SolaceThemeModifier(variant: variant))
    }
}
