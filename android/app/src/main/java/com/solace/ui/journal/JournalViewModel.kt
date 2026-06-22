package com.solace.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solace.data.journal.JournalEntry
import com.solace.data.journal.JournalRepository
import com.solace.ui.config.MoodColorKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JournalUiState(
    val entries: List<JournalEntry> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showCompose: Boolean = false,
    val draftEmoji: String = "😐",
    val draftText: String = "",
    val draftMoodColor: String = MoodColorKey.CALM_RELAXED.name,
    val error: String? = null,
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repo: JournalRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(JournalUiState())
    val state: StateFlow<JournalUiState> = _state.asStateFlow()

    private var currentUid = ""

    fun init(uid: String) {
        if (currentUid == uid) return
        currentUid = uid
        viewModelScope.launch {
            repo.getEntries(uid).collect { entries ->
                _state.update { it.copy(entries = entries, isLoading = false) }
            }
        }
    }

    fun openCompose() = _state.update {
        it.copy(showCompose = true, draftEmoji = "😐", draftText = "")
    }

    fun dismissCompose() = _state.update { it.copy(showCompose = false) }

    fun setDraftEmoji(emoji: String) = _state.update { it.copy(draftEmoji = emoji) }
    fun setDraftText(text: String) = _state.update { it.copy(draftText = text) }

    fun saveEntry() {
        val s = _state.value
        if (s.draftText.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            repo.addEntry(currentUid, s.draftEmoji, s.draftText.trim(), s.draftMoodColor)
                .onSuccess { _state.update { it.copy(showCompose = false, isSaving = false) } }
                .onFailure { e -> _state.update { it.copy(isSaving = false, error = e.message) } }
        }
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            repo.deleteEntry(currentUid, entryId)
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
