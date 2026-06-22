package com.solace.ui.emotion

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solace.data.emotion.MoodLog
import com.solace.ui.components.DefaultEmojis
import com.solace.ui.components.EmojiMoodSelector
import com.solace.ui.config.DefaultMoodPalette
import com.solace.ui.config.MoodColorKey
import com.solace.ui.config.MoodColorOption
import com.solace.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EmotionScreen(
    currentUid: String,
    vm: EmotionViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(currentUid) { vm.init(currentUid) }

    val snackHost = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { snackHost.showSnackbar(it); vm.clearError() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackHost) },
        containerColor = BackgroundLight,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = SolaceSpacing.xl),
        ) {
            // ── Check-in card ────────────────────────────────────────────
            item {
                CheckInCard(
                    state = state,
                    onEmojiSelect = vm::selectEmoji,
                    onMoodSelect = vm::selectMood,
                    onNoteChange = vm::setNote,
                    onLog = vm::logMood,
                    onDismissConfirmation = vm::dismissConfirmation,
                )
            }

            // ── History header ───────────────────────────────────────────
            if (state.logs.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent moods",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(
                            horizontal = SolaceSpacing.lg,
                            vertical = SolaceSpacing.md,
                        ),
                    )
                }
                items(state.logs, key = { it.id }) { log ->
                    MoodLogRow(log = log)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Check-in card
// ---------------------------------------------------------------------------

@Composable
private fun CheckInCard(
    state: EmotionUiState,
    onEmojiSelect: (String) -> Unit,
    onMoodSelect: (MoodColorKey) -> Unit,
    onNoteChange: (String) -> Unit,
    onLog: () -> Unit,
    onDismissConfirmation: () -> Unit,
) {
    val selectedColor = DefaultMoodPalette
        .firstOrNull { it.key == state.selectedMoodKey }?.color ?: SolaceTeal

    Card(
        shape = RoundedCornerShape(bottomStart = SolaceRadius.card, bottomEnd = SolaceRadius.card),
        colors = CardDefaults.cardColors(containerColor = BackgroundDark),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SolaceSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(SolaceSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Greeting
            Text(
                text = greeting(),
                style = MaterialTheme.typography.headlineMedium,
                color = TextOnDark,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "How are you feeling right now?",
                style = MaterialTheme.typography.bodyMedium,
                color = TextOnDark.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )

            // Emoji row
            EmojiMoodSelector(
                selected = state.selectedEmoji,
                onSelect = onEmojiSelect,
            )

            // Mood palette grid
            MoodPaletteGrid(
                selected = state.selectedMoodKey,
                onSelect = onMoodSelect,
            )

            // Selected mood label
            AnimatedContent(targetState = state.selectedMoodKey, label = "mood_label") { key ->
                val label = DefaultMoodPalette.firstOrNull { it.key == key }?.label ?: ""
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.xs),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(selectedColor),
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = selectedColor,
                    )
                }
            }

            // Optional note
            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                placeholder = { Text("Add a short note… (optional)", color = TextOnDark.copy(alpha = 0.4f)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(SolaceRadius.button),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = selectedColor,
                    unfocusedBorderColor = TextOnDark.copy(alpha = 0.3f),
                    focusedTextColor = TextOnDark,
                    unfocusedTextColor = TextOnDark,
                    cursorColor = selectedColor,
                ),
            )

            // Log / confirmation
            AnimatedContent(targetState = state.savedToday, label = "log_btn") { saved ->
                if (saved) {
                    ConfirmationBadge(
                        emoji = state.selectedEmoji,
                        color = selectedColor,
                        onClick = onDismissConfirmation,
                    )
                } else {
                    Button(
                        onClick = onLog,
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = selectedColor),
                        shape = RoundedCornerShape(SolaceRadius.button),
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                text = "Log my mood",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(SolaceSpacing.xs))
        }
    }
}

@Composable
private fun ConfirmationBadge(emoji: String, color: Color, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(SolaceRadius.button),
        color = color.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, color),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(SolaceSpacing.md),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = emoji, fontSize = 22.sp)
            Spacer(Modifier.width(SolaceSpacing.sm))
            Text(
                text = "Mood logged! Tap to log again",
                style = MaterialTheme.typography.labelMedium,
                color = color,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Mood palette grid — 2 × 4 colored circles with labels
// ---------------------------------------------------------------------------

@Composable
private fun MoodPaletteGrid(
    selected: MoodColorKey,
    onSelect: (MoodColorKey) -> Unit,
) {
    val rows = DefaultMoodPalette.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(SolaceSpacing.md)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                row.forEach { option ->
                    MoodPaletteItem(
                        option = option,
                        isSelected = option.key == selected,
                        onSelect = { onSelect(option.key) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodPaletteItem(
    option: MoodColorOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .padding(vertical = SolaceSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(option.color.copy(alpha = if (isSelected) 1f else 0.35f))
                .then(
                    if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                    else Modifier
                ),
        )
        Text(
            text = option.label.replace(" & ", "\n& ").replace(" ", "\n", ignoreCase = false)
                .let { if (it.lines().size > 2) option.label.substringBefore(" &") else option.label },
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) option.color else TextOnDark.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ---------------------------------------------------------------------------
// History row
// ---------------------------------------------------------------------------

@Composable
private fun MoodLogRow(log: MoodLog) {
    val color = DefaultMoodPalette
        .firstOrNull { it.key.name == log.moodColorKey }?.color ?: SolaceTeal
    val label = DefaultMoodPalette
        .firstOrNull { it.key.name == log.moodColorKey }?.label ?: log.moodColorKey

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SolaceSpacing.lg, vertical = SolaceSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.md),
    ) {
        // Emoji
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = log.emoji.ifBlank { "😐" }, fontSize = 22.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.xs),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                )
            }
            if (log.note.isNotBlank()) {
                Text(
                    text = log.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Text(
            text = formatLogTime(log.createdAt.toDate()),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = SolaceSpacing.lg),
        color = Divider,
    )
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun greeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11  -> "Good morning ☀️"
        in 12..17 -> "Good afternoon 🌤"
        in 18..21 -> "Good evening 🌙"
        else      -> "Hey there 🌟"
    }
}

private fun formatLogTime(date: java.util.Date): String {
    val today = Calendar.getInstance()
    val cal = Calendar.getInstance().apply { time = date }
    return if (today.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) &&
        today.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
    ) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    } else {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

@Preview(showBackground = true)
@Composable
private fun EmotionScreenPreview() {
    SolaceTheme {
        Column {
            CheckInCard(
                state = EmotionUiState(),
                onEmojiSelect = {},
                onMoodSelect = {},
                onNoteChange = {},
                onLog = {},
                onDismissConfirmation = {},
            )
        }
    }
}
