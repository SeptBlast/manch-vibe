package com.solace.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solace.ui.components.*
import com.solace.ui.config.DefaultMoodPalette
import com.solace.ui.config.MoodColorKey
import com.solace.ui.theme.*

@Composable
fun OnboardingScreen(
    uid: String,
    onComplete: (OnboardingAnswers) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onComplete(state.answers)
    }

    val step = state.currentStep

    // Milestone screens are full-screen — no chrome
    if (step.type == OnboardingStepType.MILESTONE) {
        MilestoneStepScreen(
            config = step.milestone!!,
            onContinue = {
                if (state.isLastStep) viewModel.completeOnboarding(uid)
                else viewModel.advance()
            },
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Header
        SectionHeader(
            onBack = if (state.isFirstStep) null else ({ viewModel.back() }),
            currentStep = state.currentIndex + 1,
            totalSteps = state.steps.size,
        )
        StepProgressBar(
            current = state.currentIndex + 1,
            total = state.steps.size,
            modifier = Modifier.padding(horizontal = SolaceSpacing.md),
        )

        // Step body — animated slide transition
        AnimatedContent(
            targetState = state.currentIndex,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                        slideOutHorizontally { it } + fadeOut()
                }
            },
            modifier = Modifier.weight(1f),
            label = "onboarding_step",
        ) { index ->
            val displayStep = state.steps[index]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(SolaceSpacing.lg),
            ) {
                Spacer(Modifier.height(SolaceSpacing.md))

                Text(
                    text = displayStep.question,
                    style = MaterialTheme.typography.displayMedium,
                )

                if (!displayStep.subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(SolaceSpacing.sm))
                    Text(
                        text = displayStep.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(SolaceSpacing.lg))

                when (displayStep.type) {
                    OnboardingStepType.TEXT_INPUT -> TextInputStep(
                        stepId = displayStep.id,
                        placeholder = displayStep.placeholder ?: "",
                        isMultiLine = displayStep.id == "feeling_sentence",
                        value = when (displayStep.id) {
                            "nickname"         -> state.answers.username
                            "feeling_sentence" -> state.answers.feelingSentence
                            else               -> ""
                        },
                        onValueChange = { text ->
                            when (displayStep.id) {
                                "nickname"         -> viewModel.setUsername(text)
                                "feeling_sentence" -> viewModel.setFeelingSentence(text)
                            }
                        },
                    )

                    OnboardingStepType.MULTI_SELECT_CHIPS -> {
                        val selected = when (displayStep.id) {
                            "mood_descriptor"  -> state.answers.moodDescriptors
                            "feeling_now"      -> state.answers.feelingNow
                            "feeling_like"     -> state.answers.feelingLike
                            "struggling_with"  -> state.answers.strugglingWith
                            "vibes"            -> state.answers.vibes
                            "hope_to_gain"     -> state.answers.hopeToGain
                            else               -> emptyList()
                        }
                        ChipGroup(
                            options = displayStep.options,
                            selected = selected.toSet(),
                            onSelectionChanged = { newSet ->
                                val toggled = (newSet - selected.toSet())
                                    .firstOrNull() ?: (selected.toSet() - newSet).firstOrNull()
                                if (toggled != null) {
                                    viewModel.toggleChipSelection(displayStep.id, toggled, displayStep.maxSelections)
                                }
                            },
                            maxSelections = displayStep.maxSelections,
                        )
                    }

                    OnboardingStepType.COLOR_PICKER -> ColorPickerStep(
                        selected = state.answers.moodColor,
                        onSelect = { viewModel.setMoodColor(it) },
                    )

                    OnboardingStepType.RADIO_LIST -> {
                        val selected = when (displayStep.id) {
                            "sought_support" -> state.answers.soughtSupport
                            "openness"       -> state.answers.openness
                            else             -> ""
                        }
                        RadioListStep(
                            options = displayStep.options,
                            selected = selected,
                            onSelect = { viewModel.setRadioAnswer(displayStep.id, it) },
                        )
                    }

                    OnboardingStepType.MILESTONE -> Unit // handled above
                }
            }
        }

        // CTA footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SolaceSpacing.lg, vertical = SolaceSpacing.md),
        ) {
            PrimaryButton(
                label = if (state.isLastStep) "Create profile" else "Continue",
                enabled = state.canAdvance && !state.isSaving,
                onClick = {
                    if (state.isLastStep) viewModel.completeOnboarding(uid)
                    else viewModel.advance()
                },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Step renderers
// ---------------------------------------------------------------------------

@Composable
private fun TextInputStep(
    stepId: String,
    placeholder: String,
    isMultiLine: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = if (isMultiLine) MaterialTheme.typography.bodyMedium
                else MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        },
        singleLine = !isMultiLine,
        minLines = if (isMultiLine) 4 else 1,
        maxLines = if (isMultiLine) 6 else 1,
        keyboardOptions = KeyboardOptions(imeAction = if (isMultiLine) ImeAction.Default else ImeAction.Done),
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SolaceTeal,
            unfocusedBorderColor = Divider,
            focusedLabelColor = SolaceTeal,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ColorPickerStep(
    selected: MoodColorKey,
    onSelect: (MoodColorKey) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SolaceSpacing.sm)) {
        DefaultMoodPalette.forEach { option ->
            val isSelected = option.key == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        if (isSelected) option.color.copy(alpha = 0.12f)
                        else ChipUnselectedBg,
                    )
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) option.color else Color.Transparent,
                        shape = MaterialTheme.shapes.medium,
                    )
                    .clickable { onSelect(option.key) }
                    .padding(horizontal = SolaceSpacing.md, vertical = SolaceSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.md),
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(option.color),
                )
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) TextPrimary else TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun RadioListStep(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SolaceSpacing.xs)) {
        options.forEach { option ->
            val isSelected = option == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(if (isSelected) SolaceTeal.copy(alpha = 0.08f) else Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(horizontal = SolaceSpacing.md, vertical = SolaceSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.md),
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onSelect(option) },
                    colors = RadioButtonDefaults.colors(selectedColor = SolaceTeal),
                )
                Text(text = option, style = MaterialTheme.typography.bodyLarge)
            }
            HorizontalDivider(color = Divider)
        }
    }
}

