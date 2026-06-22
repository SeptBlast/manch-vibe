package com.solace.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.solace.ui.theme.*

/**
 * Full-width teal CTA button used throughout Solace.
 * [containerColor] overrides the default brand teal for milestone/splash variants.
 */
@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = SolaceTeal,
    contentColor: Color = TextOnPrimary,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.6f),
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .height(SolaceSizes.buttonHeight),
    ) {
        if (leadingIcon != null) {
            Box(modifier = Modifier.padding(end = SolaceSpacing.sm)) {
                leadingIcon()
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/**
 * Outlined secondary button — used for "Sign in with Google / Apple".
 */
@Composable
fun OutlinedAuthButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
        modifier = modifier
            .fillMaxWidth()
            .height(SolaceSizes.buttonHeight),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (leadingIcon != null) {
                Box(modifier = Modifier.padding(end = SolaceSpacing.sm)) {
                    leadingIcon()
                }
            }
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonPreview() {
    SolaceTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PrimaryButton(label = "Continue with Email", onClick = {})
            OutlinedAuthButton(label = "Continue with Google", onClick = {})
            PrimaryButton(label = "Disabled", onClick = {}, enabled = false)
        }
    }
}
