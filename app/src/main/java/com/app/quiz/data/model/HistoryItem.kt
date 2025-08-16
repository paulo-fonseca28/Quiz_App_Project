package com.app.quiz.data.model

import com.google.firebase.Timestamp

data class HistoryItem(
    val id: String,
    val quizTitle: String,
    val correct: Int?,
    val total: Int?,
    val score: Int?,
    val finishedAt: Timestamp?,
    val durationSeconds: Int? = null
)
