package com.solace.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

enum class CardFace { FRONT, BACK }

/**
 * Wraps front/back composables in a 3-D Y-axis flip animation.
 * Tap anywhere on the card to flip. Also controllable via [face].
 */
@Composable
fun FlipCard(
    face: CardFace,
    onFlip: (CardFace) -> Unit,
    modifier: Modifier = Modifier,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (face == CardFace.FRONT) 0f else 180f,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "card_flip",
    )

    Box(
        modifier = modifier
            .graphicsLayer { rotationY = rotation; cameraDistance = 14f * density }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { onFlip(if (face == CardFace.FRONT) CardFace.BACK else CardFace.FRONT) },
    ) {
        if (rotation <= 90f) {
            front()
        } else {
            Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                back()
            }
        }
    }
}
