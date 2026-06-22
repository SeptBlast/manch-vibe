package com.solace.ui.journal

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBoxValue.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.solace.data.journal.JournalEntry
import com.solace.ui.components.EmojiMoodSelector
import com.solace.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun JournalScreen(
    currentUid: String,
    vm: JournalViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(currentUid) { vm.init(currentUid) }

    val snackHost = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { snackHost.showSnackbar(it); vm.clearError() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackHost) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = vm::openCompose,
                containerColor = SolaceTeal,
                contentColor = Color.White,
            ) {
                Icon(Icons.Default.Add, contentDescription = "New entry")
            }
        },
        containerColor = BackgroundLight,
    ) { padding ->

        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SolaceTeal)
            }
        } else {
            JournalListContent(
                entries = state.entries,
                onDelete = vm::deleteEntry,
                modifier = Modifier.padding(padding),
            )
        }
    }

    // Compose bottom sheet
    if (state.showCompose) {
        ComposeEntrySheet(
            emoji = state.draftEmoji,
            text = state.draftText,
            isSaving = state.isSaving,
            onEmojiChange = vm::setDraftEmoji,
            onTextChange = vm::setDraftText,
            onSave = vm::saveEntry,
            onDismiss = vm::dismissCompose,
        )
    }
}

// ---------------------------------------------------------------------------
// Entry list
// ---------------------------------------------------------------------------

@Composable
private fun JournalListContent(
    entries: List<JournalEntry>,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✍️", fontSize = 48.sp)
                Spacer(Modifier.height(SolaceSpacing.sm))
                Text("No entries yet", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                Spacer(Modifier.height(SolaceSpacing.xs))
                Text("Tap + to write your first entry", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        return
    }

    // Group entries by date header
    val grouped = entries.groupBy { formatDateHeader(it.createdAt.toDate()) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(SolaceSpacing.md),
        verticalArrangement = Arrangement.spacedBy(SolaceSpacing.sm),
    ) {
        grouped.forEach { (dateHeader, dayEntries) ->
            item(key = dateHeader) {
                Text(
                    text = dateHeader,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = SolaceSpacing.xs),
                )
            }
            items(dayEntries, key = { it.id }) { entry ->
                SwipeToDeleteEntry(entry = entry, onDelete = { onDelete(entry.id) })
            }
        }
        // bottom FAB clearance
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteEntry(entry: JournalEntry, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { it == EndToStart },
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == EndToStart) onDelete()
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Error.copy(alpha = 0.85f), RoundedCornerShape(SolaceRadius.card))
                    .padding(end = SolaceSpacing.md),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
        },
        enableDismissFromStartToEnd = false,
    ) {
        JournalEntryCard(entry = entry)
    }
}

@Composable
private fun JournalEntryCard(entry: JournalEntry) {
    Card(
        shape = RoundedCornerShape(SolaceRadius.card),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().animateContentSize(),
    ) {
        Row(
            modifier = Modifier.padding(SolaceSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            // Emoji badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(ChipUnselectedBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = entry.emoji.ifBlank { "📝" }, fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatTimeLabel(entry.createdAt.toDate()),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = entry.text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Compose sheet (ModalBottomSheet)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeEntrySheet(
    emoji: String,
    text: String,
    isSaving: Boolean,
    onEmojiChange: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SolaceSpacing.lg)
                .padding(bottom = SolaceSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(SolaceSpacing.md),
        ) {
            Text("How are you feeling?", style = MaterialTheme.typography.titleLarge)

            EmojiMoodSelector(selected = emoji, onSelect = onEmojiChange)

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Write about your day…", color = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
                maxLines = 10,
                shape = RoundedCornerShape(SolaceRadius.button),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SolaceTeal,
                    unfocusedBorderColor = Divider,
                ),
            )

            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = SolaceTeal,
                )
            } else {
                Button(
                    onClick = onSave,
                    enabled = text.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SolaceTeal),
                    shape = RoundedCornerShape(SolaceRadius.button),
                ) {
                    Text("Save entry", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun formatDateHeader(date: java.util.Date): String {
    val today = java.util.Calendar.getInstance()
    val cal = java.util.Calendar.getInstance().apply { time = date }
    return when {
        isSameDay(today, cal) -> "Today"
        isYesterday(today, cal) -> "Yesterday"
        else -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(date)
    }
}

private fun formatTimeLabel(date: java.util.Date) =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)

private fun isSameDay(a: java.util.Calendar, b: java.util.Calendar) =
    a.get(java.util.Calendar.YEAR) == b.get(java.util.Calendar.YEAR) &&
        a.get(java.util.Calendar.DAY_OF_YEAR) == b.get(java.util.Calendar.DAY_OF_YEAR)

private fun isYesterday(today: java.util.Calendar, other: java.util.Calendar): Boolean {
    val yesterday = java.util.Calendar.getInstance().apply {
        time = today.time; add(java.util.Calendar.DAY_OF_YEAR, -1)
    }
    return isSameDay(yesterday, other)
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true)
@Composable
private fun JournalScreenPreview() {
    SolaceTheme {
        JournalListContent(
            entries = listOf(
                JournalEntry("1", "😊", "Had a really good therapy session today. Starting to feel more grounded.", "", Timestamp.now()),
                JournalEntry("2", "😔", "Feeling a bit low. Tried to go for a walk but it started raining.", "", Timestamp.now()),
            ),
            onDelete = {},
        )
    }
}
