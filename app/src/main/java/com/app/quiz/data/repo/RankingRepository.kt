package com.app.quiz.data.repo

import com.app.quiz.data.model.RankingEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RankingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun leaderboardFlow(limit: Int = 50): Flow<List<RankingEntry>> = callbackFlow {
        val reg = firestore.collection("leaderboard")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snap?.documents?.mapIndexed { idx, d ->
                    RankingEntry(
                        uid = d.id,
                        displayName = d.getString("displayName") ?: "Usuário",
                        score = (d.getLong("score") ?: 0L).toInt(),
                        rank = idx + 1,
                        quizzes = (d.getLong("quizzes") ?: 0L).toInt(),
                        photoUrl = d.getString("photoUrl")
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }.catch { emit(emptyList()) }
}
