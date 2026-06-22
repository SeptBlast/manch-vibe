import SwiftUI

// ---------------------------------------------------------------------------
// SectionHeaderView — onboarding back-arrow + step counter
// ---------------------------------------------------------------------------

public struct SectionHeaderView: View {
    var onBack: (() -> Void)?
    var currentStep: Int = 0
    var totalSteps: Int = 0

    public var body: some View {
        HStack {
            if let back = onBack {
                Button(action: back) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundStyle(DesignTokens.Color.textPrimary)
                        .frame(width: 44, height: 44)
                }
            } else {
                Spacer().frame(width: 44, height: 44)
            }

            Spacer()

            if currentStep > 0 && totalSteps > 0 {
                Text("Step \(currentStep) of \(totalSteps)")
                    .font(DesignTokens.Font.bodySmall)
                    .foregroundStyle(DesignTokens.Color.textSecondary)
            }
        }
        .padding(.horizontal, DesignTokens.Spacing.sm)
    }
}

// ---------------------------------------------------------------------------
// StepProgressBar
// ---------------------------------------------------------------------------

public struct StepProgressBar: View {
    let current: Int
    let total: Int

    public var body: some View {
        GeometryReader { geo in
            ZStack(alignment: .leading) {
                RoundedRectangle(cornerRadius: 2)
                    .fill(DesignTokens.Color.chipUnselectedBg)
                    .frame(height: 4)

                RoundedRectangle(cornerRadius: 2)
                    .fill(DesignTokens.Color.solaceTeal)
                    .frame(width: geo.size.width * CGFloat(current) / CGFloat(total), height: 4)
                    .animation(.easeInOut(duration: 0.3), value: current)
            }
        }
        .frame(height: 4)
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

#Preview {
    VStack(spacing: 0) {
        SectionHeaderView(onBack: {}, currentStep: 3, totalSteps: 10)
        StepProgressBar(current: 3, total: 10)
            .padding(.horizontal, DesignTokens.Spacing.md)
    }
}
