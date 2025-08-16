package com.app.quiz.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.quiz.data.db.entities.SessionEntity
import com.app.quiz.data.repo.ResultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultVm @Inject constructor(
    private val repo: ResultRepository
) : ViewModel() {
    fun submit(session: SessionEntity) {
        viewModelScope.launch {
            try { repo.submit(session) } catch (_: Exception) { /* você pode logar/avisar se quiser */ }
        }
    }
}
