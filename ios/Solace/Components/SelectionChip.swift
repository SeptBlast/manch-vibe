import SwiftUI

// ---------------------------------------------------------------------------
// SelectionChip — pill-shaped selectable tag
// ---------------------------------------------------------------------------

public struct SelectionChip: View {
    let label: String
    let isSelected: Bool
    let onToggle: (Bool) -> Void

    public var body: some View {
        Button { onToggle(!isSelected) } label: {
            Text(label)
                .font(DesignTokens.Font.labelMedium)
                .foregroundStyle(isSelected
                    ? DesignTokens.Color.chipSelectedText
                    : DesignTokens.Color.chipUnselectedText)
                .padding(.horizontal, DesignTokens.Spacing.md)
                .frame(height: DesignTokens.Size.chipHeight)
                .background(isSelected
                    ? DesignTokens.Color.chipSelectedBg
                    : DesignTokens.Color.chipUnselectedBg)
                .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }
}

// ---------------------------------------------------------------------------
// ChipGroup — wrapping flow layout for a flat option list
// ---------------------------------------------------------------------------

public struct ChipGroup: View {
    let options: [String]
    @Binding var selected: Set<String>
    var maxSelections: Int = 0   // 0 = unlimited

    public var body: some View {
        FlowLayout(spacing: DesignTokens.Spacing.sm) {
            ForEach(options, id: \.self) { option in
                SelectionChip(
                    label: option,
                    isSelected: selected.contains(option),
                    onToggle: { nowSelected in
                        if nowSelected && maxSelections > 0 && selected.count >= maxSelections { return }
                        if nowSelected { selected.insert(option) } else { selected.remove(option) }
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// FlowLayout — left-to-right wrapping layout (iOS 16+ LazyVGrid alternative)
// ---------------------------------------------------------------------------

public struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    public func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        layout(proposal: proposal, subviews: subviews).size
    }

    public func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = layout(proposal: ProposedViewSize(bounds.size), subviews: subviews)
        for (index, frame) in result.frames.enumerated() {
            subviews[index].place(at: CGPoint(x: bounds.minX + frame.minX, y: bounds.minY + frame.minY), proposal: .unspecified)
        }
    }

    private func layout(proposal: ProposedViewSize, subviews: Subviews) -> (size: CGSize, frames: [CGRect]) {
        let maxWidth = proposal.width ?? .infinity
        var frames: [CGRect] = []
        var x: CGFloat = 0
        var y: CGFloat = 0
        var rowHeight: CGFloat = 0
        var maxX: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > maxWidth && x > 0 {
                x = 0
                y += rowHeight + spacing
                rowHeight = 0
            }
            frames.append(CGRect(origin: CGPoint(x: x, y: y), size: size))
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
            maxX = max(maxX, x - spacing)
        }

        return (CGSize(width: maxX, height: y + rowHeight), frames)
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

#Preview {
    @Previewable @State var selected: Set<String> = ["Relationships"]
    ChipGroup(
        options: ["Relationships", "Work Stress", "Loss & grief", "Self worth", "Anxiety", "Peaceful & Grounded"],
        selected: $selected
    )
    .padding()
}
