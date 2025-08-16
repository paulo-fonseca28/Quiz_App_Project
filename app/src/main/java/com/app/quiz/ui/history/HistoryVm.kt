package com.app.quiz.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.quiz.data.model.HistoryItem
import com.app.quiz.data.repo.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val loading: Boolean = true,
    val items: List<HistoryItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HistoryVm @Inject constructor(
    private val repo: HistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state = _state.asStateFlow()

    init { observe() }

    fun observe() {
        viewModelScope.launch {
            repo.myHistoryFlow()
                .onStart { _state.value = HistoryUiState(loading = true) }
                .catch { e -> _state.value = HistoryUiState(loading = false, error = e.message) }
                .collect { list -> _state.value = HistoryUiState(loading = false, items = list) }
        }
    }

    fun retry() = observe()
}
