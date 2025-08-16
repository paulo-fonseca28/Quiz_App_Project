package com.app.quiz.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.quiz.data.repo.HistoryRepository
import com.app.quiz.data.repo.QuizRepository
import com.app.quiz.data.repo.RankingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val loading: Boolean = true,
    val displayName: String? = null,
    val activeQuizzes: Int = 0,
    val lastQuizTitle: String? = null,
    val lastScoreText: String? = null,
    val rankPosition: Int? = null,
    val error: String? = null
)

@HiltViewModel
class DashboardVm @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val quizRepo: QuizRepository,
    private val historyRepo: HistoryRepository,
    private val rankingRepo: RankingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init { observe() }

    private fun observe() {
        viewModelScope.launch {
            combine(
                quizRepo.quizzesFlow(),
                historyRepo.myHistoryFlow(),
                rankingRepo.leaderboardFlow(),
                userDisplayNameFlow()
            ) { quizzes, history, leaderboard, nameFromDb ->
                val user = auth.currentUser
                val name = nameFromDb
                    ?: user?.displayName
                    ?: user?.email?.substringBefore('@')
                val nameClean = name?.removePrefix("@")

                val activeCount = quizzes.count { it.isActive }

                val last = history.maxByOrNull { it.finishedAt?.toDate()?.time ?: 0L }
                val lastTitle = last?.quizTitle
                val lastScore = when {
                    last?.correct != null && last.total != null -> "${last.correct}/${last.total}"
                    last?.score != null -> "${last.score} pts"
                    else -> null
                }

                val myRank = leaderboard.indexOfFirst { it.uid == user?.uid }
                    .takeIf { it >= 0 }?.let { it + 1 }

                DashboardUiState(
                    loading = false,
                    displayName = nameClean,
                    activeQuizzes = activeCount,
                    lastQuizTitle = lastTitle,
                    lastScoreText = lastScore,
                    rankPosition = myRank,
                    error = null
                )
            }
                .onStart { _state.value = _state.value.copy(loading = true, error = null) }
                .catch { e -> _state.value = _state.value.copy(loading = false, error = e.message) }
                .collect { _state.value = it }
        }
    }

    private fun userDisplayNameFlow(): Flow<String?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val ref = firestore.collection("users").document(uid)
        val reg = ref.addSnapshotListener { snap, _ ->
            val name =
                snap?.getString("displayName")
                    ?: snap?.getString("username")
                    ?: snap?.getString("nickname")
            trySend(name)
        }
        awaitClose { reg.remove() }
    }.catch { emit(null) }
}
