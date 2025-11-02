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
import com.example.myspecial.moodtunes.data.model.MoodAnalysis
import com.example.myspecial.moodtunes.data.model.MoodPattern
import kotlinx.coroutines.flow.first

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


    private val _moodAnalysis = MutableStateFlow<MoodAnalysis?>(null)
    val moodAnalysis: StateFlow<MoodAnalysis?> = _moodAnalysis.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val imageLoadingJobs = mutableMapOf<String, kotlinx.coroutines.Job>()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow("newest") // newest, oldest, mood, favorites
    val sortOption: StateFlow<String> = _sortOption.asStateFlow()

    private val _selectedMoodForSort = MutableStateFlow<String?>(null)
    val selectedMoodForSort: StateFlow<String?> = _selectedMoodForSort.asStateFlow()

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

    // Update the filteredMoodLogs to handle the new sorting logic
    val filteredMoodLogs = combine(
        _searchQuery,
        _sortOption,
        _selectedMoodForSort,
        moodLogDao.getAllMoodLogs()
    ) { query, sort, moodFilter, allLogs ->
        var filtered = allLogs

        // Apply mood filter when sorting by mood
        if (sort == "mood" && moodFilter != null) {
            filtered = filtered.filter { it.mood.equals(moodFilter, ignoreCase = true) }
        }

        // Apply search filter
        if (query.isNotBlank()) {
            filtered = filtered.filter { moodLog ->
                moodLog.songTitle.contains(query, ignoreCase = true) ||
                        moodLog.artist.contains(query, ignoreCase = true) ||
                        moodLog.note.contains(query, ignoreCase = true)
            }
        }

        // Apply sorting
        filtered = when (sort) {
            "oldest" -> filtered.sortedBy { it.timestamp }
            "mood" -> {
                // When sorting by mood, show selected mood first, then sort others by timestamp
                if (moodFilter != null) {
                    filtered.sortedByDescending { it.timestamp } // Keep selected mood in time order
                } else {
                    filtered.sortedBy { it.mood } // Fallback: sort all by mood name
                }
            }
            "favorites" -> filtered.sortedByDescending { it.isFavorite }
            else -> filtered.sortedByDescending { it.timestamp } // newest first
        }

        filtered
    }

    // Add these methods to SharedViewModel class
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: String) {
        _sortOption.value = option
    }

    fun setSelectedMoodForSort(mood: String?) {
        _selectedMoodForSort.value = mood
    }

    // Method to get current sort display text
    fun getCurrentSortDisplay(): String {
        return when (_sortOption.value) {
            "newest" -> "All songs • Newest first"
            "oldest" -> "All songs • Oldest first"
            "favorites" -> "All songs • Favorites first"
            "mood" -> {
                val mood = _selectedMoodForSort.value
                if (mood != null) {
                    "$mood mood • Newest first"
                } else {
                    "All songs • By mood"
                }
            }
            else -> "All songs • Newest first"
        }
    }

    fun toggleFavorite(moodLog: MoodLog) {
        viewModelScope.launch {
            try {
                val updatedMoodLog = moodLog.copy(isFavorite = !moodLog.isFavorite)
                moodLogDao.updateMoodLog(updatedMoodLog)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

    // Simplified analysis method - no time range needed
    fun analyzeMoodPatterns() {
        _isAnalyzing.value = true
        viewModelScope.launch {
            try {
                val allLogs = moodLogDao.getAllMoodLogs().first()
                val analysis = performMoodAnalysis(allLogs)
                _moodAnalysis.value = analysis
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    private fun performMoodAnalysis(logs: List<MoodLog>): MoodAnalysis {
        if (logs.isEmpty()) {
            return createEmptyAnalysis()
        }

        // Calculate mood frequencies
        val moodFrequencies = logs.groupingBy { it.mood }.eachCount()
        val totalLogs = logs.size

        // Find dominant mood
        val dominantMood = moodFrequencies.maxByOrNull { it.value }?.key ?: "Unknown"
        val dominantCount = moodFrequencies[dominantMood] ?: 0
        val confidence = dominantCount.toFloat() / totalLogs

        // Generate patterns
        val moodPatterns = detectMoodPatterns(logs)

        // Generate insights
        val insights = generateInsights(moodFrequencies, dominantMood, totalLogs)

        // Generate recommendations
        val recommendations = generateRecommendations(dominantMood, moodPatterns, moodFrequencies)

        return MoodAnalysis(
            dominantMood = dominantMood,
            confidence = confidence,
            moodPatterns = moodPatterns,
            insights = insights,
            recommendations = recommendations,
            totalLogs = totalLogs,
            moodDistribution = moodFrequencies
        )
    }

    private fun createEmptyAnalysis(): MoodAnalysis {
        return MoodAnalysis(
            dominantMood = "No data yet",
            confidence = 0f,
            moodPatterns = emptyList(),
            insights = listOf("Start logging your moods to see insights!"),
            recommendations = listOf("Try logging songs for different moods to get personalized advice."),
            totalLogs = 0,
            moodDistribution = emptyMap()
        )
    }

    private fun detectMoodPatterns(logs: List<MoodLog>): List<MoodPattern> {
        val patterns = mutableListOf<MoodPattern>()

        // Pattern 1: Mood diversity
        val uniqueMoods = logs.distinctBy { it.mood }.size
        patterns.add(
            MoodPattern(
                patternType = "Mood Diversity",
                description = "You've experienced $uniqueMoods different moods",
                frequency = uniqueMoods
            )
        )

        // Pattern 2: Most common mood transitions (if we have enough data)
        if (logs.size >= 3) {
            val moodSequences = logs.windowed(2).map { pair ->
                "${pair[0].mood} → ${pair[1].mood}"
            }
            val sequenceFreq = moodSequences.groupingBy { it }.eachCount()
            val commonSequence = sequenceFreq.maxByOrNull { it.value }

            commonSequence?.let {
                patterns.add(
                    MoodPattern(
                        patternType = "Common Transition",
                        description = "You often go from ${it.key}",
                        frequency = it.value
                    )
                )
            }
        }

        // Pattern 3: Recent mood trend (last 5 entries)
        if (logs.size >= 5) {
            val recentMoods = logs.takeLast(5).map { it.mood }
            val recentTrend = if (recentMoods.distinct().size == 1) {
                "Consistently ${recentMoods.first()}"
            } else {
                "Varied recently"
            }
            patterns.add(
                MoodPattern(
                    patternType = "Recent Trend",
                    description = recentTrend,
                    frequency = recentMoods.size
                )
            )
        }

        return patterns
    }

    private fun generateInsights(
        moodFrequencies: Map<String, Int>,
        dominantMood: String,
        totalLogs: Int
    ): List<String> {
        val insights = mutableListOf<String>()

        if (totalLogs == 0) {
            return insights
        }

        val dominantPercentage = (moodFrequencies[dominantMood]?.toFloat() ?: 0f) / totalLogs * 100

        insights.add("Your most common mood is $dominantMood (${"%.0f".format(dominantPercentage)}% of your songs)")

        // Mood-specific insights - UPDATED TO MATCH YOUR ACTUAL MOODS
        when (dominantMood.toLowerCase()) {
            "happy" -> {
                insights.add("You gravitate towards uplifting and positive music")
                insights.add("Your music choices reflect an optimistic outlook")
            }
            "sad" -> {
                insights.add("You find comfort in reflective and emotional music")
                insights.add("Music helps you process deeper feelings")
            }
            "calm" -> {
                insights.add("You prefer soothing and tranquil sounds")
                insights.add("Your music creates a peaceful atmosphere")
            }
            "energetic" -> {
                insights.add("You thrive on high-energy and motivating tracks")
                insights.add("Your music fuels your active spirit")
            }
            "focused" -> {
                insights.add("You use music to enhance concentration")
                insights.add("Your selections help maintain mental clarity")
            }
            "angry" -> {
                insights.add("You use music to channel intense emotions")
                insights.add("Your music choices help you release tension")
            }
            "romantic" -> {
                insights.add("You enjoy music that connects with emotions and relationships")
                insights.add("Your music sets a romantic and intimate atmosphere")
            }
            else -> {
                insights.add("Your music preferences show a unique emotional pattern")
            }
        }

        // Diversity insight
        val moodCount = moodFrequencies.size
        when {
            moodCount == 1 -> insights.add("You have a very focused musical mood preference")
            moodCount <= 3 -> insights.add("You explore a balanced range of emotional states through music")
            else -> insights.add("You enjoy a wide spectrum of musical emotions")
        }

        // Balance insight
        val positiveMoods = listOf("happy", "calm", "energetic", "focused", "romantic")
        val negativeMoods = listOf("sad", "angry")

        val positiveCount = moodFrequencies.filterKeys { it in positiveMoods }.values.sum()
        val negativeCount = moodFrequencies.filterKeys { it in negativeMoods }.values.sum()

        when {
            positiveCount > negativeCount * 2 -> insights.add("Your music leans strongly towards positive emotions")
            negativeCount > positiveCount * 2 -> insights.add("Your music often reflects more intense emotions")
            positiveCount > negativeCount -> insights.add("Your music generally maintains a positive balance")
            else -> insights.add("Your music shows a mix of emotional intensities")
        }

        return insights
    }

    private fun generateRecommendations(
        dominantMood: String,
        patterns: List<MoodPattern>,
        moodDistribution: Map<String, Int>
    ): List<String> {
        val recommendations = mutableListOf<String>()

        // Mood-specific recommendations - UPDATED TO MATCH YOUR ACTUAL MOODS
        when (dominantMood.toLowerCase()) {
            "happy" -> {
                recommendations.add("Explore different genres that maintain positive energy")
                recommendations.add("Create a 'Happy Vibes' playlist for your best days")
                recommendations.add("Try sharing your favorite upbeat songs with friends")
            }
            "sad" -> {
                recommendations.add("Gradually add some uplifting songs to balance your mood")
                recommendations.add("Explore artists who transform emotions into beautiful art")
                recommendations.add("Try instrumental music for deep reflection")
            }
            "calm" -> {
                recommendations.add("Perfect for meditation, reading, or relaxing baths")
                recommendations.add("Explore ambient and nature sound combinations")
                recommendations.add("Try different cultural relaxation music traditions")
            }
            "energetic" -> {
                recommendations.add("Great for workouts, cleaning, or morning routines")
                recommendations.add("Create different energy-level playlists for various activities")
                recommendations.add("Explore new high-energy genres you haven't tried")
            }
            "focused" -> {
                recommendations.add("Ideal for work, study, or deep concentration sessions")
                recommendations.add("Try lo-fi, classical, or ambient music for focus")
                recommendations.add("Create a 'Deep Work' playlist with minimal vocals")
            }
            "angry" -> {
                recommendations.add("Try transitioning from high-intensity to calming music")
                recommendations.add("Explore music with strong beats and powerful lyrics")
                recommendations.add("Consider creating a 'Release' playlist for intense emotions")
            }
            "romantic" -> {
                recommendations.add("Create a romantic playlist for special moments")
                recommendations.add("Explore different languages of love songs")
                recommendations.add("Try classic love ballads and modern romantic tracks")
            }
            else -> {
                recommendations.add("Keep exploring different music to understand your patterns")
                recommendations.add("Try logging songs from various moods for better insights")
            }
        }

        // General recommendations based on patterns
        if (moodDistribution.size == 1) {
            recommendations.add("Try exploring a completely different mood to broaden your musical taste")
        }

        // Check if user tends to stay in intense moods
        val intenseMoods = listOf("angry", "sad")
        val hasIntensePattern = patterns.any { pattern ->
            intenseMoods.any { mood ->
                pattern.description.contains(mood, ignoreCase = true)
            }
        }

        if (hasIntensePattern) {
            recommendations.add("Consider creating playlists that gradually transition to lighter moods")
        }

        // Encourage exploration if user has limited mood variety
        if (moodDistribution.size < 3 && moodDistribution.values.sum() >= 5) {
            recommendations.add("You might enjoy exploring moods you haven't tried much, like ${getLeastUsedMood(moodDistribution)}")
        }

        return recommendations
    }

    // Helper function to find the least used mood
    private fun getLeastUsedMood(moodDistribution: Map<String, Int>): String {
        return moodDistribution.minByOrNull { it.value }?.key ?: "calm"
    }

    override fun onCleared() {
        super.onCleared()
        imageLoadingJobs.values.forEach { it.cancel() }
        imageLoadingJobs.clear()
    }
}