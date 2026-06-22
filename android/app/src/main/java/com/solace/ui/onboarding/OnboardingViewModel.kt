package com.solace.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solace.data.onboarding.OnboardingRepository
import com.solace.ui.config.DefaultMoodPalette
import com.solace.ui.config.MoodColorKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val steps: List<OnboardingStep> = DefaultOnboardingSteps,
    val currentIndex: Int = 0,
    val answers: OnboardingAnswers = OnboardingAnswers(),
    val isComplete: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
) {
    val currentStep: OnboardingStep get() = steps[currentIndex]
    val progress: Float get() = (currentIndex + 1).toFloat() / steps.size.toFloat()
    val isFirstStep: Boolean get() = currentIndex == 0
    val isLastStep: Boolean get() = currentIndex == steps.lastIndex

    /** Whether the current step has a valid answer to advance. */
    val canAdvance: Boolean get() = when (currentStep.type) {
        OnboardingStepType.TEXT_INPUT -> when (currentStep.id) {
            "nickname" -> answers.username.length >= 2
            "feeling_sentence" -> answers.feelingSentence.isNotBlank()
            else -> true
        }
        OnboardingStepType.MULTI_SELECT_CHIPS -> {
            val selections = currentStep.currentSelections(answers)
            !currentStep.isRequired || selections.size >= currentStep.minSelections
        }
        OnboardingStepType.COLOR_PICKER -> true
        OnboardingStepType.RADIO_LIST -> {
            val selection = currentStep.currentRadioAnswer(answers)
            !currentStep.isRequired || selection.isNotBlank()
        }
        OnboardingStepType.MILESTONE -> true
    }
}

// Extension helpers to read answers per step
private fun OnboardingStep.currentSelections(answers: OnboardingAnswers): List<String> = when (id) {
    "mood_descriptor"  -> answers.moodDescriptors
    "feeling_now"      -> answers.feelingNow
    "feeling_like"     -> answers.feelingLike
    "struggling_with"  -> answers.strugglingWith
    "vibes"            -> answers.vibes
    "hope_to_gain"     -> answers.hopeToGain
    else               -> emptyList()
}

private fun OnboardingStep.currentRadioAnswer(answers: OnboardingAnswers): String = when (id) {
    "sought_support" -> answers.soughtSupport
    "openness"       -> answers.openness
    else             -> ""
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepo: OnboardingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    // ── Navigation ────────────────────────────────────────────────────────

    fun advance() {
        val s = _state.value
        if (s.isLastStep) return
        _state.update { it.copy(currentIndex = it.currentIndex + 1) }
    }

    fun back() {
        val s = _state.value
        if (s.isFirstStep) return
        _state.update { it.copy(currentIndex = it.currentIndex - 1) }
    }

    // ── Answer setters ────────────────────────────────────────────────────

    fun setUsername(value: String) =
        _state.update { it.copy(answers = it.answers.copy(username = value)) }

    fun setFeelingSentence(value: String) =
        _state.update { it.copy(answers = it.answers.copy(feelingSentence = value)) }

    fun toggleChipSelection(stepId: String, option: String, maxSelections: Int) {
        _state.update { s ->
            val answers = s.answers
            val updated = when (stepId) {
                "mood_descriptor" -> answers.copy(moodDescriptors = toggle(answers.moodDescriptors, option, maxSelections))
                "feeling_now"     -> answers.copy(feelingNow     = toggle(answers.feelingNow,     option, maxSelections))
                "feeling_like"    -> answers.copy(feelingLike    = toggle(answers.feelingLike,    option, maxSelections))
                "struggling_with" -> answers.copy(strugglingWith = toggle(answers.strugglingWith, option, maxSelections))
                "vibes"           -> answers.copy(vibes          = toggle(answers.vibes,          option, maxSelections))
                "hope_to_gain"    -> answers.copy(hopeToGain     = toggle(answers.hopeToGain,     option, maxSelections))
                else              -> answers
            }
            s.copy(answers = updated)
        }
    }

    fun setMoodColor(key: MoodColorKey) {
        val label = DefaultMoodPalette.firstOrNull { it.key == key }?.label ?: ""
        _state.update { it.copy(answers = it.answers.copy(moodColor = key, moodLabel = label)) }
    }

    fun setRadioAnswer(stepId: String, option: String) {
        _state.update { s ->
            val updated = when (stepId) {
                "sought_support" -> s.answers.copy(soughtSupport = option)
                "openness"       -> s.answers.copy(openness = option)
                else             -> s.answers
            }
            s.copy(answers = updated)
        }
    }

    // ── Completion ────────────────────────────────────────────────────────

    fun completeOnboarding(uid: String) {
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            onboardingRepo.saveOnboardingAnswers(uid, _state.value.answers)
                .onSuccess { _state.update { it.copy(isComplete = true, isSaving = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isSaving = false) } }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun toggle(list: List<String>, item: String, max: Int): List<String> =
        if (item in list) list - item
        else if (max > 0 && list.size >= max) list
        else list + item
}
