package com.example.myspecial.moodtunes.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myspecial.moodtunes.R
import com.example.myspecial.moodtunes.data.model.MoodLog
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*

class MoodHistoryAdapter(
    private val onFavoriteClick: (MoodLog) -> Unit,  // Changed from onEditClick
    private val onDeleteClick: (MoodLog) -> Unit,
    private val loadImage: (String?, android.widget.ImageView) -> Unit
) : ListAdapter<MoodLog, MoodHistoryAdapter.MoodHistoryViewHolder>(DiffCallback) {

    inner class MoodHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val albumCover: android.widget.ImageView = itemView.findViewById(R.id.ivAlbumCover)
        private val songTitle: TextView = itemView.findViewById(R.id.tvSongTitle)
        private val artist: TextView = itemView.findViewById(R.id.tvArtist)
        private val note: TextView = itemView.findViewById(R.id.tvNote)
        private val timestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(moodLog: MoodLog) {
            // Load album cover
            loadImage(moodLog.albumCoverUrl, albumCover)

            songTitle.text = moodLog.songTitle
            artist.text = moodLog.artist


            // Show note if exists
            if (moodLog.note.isNotEmpty()) {
                note.text = moodLog.note
                note.visibility = View.VISIBLE
            } else {
                note.visibility = View.GONE
            }

            // Format timestamp
            timestamp.text = formatTimestamp(moodLog.timestamp)

            // Set favorite button state
            if (moodLog.isFavorite) {
                btnFavorite.setImageResource(R.drawable.ic_heart_filled)
                btnFavorite.contentDescription = "Remove from favorites"
            } else {
                btnFavorite.setImageResource(R.drawable.ic_heart_outline)
                btnFavorite.contentDescription = "Add to favorites"
            }

            // Set click listeners
            btnFavorite.setOnClickListener { onFavoriteClick(moodLog) }
            btnDelete.setOnClickListener { onDeleteClick(moodLog) }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000} min ago"
                diff < 86400000 -> "${diff / 3600000} hours ago"
                else -> {
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    sdf.format(Date(timestamp))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_history, parent, false)
        return MoodHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MoodLog>() {
        override fun areItemsTheSame(oldItem: MoodLog, newItem: MoodLog): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MoodLog, newItem: MoodLog): Boolean {
            return oldItem == newItem
        }
    }
}
