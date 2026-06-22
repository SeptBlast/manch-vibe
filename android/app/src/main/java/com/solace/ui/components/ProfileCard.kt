package com.solace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.solace.ui.config.*
import com.solace.ui.theme.*

/**
 * Full profile card matching the Figma "front" frame:
 * - Top half: avatar image
 * - Username row with mood color dot and vibe badge
 * - Bio text
 * - Vibe tags row
 * - "You're all ❤️" tagline + CTA
 */
@Composable
fun ProfileCardComposable(
    profile: UserProfile,
    modifier: Modifier = Modifier,
    onConnectClick: () -> Unit = {},
) {
    val moodColor = DefaultMoodPalette.firstOrNull { it.key == profile.moodColor }?.color
        ?: SolaceTeal

    Card(
        shape = RoundedCornerShape(SolaceRadius.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            // Avatar
            if (profile.avatarUrl != null) {
                AsyncImage(
                    model = profile.avatarUrl,
                    contentDescription = "${profile.username} avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SolaceSizes.profileImageHeight)
                        .clip(RoundedCornerShape(topStart = SolaceRadius.card, topEnd = SolaceRadius.card)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SolaceSizes.profileImageHeight)
                        .background(ChipUnselectedBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = profile.username.take(2).uppercase(),
                        style = MaterialTheme.typography.displayMedium,
                        color = TextSecondary,
                    )
                }
            }

            Column(modifier = Modifier.padding(SolaceSpacing.md)) {
                // Username + mood dot
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.sm),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(moodColor),
                    )
                    Text(
                        text = profile.username,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(SolaceSpacing.xs))

                // Vibe badge
                if (profile.minutesPerDay.isNotBlank()) {
                    SurfaceChip(label = profile.minutesPerDay)
                    Spacer(Modifier.height(SolaceSpacing.sm))
                }

                // Mood label
                Text(
                    text = profile.moodLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(SolaceSpacing.sm))

                // Bio
                Text(
                    text = profile.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(SolaceSpacing.sm))

                // Vibe tags
                if (profile.vibes.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.xs)) {
                        profile.vibes.take(3).forEach { vibe ->
                            SurfaceChip(label = vibe)
                        }
                    }
                    Spacer(Modifier.height(SolaceSpacing.md))
                }

                // CTA
                PrimaryButton(
                    label = "Connect ahead",
                    onClick = onConnectClick,
                )
            }
        }
    }
}

/**
 * Compact profile tile for the Likes grid (3-column).
 */
@Composable
fun ProfileGridItem(
    profile: UserProfile,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val moodColor = DefaultMoodPalette.firstOrNull { it.key == profile.moodColor }?.color
        ?: SolaceTeal

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(SolaceRadius.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.aspectRatio(0.75f),
    ) {
        Column {
            if (profile.avatarUrl != null) {
                AsyncImage(
                    model = profile.avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = SolaceRadius.card, topEnd = SolaceRadius.card)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(moodColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(moodColor),
                    )
                }
            }
            Text(
                text = profile.username,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun SurfaceChip(label: String) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = ChipUnselectedBg,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = SolaceSpacing.sm, vertical = 4.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileCardPreview() {
    SolaceTheme {
        ProfileCardComposable(
            profile = UserProfile(
                userId = "1",
                username = "CalmWaves27",
                avatarUrl = null,
                moodColor = MoodColorKey.CALM_RELAXED,
                moodLabel = "Calm and reflective",
                bio = "There's a part of me that wants to stay in bed all day, but I'm making an effort to keep going.",
                vibes = listOf("Relationships", "Calm and reflective"),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
