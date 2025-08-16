package com.app.quiz.data.db.entities

import androidx.room.Entity

@Entity(
    tableName = "question",
    primaryKeys = ["id", "quizId"]
)
data class QuestionEntity(
    val id: String,
    val quizId: String,
    val text: String,
    val optionsJson: String,
    val correctIndex: Int,
    val updatedAt: Long
)
