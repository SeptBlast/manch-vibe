package com.solace.ui.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.solace.data.chat.ChatMessage
import com.solace.ui.config.UserProfile
import com.solace.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatScreen(
    currentUid: String,
    vm: ChatViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(currentUid) { vm.init(currentUid) }

    // Handle back press from thread
    val partner = state.activeChatPartner

    AnimatedContent(
        targetState = partner,
        transitionSpec = {
            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
        },
        label = "chat_nav",
    ) { activePartner ->
        if (activePartner != null) {
            ChatThreadScreen(
                currentUid = currentUid,
                partner = activePartner,
                messages = state.messages,
                isLoading = state.isLoadingMessages,
                isSending = state.isSending,
                onSend = vm::sendMessage,
                onBack = vm::closeChat,
            )
        } else {
            ChatListScreen(
                previews = state.previews,
                onOpenChat = { vm.openChat(it) },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Chat list — all conversations
// ---------------------------------------------------------------------------

@Composable
private fun ChatListScreen(
    previews: List<ChatPreviewUiItem>,
    onOpenChat: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        // Top bar
        Surface(shadowElevation = 2.dp, color = Color.White) {
            Text(
                text = "Messages",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SolaceSpacing.lg, vertical = SolaceSpacing.md),
            )
        }

        if (previews.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No messages yet", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                    Spacer(Modifier.height(SolaceSpacing.xs))
                    Text("Connect with someone to start chatting", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(previews, key = { it.chatId }) { item ->
                    ChatPreviewRow(item = item, onClick = { onOpenChat(item.otherUserId) })
                    HorizontalDivider(modifier = Modifier.padding(start = 72.dp), color = Divider)
                }
            }
        }
    }
}

@Composable
private fun ChatPreviewRow(item: ChatPreviewUiItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SolaceSpacing.md, vertical = SolaceSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.md),
    ) {
        // Avatar
        AvatarCircle(avatarUrl = item.otherAvatarUrl, username = item.otherUsername, size = 48.dp)

        // Text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.otherUsername,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
            )
            Text(
                text = item.lastMessage,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Timestamp
        Text(
            text = formatTimestamp(item.lastMessageAt.toDate()),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
    }
}

// ---------------------------------------------------------------------------
// Chat thread — single conversation
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatThreadScreen(
    currentUid: String,
    partner: UserProfile,
    messages: List<ChatMessage>,
    isLoading: Boolean,
    isSending: Boolean,
    onSend: (String) -> Unit,
    onBack: () -> Unit,
) {
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.sm),
                    ) {
                        AvatarCircle(avatarUrl = partner.avatarUrl, username = partner.username, size = 36.dp)
                        Text(partner.username, style = MaterialTheme.typography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        bottomBar = {
            ComposerBar(
                draft = draft,
                onDraftChange = { draft = it },
                onSend = {
                    onSend(draft)
                    draft = ""
                },
                isSending = isSending,
            )
        },
        containerColor = BackgroundLight,
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SolaceTeal)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(SolaceSpacing.md),
                verticalArrangement = Arrangement.spacedBy(SolaceSpacing.sm),
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(msg = msg, isMine = msg.senderId == currentUid)
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage, isMine: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp,
            ),
            color = if (isMine) SolaceTeal else Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp),
        ) {
            Text(
                text = msg.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = SolaceSpacing.md, vertical = SolaceSpacing.sm),
            )
        }
    }
}

@Composable
private fun ComposerBar(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
) {
    Surface(shadowElevation = 8.dp, color = Color.White) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = SolaceSpacing.md, vertical = SolaceSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SolaceSpacing.sm),
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                placeholder = { Text("Write a message…", color = TextSecondary) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SolaceTeal,
                    unfocusedBorderColor = Divider,
                ),
            )

            IconButton(
                onClick = onSend,
                enabled = draft.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (draft.isNotBlank()) SolaceTeal else ChipUnselectedBg),
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (draft.isNotBlank()) Color.White else TextSecondary,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Shared helpers
// ---------------------------------------------------------------------------

@Composable
private fun AvatarCircle(avatarUrl: String?, username: String, size: androidx.compose.ui.unit.Dp) {
    if (avatarUrl != null) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size).clip(CircleShape),
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(SolaceTeal.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = username.take(2).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = SolaceTeal,
            )
        }
    }
}

private fun formatTimestamp(date: java.util.Date): String {
    val now = java.util.Date()
    val diffMs = now.time - date.time
    val diffMin = diffMs / 60_000
    return when {
        diffMin < 1    -> "now"
        diffMin < 60   -> "${diffMin}m"
        diffMin < 1440 -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
        else           -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    }
}
