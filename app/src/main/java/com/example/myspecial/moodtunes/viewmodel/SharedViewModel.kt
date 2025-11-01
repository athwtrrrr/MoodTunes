package com.example.myspecial.moodtunes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myspecial.moodtunes.data.local.AppDatabase
import com.example.myspecial.moodtunes.data.local.MoodLogDao
import com.example.myspecial.moodtunes.data.model.MoodLog
import com.example.myspecial.moodtunes.data.repository.MusicRepository
import com.example.myspecial.moodtunes.data.repository.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import java.net.URL
import com.example.myspecial.moodtunes.R

class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private val musicRepository = MusicRepository()
    private val moodLogDao: MoodLogDao = AppDatabase.getInstance(application).moodLogDao()

    // Mood and song recommendation flows
    private val _selectedMood = MutableStateFlow<String?>(null)
    val selectedMood: StateFlow<String?> = _selectedMood.asStateFlow()

    private val _recommendedSongs = MutableStateFlow<List<Song>>(emptyList())
    val recommendedSongs: StateFlow<List<Song>> = _recommendedSongs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Playback state flows
    private val _currentlyPlayingSong = MutableStateFlow<Song?>(null)
    val currentlyPlayingSong: StateFlow<Song?> = _currentlyPlayingSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Mood logs filtering
    private val _currentFilter = MutableStateFlow("All")
    val currentFilter: StateFlow<String> = _currentFilter.asStateFlow()

    // Combine flows for filtered mood logs
    val filteredMoodLogs = _currentFilter.combine(moodLogDao.getAllMoodLogs()) { filter, allLogs ->
        when (filter) {
            "All" -> allLogs
            else -> allLogs.filter { it.mood == filter }
        }
    }

    private val imageLoadingJobs = mutableMapOf<String, kotlinx.coroutines.Job>()

    // Mood and song methods
    fun setSelectedMood(mood: String) {
        _selectedMood.value = mood
        fetchSongsForMood(mood)
    }

    private fun fetchSongsForMood(mood: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val songs = musicRepository.getSongsByMood(mood)
                _recommendedSongs.value = songs
            } catch (e: Exception) {
                _recommendedSongs.value = emptyList()
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Playback control methods
    fun togglePlayback(song: Song? = null) {
        if (song != null && _currentlyPlayingSong.value != song) {
            // New song selected
            _currentlyPlayingSong.value = song
            _isPlaying.value = true
        } else if (_currentlyPlayingSong.value != null) {
            // Toggle current song
            _isPlaying.value = !_isPlaying.value
        } else if (song != null) {
            // Start playing a new song
            _currentlyPlayingSong.value = song
            _isPlaying.value = true
        }
    }

    fun playSong(song: Song) {
        _currentlyPlayingSong.value = song
        _isPlaying.value = true
    }

    fun pausePlayback() {
        _isPlaying.value = false
    }

    fun resumePlayback() {
        if (_currentlyPlayingSong.value != null) {
            _isPlaying.value = true
        }
    }

    fun stopPlayback() {
        _isPlaying.value = false
        _currentlyPlayingSong.value = null
    }

    fun onPlaybackCompleted() {
        _isPlaying.value = false
        // Keep the current song reference for potential replay
    }

    fun onPlaybackError() {
        _isPlaying.value = false
        // Optionally clear the current song on error
        _currentlyPlayingSong.value = null
    }

    fun clearSongs() {
        _recommendedSongs.value = emptyList()
        stopPlayback()
    }

    // Database CRUD operations
    fun saveMoodLog(mood: String, songTitle: String, artist: String, albumCoverUrl: String?, note: String = "") {
        viewModelScope.launch {
            try {
                val moodLog = MoodLog(
                    mood = mood,
                    songTitle = songTitle,
                    artist = artist,
                    albumCoverUrl = albumCoverUrl,
                    note = note
                )
                moodLogDao.insertMoodLog(moodLog)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateMoodLogNote(moodLog: MoodLog, newNote: String) {
        viewModelScope.launch {
            try {
                val updatedMoodLog = moodLog.copy(note = newNote)
                moodLogDao.updateMoodLog(updatedMoodLog)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteMoodLog(moodLog: MoodLog) {
        viewModelScope.launch {
            try {
                moodLogDao.deleteMoodLog(moodLog)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Filter mood logs by mood type
    fun filterMoodLogs(moodType: String) {
        _currentFilter.value = moodType
    }

    // Get stats for display
    fun getMoodStats(moodLogs: List<MoodLog>): Map<String, Int> {
        return moodLogs.groupingBy { it.mood }.eachCount()
    }

    // Image loading methods
    fun loadImageForView(imageUrl: String, imageView: ImageView) {
        val viewId = imageView.hashCode().toString()
        imageLoadingJobs[viewId]?.cancel()

        val job = viewModelScope.launch {
            try {
                val bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    loadImageBitmap(imageUrl)
                }

                if (bitmap != null) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        imageView.setImageBitmap(bitmap)
                    }
                } else {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        imageView.setImageResource(R.drawable.ic_music_note)
                    }
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    imageView.setImageResource(R.drawable.ic_music_note)
                }
            }
        }

        imageLoadingJobs[viewId] = job
        job.invokeOnCompletion { imageLoadingJobs.remove(viewId) }
    }

    fun cancelImageLoading(imageView: ImageView) {
        val viewId = imageView.hashCode().toString()
        imageLoadingJobs[viewId]?.cancel()
        imageLoadingJobs.remove(viewId)
    }

    private suspend fun loadImageBitmap(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection().apply {
                connectTimeout = 10000
                readTimeout = 10000
                doInput = true
                connect()
            }

            val inputStream = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        imageLoadingJobs.values.forEach { it.cancel() }
        imageLoadingJobs.clear()
    }
}