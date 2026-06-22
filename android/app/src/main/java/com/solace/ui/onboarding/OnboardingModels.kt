package com.solace.ui.onboarding

import com.solace.ui.config.MoodColorKey

// ---------------------------------------------------------------------------
// Onboarding step config — fully remote-driven via onboarding_config_json
// TODO: fetch from RemoteConfig key "onboarding_config_json"
// ---------------------------------------------------------------------------

enum class OnboardingStepType {
    TEXT_INPUT,           // free text field (nickname, journal sentence)
    MULTI_SELECT_CHIPS,   // teal chip grid, optional maxSelections cap
    COLOR_PICKER,         // mood colour grid
    RADIO_LIST,           // single-select list
    MILESTONE,            // full-screen celebration interstitial
}

enum class MilestoneStyle { CORAL, TEAL, NAVY, PURPLE }

data class MilestoneConfig(
    val title: String,
    val subtitle: String = "",
    val style: MilestoneStyle = MilestoneStyle.CORAL,
    val ctaLabel: String = "Continue",
)

data class OnboardingStep(
    val id: String,
    val type: OnboardingStepType,
    val question: String = "",
    val subtitle: String? = null,
    val placeholder: String? = null,
    val options: List<String> = emptyList(),
    val maxSelections: Int = 0,         // 0 = unlimited
    val minSelections: Int = 1,
    val isRequired: Boolean = true,
    val milestone: MilestoneConfig? = null,
)

// ---------------------------------------------------------------------------
// Accumulated onboarding answers — what gets written to Firestore
// ---------------------------------------------------------------------------

data class OnboardingAnswers(
    val username: String = "",
    val moodDescriptors: List<String> = emptyList(),
    val moodColor: MoodColorKey = MoodColorKey.CALM_RELAXED,
    val moodLabel: String = "",
    val feelingNow: List<String> = emptyList(),
    val feelingLike: List<String> = emptyList(),
    val strugglingWith: List<String> = emptyList(),
    val soughtSupport: String = "",
    val openness: String = "",
    val vibes: List<String> = emptyList(),
    val hopeToGain: List<String> = emptyList(),
    val feelingSentence: String = "",
)

// ---------------------------------------------------------------------------
// Default steps — mirrors the Figma design exactly
// Backend can override any step's copy, options, or visibility
// ---------------------------------------------------------------------------

val DefaultOnboardingSteps: List<OnboardingStep> = listOf(
    OnboardingStep(
        id = "nickname",
        type = OnboardingStepType.TEXT_INPUT,
        question = "Choose a nick name?",
        subtitle = "This is the name your matches will see. It can be playful, thoughtful, or anything that feels like you.",
        placeholder = "Coolboy28",
    ),
    OnboardingStep(
        id = "mood_descriptor",
        type = OnboardingStepType.MULTI_SELECT_CHIPS,
        question = "Describe your mood in a few words.",
        options = listOf(
            "Feeling hopeful",
            "Just wants to talk",
            "Creative thoughts",
            "Seeking empathy",
            "Trying to stay strong",
            "Looking for good vibes",
        ),
        maxSelections = 3,
    ),
    OnboardingStep(
        id = "mood_color",
        type = OnboardingStepType.COLOR_PICKER,
        question = "Pick a color for your mood.",
        subtitle = "Choose the color that best reflects how you feel today.",
    ),
    OnboardingStep(
        id = "feeling_now",
        type = OnboardingStepType.MULTI_SELECT_CHIPS,
        question = "How are you feeling right now?",
        options = listOf(
            "Overwhelmed & struggling",
            "Anxious & unsettled",
            "Lonely or managing alone",
            "Difficulty focusing or making decisions",
            "Physically unwell or tense",
            "Concerned about relationship or family",
            "Emotionally exhausted or depleted",
            "Low motivation or feeling stuck",
            "Feeling light & hopeful",
            "Feeling happy & in balance",
        ),
        maxSelections = 3,
    ),
    OnboardingStep(
        id = "milestone_1",
        type = OnboardingStepType.MILESTONE,
        milestone = MilestoneConfig(
            title = "Looks like a great start!",
            style = MilestoneStyle.CORAL,
            ctaLabel = "Continue",
        ),
    ),
    OnboardingStep(
        id = "feeling_like",
        type = OnboardingStepType.MULTI_SELECT_CHIPS,
        question = "What feels most like you?",
        options = listOf(
            "General sadness or low energy",
            "Overwhelming loneliness",
            "Difficulty focusing or making decisions",
            "Physically unwell or tense",
            "Concerned about relationships or family",
            "Emotionally exhausted or depleted",
        ),
        maxSelections = 3,
    ),
    OnboardingStep(
        id = "struggling_with",
        type = OnboardingStepType.MULTI_SELECT_CHIPS,
        question = "What are you struggling with?",
        options = listOf(
            "Relationships",
            "Work Stress",
            "Loss & grief",
            "Self worth & confidence",
            "General sadness/depression",
            "Sought Therapy",
            "Open to all",
        ),
        maxSelections = 0,
    ),
    OnboardingStep(
        id = "milestone_2",
        type = OnboardingStepType.MILESTONE,
        milestone = MilestoneConfig(
            title = "You're doing great!\nLet's move forward",
            style = MilestoneStyle.TEAL,
            ctaLabel = "Continue",
        ),
    ),
    OnboardingStep(
        id = "sought_support",
        type = OnboardingStepType.RADIO_LIST,
        question = "Have you sought support before?",
        options = listOf(
            "Yes, I'm currently in therapy",
            "Yes, I've tried therapy but not currently",
            "No, but I'm open to exploring therapy",
            "No, I prefer managing things my own way",
        ),
    ),
    OnboardingStep(
        id = "openness",
        type = OnboardingStepType.RADIO_LIST,
        question = "How open are you about your feelings?",
        options = listOf(
            "I prefer to discuss emotional matters with a few",
            "I'm open to connect with a few like-minded people",
            "I'm open to meaningful conversations with strangers",
        ),
    ),
    OnboardingStep(
        id = "milestone_3",
        type = OnboardingStepType.MILESTONE,
        milestone = MilestoneConfig(
            title = "Thanks for sharing.\nLet's keep going!",
            style = MilestoneStyle.PURPLE,
            ctaLabel = "Continue",
        ),
    ),
    OnboardingStep(
        id = "vibes",
        type = OnboardingStepType.MULTI_SELECT_CHIPS,
        question = "What vibes do you seek?",
        options = listOf(
            "Uplifting and positive",
            "Calm and reflective",
            "Motivational devices",
            "Empathetic understanding",
            "Open to all",
        ),
        maxSelections = 3,
    ),
    OnboardingStep(
        id = "hope_to_gain",
        type = OnboardingStepType.MULTI_SELECT_CHIPS,
        question = "What do you hope to gain from Solace?",
        options = listOf(
            "Make connections with people who understand me",
            "I'd like to have space to share my feelings",
            "Find uplifting and calming content",
        ),
        maxSelections = 0,
    ),
    OnboardingStep(
        id = "feeling_sentence",
        type = OnboardingStepType.TEXT_INPUT,
        question = "Write a short sentence about your feelings today.",
        subtitle = "There's a part of me that…",
        placeholder = "There's a part of me that wants to stay in bed all day, but I'm making an effort to keep going.",
    ),
    OnboardingStep(
        id = "milestone_4",
        type = OnboardingStepType.MILESTONE,
        milestone = MilestoneConfig(
            title = "You're all set!\nLet's create your profile card",
            style = MilestoneStyle.NAVY,
            ctaLabel = "Create profile",
        ),
    ),
)