@Composable
private fun MilestoneStepScreen(
    config: MilestoneConfig,
    onContinue: () -> Unit,
) {
    val bg = when (config.style) {
        MilestoneStyle.CORAL  -> MilestoneCoral
        MilestoneStyle.TEAL   -> MilestoneTeal
        MilestoneStyle.NAVY   -> MilestoneNavy
        MilestoneStyle.PURPLE -> Color(0xFF7B61FF)
    }
    val textColor = TextOnDark

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(SolaceSpacing.xl),
        ) {
            Text(
                text = config.title,
                style = MaterialTheme.typography.displayLarge,
                color = textColor,
                textAlign = TextAlign.Center,
            )
            if (config.subtitle.isNotBlank()) {
                Spacer(Modifier.height(SolaceSpacing.md))
                Text(
                    text = config.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                )
            }
        }

        // CTA pinned to bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(SolaceSpacing.lg),
        ) {
            PrimaryButton(
                label = config.ctaLabel,
                onClick = onContinue,
                containerColor = textColor.copy(alpha = 0.2f),
                contentColor = textColor,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

@Preview(showBackground = true)
@Composable
private fun OnboardingNicknamePreview() {
    SolaceTheme {
        // Preview the nickname step in isolation
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SolaceSpacing.lg),
        ) {
            Text("Choose a nick name?", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(SolaceSpacing.lg))
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Coolboy28") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            )
        }
    }
}

@Preview(showBackground = true, name = "Milestone coral")
@Composable
private fun OnboardingMilestonePreview() {
    SolaceTheme {
        MilestoneStepScreen(
            config = MilestoneConfig(
                title = "Looks like a great start!",
                style = MilestoneStyle.CORAL,
            ),
            onContinue = {},
        )
    }
}
