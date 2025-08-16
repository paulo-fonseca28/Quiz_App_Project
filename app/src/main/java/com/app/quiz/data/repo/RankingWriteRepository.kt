package com.app.quiz.data.repo

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RankingWriteRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun bumpScore(
        uid: String,
        displayName: String,
        addScore: Int,
        quizzesInc: Int = 1,
        photoUrl: String? = null
    ) {
        val data = mutableMapOf<String, Any>(
            "displayName" to displayName,
            "score" to FieldValue.increment(addScore.toLong()),
            "quizzes" to FieldValue.increment(quizzesInc.toLong())
        )
        if (!photoUrl.isNullOrBlank()) data["photoUrl"] = photoUrl

        firestore.collection("leaderboard")
            .document(uid)
            .set(data, SetOptions.merge())
            .await()
    }
}
