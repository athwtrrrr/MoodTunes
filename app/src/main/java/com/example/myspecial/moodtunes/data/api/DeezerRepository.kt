package com.example.myspecial.moodtunes.data.api

import android.util.Log
import com.example.myspecial.moodtunes.data.repository.Song

class DeezerRepository {
    private val apiService = DeezerService.createApiService()

    suspend fun getSongsByMood(mood: String): List<Song> {
        return try {
            // Use mood-specific trending searches that combine mood + trending indicators
            val songs = getMoodGenreTrending(mood)

            Log.d("DeezerRepository", "Retrieved ${songs.size} mood-specific songs for: $mood")
            songs
        } catch (e: Exception) {
            Log.e("DeezerRepository", "Error fetching mood songs: ${e.message}", e)
            emptyList()
        }
    }



//     Use genre charts that match the mood
    private suspend fun getMoodGenreTrending(mood: String): List<Song> {
        return try {
            val genreId = getGenreIdByMood(mood)
            Log.d("DeezerRepository", "Getting genre chart for mood: $mood (genre: $genreId)")
            val response = apiService.getGenreChart(genreId = genreId, limit = 15)
            response.tracks?.data?.map { mapToSong(it) } ?: emptyList()
        } catch (e: Exception) {
            Log.e("DeezerRepository", "Error fetching mood genre: ${e.message}", e)
            emptyList()
        }
    }





    private fun getGenreIdByMood(mood: String): Int {
        return when (mood.toLowerCase()) {
            // HAPPY: Pop
            "happy" -> 132

            // SAD: New Age
            "sad" -> 85

            // CALM: Lo-fi
            "calm" -> 84

            // ENERGETIC: Disco
            "energetic" -> 169

            // ANGRY: Rock
            "angry" -> 152

            // FOCUSED: Classical
            "focused" -> 129

            // ROMANTIC: R&B
            "romantic" -> 144

            else -> 132
        }
    }


    private fun mapToSong(deezerTrack: DeezerTrack): Song {
        return Song(
            id = deezerTrack.id.toString(),
            title = deezerTrack.title,
            artist = deezerTrack.artist.name,
            albumCoverUrl = deezerTrack.album.cover_medium,
            previewUrl = deezerTrack.preview,
            externalUrl = deezerTrack.link
        )
    }
}