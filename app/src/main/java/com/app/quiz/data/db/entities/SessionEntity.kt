package com.app.quiz.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val uid: String,
    val quizId: String,
    val score: Int,
    val correct: Int,
    val total: Int,
    val durationMs: Long,
    val finishedAt: Long
)
