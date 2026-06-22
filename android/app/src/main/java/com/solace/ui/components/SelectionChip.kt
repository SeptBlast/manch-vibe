package com.solace.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.solace.ui.theme.*

/**
 * Pill-shaped selectable tag. Teal when selected, light-grey when not.
 * Used in mood pickers, vibe selectors, and struggle categories.
 */
@Composable
fun SelectionChip(
    label: String,
    selected: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = { onToggle(!selected) },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ChipSelectedBg,
            selectedLabelColor = ChipSelectedText,
            containerColor = ChipUnselectedBg,
            labelColor = ChipUnselectedText,
        ),
        border = null,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.height(SolaceSizes.chipHeight),
    )
}

/**
 * Wrapping flow layout of [SelectionChip]s from a flat list.
 * [maxSelections] = 0 means unlimited.
 */
@Composable
fun ChipGroup(
    options: List<String>,
    selected: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    maxSelections: Int = 0,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(SolaceSpacing.sm),
        modifier = modifier,
    ) {
        options.forEach { option ->
            val isSelected = option in selected
            SelectionChip(
                label = option,
                selected = isSelected,
                onToggle = { nowSelected ->
                    if (nowSelected && maxSelections > 0 && selected.size >= maxSelections) return@SelectionChip
                    onSelectionChanged(
                        if (nowSelected) selected + option else selected - option,
                    )
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectionChipPreview() {
    SolaceTheme {
        var selected by remember { mutableStateOf(setOf("Relationships")) }
        ChipGroup(
            options = listOf(
                "Relationships", "Work Stress", "Loss & grief",
                "Self worth", "Peaceful & Grounded", "Anxiety",
            ),
            selected = selected,
            onSelectionChanged = { selected = it },
            modifier = Modifier.padding(SolaceSpacing.md),
        )
    }
}
