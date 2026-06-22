package com.solace.ui.likes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.solace.ui.config.DefaultMoodPalette
import com.solace.ui.config.UserProfile
import com.solace.ui.theme.*

@Composable
fun LikesScreen(
    currentUid: String,
    onOpenChat: (String) -> Unit = {},
    vm: LikesViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(currentUid) { vm.init(currentUid) }

    val snackHost = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { snackHost.showSnackbar(it); vm.clearError() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackHost) },
        containerColor = BackgroundLight,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header
            Surface(shadowElevation = 2.dp, color = Color.White) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SolaceSpacing.lg, vertical = SolaceSpacing.md)) {
                    Text("Likes you", style = MaterialTheme.typography.headlineMedium)
                    if (!state.isLoading) {
                        Text(
                            text = "${state.likers.size} ${if (state.likers.size == 1) "person" else "people"} liked your profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
            }

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SolaceTeal)
                    }
                }
                state.likers.isEmpty() -> EmptyLikesState()
                else -> LikersGrid(
                    likers = state.likers,
                    likedBack = state.likedBack,
                    onLikeBack = vm::likeBack,
                    onConnect = onOpenChat,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Grid of liker cards
// ---------------------------------------------------------------------------

@Composable
private fun LikersGrid(
    likers: List<UserProfile>,
    likedBack: Set<String>,
    onLikeBack: (String) -> Unit,
    onConnect: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(SolaceSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(SolaceSpacing.sm),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(likers, key = { it.userId }) { profile ->
            LikerCard(
                profile = profile,
                isLikedBack = profile.userId in likedBack,
                onLikeBack = { onLikeBack(profile.userId) },
                onConnect = { onConnect(profile.userId) },
            )
        }
        // bottom padding
        item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(SolaceSpacing.md)) }
    }
}

@Composable
private fun LikerCard(
    profile: UserProfile,
    isLikedBack: Boolean,
    onLikeBack: () -> Unit,
    onConnect: () -> Unit,
) {
    val moodColor = DefaultMoodPalette.firstOrNull { it.key == profile.moodColor }?.color ?: SolaceTeal

    Card(
        shape = RoundedCornerShape(SolaceRadius.card),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            // Avatar
            Box {
                if (profile.avatarUrl != null) {
                    AsyncImage(
                        model = profile.avatarUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(topStart = SolaceRadius.card, topEnd = SolaceRadius.card)),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(moodColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = profile.username.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = moodColor,
                        )
                    }
                }

                // Mutual match badge
                if (isLikedBack) {
                    Surface(
                        shape = RoundedCornerShape(bottomEnd = SolaceRadius.card),
                        color = SolaceTeal,
                        modifier = Modifier.align(Alignment.TopStart),
                    ) {
                        Text(
                            text = "Match ✨",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = SolaceSpacing.sm, vertical = 3.dp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(SolaceSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Username
                Text(
                    text = profile.username,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Mood chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(moodColor))
                    Text(
                        text = profile.moodLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = moodColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.xs),
                ) {
                    // Like back
                    IconButton(
                        onClick = onLikeBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isLikedBack) SolaceTeal.copy(alpha = 0.1f) else ChipUnselectedBg),
                    ) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = "Like back",
                            tint = if (isLikedBack) SolaceTeal else TextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    // Connect / chat
                    Button(
                        onClick = onConnect,
                        modifier = Modifier.weight(1f).height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolaceTeal),
                        shape = RoundedCornerShape(SolaceRadius.button),
                        contentPadding = PaddingValues(horizontal = SolaceSpacing.sm),
                    ) {
                        Text(
                            text = "Connect",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Empty state
// ---------------------------------------------------------------------------

@Composable
private fun EmptyLikesState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SolaceSpacing.sm),
        ) {
            Text("💝", fontSize = 56.sp)
            Text("No likes yet", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
            Text(
                text = "Keep your profile active to attract connections",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}
