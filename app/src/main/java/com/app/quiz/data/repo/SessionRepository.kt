package com.app.quiz.data.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.app.quiz.data.db.dao.SessionDao
import com.app.quiz.data.db.entities.SessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao,
    private val fs: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val profileRepo: ProfileRepository
) {
    fun observeMySessions(): Flow<List<SessionEntity>> =
        sessionDao.observeByUser(auth.currentUser?.uid ?: "")

    suspend fun saveResult(local: SessionEntity) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Usuário não autenticado.")

        // garante id e uid válidos
        val entity = local.copy(
            id = local.id.ifBlank { UUID.randomUUID().toString() },
            uid = uid
        )

        sessionDao.insert(entity)

        fs.collection("users").document(uid)
            .collection("sessions").document(entity.id)
            .set(entity).await()

        val nick = profileRepo.getPreferredName()
        fs.collection("leaderboard").document(entity.quizId)
            .set(mapOf("lastUpdated" to FieldValue.serverTimestamp()), SetOptions.merge()).await()
        fs.collection("leaderboard").document(entity.quizId)
            .collection("top").document(uid)
            .set(
                mapOf(
                    "uid" to uid,
                    "nickname" to nick,
                    "score" to entity.score,
                    "finishedAt" to entity.finishedAt
                ),
                SetOptions.merge()
            ).await()
    }
}
