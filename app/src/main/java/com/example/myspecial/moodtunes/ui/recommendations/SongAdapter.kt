package com.example.myspecial.moodtunes.ui.recommendations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myspecial.moodtunes.R
import com.example.myspecial.moodtunes.data.repository.Song
import com.example.myspecial.moodtunes.viewmodel.SharedViewModel

class SongAdapter(
    private var songs: List<Song>,
    private val onSongSelected: (Song) -> Unit,
    private val viewModel: SharedViewModel
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private var selectedPosition = -1

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumCover: ImageView = itemView.findViewById(R.id.ivAlbumCover)
        val songTitle: TextView = itemView.findViewById(R.id.tvSongTitle)
        val artist: TextView = itemView.findViewById(R.id.tvArtist)
        val selectedIcon: ImageView = itemView.findViewById(R.id.ivSelected)
        val playButton: ImageView = itemView.findViewById(R.id.ivPlayButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]

        holder.songTitle.text = song.title
        holder.artist.text = song.artist

        // Set placeholder first
        holder.albumCover.setImageResource(R.drawable.ic_music_note)

        // Use ViewModel to load album cover
        if (!song.albumCoverUrl.isNullOrEmpty()) {
            viewModel.loadImageForView(song.albumCoverUrl, holder.albumCover)
        }

        // Show selection state
        val isSelected = position == selectedPosition
        holder.selectedIcon.setImageResource(
            if (isSelected) android.R.drawable.checkbox_on_background
            else android.R.drawable.checkbox_off_background
        )

        // DEBUG: Check all song data
        println("DEBUG Song Data for '${song.title}':")
        println("  - ID: ${song.id}")
        println("  - Title: ${song.title}")
        println("  - Artist: ${song.artist}")
        println("  - Preview URL: ${song.previewUrl}")
        println("  - Album Cover URL: ${song.albumCoverUrl}")

        // Handle play button visibility and state using ViewModel's state
        val hasPreview = !song.previewUrl.isNullOrEmpty()
        holder.playButton.visibility = if (hasPreview) View.VISIBLE else View.GONE

        // Observe the ViewModel's playback state
        val currentlyPlayingSong = viewModel.currentlyPlayingSong.value
        val isPlaying = viewModel.isPlaying.value ?: false

        val isThisSongPlaying = currentlyPlayingSong?.id == song.id

        // Set play/pause icon based on ViewModel state
        val playIcon = if (isThisSongPlaying && isPlaying) {
            R.drawable.ic_pause
        } else {
            R.drawable.ic_play
        }
        holder.playButton.setImageResource(playIcon)

        holder.itemView.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                selectedPosition = currentPosition
                onSongSelected(songs[currentPosition])
                notifyDataSetChanged()
            }
        }

        // Play button click listener - use ViewModel's toggle method
        holder.playButton.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                viewModel.togglePlayPause(songs[currentPosition])
                notifyDataSetChanged() // Refresh to show state change
            }
        }
    }

    override fun onViewRecycled(holder: SongViewHolder) {
        super.onViewRecycled(holder)
        viewModel.cancelImageLoading(holder.albumCover)
    }

    override fun getItemCount(): Int = songs.size

    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        selectedPosition = -1
        notifyDataSetChanged()
    }

    fun getSelectedSong(): Song? {
        return if (selectedPosition != -1) songs[selectedPosition] else null
    }
}