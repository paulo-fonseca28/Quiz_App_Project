package com.app.quiz.data.model

import com.google.firebase.Timestamp

data class QuizItem(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: String,
    val isActive: Boolean,
    val timeLimitSeconds: Int?,
    val updatedAt: Timestamp? = null
)
