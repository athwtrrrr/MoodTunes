package com.example.myspecial.moodtunes.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myspecial.moodtunes.R
import com.example.myspecial.moodtunes.data.model.MoodLog
import java.text.SimpleDateFormat
import java.util.*

class MoodHistoryAdapter(
    private val onEditClick: (MoodLog) -> Unit,
    private val onDeleteClick: (MoodLog) -> Unit
) : ListAdapter<MoodLog, MoodHistoryAdapter.MoodHistoryViewHolder>(DiffCallback) {

    inner class MoodHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moodIcon: TextView = itemView.findViewById(R.id.tvMoodIcon)
        private val songTitle: TextView = itemView.findViewById(R.id.tvSongTitle)
        private val artist: TextView = itemView.findViewById(R.id.tvArtist)
        private val note: TextView = itemView.findViewById(R.id.tvNote)
        private val timestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(moodLog: MoodLog) {
            // Set mood icon
            moodIcon.text = when (moodLog.mood) {
                "Happy" -> "😊"
                "Sad" -> "😢"
                "Calm" -> "😌"
                "Energetic" -> "⚡"
                "Chill" -> "🌙"
                else -> "😊"
            }

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

            // Set click listeners
            btnEdit.setOnClickListener { onEditClick(moodLog) }
            btnDelete.setOnClickListener { onDeleteClick(moodLog) }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60000 -> "Just now" // Less than 1 minute
                diff < 3600000 -> "${diff / 60000} min ago" // Less than 1 hour
                diff < 86400000 -> "${diff / 3600000} hours ago" // Less than 1 day
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