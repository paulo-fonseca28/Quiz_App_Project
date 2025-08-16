package com.app.quiz.data.repo

import com.app.quiz.data.db.entities.SessionEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResultRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val fs: FirebaseFirestore
) {
    suspend fun submit(session: SessionEntity) {
        val user = auth.currentUser ?: throw IllegalStateException("Usuário não autenticado.")
        val uid = user.uid
        val quizId = session.quizId
        val newScore = session.score.coerceIn(0, 100) // 0..100

        val quizSnap = fs.collection("quizzes").document(quizId).get().await()
        val quizTitle = quizSnap.getString("title") ?: "Quiz"

        val data = hashMapOf(
            "uid" to uid,
            "quizId" to quizId,
            "quizTitle" to quizTitle,
            "correct" to session.correct,
            "total" to session.total,
            "score" to newScore,
            "durationSeconds" to (session.durationMs / 1000).toInt(),
            "finishedAt" to FieldValue.serverTimestamp()
        )
        fs.collection("sessions").add(data).await()

        val userDoc = fs.collection("users").document(uid).get().await()
        val displayName = userDoc.getString("displayName")
            ?: userDoc.getString("username")
            ?: userDoc.getString("nickname")
            ?: user.displayName
            ?: user.email?.substringBefore('@')
            ?: "Usuário"

        val lbRef = fs.collection("leaderboard").document(uid)

        fs.runTransaction { tx ->
            val snap = tx.get(lbRef)

            val bestMap: Map<String, Int> =
                (snap.get("best") as? Map<*, *>)?.mapNotNull { (k, v) ->
                    val key = k as? String ?: return@mapNotNull null
                    val value = when (v) {
                        is Long -> v.toInt()
                        is Int -> v
                        else -> null
                    } ?: return@mapNotNull null
                    key to value
                }?.toMap() ?: emptyMap()

            val prevBestOrNull = bestMap[quizId]
            val prevBest = prevBestOrNull ?: 0
            val improved = newScore > prevBest
            val delta = if (improved) newScore - prevBest else 0

            if (!snap.exists()) {
                val initialScore = if (newScore > 0) newScore else 0
                val initialQuizzes = if (newScore > 0) 1 else 0
                tx.set(
                    lbRef,
                    mapOf(
                        "displayName" to displayName,
                        "score" to initialScore,
                        "quizzes" to initialQuizzes, // conta quizzes com melhor > 0
                        "best" to mapOf(quizId to newScore),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
            } else {
                tx.update(
                    lbRef,
                    mapOf(
                        "displayName" to displayName,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                if (improved) {
                    tx.update(lbRef, "score", FieldValue.increment(delta.toLong()))
                    tx.update(lbRef, FieldPath.of("best", quizId), newScore)

                    if (prevBestOrNull == null && newScore > 0) {
                        tx.update(lbRef, "quizzes", FieldValue.increment(1))
                    }
                }
            }

            return@runTransaction null
        }.await()
    }
}
