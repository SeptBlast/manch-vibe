package com.solace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solace.ui.theme.*

/**
 * Horizontal row of 5 emoji for the Emotion / journal entry screen.
 * [emojis] can be overridden via config to localise or experiment with options.
 */
@Composable
fun EmojiMoodSelector(
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    emojis: List<String> = DefaultEmojis,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        emojis.forEach { emoji ->
            val isSelected = emoji == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .then(
                        if (isSelected) Modifier.background(ChipSelectedBg.copy(alpha = 0.15f))
                            .border(2.dp, SolaceTeal, CircleShape)
                        else Modifier,
                    )
                    .clickable { onSelect(emoji) },
            ) {
                Text(text = emoji, fontSize = 28.sp)
            }
        }
    }
}

val DefaultEmojis = listOf("😢", "😔", "😐", "🙂", "😊")

@Preview(showBackground = true)
@Composable
private fun EmojiMoodSelectorPreview() {
    SolaceTheme {
        var selected by remember { mutableStateOf<String?>(null) }
        EmojiMoodSelector(
            selected = selected,
            onSelect = { selected = it },
            modifier = Modifier.padding(16.dp),
        )
    }
}
