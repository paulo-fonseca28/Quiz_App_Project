package com.app.quiz.data.db

import androidx.room.TypeConverter
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun listToJson(value: List<String>): String = JSONArray(value).toString()

    @TypeConverter
    fun jsonToList(value: String): List<String> {
        val arr = JSONArray(value)
        return (0 until arr.length()).map { arr.getString(it) }
    }
}
