package com.solace.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.solace.ui.chat.ChatScreen
import com.solace.ui.chat.ChatViewModel
import com.solace.ui.emotion.EmotionScreen
import com.solace.ui.journal.JournalScreen
import com.solace.ui.likes.LikesScreen
import com.solace.ui.config.*
import com.solace.ui.theme.*

/**
 * Root home screen. Renders a bottom nav and delegates each tab's body
 * to [ConfigurableSection] driven by [HomeScreenConfig].
 *
 * Tab visibility and order are fully remote-controlled:
 * hidden tabs are simply filtered out, so the nav shrinks gracefully.
 */
@Composable
fun HomeScreen(
    config: HomeScreenConfig = HomeScreenConfig(),
    profiles: List<UserProfile> = emptyList(),
    currentUid: String = "",
) {
    val visibleTabs = config.bottomNav.filter { it.visible }
    var selectedTabId by remember { mutableStateOf(visibleTabs.firstOrNull()?.id ?: "home") }

    Scaffold(
        bottomBar = {
            if (visibleTabs.isNotEmpty()) {
                SolaceBottomNav(
                    tabs = visibleTabs,
                    selectedId = selectedTabId,
                    onTabSelected = { selectedTabId = it },
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTabId) {
                "home" -> HomeTabContent(config = config, profiles = profiles)
                "journal" -> JournalTabContent(currentUid = currentUid)
                "emotion" -> EmotionTabContent(currentUid = currentUid)
                "likes" -> LikesTabContent(currentUid = currentUid)
                "chat" -> ChatTabContent(currentUid = currentUid)
                else -> HomeTabContent(config = config, profiles = profiles)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Tab bodies
// ---------------------------------------------------------------------------

@Composable
private fun HomeTabContent(
    config: HomeScreenConfig,
    profiles: List<UserProfile>,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        config.visibleSections.forEach { section ->
            com.solace.ui.config.ConfigurableSection(
                config = section,
                profiles = profiles,
            )
        }
    }
}

@Composable
private fun JournalTabContent(currentUid: String) {
    JournalScreen(currentUid = currentUid)
}

@Composable
private fun EmotionTabContent(currentUid: String) {
    EmotionScreen(currentUid = currentUid)
}

@Composable
private fun LikesTabContent(currentUid: String) {
    LikesScreen(currentUid = currentUid)
}

@Composable
private fun ChatTabContent(currentUid: String) {
    ChatScreen(currentUid = currentUid)
}

// ---------------------------------------------------------------------------
// Bottom navigation
// ---------------------------------------------------------------------------

@Composable
private fun SolaceBottomNav(
    tabs: List<BottomNavTabConfig>,
    selectedId: String,
    onTabSelected: (String) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = SolaceSpacing.xs,
    ) {
        tabs.forEach { tab ->
            val selected = tab.id == selectedId
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab.id) },
                icon = {
                    Icon(
                        imageVector = iconForTab(tab.icon),
                        contentDescription = tab.label,
                    )
                },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavActive,
                    selectedTextColor = NavActive,
                    indicatorColor = NavActive.copy(alpha = 0.12f),
                    unselectedIconColor = NavInactive,
                    unselectedTextColor = NavInactive,
                ),
            )
        }
    }
}

private fun iconForTab(iconName: String): ImageVector = when (iconName) {
    "home" -> Icons.Filled.Home
    "book_open" -> Icons.Outlined.Book
    "emoji_emotions" -> Icons.Outlined.Face
    "favorite" -> Icons.Outlined.FavoriteBorder
    "chat_bubble" -> Icons.Outlined.ChatBubbleOutline
    else -> Icons.Filled.Circle
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    SolaceTheme {
        HomeScreen(
            config = HomeScreenConfig(),
            profiles = listOf(
                UserProfile(
                    userId = "1",
                    username = "CalmWaves27",
                    avatarUrl = null,
                    moodColor = MoodColorKey.CALM_RELAXED,
                    moodLabel = "Calm and reflective",
                    bio = "There's a part of me that wants to stay in bed all day, but I'm making an effort.",
                    vibes = listOf("Relationships"),
                ),
                UserProfile(
                    userId = "2",
                    username = "kittycorner42",
                    avatarUrl = null,
                    moodColor = MoodColorKey.HAPPY_OPTIMISTIC,
                    moodLabel = "Happy & Optimistic",
                    bio = "Feeling good today!",
                    vibes = listOf("Calm and reflective"),
                ),
            ),
        )
    }
}

@Preview(showBackground = true, name = "Chat-only nav")
@Composable
private fun HomeScreenChatOnlyPreview() {
    SolaceTheme {
        HomeScreen(
            config = HomeScreenConfig(
                bottomNav = listOf(
                    BottomNavTabConfig(id = "home", label = "Home", icon = "home"),
                    BottomNavTabConfig(id = "chat", label = "Chat", icon = "chat_bubble"),
                ),
            ),
        )
    }
}
