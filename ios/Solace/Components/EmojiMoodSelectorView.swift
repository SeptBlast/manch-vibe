import SwiftUI

// ---------------------------------------------------------------------------
// EmojiMoodSelectorView — horizontal 5-emoji row for emotion/journal screen
// ---------------------------------------------------------------------------

public let defaultEmojis = ["😢", "😔", "😐", "🙂", "😊"]

public struct EmojiMoodSelectorView: View {
    @Binding var selected: String?
    var emojis: [String] = defaultEmojis  // overridable via config

    public var body: some View {
        HStack(spacing: 0) {
            ForEach(emojis, id: \.self) { emoji in
                let isSelected = emoji == selected
                Button {
                    selected = emoji
                } label: {
                    Text(emoji)
                        .font(.system(size: 28))
                        .frame(width: 52, height: 52)
                        .background(
                            isSelected
                            ? DesignTokens.Color.solaceTeal.opacity(0.15)
                            : Color.clear
                        )
                        .clipShape(Circle())
                        .overlay(
                            Circle()
                                .stroke(
                                    isSelected ? DesignTokens.Color.solaceTeal : Color.clear,
                                    lineWidth: 2
                                )
                        )
                }
                .buttonStyle(.plain)
                .frame(maxWidth: .infinity)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

#Preview {
    @Previewable @State var selected: String? = nil
    EmojiMoodSelectorView(selected: $selected)
        .padding()
}
