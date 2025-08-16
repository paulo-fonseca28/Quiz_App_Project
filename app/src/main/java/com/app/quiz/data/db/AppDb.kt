package com.app.quiz.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.quiz.data.db.dao.QuestionDao
import com.app.quiz.data.db.dao.QuizDao
import com.app.quiz.data.db.dao.SessionDao
import com.app.quiz.data.db.entities.QuestionEntity
import com.app.quiz.data.db.entities.QuizEntity
import com.app.quiz.data.db.entities.SessionEntity

@Database(
    entities = [
        SessionEntity::class,
        QuizEntity::class,
        QuestionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDb : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun quizDao(): QuizDao
    abstract fun questionDao(): QuestionDao
}
