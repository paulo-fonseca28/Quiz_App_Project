package com.app.quiz.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.quiz.data.model.QuizItem
import com.app.quiz.data.repo.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = true,
    val items: List<QuizItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeVm @Inject constructor(
    private val repo: QuizRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            repo.quizzesFlow()
                .onStart { _state.value = HomeUiState(loading = true) }
                .catch  { e -> _state.value = HomeUiState(loading = false, error = e.message) }
                .collect { list -> _state.value = HomeUiState(loading = false, items = list) }
        }
    }
}
