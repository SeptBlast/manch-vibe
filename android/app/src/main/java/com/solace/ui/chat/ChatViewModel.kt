package com.solace.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solace.data.chat.ChatMessage
import com.solace.data.chat.ChatPreview
import com.solace.data.chat.ChatRepository
import com.solace.data.user.UserRepository
import com.solace.ui.config.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatPreviewUiItem(
    val chatId: String,
    val otherUserId: String,
    val otherUsername: String,
    val otherAvatarUrl: String?,
    val lastMessage: String,
    val lastMessageAt: com.google.firebase.Timestamp,
)

data class ChatUiState(
    val previews: List<ChatPreviewUiItem> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val activeChatPartner: UserProfile? = null,
    val isLoadingMessages: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepo: ChatRepository,
    private val userRepo: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var currentUid: String = ""

    fun init(uid: String) {
        if (currentUid == uid) return
        currentUid = uid
        observePreviews()
    }

    private fun observePreviews() {
        viewModelScope.launch {
            chatRepo.getChatPreviews(currentUid).collect { previews ->
                val items = previews.map { it.toUiItem() }
                _state.update { it.copy(previews = items) }
            }
        }
    }

    fun openChat(otherUserId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMessages = true) }
            val profile = userRepo.getProfile(otherUserId)
            _state.update { it.copy(activeChatPartner = profile) }
            chatRepo.getMessages(currentUid, otherUserId).collect { msgs ->
                _state.update { it.copy(messages = msgs, isLoadingMessages = false) }
            }
        }
    }

    fun closeChat() = _state.update { it.copy(activeChatPartner = null, messages = emptyList()) }

    fun sendMessage(text: String) {
        val partner = _state.value.activeChatPartner ?: return
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(isSending = true) }
            chatRepo.sendMessage(currentUid, partner.userId, trimmed)
            _state.update { it.copy(isSending = false) }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    private suspend fun ChatPreview.toUiItem(): ChatPreviewUiItem {
        val profile = userRepo.getProfile(otherUserId)
        return ChatPreviewUiItem(
            chatId = chatId,
            otherUserId = otherUserId,
            otherUsername = profile?.username ?: otherUserId,
            otherAvatarUrl = profile?.avatarUrl,
            lastMessage = lastMessage,
            lastMessageAt = lastMessageAt,
        )
    }
}
