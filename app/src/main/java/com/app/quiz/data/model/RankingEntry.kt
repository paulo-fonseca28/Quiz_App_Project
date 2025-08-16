package com.app.quiz.data.model

data class RankingEntry(
    val uid: String,
    val displayName: String,
    val score: Int,
    val rank: Int,
    val quizzes: Int = 0,
    val photoUrl: String? = null
)
