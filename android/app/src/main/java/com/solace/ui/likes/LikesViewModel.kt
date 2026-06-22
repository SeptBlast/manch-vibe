package com.solace.ui.likes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solace.data.user.UserRepository
import com.solace.ui.config.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LikesUiState(
    val likers: List<UserProfile> = emptyList(),
    val isLoading: Boolean = true,
    val likedBack: Set<String> = emptySet(),   // UIDs the current user has liked back
    val error: String? = null,
)

@HiltViewModel
class LikesViewModel @Inject constructor(
    private val userRepo: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LikesUiState())
    val state: StateFlow<LikesUiState> = _state.asStateFlow()

    private var currentUid = ""

    fun init(uid: String) {
        if (currentUid == uid) return
        currentUid = uid
        viewModelScope.launch {
            userRepo.getLikes(uid)
                .catch { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
                .collect { profiles ->
                    _state.update { it.copy(likers = profiles, isLoading = false) }
                }
        }
    }

    /** Like back a profile that liked you. */
    fun likeBack(targetUid: String) {
        viewModelScope.launch {
            userRepo.sendLike(fromUid = currentUid, toUid = targetUid)
                .onSuccess {
                    _state.update { it.copy(likedBack = it.likedBack + targetUid) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message) }
                }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
