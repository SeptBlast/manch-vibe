package com.solace.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solace.data.config.RemoteConfigRepository
import com.solace.data.user.UserRepository
import com.solace.ui.config.HomeScreenConfig
import com.solace.ui.config.SolaceUiConfig
import com.solace.ui.config.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val uiConfig: SolaceUiConfig = SolaceUiConfig(),
    val profiles: List<UserProfile> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val configRepo: RemoteConfigRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
        observeProfiles()
    }

    private fun loadConfig() {
        viewModelScope.launch {
            val config = configRepo.fetchUiConfig()
            _uiState.update { it.copy(uiConfig = config) }
        }
    }

    private fun observeProfiles() {
        viewModelScope.launch {
            userRepo.getProfiles()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { profiles ->
                    _uiState.update { it.copy(profiles = profiles, isLoading = false) }
                }
        }
    }

    fun sendLike(currentUid: String, targetUid: String) {
        viewModelScope.launch {
            userRepo.sendLike(fromUid = currentUid, toUid = targetUid)
        }
    }

    fun dismissError() = _uiState.update { it.copy(error = null) }
}
