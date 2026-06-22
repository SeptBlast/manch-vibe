package com.solace.ui.emotion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solace.data.emotion.MoodLog
import com.solace.data.emotion.MoodRepository
import com.solace.ui.config.MoodColorKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmotionUiState(
    val logs: List<MoodLog> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val savedToday: Boolean = false,  // shows confirmation after logging
    val selectedEmoji: String = "😐",
    val selectedMoodKey: MoodColorKey = MoodColorKey.CALM_RELAXED,
    val note: String = "",
    val error: String? = null,
)

@HiltViewModel
class EmotionViewModel @Inject constructor(
    private val repo: MoodRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EmotionUiState())
    val state: StateFlow<EmotionUiState> = _state.asStateFlow()

    private var currentUid = ""

    fun init(uid: String) {
        if (currentUid == uid) return
        currentUid = uid
        viewModelScope.launch {
            repo.getLogs(uid).collect { logs ->
                _state.update { it.copy(logs = logs, isLoading = false) }
            }
        }
    }

    fun selectEmoji(emoji: String) = _state.update { it.copy(selectedEmoji = emoji) }
    fun selectMood(key: MoodColorKey) = _state.update { it.copy(selectedMoodKey = key) }
    fun setNote(note: String) = _state.update { it.copy(note = note) }
    fun clearError() = _state.update { it.copy(error = null) }
    fun dismissConfirmation() = _state.update { it.copy(savedToday = false) }

    fun logMood() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            repo.addLog(
                uid = currentUid,
                emoji = s.selectedEmoji,
                moodColorKey = s.selectedMoodKey.name,
                note = s.note.trim(),
            ).onSuccess {
                _state.update { it.copy(isSaving = false, savedToday = true, note = "") }
            }.onFailure { e ->
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
