import SwiftUI

// ---------------------------------------------------------------------------
// EmotionView — mood check-in + history
// ---------------------------------------------------------------------------

struct EmotionView: View {
    let currentUid: String

    @StateObject private var vm = EmotionViewModel()

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 0) {
                    CheckInCard(vm: vm)
                    historySection
                }
            }
            .background(DesignTokens.Color.backgroundLight)
            .navigationTitle("Emotion")
            .navigationBarTitleDisplayMode(.large)
        }
        .onAppear { vm.initialize(uid: currentUid) }
        .alert("Error", isPresented: .constant(vm.error != nil)) {
            Button("OK") { vm.error = nil }
        } message: {
            Text(vm.error ?? "")
        }
    }

    @ViewBuilder
    private var historySection: some View {
        if !vm.logs.isEmpty {
            VStack(alignment: .leading, spacing: 0) {
                Text("Recent moods")
                    .font(DesignTokens.Font.headlineSmall)
                    .padding(.horizontal, DesignTokens.Spacing.lg)
                    .padding(.top, DesignTokens.Spacing.lg)
                    .padding(.bottom, DesignTokens.Spacing.sm)

                ForEach(vm.logs) { log in
                    MoodLogRow(log: log)
                    Divider()
                        .padding(.leading, DesignTokens.Spacing.lg + 44 + DesignTokens.Spacing.md)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Check-in card (dark navy background)
// ---------------------------------------------------------------------------

private struct CheckInCard: View {
    @ObservedObject var vm: EmotionViewModel

    private var selectedColor: Color {
        defaultMoodPalette.first { $0.key == vm.selectedMoodKey }?.color
            ?? DesignTokens.Color.solaceTeal
    }

    var body: some View {
        VStack(spacing: DesignTokens.Spacing.lg) {
            // Greeting + prompt
            VStack(spacing: DesignTokens.Spacing.xs) {
                Text(greeting())
                    .font(DesignTokens.Font.headlineMedium)
                    .foregroundStyle(DesignTokens.Color.textOnDark)
                Text("How are you feeling right now?")
                    .font(DesignTokens.Font.bodyMedium)
                    .foregroundStyle(DesignTokens.Color.textOnDark.opacity(0.7))
            }
            .multilineTextAlignment(.center)

            // Emoji row
            EmojiMoodSelectorView(selected: Binding(
                get: { vm.selectedEmoji },
                set: { vm.selectedEmoji = $0 ?? "😐" }
            ))

            // Mood palette grid
            MoodPaletteGrid(selected: vm.selectedMoodKey) { vm.selectedMoodKey = $0 }

            // Selected mood label
            HStack(spacing: 6) {
                Circle()
                    .fill(selectedColor)
                    .frame(width: 10, height: 10)
                Text(defaultMoodPalette.first { $0.key == vm.selectedMoodKey }?.label ?? "")
                    .font(DesignTokens.Font.labelLarge)
                    .foregroundStyle(selectedColor)
            }
            .animation(.easeInOut(duration: 0.2), value: vm.selectedMoodKey)

            // Note input
            NoteField(note: $vm.note, accentColor: selectedColor)

            // Log button / confirmation
            if vm.savedToday {
                ConfirmationBadge(emoji: vm.selectedEmoji, color: selectedColor) {
                    vm.dismissConfirmation()
                }
            } else {
                Button(action: vm.logMood) {
                    Group {
                        if vm.isSaving {
                            ProgressView().tint(.white).scaleEffect(0.9)
                        } else {
                            Text("Log my mood")
                                .font(DesignTokens.Font.labelLarge)
                                .foregroundStyle(.white)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: DesignTokens.Size.buttonHeight)
                }
                .background(selectedColor)
                .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.button))
                .disabled(vm.isSaving)
                .animation(.easeInOut(duration: 0.2), value: vm.selectedMoodKey)
            }

            Spacer().frame(height: DesignTokens.Spacing.sm)
        }
        .padding(DesignTokens.Spacing.lg)
        .background(DesignTokens.Color.backgroundDark)
        .clipShape(RoundedRectangle(cornerRadius: 0).inset(by: 0))
    }
}

// ---------------------------------------------------------------------------
// Mood palette grid — 2 rows × 4 circles
// ---------------------------------------------------------------------------

private struct MoodPaletteGrid: View {
    let selected: MoodColorKey
    let onSelect: (MoodColorKey) -> Void

    private let columns = Array(repeating: GridItem(.flexible()), count: 4)

    var body: some View {
        LazyVGrid(columns: columns, spacing: DesignTokens.Spacing.md) {
            ForEach(defaultMoodPalette) { option in
                MoodPaletteItem(option: option, isSelected: option.key == selected) {
                    onSelect(option.key)
                }
            }
        }
    }
}

private struct MoodPaletteItem: View {
    let option: MoodColorOption
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 4) {
                ZStack {
                    Circle()
                        .fill(option.color.opacity(isSelected ? 1.0 : 0.35))
                        .frame(width: 44, height: 44)
                    if isSelected {
                        Circle()
                            .stroke(Color.white, lineWidth: 3)
                            .frame(width: 44, height: 44)
                    }
                }
                Text(shortLabel(option.label))
                    .font(DesignTokens.Font.labelSmall)
                    .foregroundStyle(isSelected
                        ? option.color
                        : DesignTokens.Color.textOnDark.opacity(0.7))
                    .multilineTextAlignment(.center)
                    .lineLimit(2)
            }
        }
        .buttonStyle(.plain)
    }

    private func shortLabel(_ label: String) -> String {
        let parts = label.components(separatedBy: " & ")
        return parts.first ?? label
    }
}

