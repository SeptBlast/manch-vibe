package com.solace.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.solace.ui.components.*
import com.solace.ui.config.DefaultMoodPalette
import com.solace.ui.config.UserProfile
import com.solace.ui.onboarding.OnboardingAnswers
import com.solace.ui.theme.*

// Two-step flow: Step.PHOTO → Step.PREVIEW
private enum class CreationStep { PHOTO, PREVIEW }

@Composable
fun ProfileCardCreationScreen(
    uid: String,
    answers: OnboardingAnswers,
    onComplete: () -> Unit,
    vm: ProfileCardCreationViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    var step by remember { mutableStateOf(CreationStep.PHOTO) }
    var cardFace by remember { mutableStateOf(CardFace.FRONT) }

    // Navigate to home once Firestore save completes
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onComplete()
    }

    // Error snackbar
    val snackHost = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackHost.showSnackbar(it)
            vm.clearError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackHost) }) { padding ->
        when (step) {
            CreationStep.PHOTO -> PhotoPickerStep(
                selectedUri = state.photoUri,
                onPhotoSelected = vm::setPhoto,
                onNext = { step = CreationStep.PREVIEW },
                modifier = Modifier.padding(padding),
            )

            CreationStep.PREVIEW -> CardPreviewStep(
                uid = uid,
                answers = answers,
                photoUri = state.photoUri,
                cardFace = cardFace,
                onFlip = { cardFace = it },
                isPublishing = state.isUploading,
                onPublish = { vm.publishCard(uid, answers) },
                onBack = { step = CreationStep.PHOTO },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Step 1 — Photo picker
// ---------------------------------------------------------------------------

@Composable
private fun PhotoPickerStep(
    selectedUri: Uri?,
    onPhotoSelected: (Uri) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val launcher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        uri?.let(onPhotoSelected)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(SolaceSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SolaceSpacing.lg),
    ) {
        Spacer(Modifier.height(SolaceSpacing.xl))

        Text(
            text = "Add your photo",
            style = MaterialTheme.typography.headlineLarge,
            color = TextOnDark,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "This is the first thing people see on your card",
            style = MaterialTheme.typography.bodyMedium,
            color = TextOnDark.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(SolaceSpacing.md))

        // Avatar preview / picker
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(SolaceRadius.card))
                .background(ChipUnselectedBg)
                .border(2.dp, SolaceTeal, RoundedCornerShape(SolaceRadius.card))
                .clickable {
                    launcher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                },
            contentAlignment = Alignment.Center,
        ) {
            if (selectedUri != null) {
                AsyncImage(
                    model = selectedUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(SolaceSpacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = SolaceTeal,
                        modifier = Modifier.size(48.dp),
                    )
                    Text(
                        text = "Tap to choose",
                        style = MaterialTheme.typography.labelMedium,
                        color = SolaceTeal,
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        PrimaryButton(
            label = if (selectedUri != null) "Preview my card" else "Skip, preview card",
            onClick = onNext,
        )

        Spacer(Modifier.height(SolaceSpacing.md))
    }
}

// ---------------------------------------------------------------------------
// Step 2 — Card preview with 3-D flip
// ---------------------------------------------------------------------------

@Composable
private fun CardPreviewStep(
    uid: String,
    answers: OnboardingAnswers,
    photoUri: Uri?,
    cardFace: CardFace,
    onFlip: (CardFace) -> Unit,
    isPublishing: Boolean,
    onPublish: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = remember(answers, photoUri) {
        UserProfile(
            userId = uid,
            username = answers.username,
            avatarUrl = photoUri?.toString(),
            moodColor = answers.moodColor,
            moodLabel = DefaultMoodPalette
                .firstOrNull { it.key == answers.moodColor }?.label ?: "Calm",
            bio = answers.feelingSentence,
            vibes = answers.vibes,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(SolaceSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SolaceSpacing.md),
    ) {
        Spacer(Modifier.height(SolaceSpacing.md))

        Text(
            text = "Your profile card",
            style = MaterialTheme.typography.headlineLarge,
            color = TextOnDark,
        )
        Text(
            text = "Tap the card to flip it",
            style = MaterialTheme.typography.bodySmall,
            color = TextOnDark.copy(alpha = 0.6f),
        )

        FlipCard(
            face = cardFace,
            onFlip = onFlip,
            modifier = Modifier.fillMaxWidth(),
            front = {
                ProfileCardFront(
                    profile = profile,
                    onEditClick = onBack,
                )
            },
            back = {
                ProfileCardBack(
                    profile = profile,
                    soughtSupport = answers.soughtSupport,
                    openness = answers.openness,
                    feelingNow = answers.feelingNow,
                    onEditClick = onBack,
                )
            },
        )

        Spacer(Modifier.weight(1f))

        if (isPublishing) {
            CircularProgressIndicator(color = SolaceTeal)
        } else {
            PrimaryButton(label = "Publish my card ✨", onClick = onPublish)
        }

        Spacer(Modifier.height(SolaceSpacing.md))
    }
}
