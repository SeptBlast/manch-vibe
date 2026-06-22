import SwiftUI

// ---------------------------------------------------------------------------
// OnboardingView — multi-step questionnaire
// Milestone screens are full-screen overlays; question screens share a chrome.
// ---------------------------------------------------------------------------

public struct OnboardingView: View {

    let uid: String
    var onComplete: (OnboardingAnswers) -> Void = { _ in }

    @StateObject private var vm = OnboardingViewModel()

    public var body: some View {
        ZStack {
            if vm.currentStep.type == .milestone, let ms = vm.currentStep.milestone {
                MilestoneView(config: ms, isSaving: vm.isSaving) {
                    if vm.isLastStep { vm.completeOnboarding(uid: uid) }
                    else { vm.advance() }
                }
                .transition(.opacity.combined(with: .scale(scale: 1.02)))
            } else {
                questionChrome
                    .transition(.asymmetric(
                        insertion: .move(edge: .trailing),
                        removal: .move(edge: .leading)
                    ))
            }
        }
        .animation(.easeInOut(duration: 0.28), value: vm.currentIndex)
        .onChange(of: vm.isComplete) { _, complete in
            if complete { onComplete(vm.answers) }
        }
        .alert("Something went wrong", isPresented: .constant(vm.error != nil)) {
            Button("OK") { vm.clearError() }
        } message: {
            Text(vm.error ?? "")
        }
    }

    // MARK: - Question chrome (header + body + CTA)

    private var questionChrome: some View {
        VStack(spacing: 0) {
            // Header
            SectionHeaderView(
                onBack: vm.isFirstStep ? nil : { vm.back() },
                currentStep: vm.currentIndex + 1,
                totalSteps: vm.steps.count
            )
            StepProgressBar(current: vm.currentIndex + 1, total: vm.steps.count)
                .padding(.horizontal, DesignTokens.Spacing.md)

            // Body
            ScrollView {
                VStack(alignment: .leading, spacing: DesignTokens.Spacing.lg) {
                    Spacer().frame(height: DesignTokens.Spacing.sm)

                    Text(vm.currentStep.question)
                        .font(DesignTokens.Font.displayMedium)

                    if let sub = vm.currentStep.subtitle {
                        Text(sub)
                            .font(DesignTokens.Font.bodyMedium)
                            .foregroundStyle(DesignTokens.Color.textSecondary)
                    }

                    stepBody(for: vm.currentStep)
                }
                .padding(DesignTokens.Spacing.lg)
            }

            // CTA
            PrimaryButton(
                label: vm.isLastStep ? "Create profile" : "Continue",
                action: {
                    if vm.isLastStep { vm.completeOnboarding(uid: uid) }
                    else { vm.advance() }
                },
                isEnabled: vm.canAdvance && !vm.isSaving
            )
            .padding(DesignTokens.Spacing.lg)
        }
        .background(DesignTokens.Color.backgroundLight)
    }

    // MARK: - Step body dispatcher

    @ViewBuilder
    private func stepBody(for step: OnboardingStep) -> some View {
        switch step.type {

        case .textInput:
            TextInputStepView(
                value: textBinding(for: step.id),
                placeholder: step.placeholder ?? "",
                isMultiLine: step.id == "feeling_sentence"
            )

        case .multiSelectChips:
            let selections = vm.selectionsFor(step: step)
            FlowLayout(spacing: DesignTokens.Spacing.sm) {
                ForEach(step.options, id: \.self) { option in
                    SelectionChip(
                        label: option,
                        isSelected: selections.contains(option),
                        onToggle: { _ in
                            vm.toggleChip(stepId: step.id, option: option, max: step.maxSelections)
                        }
                    )
                }
            }

        case .colorPicker:
            ColorPickerStepView(
                selected: vm.answers.moodColor,
                onSelect: { vm.setMoodColor($0) }
            )

        case .radioList:
            let selected = vm.radioAnswerFor(step: step)
            RadioListStepView(
                options: step.options,
                selected: selected,
                onSelect: { vm.setRadioAnswer(stepId: step.id, option: $0) }
            )

        case .milestone:
            EmptyView()
        }
    }

    // MARK: - Text bindings per step

    private func textBinding(for stepId: String) -> Binding<String> {
        switch stepId {
        case "nickname":
            return Binding(get: { vm.answers.username }, set: { vm.answers.username = $0 })
        case "feeling_sentence":
            return Binding(get: { vm.answers.feelingSentence }, set: { vm.answers.feelingSentence = $0 })
        default:
            return .constant("")
        }
    }
}

// ---------------------------------------------------------------------------
// Step sub-views
// ---------------------------------------------------------------------------

private struct TextInputStepView: View {
    @Binding var value: String
    let placeholder: String
    let isMultiLine: Bool
    @FocusState private var focused: Bool