// ---------------------------------------------------------------------------
// Note field
// ---------------------------------------------------------------------------

private struct NoteField: View {
    @Binding var note: String
    let accentColor: Color
    @FocusState private var focused: Bool

    var body: some View {
        ZStack(alignment: .topLeading) {
            if note.isEmpty {
                Text("Add a short note… (optional)")
                    .font(DesignTokens.Font.bodyMedium)
                    .foregroundStyle(DesignTokens.Color.textOnDark.opacity(0.4))
                    .padding(.top, 12)
                    .padding(.leading, 4)
                    .allowsHitTesting(false)
            }
            TextEditor(text: $note)
                .font(DesignTokens.Font.bodyMedium)
                .foregroundStyle(DesignTokens.Color.textOnDark)
                .focused($focused)
                .frame(minHeight: 72)
                .scrollContentBackground(.hidden)
        }
        .padding(DesignTokens.Spacing.md)
        .background(DesignTokens.Color.textOnDark.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.button))
        .overlay(
            RoundedRectangle(cornerRadius: DesignTokens.Radius.button)
                .stroke(focused ? accentColor : DesignTokens.Color.textOnDark.opacity(0.3), lineWidth: 1)
        )
        .onTapGesture { focused = true }
    }
}

// ---------------------------------------------------------------------------
// Confirmation badge
// ---------------------------------------------------------------------------

private struct ConfirmationBadge: View {
    let emoji: String
    let color: Color
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: DesignTokens.Spacing.sm) {
                Text(emoji).font(.system(size: 22))
                Text("Mood logged! Tap to log again")
                    .font(DesignTokens.Font.labelMedium)
                    .foregroundStyle(color)
            }
            .frame(maxWidth: .infinity)
            .padding(DesignTokens.Spacing.md)
            .background(color.opacity(0.2))
            .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.button))
            .overlay(
                RoundedRectangle(cornerRadius: DesignTokens.Radius.button)
                    .stroke(color, lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
    }
}

// ---------------------------------------------------------------------------
// History row
// ---------------------------------------------------------------------------

private struct MoodLogRow: View {
    let log: MoodLog

    private var option: MoodColorOption? {
        defaultMoodPalette.first { $0.key.rawValue == log.moodColorKey }
    }

    var body: some View {
        HStack(spacing: DesignTokens.Spacing.md) {
            ZStack {
                Circle()
                    .fill((option?.color ?? DesignTokens.Color.solaceTeal).opacity(0.12))
                    .frame(width: 44, height: 44)
                Text(log.emoji.isEmpty ? "😐" : log.emoji)
                    .font(.system(size: 22))
            }

            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 6) {
                    Circle()
                        .fill(option?.color ?? DesignTokens.Color.solaceTeal)
                        .frame(width: 8, height: 8)
                    Text(option?.label ?? log.moodColorKey)
                        .font(DesignTokens.Font.labelMedium)
                        .foregroundStyle(option?.color ?? DesignTokens.Color.solaceTeal)
                }
                if !log.note.isEmpty {
                    Text(log.note)
                        .font(DesignTokens.Font.bodySmall)
                        .foregroundStyle(DesignTokens.Color.textSecondary)
                        .lineLimit(1)
                }
            }

            Spacer()

            Text(log.createdAt.logTimestamp())
                .font(DesignTokens.Font.labelSmall)
                .foregroundStyle(DesignTokens.Color.textSecondary)
        }
        .padding(.horizontal, DesignTokens.Spacing.lg)
        .padding(.vertical, DesignTokens.Spacing.sm)
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private func greeting() -> String {
    let hour = Calendar.current.component(.hour, from: Date())
    switch hour {
    case 5..<12:  return "Good morning ☀️"
    case 12..<18: return "Good afternoon 🌤"
    case 18..<22: return "Good evening 🌙"
    default:      return "Hey there 🌟"
    }
}

private extension Date {
    func logTimestamp() -> String {
        if Calendar.current.isDateInToday(self) {
            let f = DateFormatter(); f.dateFormat = "h:mm a"
            return f.string(from: self)
        }
        let f = DateFormatter(); f.dateFormat = "MMM d"
        return f.string(from: self)
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

#Preview {
    EmotionView(currentUid: "preview_uid")
        .solaceTheme()
}
