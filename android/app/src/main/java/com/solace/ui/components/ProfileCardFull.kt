package com.solace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.solace.ui.config.*
import com.solace.ui.theme.*

// ---------------------------------------------------------------------------
// ProfileCardFront — matches Figma "front" frame exactly
// ---------------------------------------------------------------------------

@Composable
fun ProfileCardFront(
    profile: UserProfile,
    onEditClick: (() -> Unit)? = null,
    onConnectClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val moodColor = DefaultMoodPalette.firstOrNull { it.key == profile.moodColor }?.color ?: SolaceTeal

    Card(
        shape = RoundedCornerShape(SolaceRadius.card),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            // ── Card header ─────────────────────────────────────────────
            CardHeader(onEditClick = onEditClick)

            // ── Avatar ──────────────────────────────────────────────────
            AvatarSection(avatarUrl = profile.avatarUrl, username = profile.username)

            // ── Body ────────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = SolaceSpacing.md, vertical = SolaceSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(SolaceSpacing.sm),
            ) {
                // Mood descriptor chips (from onboarding)
                if (profile.vibes.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.xs)) {
                        profile.vibes.take(2).forEach { tag ->
                            MoodChip(label = tag, color = moodColor)
                        }
                    }
                }

                // Username row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.xs),
                ) {
                    Text(
                        text = profile.username,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f),
                    )
                    // Verified dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(moodColor),
                    )
                }

                // Mood label
                Text(
                    text = profile.moodLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )

                HorizontalDivider(color = Divider)

                // Bio
                Text(
                    text = profile.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                // Vibe tags
                Row(horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.xs)) {
                    profile.vibes.take(3).forEach { vibe ->
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = ChipUnselectedBg,
                        ) {
                            Text(
                                text = vibe,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = SolaceSpacing.sm, vertical = 4.dp),
                            )
                        }
                    }
                }

                // Tagline
                Text(
                    text = "You're all ❤️  Tailored to connect your thoughts",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = TextSecondary,
                )

                // CTA
                PrimaryButton(label = "Connect ahead", onClick = onConnectClick)

                Spacer(Modifier.height(SolaceSpacing.xs))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ProfileCardBack — matches Figma "back" frame exactly
// ---------------------------------------------------------------------------

@Composable
fun ProfileCardBack(
    profile: UserProfile,
    soughtSupport: String = "",
    openness: String = "",
    feelingNow: List<String> = emptyList(),
    onEditClick: (() -> Unit)? = null,
    onConnectClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val moodColor = DefaultMoodPalette.firstOrNull { it.key == profile.moodColor }?.color ?: SolaceTeal

    Card(
        shape = RoundedCornerShape(SolaceRadius.card),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            CardHeader(onEditClick = onEditClick)

            Column(
                modifier = Modifier.padding(horizontal = SolaceSpacing.md, vertical = SolaceSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(SolaceSpacing.sm),
            ) {
                // Username large
                Text(text = profile.username, style = MaterialTheme.typography.headlineLarge)

                // Tagline / bio summary
                Text(
                    text = profile.bio.take(60).let { if (profile.bio.length > 60) "$it…" else it },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )

                HorizontalDivider(color = Divider)

                // Feeling now (first 2)
                if (feelingNow.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        feelingNow.take(2).forEach { f ->
                            Text(text = f, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }

                // Mood chip (teal selected)
                MoodChip(label = profile.moodLabel, color = moodColor, filled = true)

                HorizontalDivider(color = Divider)

                // Full bio
                Text(
                    text = profile.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                // Sought support answer
                if (soughtSupport.isNotBlank()) {
                    BackInfoRow(text = soughtSupport)
                }

                // Openness answer
                if (openness.isNotBlank()) {
                    BackInfoRow(text = openness)
                }

                HorizontalDivider(color = Divider)

                // Vibe tags
                Row(horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.xs)) {
                    profile.vibes.take(3).forEach { vibe ->
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = ChipUnselectedBg,
                        ) {
                            Text(
                                text = vibe,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = SolaceSpacing.sm, vertical = 4.dp),
                            )
                        }
                    }
                }

                Text(
                    text = "You're all ❤️  Tailored to connect your thoughts",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = TextSecondary,
                )

                PrimaryButton(label = "Connect ahead", onClick = onConnectClick)
                Spacer(Modifier.height(SolaceSpacing.xs))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Shared sub-components
// ---------------------------------------------------------------------------

@Composable
private fun CardHeader(onEditClick: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SolaceSpacing.md, vertical = SolaceSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Profile Card",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary,
            modifier = Modifier.weight(1f),
        )
        if (onEditClick != null) {
            TextButton(onClick = onEditClick, contentPadding = PaddingValues(4.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(16.dp),
                    tint = TextSecondary,
                )
                Spacer(Modifier.width(4.dp))
                Text(text = "Edit", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun AvatarSection(avatarUrl: String?, username: String) {
    if (avatarUrl != null) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(SolaceSizes.profileImageHeight),
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
                text = username.take(2).uppercase(),
                style = MaterialTheme.typography.displayMedium,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun MoodChip(label: String, color: Color, filled: Boolean = false) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = if (filled) SolaceTeal else color.copy(alpha = 0.12f),
        border = if (!filled) androidx.compose.foundation.BorderStroke(1.dp, color) else null,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SolaceSpacing.sm, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (filled) Color.White else color,
            )
        }
    }
}

@Composable
private fun BackInfoRow(text: String) {
    Text(
        text = "• $text",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val sampleProfile = UserProfile(
    userId = "1",
    username = "CalmWaves27",
    avatarUrl = null,
    moodColor = MoodColorKey.CALM_RELAXED,
    moodLabel = "Calm and reflective",
    bio = "There's a part of me that wants to stay in bed all day, but I'm making an effort to keep going.",
    vibes = listOf("Relationships", "Calm and reflective"),
)

@Preview(showBackground = true)
@Composable
private fun ProfileCardFrontPreview() {
    SolaceTheme {
        ProfileCardFront(profile = sampleProfile, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileCardBackPreview() {
    SolaceTheme {
        ProfileCardBack(
            profile = sampleProfile,
            soughtSupport = "Yes, I'm currently in therapy",
            openness = "I'd like to connect with a few like-minded people",
            feelingNow = listOf("I'm feeling numb and disconnected"),
            modifier = Modifier.padding(16.dp),
        )
    }
}
