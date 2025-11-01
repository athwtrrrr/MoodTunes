package com.example.myspecial.moodtunes.ui.recommendations

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myspecial.moodtunes.R
import com.example.myspecial.moodtunes.data.repository.Song
import com.example.myspecial.moodtunes.viewmodel.SharedViewModel
import android.media.AudioManager
import android.util.Log
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecommendationsFragment : Fragment() {

    private val viewModel: SharedViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvMoodTitle: TextView
    private lateinit var btnSaveToLog: Button
    private lateinit var adapter: SongAdapter

    private var mediaPlayer: MediaPlayer? = null
    private var selectedSong: Song? = null
    private var currentPreviewUrl: String? = null
    private var isMediaPlayerPrepared = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recommendations, container, false)

        recyclerView = view.findViewById(R.id.rvSongs)
        progressBar = view.findViewById(R.id.progressBar)
        tvMoodTitle = view.findViewById(R.id.tvMoodTitle)
        btnSaveToLog = view.findViewById(R.id.btnSaveToLog)

        // Initially hide save button until song is selected
        btnSaveToLog.visibility = View.GONE

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        // Set initial mood title
        viewModel.selectedMood.value?.let { mood ->
            tvMoodTitle.text = "Songs for $mood Mood"
        }
    }

    private fun setupRecyclerView() {
        adapter = SongAdapter(
            songs = emptyList(),
            onSongSelected = { song ->
                selectedSong = song
                btnSaveToLog.visibility = View.VISIBLE
            },
            viewModel = viewModel
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        // Observe songs list
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recommendedSongs.collectLatest { songs ->
                    Log.d("RecommendationsFragment", "Received ${songs.size} songs")
                    adapter.updateSongs(songs)
                }
            }
        }

        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collectLatest { isLoading ->
                    Log.d("RecommendationsFragment", "Loading state: $isLoading")
                    progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
                }
            }
        }

        // Observe selected mood
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedMood.collectLatest { mood ->
                    mood?.let {
                        tvMoodTitle.text = "Songs for $mood Mood"
                    }
                }
            }
        }

        // Observe playback state changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentlyPlayingSong.collectLatest { playingSong ->
                    Log.d("RecommendationsFragment", "Currently playing song changed: ${playingSong?.title}")
                    adapter.notifyDataSetChanged()

                    if (playingSong != null) {
                        handleSongChange(playingSong)
                    } else {
                        stopMediaPlayer()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isPlaying.collectLatest { isPlaying ->
                    Log.d("RecommendationsFragment", "IsPlaying state changed: $isPlaying")
                    adapter.notifyDataSetChanged()

                    // Only handle play/pause if we have a current song
                    if (viewModel.currentlyPlayingSong.value != null) {
                        handlePlayPauseState(isPlaying)
                    }
                }
            }
        }
    }

    private fun handleSongChange(song: Song) {
        val previewUrl = song.previewUrl
        if (previewUrl.isNullOrEmpty()) {
            Log.d("RecommendationsFragment", "No preview URL for song ${song.title}")
            Toast.makeText(requireContext(), "No preview available for this song", Toast.LENGTH_SHORT).show()
            return
        }

        // If it's a different song, reset the media player
        if (previewUrl != currentPreviewUrl) {
            stopMediaPlayer()
            currentPreviewUrl = previewUrl
            isMediaPlayerPrepared = false
            prepareMediaPlayer(previewUrl)
        } else {
            // Same song, just handle play/pause
            handlePlayPauseState(viewModel.isPlaying.value ?: false)
        }
    }

    private fun prepareMediaPlayer(previewUrl: String) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDataSource(previewUrl)
                setOnPreparedListener {
                    Log.d("RecommendationsFragment", "MediaPlayer prepared - starting playback")
                    isMediaPlayerPrepared = true
                    // Only start if we're supposed to be playing
                    if (viewModel.isPlaying.value == true) {
                        start()
                    }
                }
                setOnCompletionListener {
                    Log.d("RecommendationsFragment", "Playback completed")
                    isMediaPlayerPrepared = false
                    viewModel.onPlaybackCompleted()
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e("RecommendationsFragment", "MediaPlayer error: $what, $extra")
                    isMediaPlayerPrepared = false
                    viewModel.onPlaybackError()
                    Toast.makeText(requireContext(), "Playback error occurred", Toast.LENGTH_SHORT).show()
                    true
                }
                prepareAsync()
            }
            Log.d("RecommendationsFragment", "Started preparing MediaPlayer for: $previewUrl")
        } catch (e: Exception) {
            Log.e("RecommendationsFragment", "Error setting up MediaPlayer: ${e.message}")
            e.printStackTrace()
            isMediaPlayerPrepared = false
            viewModel.onPlaybackError()
            Toast.makeText(requireContext(), "Failed to prepare audio playback", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePlayPauseState(isPlaying: Boolean) {
        if (isPlaying) {
            // Only start if media player is prepared
            if (isMediaPlayerPrepared && mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
                Log.d("RecommendationsFragment", "MediaPlayer started")
            } else if (!isMediaPlayerPrepared) {
                Log.d("RecommendationsFragment", "MediaPlayer not prepared yet, waiting for preparation...")
            }
        } else {
            // Pause playback if playing
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                Log.d("RecommendationsFragment", "MediaPlayer paused")
            }
        }
    }

    private fun stopMediaPlayer() {
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
                Log.d("RecommendationsFragment", "MediaPlayer stopped and released")
            } catch (e: Exception) {
                Log.e("RecommendationsFragment", "Error stopping MediaPlayer: ${e.message}")
                try {
                    mp.release()
                } catch (releaseEx: Exception) {
                    Log.e("RecommendationsFragment", "Error releasing MediaPlayer: ${releaseEx.message}")
                }
            }
        }
        mediaPlayer = null
        currentPreviewUrl = null
        isMediaPlayerPrepared = false
    }

    private fun setupClickListeners() {
        btnSaveToLog.setOnClickListener {
            selectedSong?.let { song ->
                val mood = viewModel.selectedMood.value ?: "Unknown"

                Log.d("RecommendationsFragment", "Saving mood log: mood=$mood, song=${song.title}, artist=${song.artist}")

                viewModel.saveMoodLog(
                    mood = mood,
                    songTitle = song.title,
                    artist = song.artist,
                    albumCoverUrl = song.albumCoverUrl,
                    note = ""
                )

                // Show confirmation
                Toast.makeText(requireContext(), "Mood saved!", Toast.LENGTH_SHORT).show()

                // Navigate to history
                findNavController().navigate(R.id.moodHistoryFragment)
            } ?: run {
                Log.d("RecommendationsFragment", "No song selected when trying to save")
                Toast.makeText(requireContext(), "Please select a song first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopMediaPlayer()
        viewModel.clearSongs()
    }

    override fun onPause() {
        super.onPause()
        // Pause playback when fragment is not visible
        mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        // Resume playback if it was playing
        if (viewModel.isPlaying.value == true && isMediaPlayerPrepared) {
            mediaPlayer?.start()
        }
    }
}