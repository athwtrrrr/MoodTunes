package com.example.myspecial.moodtunes.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myspecial.moodtunes.data.model.MoodLog
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodLogDao {
    @Query("SELECT * FROM mood_logs ORDER BY timestamp DESC")
    fun getAllMoodLogs(): Flow<List<MoodLog>>  // Changed to Flow

    @Insert
    suspend fun insertMoodLog(moodLog: MoodLog)

    @Update
    suspend fun updateMoodLog(moodLog: MoodLog)

    @Delete
    suspend fun deleteMoodLog(moodLog: MoodLog)

    @Query("SELECT * FROM mood_logs WHERE mood = :moodType ORDER BY timestamp DESC")
    fun getMoodLogsByType(moodType: String): Flow<List<MoodLog>>  // Changed to Flow

    // Keep non-Flow versions for compatibility if needed
    @Query("SELECT * FROM mood_logs ORDER BY timestamp DESC")
    suspend fun getAllMoodLogsNonLive(): List<MoodLog>

    @Query("SELECT * FROM mood_logs WHERE mood = :moodType ORDER BY timestamp DESC")
    suspend fun getMoodLogsByTypeNonLive(moodType: String): List<MoodLog>
}
