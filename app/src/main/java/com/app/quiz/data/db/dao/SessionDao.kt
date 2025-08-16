package com.app.quiz.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.quiz.data.db.entities.SessionEntity

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(e: SessionEntity)

    @Query("SELECT * FROM sessions WHERE uid = :uid ORDER BY finishedAt DESC")
    fun observeByUser(uid: String): kotlinx.coroutines.flow.Flow<List<SessionEntity>>
}

