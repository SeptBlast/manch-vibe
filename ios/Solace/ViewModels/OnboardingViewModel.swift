import SwiftUI
import Combine

@MainActor
public final class OnboardingViewModel: ObservableObject {

    // Steps — remotely configurable; defaults baked in
    @Published public private(set) var steps: [OnboardingStep] = DefaultOnboardingSteps
    @Published public private(set) var currentIndex: Int = 0
    @Published public var answers: OnboardingAnswers = OnboardingAnswers()
    @Published public private(set) var isComplete: Bool = false
    @Published public private(set) var isSaving: Bool = false
    @Published public private(set) var error: String? = nil

    private let repo = OnboardingRepository.shared

    // MARK: - Derived state

    public var currentStep: OnboardingStep { steps[currentIndex] }
    public var progress: Double { Double(currentIndex + 1) / Double(steps.count) }
    public var isFirstStep: Bool { currentIndex == 0 }
    public var isLastStep: Bool { currentIndex == steps.count - 1 }

    public var canAdvance: Bool {
        switch currentStep.type {
        case .textInput:
            switch currentStep.id {
            case "nickname":         return answers.username.count >= 2
            case "feeling_sentence": return !answers.feelingSentence.isEmpty
            default:                 return true
            }
        case .multiSelectChips:
            guard currentStep.isRequired else { return true }
            return selectionsFor(step: currentStep).count >= currentStep.minSelections
        case .colorPicker: return true
        case .radioList:
            guard currentStep.isRequired else { return true }
            return !radioAnswerFor(step: currentStep).isEmpty
        case .milestone: return true
        }
    }

    // MARK: - Navigation

    public func advance() {
        guard currentIndex < steps.count - 1 else { return }
        currentIndex += 1
    }

    public func back() {
        guard currentIndex > 0 else { return }
        currentIndex -= 1
    }

    // MARK: - Answer setters

    public func setMoodColor(_ key: MoodColorKey) {
        answers.moodColor = key
        answers.moodLabel = defaultMoodPalette.first { $0.key == key }?.label ?? ""
    }

    public func toggleChip(stepId: String, option: String, max: Int) {
        switch stepId {
        case "mood_descriptor":  toggle(&answers.moodDescriptors, option, max: max)
        case "feeling_now":      toggle(&answers.feelingNow,      option, max: max)
        case "feeling_like":     toggle(&answers.feelingLike,     option, max: max)
        case "struggling_with":  toggle(&answers.strugglingWith,  option, max: max)
        case "vibes":            toggle(&answers.vibes,           option, max: max)
        case "hope_to_gain":     toggle(&answers.hopeToGain,      option, max: max)
        default: break
        }
    }

    public func setRadioAnswer(stepId: String, option: String) {
        switch stepId {
        case "sought_support": answers.soughtSupport = option
        case "openness":       answers.openness = option
        default: break
        }
    }

    // MARK: - Completion

    public func completeOnboarding(uid: String) {
        isSaving = true
        Task {
            do {
                try await repo.saveOnboardingAnswers(uid: uid, answers: answers)
                isComplete = true
            } catch {
                self.error = error.localizedDescription
            }
            isSaving = false
        }
    }

    public func clearError() { error = nil }

    // MARK: - Helpers

    public func selectionsFor(step: OnboardingStep) -> [String] {
        switch step.id {
        case "mood_descriptor":  return answers.moodDescriptors
        case "feeling_now":      return answers.feelingNow
        case "feeling_like":     return answers.feelingLike
        case "struggling_with":  return answers.strugglingWith
        case "vibes":            return answers.vibes
        case "hope_to_gain":     return answers.hopeToGain
        default:                 return []
        }
    }

    public func radioAnswerFor(step: OnboardingStep) -> String {
        switch step.id {
        case "sought_support": return answers.soughtSupport
        case "openness":       return answers.openness
        default:               return ""
        }
    }

    private func toggle(_ list: inout [String], _ item: String, max: Int) {
        if let i = list.firstIndex(of: item) {
            list.remove(at: i)
        } else if max == 0 || list.count < max {
            list.append(item)
        }
    }
}
