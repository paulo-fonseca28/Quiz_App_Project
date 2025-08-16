package com.app.quiz.data.repo

import com.app.quiz.data.model.QuizItem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = "quizzes" // ajuste se usar outro nome

    fun quizzesFlow(): Flow<List<QuizItem>> = callbackFlow {
        val reg = firestore.collection(collection)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList()); return@addSnapshotListener
                }
                val raw = snap?.documents.orEmpty().mapNotNull { d ->
                    try {
                        val title = d.getString("title") ?: "Sem título"
                        val description = d.getString("description") ?: ""
                        val difficulty = d.getString("difficulty")
                            ?: d.getString("dificculty") // tolera seu typo
                            ?: "Fácil"
                        val isActive = d.getBoolean("isActive") ?: true
                        val tls = (d.getLong("timeLimitSeconds") ?: 0L).toInt().takeIf { it > 0 }

                        val updatedAt = d.getTimestamp("updatedAt") ?: d.getString("updatedAt")?.let { str ->
                            try {
                                val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                                val date = formatter.parse(str)
                                date?.let { com.google.firebase.Timestamp(it) }
                            } catch (e: Exception) {
                                null
                            }
                        }

                        QuizItem(
                            id = d.id,
                            title = title,
                            description = description,
                            difficulty = difficulty,
                            isActive = isActive,
                            timeLimitSeconds = tls,
                            updatedAt = updatedAt
                        )
                    } catch (_: Exception) { null }
                }

                val items = raw
                    .filter { it.isActive }
                    .sortedWith(
                        compareByDescending<QuizItem> { it.updatedAt?.toDate()?.time ?: 0L }
                            .thenBy { it.title.lowercase() }
                    )

                trySend(items)
            }
        awaitClose { reg.remove() }
    }.catch { emit(emptyList()) }
}
