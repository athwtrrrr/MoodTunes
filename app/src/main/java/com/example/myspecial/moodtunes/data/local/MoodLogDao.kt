package com.example.myspecial.moodtunes.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.myspecial.moodtunes.data.model.MoodLog

@Dao
interface MoodLogDao {
    @Query("SELECT * FROM mood_logs ORDER BY timestamp DESC")
    fun getAllMoodLogs(): LiveData<List<MoodLog>>

    @Insert
    suspend fun insertMoodLog(moodLog: MoodLog)

    @Delete
    suspend fun deleteMoodLog(moodLog: MoodLog)

    @Query("SELECT * FROM mood_logs WHERE mood = :moodType ORDER BY timestamp DESC")
    fun getMoodLogsByType(moodType: String): LiveData<List<MoodLog>>
}