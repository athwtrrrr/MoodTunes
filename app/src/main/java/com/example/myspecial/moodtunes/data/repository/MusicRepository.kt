package com.example.myspecial.moodtunes.data.repository

import com.example.myspecial.moodtunes.data.api.DeezerRepository


class MusicRepository {
    private val deezerRepository = DeezerRepository()

    suspend fun getSongsByMood(mood: String): List<Song> {
        println("DEBUG: Fetching songs from Deezer for mood: $mood")
        return deezerRepository.getSongsByMood(mood)
    }
}

// Simple Song data class for UI
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val albumCoverUrl: String? = null,
    val previewUrl: String? = null,  // For 30-second preview
    val externalUrl: String? = null   // Full track on Spotify
)