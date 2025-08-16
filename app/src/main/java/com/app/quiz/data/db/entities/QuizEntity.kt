package com.app.quiz.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz")
data class QuizEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val updatedAt: Long,
    val isActive: Boolean = true
)
