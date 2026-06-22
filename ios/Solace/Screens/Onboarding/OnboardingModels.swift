import SwiftUI

// ---------------------------------------------------------------------------
// Onboarding step config — remotely driven via "onboarding_config_json"
// TODO: fetch from RemoteConfig; use DefaultOnboardingSteps as fallback
// ---------------------------------------------------------------------------

public enum OnboardingStepType: String, Codable {
    case textInput         = "TEXT_INPUT"
    case multiSelectChips  = "MULTI_SELECT_CHIPS"
    case colorPicker       = "COLOR_PICKER"
    case radioList         = "RADIO_LIST"
    case milestone         = "MILESTONE"
}

public enum MilestoneStyle: String, Codable {
    case coral, teal, navy, purple
}

public struct MilestoneConfig: Codable {
    public var title: String
    public var subtitle: String = ""
    public var style: MilestoneStyle = .coral
    public var ctaLabel: String = "Continue"
}

public struct OnboardingStep: Identifiable, Codable {
    public var id: String
    public var type: OnboardingStepType
    public var question: String = ""
    public var subtitle: String? = nil
    public var placeholder: String? = nil
    public var options: [String] = []
    public var maxSelections: Int = 0   // 0 = unlimited
    public var minSelections: Int = 1
    public var isRequired: Bool = true
    public var milestone: MilestoneConfig? = nil
}

// ---------------------------------------------------------------------------
// Accumulated answers — written to Firestore on completion
// ---------------------------------------------------------------------------

public struct OnboardingAnswers {
    public var username: String = ""
    public var moodDescriptors: [String] = []
    public var moodColor: MoodColorKey = .calmRelaxed
    public var moodLabel: String = ""
    public var feelingNow: [String] = []
    public var feelingLike: [String] = []
    public var strugglingWith: [String] = []
    public var soughtSupport: String = ""
    public var openness: String = ""
    public var vibes: [String] = []
    public var hopeToGain: [String] = []
    public var feelingSentence: String = ""
}

// ---------------------------------------------------------------------------
// Default steps — mirrors the Figma design exactly
// ---------------------------------------------------------------------------

public let DefaultOnboardingSteps: [OnboardingStep] = [
    OnboardingStep(
        id: "nickname", type: .textInput,
        question: "Choose a nick name?",
        subtitle: "This is the name your matches will see. It can be playful, thoughtful, or anything that feels like you.",
        placeholder: "Coolboy28"
    ),
    OnboardingStep(
        id: "mood_descriptor", type: .multiSelectChips,
        question: "Describe your mood in a few words.",
        options: ["Feeling hopeful", "Just wants to talk", "Creative thoughts",
                  "Seeking empathy", "Trying to stay strong", "Looking for good vibes"],
        maxSelections: 3
    ),
    OnboardingStep(
        id: "mood_color", type: .colorPicker,
        question: "Pick a color for your mood.",
        subtitle: "Choose the color that best reflects how you feel today."
    ),
    OnboardingStep(
        id: "feeling_now", type: .multiSelectChips,
        question: "How are you feeling right now?",
        options: ["Overwhelmed & struggling", "Anxious & unsettled", "Lonely or managing alone",
                  "Difficulty focusing or making decisions", "Physically unwell or tense",
                  "Concerned about relationship or family", "Emotionally exhausted or depleted",
                  "Low motivation or feeling stuck", "Feeling light & hopeful", "Feeling happy & in balance"],
        maxSelections: 3
    ),
    OnboardingStep(
        id: "milestone_1", type: .milestone,
        milestone: MilestoneConfig(title: "Looks like a great start!", style: .coral)
    ),
    OnboardingStep(
        id: "feeling_like", type: .multiSelectChips,
        question: "What feels most like you?",
        options: ["General sadness or low energy", "Overwhelming loneliness",
                  "Difficulty focusing or making decisions", "Physically unwell or tense",
                  "Concerned about relationships or family", "Emotionally exhausted or depleted"],
        maxSelections: 3
    ),
    OnboardingStep(
        id: "struggling_with", type: .multiSelectChips,
        question: "What are you struggling with?",
        options: ["Relationships", "Work Stress", "Loss & grief", "Self worth & confidence",
                  "General sadness/depression", "Sought Therapy", "Open to all"],
        maxSelections: 0
    ),
    OnboardingStep(
        id: "milestone_2", type: .milestone,
        milestone: MilestoneConfig(title: "You're doing great!\nLet's move forward", style: .teal)
    ),
    OnboardingStep(
        id: "sought_support", type: .radioList,
        question: "Have you sought support before?",
        options: ["Yes, I'm currently in therapy",
                  "Yes, I've tried therapy but not currently",
                  "No, but I'm open to exploring therapy",
                  "No, I prefer managing things my own way"]
    ),
    OnboardingStep(
        id: "openness", type: .radioList,
        question: "How open are you about your feelings?",
        options: ["I prefer to discuss emotional matters with a few",
                  "I'm open to connect with a few like-minded people",
                  "I'm open to meaningful conversations with strangers"]
    ),
    OnboardingStep(
        id: "milestone_3", type: .milestone,
        milestone: MilestoneConfig(title: "Thanks for sharing.\nLet's keep going!", style: .purple)
    ),
    OnboardingStep(
        id: "vibes", type: .multiSelectChips,
        question: "What vibes do you seek?",
        options: ["Uplifting and positive", "Calm and reflective",
                  "Motivational devices", "Empathetic understanding", "Open to all"],
        maxSelections: 3
    ),
    OnboardingStep(
        id: "hope_to_gain", type: .multiSelectChips,
        question: "What do you hope to gain from Solace?",
        options: ["Make connections with people who understand me",
                  "I'd like to have space to share my feelings",
                  "Find uplifting and calming content"],
        maxSelections: 0
    ),
    OnboardingStep(
        id: "feeling_sentence", type: .textInput,
        question: "Write a short sentence about your feelings today.",
        subtitle: "There's a part of me that…",
        placeholder: "There's a part of me that wants to stay in bed all day, but I'm making an effort to keep going."
    ),
    OnboardingStep(
        id: "milestone_4", type: .milestone,
        milestone: MilestoneConfig(title: "You're all set!\nLet's create your profile card", style: .navy, ctaLabel: "Create profile")
    ),
]
