package com.solace.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solace.data.user.UserRepository
import com.solace.ui.config.MoodColorKey
import com.solace.ui.config.UserProfile
import com.solace.ui.onboarding.OnboardingAnswers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileCreationState(
    val photoUri: Uri? = null,
    val isUploading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ProfileCardCreationViewModel @Inject constructor(
    private val userRepo: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileCreationState())
    val state: StateFlow<ProfileCreationState> = _state.asStateFlow()

    fun setPhoto(uri: Uri) = _state.update { it.copy(photoUri = uri) }

    fun clearError() = _state.update { it.copy(error = null) }

    fun publishCard(uid: String, answers: OnboardingAnswers) {
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true, error = null) }
            try {
                val avatarUrl = _state.value.photoUri?.let { uri ->
                    userRepo.uploadAvatar(uid, uri).getOrThrow()
                }

                val profile = buildProfile(uid, answers, avatarUrl)
                userRepo.saveProfile(uid, profile).getOrThrow()
                _state.update { it.copy(isSaved = true, isUploading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isUploading = false, error = e.message) }
            }
        }
    }

    private fun buildProfile(uid: String, a: OnboardingAnswers, avatarUrl: String?): UserProfile {
        val moodEntry = DefaultMoodPalette.firstOrNull { it.key == a.moodColor }
        return UserProfile(
            userId = uid,
            username = a.username.ifBlank { "Anonymous" },
            avatarUrl = avatarUrl,
            moodColor = a.moodColor,
            moodLabel = moodEntry?.label ?: a.moodColor.name.replace('_', ' ').lowercase()
                .replaceFirstChar { it.uppercase() },
            bio = a.feelingSentence,
            vibes = a.vibes,
        )
    }
}
