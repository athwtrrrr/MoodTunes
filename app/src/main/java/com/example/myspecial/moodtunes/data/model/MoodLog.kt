package com.example.myspecial.moodtunes.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity(tableName = "mood_logs")
data class MoodLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "mood")
    val mood: String,

    @ColumnInfo(name = "song_title")
    val songTitle: String,

    @ColumnInfo(name = "artist")
    val artist: String,

    @ColumnInfo(name = "album_cover_url")
    val albumCoverUrl: String? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
