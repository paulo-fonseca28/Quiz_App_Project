package com.app.quiz.data.repo

import com.app.quiz.data.model.HistoryItem
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val fs: FirebaseFirestore
) {
    fun myHistoryFlow(): Flow<List<HistoryItem>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) { trySend(emptyList()); awaitClose { }; return@callbackFlow }

        val reg = fs.collection("sessions")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snap?.documents.orEmpty().mapNotNull { d ->
                    try {
                        HistoryItem(
                            id = d.id,
                            quizTitle = d.getString("quizTitle") ?: "Quiz",
                            correct = (d.getLong("correct") ?: 0L).toInt(),
                            total = (d.getLong("total") ?: 0L).toInt(),
                            score = (d.getLong("score") ?: 0L).toInt(),
                            finishedAt = d.getTimestamp("finishedAt") ?: d.getTimestamp("createdAt"),
                            durationSeconds = (d.getLong("durationSeconds") ?: 0L).toInt()
                        )
                    } catch (_: Exception) { null }
                }.sortedByDescending { it.finishedAt?.toDate()?.time ?: 0L }
                trySend(list)
            }
        awaitClose { reg.remove() }
    }.catch { emit(emptyList()) }
}