    var body: some View {
        ZStack(alignment: .topLeading) {
            if value.isEmpty {
                Text(placeholder)
                    .font(isMultiLine ? DesignTokens.Font.bodyMedium : DesignTokens.Font.bodyLarge)
                    .foregroundStyle(DesignTokens.Color.textSecondary.opacity(0.5))
                    .padding(.top, isMultiLine ? 12 : 0)
                    .padding(.leading, 4)
                    .allowsHitTesting(false)
            }
            if isMultiLine {
                TextEditor(text: $value)
                    .font(DesignTokens.Font.bodyMedium)
                    .focused($focused)
                    .frame(minHeight: 100)
                    .scrollContentBackground(.hidden)
            } else {
                TextField("", text: $value)
                    .font(DesignTokens.Font.bodyLarge)
                    .focused($focused)
                    .autocorrectionDisabled()
            }
        }
        .padding(DesignTokens.Spacing.md)
        .background(DesignTokens.Color.surfaceLight)
        .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.button))
        .overlay(
            RoundedRectangle(cornerRadius: DesignTokens.Radius.button)
                .stroke(focused ? DesignTokens.Color.solaceTeal : DesignTokens.Color.divider, lineWidth: 1)
        )
        .onTapGesture { focused = true }
    }
}

private struct ColorPickerStepView: View {
    let selected: MoodColorKey
    let onSelect: (MoodColorKey) -> Void

    var body: some View {
        VStack(spacing: DesignTokens.Spacing.sm) {
            ForEach(defaultMoodPalette) { option in
                let isSelected = option.key == selected
                HStack(spacing: DesignTokens.Spacing.md) {
                    Circle()
                        .fill(option.color)
                        .frame(width: 20, height: 20)
                    Text(option.label)
                        .font(DesignTokens.Font.bodyLarge)
                        .foregroundStyle(isSelected
                            ? DesignTokens.Color.textPrimary
                            : DesignTokens.Color.textSecondary)
                    Spacer()
                    if isSelected {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundStyle(option.color)
                    }
                }
                .padding(.horizontal, DesignTokens.Spacing.md)
                .padding(.vertical, DesignTokens.Spacing.sm)
                .background(
                    isSelected
                    ? option.color.opacity(0.12)
                    : DesignTokens.Color.chipUnselectedBg
                )
                .clipShape(RoundedRectangle(cornerRadius: DesignTokens.Radius.button))
                .overlay(
                    RoundedRectangle(cornerRadius: DesignTokens.Radius.button)
                        .stroke(isSelected ? option.color : Color.clear, lineWidth: 2)
                )
                .onTapGesture { onSelect(option.key) }
            }
        }
    }
}

private struct RadioListStepView: View {
    let options: [String]
    let selected: String
    let onSelect: (String) -> Void

    var body: some View {
        VStack(spacing: 0) {
            ForEach(options, id: \.self) { option in
                let isSelected = option == selected
                Button { onSelect(option) } label: {
                    HStack(spacing: DesignTokens.Spacing.md) {
                        ZStack {
                            Circle()
                                .stroke(isSelected
                                    ? DesignTokens.Color.solaceTeal
                                    : DesignTokens.Color.divider,
                                    lineWidth: 2)
                                .frame(width: 22, height: 22)
                            if isSelected {
                                Circle()
                                    .fill(DesignTokens.Color.solaceTeal)
                                    .frame(width: 12, height: 12)
                            }
                        }
                        Text(option)
                            .font(DesignTokens.Font.bodyLarge)
                            .foregroundStyle(DesignTokens.Color.textPrimary)
                            .multilineTextAlignment(.leading)
                        Spacer()
                    }
                    .padding(.horizontal, DesignTokens.Spacing.md)
                    .padding(.vertical, DesignTokens.Spacing.sm + 2)
                    .background(isSelected
                        ? DesignTokens.Color.solaceTeal.opacity(0.08)
                        : Color.clear)
                }
                .buttonStyle(.plain)
                Divider()
                    .padding(.leading, DesignTokens.Spacing.md + 22 + DesignTokens.Spacing.md)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Milestone full-screen view
// ---------------------------------------------------------------------------

private struct MilestoneView: View {
    let config: MilestoneConfig
    let isSaving: Bool
    let onContinue: () -> Void

    private var bg: Color {
        switch config.style {
        case .coral:  return DesignTokens.Color.milestoneCoral
        case .teal:   return DesignTokens.Color.milestoneTeal
        case .navy:   return DesignTokens.Color.milestoneNavy
        case .purple: return Color(hex: "#7B61FF")
        }
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            bg.ignoresSafeArea()

            VStack {
                Spacer()
                Text(config.title)
                    .font(DesignTokens.Font.displayLarge)
                    .foregroundStyle(DesignTokens.Color.textOnDark)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, DesignTokens.Spacing.xl)
                if !config.subtitle.isEmpty {
                    Text(config.subtitle)
                        .font(DesignTokens.Font.bodyLarge)
                        .foregroundStyle(DesignTokens.Color.textOnDark.opacity(0.8))
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, DesignTokens.Spacing.xl)
                }
                Spacer()
            }

            PrimaryButton(
                label: config.ctaLabel,
                action: onContinue,
                isEnabled: !isSaving,
                containerColor: DesignTokens.Color.textOnDark.opacity(0.2),
                contentColor: DesignTokens.Color.textOnDark
            )
            .padding(DesignTokens.Spacing.lg)
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

#Preview("Nickname step") {
    OnboardingView(uid: "preview_uid")
        .solaceTheme()
}

#Preview("Milestone coral") {
    MilestoneView(
        config: MilestoneConfig(title: "Looks like a great start!", style: .coral),
        isSaving: false,
        onContinue: {}
    )
}

#Preview("Color picker") {
    ColorPickerStepView(selected: .calmRelaxed, onSelect: { _ in })
        .padding()
}
