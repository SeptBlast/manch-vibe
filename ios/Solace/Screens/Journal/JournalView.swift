import SwiftUI

// ---------------------------------------------------------------------------
// JournalView — entry list + compose sheet
// ---------------------------------------------------------------------------

struct JournalView: View {
    let currentUid: String

    @StateObject private var vm = JournalViewModel()
    @State private var showCompose = false

    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottomTrailing) {
                content

                // FAB
                Button {
                    vm.openCompose()
                    showCompose = true
                } label: {
                    Image(systemName: "plus")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundStyle(.white)
                        .frame(width: 56, height: 56)
                        .background(DesignTokens.Color.solaceTeal)
                        .clipShape(Circle())
                        .shadow(color: DesignTokens.Color.solaceTeal.opacity(0.4), radius: 8, y: 4)
                }
                .padding(DesignTokens.Spacing.lg)
            }
            .navigationTitle("Journal")
            .navigationBarTitleDisplayMode(.large)
        }
        .onAppear { vm.initialize(uid: currentUid) }
        .sheet(isPresented: $showCompose) {
            ComposeEntrySheet(vm: vm, isPresented: $showCompose)
        }
        .alert("Error", isPresented: .constant(vm.error != nil)) {
            Button("OK") { vm.error = nil }
        } message: {
            Text(vm.error ?? "")
        }
    }

    @ViewBuilder
    private var content: some View {
        if vm.isLoading {
            ProgressView().tint(DesignTokens.Color.solaceTeal)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if vm.entries.isEmpty {
            emptyState
        } else {
            entryList
        }
    }

    private var emptyState: some View {
        VStack(spacing: DesignTokens.Spacing.sm) {
            Spacer()
            Text("✍️").font(.system(size: 52))
            Text("No entries yet")
                .font(DesignTokens.Font.headlineSmall)
                .foregroundStyle(DesignTokens.Color.textSecondary)
            Text("Tap + to write your first entry")
                .font(DesignTokens.Font.bodySmall)
                .foregroundStyle(DesignTokens.Color.textSecondary)
            Spacer()
        }
        .frame(maxWidth: .infinity)
    }

    private var entryList: some View {
        List {
            // Group by date header
            ForEach(groupedEntries, id: \.0) { (header, dayEntries) in
                Section(header:
                    Text(header)
                        .font(DesignTokens.Font.labelSmall)
                        .foregroundStyle(DesignTokens.Color.textSecondary)
                        .textCase(nil)
                ) {
                    ForEach(dayEntries) { entry in
                        JournalEntryRow(entry: entry)
                            .listRowInsets(EdgeInsets(
                                top: DesignTokens.Spacing.xs,
                                leading: DesignTokens.Spacing.md,
                                bottom: DesignTokens.Spacing.xs,
                                trailing: DesignTokens.Spacing.md
                            ))
                            .listRowSeparator(.hidden)
                            .listRowBackground(Color.clear)
                    }
                    .onDelete { offsets in
                        offsets.forEach { vm.deleteEntry(dayEntries[$0]) }
                    }
                }
            }
            // FAB clearance
            Color.clear.frame(height: 80).listRowBackground(Color.clear).listRowSeparator(.hidden)
        }
        .listStyle(.plain)
        .background(DesignTokens.Color.backgroundLight)
    }

    private var groupedEntries: [(String, [JournalEntry])] {
        var result: [(String, [JournalEntry])] = []
        var seen: [String: Int] = [:]
        for entry in vm.entries {
            let header = dateHeader(entry.createdAt)
            if let idx = seen[header] {
                result[idx].1.append(entry)
            } else {
                seen[header] = result.count
                result.append((header, [entry]))
            }
        }
        return result
    }
}

// ---------------------------------------------------------------------------
// Entry row card
// ---------------------------------------------------------------------------

private struct JournalEntryRow: View {
    let entry: JournalEntry

    var body: some View {
        HStack(alignment: .top, spacing: DesignTokens.Spacing.md) {
            // Emoji badge
            ZStack {
                RoundedRectangle(cornerRadius: 12)
                    .fill(DesignTokens.Color.chipUnselectedBg)
                    .frame(width: 48, height: 48)
                Text(entry.emoji.isEmpty ? "📝" : entry.emoji)
                    .font(.system(size: 24))
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(timeLabel(entry.createdAt))
                    .font(DesignTokens.Font.labelSmall)
                    .foregroundStyle(DesignTokens.Color.textSecondary)
                Text(entry.text)
                    .font(DesignTokens.Font.bodyMedium)
                    .lineLimit(4)
            }
        }
        .padding(DesignTokens.Spacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.card))
        .shadow(color: .black.opacity(0.05), radius: 4, y: 2)
    }
}

// ---------------------------------------------------------------------------
// Compose sheet
// ---------------------------------------------------------------------------

private struct ComposeEntrySheet: View {
    @ObservedObject var vm: JournalViewModel
    @Binding var isPresented: Bool
    @FocusState private var textFocused: Bool

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: DesignTokens.Spacing.lg) {

                    Text("How are you feeling?")
                        .font(DesignTokens.Font.headlineMedium)

                    // Emoji row — bridge String to String?
                    EmojiMoodSelectorView(selected: Binding(
                        get: { vm.draftEmoji },
                        set: { vm.draftEmoji = $0 ?? "😐" }
                    ))

                    // Text field
                    ZStack(alignment: .topLeading) {
                        if vm.draftText.isEmpty {
                            Text("Write about your day…")
                                .font(DesignTokens.Font.bodyMedium)
                                .foregroundStyle(DesignTokens.Color.textSecondary.opacity(0.5))
                                .padding(.top, 12)
                                .padding(.leading, 4)
                                .allowsHitTesting(false)
                        }
                        TextEditor(text: $vm.draftText)
                            .font(DesignTokens.Font.bodyMedium)
                            .focused($textFocused)
                            .frame(minHeight: 140)
                            .scrollContentBackground(.hidden)
                    }
                    .padding(DesignTokens.Spacing.md)
                    .background(DesignTokens.Color.chipUnselectedBg)
                    .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.button))

                    // Save button
                    if vm.isSaving {
                        ProgressView().tint(DesignTokens.Color.solaceTeal)
                            .frame(maxWidth: .infinity)
                    } else {
                        PrimaryButton(
                            label: "Save entry",
                            action: {
                                vm.saveEntry()
                                if !vm.showCompose { isPresented = false }
                            },
                            isEnabled: !vm.draftText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                        )
                    }
                }
                .padding(DesignTokens.Spacing.lg)
            }
            .navigationTitle("New Entry")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { isPresented = false }
                }
            }
            .background(Color.white)
            .onAppear { textFocused = true }
        }
        .onChange(of: vm.showCompose) { show in
            if !show { isPresented = false }
        }
        .presentationDetents([.medium, .large])
        .presentationDragIndicator(.visible)
    }
}

// ---------------------------------------------------------------------------
// Date helpers
// ---------------------------------------------------------------------------

private func dateHeader(_ date: Date) -> String {
    let cal = Calendar.current
    if cal.isDateInToday(date) { return "Today" }
    if cal.isDateInYesterday(date) { return "Yesterday" }
    let f = DateFormatter(); f.dateFormat = "MMMM d, yyyy"
    return f.string(from: date)
}

private func timeLabel(_ date: Date) -> String {
    let f = DateFormatter(); f.dateFormat = "h:mm a"
    return f.string(from: date)
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

#Preview("Journal list") {
    JournalView(currentUid: "preview_uid")
        .solaceTheme()
}
