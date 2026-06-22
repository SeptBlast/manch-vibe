package com.solace.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.solace.ui.theme.*

/**
 * Onboarding-screen header:  ← back    Step N of M
 * [currentStep] / [totalSteps] = 0 hides the step counter.
 */
@Composable
fun SectionHeader(
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    currentStep: Int = 0,
    totalSteps: Int = 0,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SolaceSpacing.sm, vertical = SolaceSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        } else {
            Spacer(Modifier.size(48.dp))
        }

        Spacer(Modifier.weight(1f))

        if (currentStep > 0 && totalSteps > 0) {
            Text(
                text = "Step $currentStep of $totalSteps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(SolaceSpacing.sm))
        }
    }
}

/**
 * Linear step progress bar shown below [SectionHeader] on onboarding screens.
 */
@Composable
fun StepProgressBar(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    LinearProgressIndicator(
        progress = { current.toFloat() / total.toFloat() },
        color = SolaceTeal,
        trackColor = ChipUnselectedBg,
        modifier = modifier
            .fillMaxWidth()
            .height(SolaceSpacing.xs),
    )
}

@Preview(showBackground = true)
@Composable
private fun SectionHeaderPreview() {
    SolaceTheme {
        Column {
            SectionHeader(onBack = {}, currentStep = 3, totalSteps = 10)
            StepProgressBar(current = 3, total = 10)
        }
    }
}
