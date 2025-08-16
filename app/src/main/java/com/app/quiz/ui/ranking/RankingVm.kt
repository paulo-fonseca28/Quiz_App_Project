package com.app.quiz.ui.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.quiz.data.model.RankingEntry
import com.app.quiz.data.repo.RankingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RankingUiState(
    val loading: Boolean = true,
    val items: List<RankingEntry> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class RankingVm @Inject constructor(
    private val repo: RankingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RankingUiState())
    val state = _state.asStateFlow()

    init { observe() }

    fun observe() {
        viewModelScope.launch {
            repo.leaderboardFlow()
                .onStart { _state.value = RankingUiState(loading = true) }
                .catch { e -> _state.value = RankingUiState(loading = false, error = e.message) }
                .collect { list -> _state.value = RankingUiState(loading = false, items = list) }
        }
    }

    fun retry() = observe()
}
