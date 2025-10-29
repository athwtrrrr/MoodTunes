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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myspecial.moodtunes.R
import com.example.myspecial.moodtunes.data.repository.Song
import com.example.myspecial.moodtunes.viewmodel.SharedViewModel
import android.media.AudioManager
class RecommendationsFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel
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

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

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
        viewModel.recommendedSongs.observe(viewLifecycleOwner) { songs ->
            println("DEBUG Fragment: Received ${songs.size} songs")
            adapter.updateSongs(songs)
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            println("DEBUG Fragment: Loading state: $isLoading")
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        // Observe selected mood
        viewModel.selectedMood.observe(viewLifecycleOwner) { mood ->
            mood?.let {
                tvMoodTitle.text = "Songs for $mood Mood"
            }
        }

        // Observe playback state changes
        viewModel.currentlyPlayingSong.observe(viewLifecycleOwner) { playingSong ->
            println("DEBUG: Currently playing song changed: ${playingSong?.title}")
            adapter.notifyDataSetChanged()

            if (playingSong != null) {
                handleSongChange(playingSong)
            } else {
                stopMediaPlayer()
            }
        }

        viewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            println("DEBUG: IsPlaying state changed: $isPlaying")
            adapter.notifyDataSetChanged()

            // Only handle play/pause if we have a current song
            if (viewModel.currentlyPlayingSong.value != null) {
                handlePlayPauseState(isPlaying)
            }
        }
    }

    private fun handleSongChange(song: Song) {
        val previewUrl = song.previewUrl
        if (previewUrl.isNullOrEmpty()) {
            println("DEBUG: No preview URL for song ${song.title}")
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
                setDataSource(previewUrl)
                setOnPreparedListener {
                    println("DEBUG: MediaPlayer prepared - starting playback")
                    isMediaPlayerPrepared = true
                    // Only start if we're supposed to be playing
                    if (viewModel.isPlaying.value == true) {
                        start()
                    }
                }
                setOnCompletionListener {
                    println("DEBUG: Playback completed")
                    isMediaPlayerPrepared = false
                    viewModel.onPlaybackCompleted()
                }
                setOnErrorListener { mp, what, extra ->
                    println("DEBUG: MediaPlayer error: $what, $extra")
                    isMediaPlayerPrepared = false
                    viewModel.onPlaybackError()
                    true
                }
                prepareAsync()
            }
            println("DEBUG: Started preparing MediaPlayer for: $previewUrl")
        } catch (e: Exception) {
            println("DEBUG: Error setting up MediaPlayer: ${e.message}")
            e.printStackTrace()
            isMediaPlayerPrepared = false
            viewModel.onPlaybackError()
        }
    }

    private fun handlePlayPauseState(isPlaying: Boolean) {
        if (isPlaying) {
            // Only start if media player is prepared
            if (isMediaPlayerPrepared && mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
                println("DEBUG: MediaPlayer started")
            } else if (!isMediaPlayerPrepared) {
                println("DEBUG: MediaPlayer not prepared yet, waiting for preparation...")
            }
        } else {
            // Pause playback if playing
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                println("DEBUG: MediaPlayer paused")
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
                println("DEBUG: MediaPlayer stopped and released")
            } catch (e: Exception) {
                println("DEBUG: Error stopping MediaPlayer: ${e.message}")
                try {
                    mp.release()
                } catch (releaseEx: Exception) {
                    println("DEBUG: Error releasing MediaPlayer: ${releaseEx.message}")
                }
            }
        }
        mediaPlayer = null
        currentPreviewUrl = null
        isMediaPlayerPrepared = false
    }

    // Helper method to check if MediaPlayer is in a prepared state
    private fun isMediaPlayerPrepared(mp: MediaPlayer): Boolean {
        return try {
            // Try to get current position - if it doesn't throw an exception,
            // the MediaPlayer is likely in a prepared state
            mp.currentPosition
            true
        } catch (e: Exception) {
            false
        }
    }


    private fun setupClickListeners() {
        btnSaveToLog.setOnClickListener {
            selectedSong?.let { song ->
                // TODO: Save to database
                requireActivity().onBackPressed()
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
        if (viewModel.isPlaying.value == true) {
            mediaPlayer?.start()
        }
    }
}