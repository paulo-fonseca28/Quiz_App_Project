package com.app.quiz.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.quiz.data.db.entities.QuestionEntity

@Dao
interface QuestionDao {
    @Query("SELECT * FROM question WHERE quizId = :quizId ORDER BY updatedAt")
    suspend fun getByQuiz(quizId: String): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(questions: List<QuestionEntity>)

    @Query("DELETE FROM question WHERE quizId = :quizId")
    suspend fun deleteByQuiz(quizId: String)
}
