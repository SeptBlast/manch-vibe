import SwiftUI

// ---------------------------------------------------------------------------
// Design tokens — single source of truth for Solace iOS
// All values mirror Color.kt / Theme.kt on Android.
// ---------------------------------------------------------------------------

public enum DesignTokens {

    // MARK: - Colors

    public enum Color {
        // Brand
        public static let solaceTeal       = SwiftUI.Color(hex: "#4ECDC4")
        public static let solaceTealDark   = SwiftUI.Color(hex: "#38B2AA")

        // Backgrounds
        public static let backgroundDark   = SwiftUI.Color(hex: "#120B3C")
        public static let backgroundLight  = SwiftUI.Color.white
        public static let surfaceLight     = SwiftUI.Color(hex: "#F8F8F8")

        // Text
        public static let textPrimary      = SwiftUI.Color(hex: "#0A0A0A")
        public static let textSecondary    = SwiftUI.Color(hex: "#6B6B6B")
        public static let textOnDark       = SwiftUI.Color.white
        public static let textOnPrimary    = SwiftUI.Color.white

        // Chips
        public static let chipSelectedBg   = solaceTeal
        public static let chipUnselectedBg = SwiftUI.Color(hex: "#F0F0F0")
        public static let chipSelectedText = SwiftUI.Color.white
        public static let chipUnselectedText = textPrimary

        // Dividers
        public static let divider          = SwiftUI.Color(hex: "#E0E0E0")
        public static let inputBorder      = SwiftUI.Color(hex: "#D0D0D0")

        // Nav
        public static let navActive        = solaceTeal
        public static let navInactive      = SwiftUI.Color(hex: "#888888")

        // Status
        public static let error            = SwiftUI.Color(hex: "#FF4444")

        // Milestone backgrounds
        public static let milestoneCoral   = SwiftUI.Color(hex: "#E8705A")
        public static let milestoneTeal    = SwiftUI.Color(hex: "#5BC8B5")
        public static let milestoneNavy    = backgroundDark

        // Mood palette
        public static let moodCalmRelaxed          = solaceTeal
        public static let moodEnergeticPassionate  = SwiftUI.Color(hex: "#FF6B35")
        public static let moodHappyOptimistic      = SwiftUI.Color(hex: "#FFD93D")
        public static let moodNorthReflective      = SwiftUI.Color(hex: "#4A5568")
        public static let moodPeacefulGrounded     = SwiftUI.Color(hex: "#6BCB77")
        public static let moodConfusedOverwhelmed  = SwiftUI.Color(hex: "#9B72CF")
        public static let moodNeutralNumb          = SwiftUI.Color(hex: "#9E9E9E")
        public static let moodWarmHopeful          = SwiftUI.Color(hex: "#FF8FAB")
    }

    // MARK: - Spacing

    public enum Spacing {
        public static let xs:  CGFloat = 4
        public static let sm:  CGFloat = 8
        public static let md:  CGFloat = 16
        public static let lg:  CGFloat = 24
        public static let xl:  CGFloat = 32
        public static let xxl: CGFloat = 48
    }

    // MARK: - Radius

    public enum Radius {
        public static let button: CGFloat = 12
        public static let chip:   CGFloat = 20
        public static let card:   CGFloat = 16
        public static let small:  CGFloat = 8
    }

    // MARK: - Sizes

    public enum Size {
        public static let buttonHeight:       CGFloat = 52
        public static let chipHeight:         CGFloat = 36
        public static let bottomNavHeight:    CGFloat = 64
        public static let profileImageHeight: CGFloat = 200
    }

    // MARK: - Typography

    public enum Font {
        public static let displayLarge   = SwiftUI.Font.system(size: 32, weight: .bold)
        public static let displayMedium  = SwiftUI.Font.system(size: 28, weight: .bold)
        public static let headlineLarge  = SwiftUI.Font.system(size: 24, weight: .bold)
        public static let headlineMedium = SwiftUI.Font.system(size: 20, weight: .semibold)
        public static let headlineSmall  = SwiftUI.Font.system(size: 18, weight: .semibold)
        public static let bodyLarge      = SwiftUI.Font.system(size: 16, weight: .regular)
        public static let bodyMedium     = SwiftUI.Font.system(size: 14, weight: .regular)
        public static let bodySmall      = SwiftUI.Font.system(size: 12, weight: .regular)
        public static let labelLarge     = SwiftUI.Font.system(size: 16, weight: .medium)
        public static let labelMedium    = SwiftUI.Font.system(size: 14, weight: .medium)
        public static let labelSmall     = SwiftUI.Font.system(size: 12, weight: .medium)
    }
}

// MARK: - Color hex initialiser

extension SwiftUI.Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r = Double((int >> 16) & 0xFF) / 255
        let g = Double((int >>  8) & 0xFF) / 255
        let b = Double( int        & 0xFF) / 255
        self.init(red: r, green: g, blue: b)
    }
}
