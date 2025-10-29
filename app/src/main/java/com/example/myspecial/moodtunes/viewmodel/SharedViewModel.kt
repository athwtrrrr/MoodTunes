package com.example.myspecial.moodtunes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myspecial.moodtunes.data.repository.MusicRepository
import com.example.myspecial.moodtunes.data.repository.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Job
import androidx.lifecycle.asLiveData
import androidx.lifecycle.LiveData
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import java.net.URL
import com.example.myspecial.moodtunes.R
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import kotlinx.coroutines.delay

class SharedViewModel : ViewModel() {
    private val musicRepository = MusicRepository()

    private val _selectedMood = MutableStateFlow<String?>(null)
    val selectedMood: LiveData<String?> = _selectedMood.asLiveData()

    private val _recommendedSongs = MutableStateFlow<List<Song>>(emptyList())
    val recommendedSongs: LiveData<List<Song>> = _recommendedSongs.asLiveData()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: LiveData<Boolean> = _isLoading.asLiveData()

    fun prepareSong(song: Song) {
        val currentState = _playbackState.value ?: PlaybackState()
        _playbackState.value = currentState.copy(currentSong = song, isPlaying = false)
        println("DEBUG: Preparing song: ${song.title}")
    }

    // Combined playback state
    data class PlaybackState(
        val currentSong: Song? = null,
        val isPlaying: Boolean = false
    )

    private val _playbackState = MutableLiveData<PlaybackState>(PlaybackState())
    val playbackState: LiveData<PlaybackState> = _playbackState

    val currentlyPlayingSong = _playbackState.map { it.currentSong }
    val isPlaying = _playbackState.map { it.isPlaying }

    private val imageLoadingJobs = mutableMapOf<String, Job>()

    fun setSelectedMood(mood: String) {
        _selectedMood.value = mood
        fetchSongsForMood(mood)
    }

    private fun fetchSongsForMood(mood: String) {
        println("DEBUG: Fetching songs for mood: $mood")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val songs = musicRepository.getSongsByMood(mood)
                println("DEBUG: Retrieved ${songs.size} songs")
                _recommendedSongs.value = songs
            } catch (e: Exception) {
                println("DEBUG: Error fetching songs: ${e.message}")
                _recommendedSongs.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Playback control methods using combined state
    fun playSong(song: Song) {
        _playbackState.value = PlaybackState(song, true)
        println("DEBUG: Playing song: ${song.title}")
    }

    fun pauseSong() {
        val currentState = _playbackState.value ?: PlaybackState()
        _playbackState.value = currentState.copy(isPlaying = false)
        println("DEBUG: Playback paused")
    }

    fun resumeSong() {
        val currentState = _playbackState.value ?: PlaybackState()
        _playbackState.value = currentState.copy(isPlaying = true)
        println("DEBUG: Playback resumed")
    }

    fun stopSong() {
        _playbackState.value = PlaybackState()
        println("DEBUG: Playback stopped")
    }

    fun togglePlayPause(song: Song) {
        val currentState = _playbackState.value ?: PlaybackState()
        val isSameSong = currentState.currentSong?.id == song.id

        if (isSameSong && currentState.isPlaying) {
            // Pause current song
            _playbackState.value = currentState.copy(isPlaying = false)
            println("DEBUG: Pausing current song")
        } else if (isSameSong && !currentState.isPlaying) {
            // Resume current song
            _playbackState.value = currentState.copy(isPlaying = true)
            println("DEBUG: Resuming current song")
        } else {
            // New song - first set the song without playing, then start playing
            // This gives the fragment time to prepare the MediaPlayer
            _playbackState.value = PlaybackState(song, false)
            println("DEBUG: Setting new song: ${song.title}")

            // Start playing after a short delay to allow preparation
            viewModelScope.launch {
                delay(100) // Short delay to allow MediaPlayer preparation
                _playbackState.value = PlaybackState(song, true)
                println("DEBUG: Starting playback for: ${song.title}")
            }
        }
    }

    fun onPlaybackCompleted() {
        _playbackState.value = PlaybackState()
        println("DEBUG: Playback completed")
    }

    fun onPlaybackError() {
        _playbackState.value = PlaybackState()
        println("DEBUG: Playback error occurred")
    }

    // Improved image loading with better memory management
    fun loadImageForView(imageUrl: String, imageView: ImageView) {
        // Cancel any existing job for this ImageView
        val viewId = imageView.hashCode().toString()
        imageLoadingJobs[viewId]?.cancel()

        // Start new image loading job
        val job = viewModelScope.launch {
            try {
                println("DEBUG: Loading image from: $imageUrl")
                val bitmap = withContext(Dispatchers.IO) {
                    loadImageBitmap(imageUrl)
                }

                if (bitmap != null) {
                    println("DEBUG: Image loaded successfully")
                    // Set bitmap on main thread
                    withContext(Dispatchers.Main) {
                        imageView.setImageBitmap(bitmap)
                    }
                } else {
                    println("DEBUG: Failed to load image, using placeholder")
                    withContext(Dispatchers.Main) {
                        imageView.setImageResource(R.drawable.ic_music_note)
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Image loading error: ${e.message}")
                withContext(Dispatchers.Main) {
                    imageView.setImageResource(R.drawable.ic_music_note)
                }
            }
        }

        imageLoadingJobs[viewId] = job

        // Clean up job when it completes
        job.invokeOnCompletion {
            imageLoadingJobs.remove(viewId)
        }
    }

    // Cancel image loading for a specific ImageView
    fun cancelImageLoading(imageView: ImageView) {
        val viewId = imageView.hashCode().toString()
        imageLoadingJobs[viewId]?.cancel()
        imageLoadingJobs.remove(viewId)
    }

    private suspend fun loadImageBitmap(url: String): Bitmap? {
        return try {
            println("DEBUG: Downloading image from: $url")
            val connection = URL(url).openConnection().apply {
                connectTimeout = 10000
                readTimeout = 10000
                doInput = true
                connect()
            }

            val inputStream = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) {
                println("DEBUG: BitmapFactory returned null for URL: $url")
            }

            bitmap
        } catch (e: Exception) {
            println("DEBUG: Error loading image: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    fun clearSongs() {
        _recommendedSongs.value = emptyList()
        stopSong()
        // Cancel all image loading jobs
        imageLoadingJobs.values.forEach { it.cancel() }
        imageLoadingJobs.clear()
    }

    override fun onCleared() {
        super.onCleared()
        // Cancel all coroutines when ViewModel is cleared
        imageLoadingJobs.values.forEach { it.cancel() }
        imageLoadingJobs.clear()
    }
}