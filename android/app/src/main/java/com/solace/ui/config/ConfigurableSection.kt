package com.solace.ui.config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.solace.ui.components.ProfileCardComposable
import com.solace.ui.components.ProfileGridItem
import com.solace.ui.theme.SolaceSpacing
import com.solace.ui.theme.SolaceTheme

/**
 * Renders a single remotely-configured section.
 * Add new HomeSectionType cases here as the product grows; the outer
 * HomeScreen iterates visibleSections and delegates to this function.
 */
@Composable
fun ConfigurableSection(
    config: HomeSectionConfig,
    profiles: List<UserProfile>,
    modifier: Modifier = Modifier,
) {
    when (config.type) {
        HomeSectionType.PROFILE_FEED -> ProfileFeedSection(
            title = config.title,
            ctaLabel = config.ctaLabel,
            profiles = profiles,
            modifier = modifier,
        )

        HomeSectionType.LIKES_GRID -> LikesGridSection(
            title = config.title,
            profiles = profiles,
            modifier = modifier,
        )

        HomeSectionType.JOURNAL_PREVIEW -> JournalPreviewSection(
            title = config.title,
            modifier = modifier,
        )

        HomeSectionType.EMOTION_PROMPT -> EmotionPromptSection(
            title = config.title,
            modifier = modifier,
        )

        HomeSectionType.UNKNOWN -> Unit // graceful no-op for future unknown types
    }
}

// ---------------------------------------------------------------------------
// Section renderers
// ---------------------------------------------------------------------------

@Composable
private fun ProfileFeedSection(
    title: String,
    ctaLabel: String?,
    profiles: List<UserProfile>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(
                    horizontal = SolaceSpacing.md,
                    vertical = SolaceSpacing.sm,
                ),
            )
        }
        LazyColumn(
            contentPadding = PaddingValues(horizontal = SolaceSpacing.md),
            verticalArrangement = Arrangement.spacedBy(SolaceSpacing.md),
        ) {
            items(profiles, key = { it.userId }) { profile ->
                ProfileCardComposable(profile = profile)
            }
        }
    }
}

@Composable
private fun LikesGridSection(
    title: String,
    profiles: List<UserProfile>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(
                    horizontal = SolaceSpacing.md,
                    vertical = SolaceSpacing.sm,
                ),
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(SolaceSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(SolaceSpacing.sm),
            modifier = Modifier.height(400.dp),
        ) {
            items(profiles, key = { it.userId }) { profile ->
                ProfileGridItem(profile = profile)
            }
        }
    }
}

@Composable
private fun JournalPreviewSection(
    title: String,
    modifier: Modifier = Modifier,
) {
    // TODO: wire to JournalViewModel once journal data model is defined
    Column(modifier = modifier.padding(SolaceSpacing.md)) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(SolaceSpacing.sm))
        Text(
            text = "Your journal entries will appear here.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun EmotionPromptSection(
    title: String,
    modifier: Modifier = Modifier,
) {
    // TODO: wire to EmotionViewModel once emotion data model is defined
    Column(modifier = modifier.padding(SolaceSpacing.md)) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(SolaceSpacing.sm))
        Text(
            text = "How are you feeling today?",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

@Preview(showBackground = true)
@Composable
private fun ConfigurableSectionPreview() {
    SolaceTheme {
        ConfigurableSection(
            config = HomeSectionConfig(
                id = "profile_feed",
                type = HomeSectionType.PROFILE_FEED,
                title = "Discover",
            ),
            profiles = listOf(
                UserProfile(
                    userId = "1",
                    username = "CalmWaves27",
                    avatarUrl = null,
                    moodColor = MoodColorKey.CALM_RELAXED,
                    moodLabel = "Calm and reflective",
                    bio = "There's a part of me that wants to stay in bed all day.",
                    vibes = listOf("Relationships", "Calm and reflective"),
                ),
            ),
        )
    }
}
