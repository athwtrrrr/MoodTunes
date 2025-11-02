
package com.example.myspecial.moodtunes.ui.history

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myspecial.moodtunes.R
import com.example.myspecial.moodtunes.data.model.MoodLog
import com.example.myspecial.moodtunes.viewmodel.SharedViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MoodHistoryFragment : Fragment() {

    private val viewModel: SharedViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MoodHistoryAdapter
    private lateinit var tvEmptyState: TextView
    private lateinit var btnAnalyze: MaterialButton
    private lateinit var etSearch: TextInputEditText
    private lateinit var btnSort: MaterialButton
    private lateinit var tvCurrentSort: TextView

    companion object {
        private const val TAG = "MoodHistoryFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupSearch()
        setupSortButton()
        setupObservers()

        Log.d(TAG, "Fragment created")
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.rvMoodHistory)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        btnAnalyze = view.findViewById(R.id.btnAnalyze)
        etSearch = view.findViewById(R.id.etSearch)
        btnSort = view.findViewById(R.id.btnSort)
        tvCurrentSort = view.findViewById(R.id.tvCurrentSort)

        btnAnalyze.setOnClickListener {
            findNavController().navigate(R.id.moodAnalysisFragment)
        }
    }

    private fun setupRecyclerView() {
        adapter = MoodHistoryAdapter(
            onFavoriteClick = { moodLog ->
                viewModel.toggleFavorite(moodLog)
            },
            onDeleteClick = { moodLog -> showDeleteConfirmation(moodLog) },
            loadImage = { url, imageView ->
                if (!url.isNullOrEmpty()) {
                    viewModel.loadImageForView(url, imageView)
                } else {
                    imageView.setImageResource(R.drawable.ic_music_note)
                }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        setupSwipeToDelete()
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSortButton() {
        btnSort.setOnClickListener {
            showSortOptionsDialog()
        }
    }

    private fun showSortOptionsDialog() {
        val sortOptions = arrayOf("Newest First", "Oldest First", "By Mood", "Favorites First")

        AlertDialog.Builder(requireContext())
            .setTitle("Sort By")
            .setItems(sortOptions) { _, which ->
                when (which) {
                    0 -> {
                        // Newest first - show all songs
                        viewModel.setSortOption("newest")
                        viewModel.setSelectedMoodForSort(null)
                    }
                    1 -> {
                        // Oldest first - show all songs
                        viewModel.setSortOption("oldest")
                        viewModel.setSelectedMoodForSort(null)
                    }
                    2 -> {
                        // By Mood - first ask which mood to filter by
                        showMoodSelectionDialog()
                    }
                    3 -> {
                        // Favorites first - show all songs
                        viewModel.setSortOption("favorites")
                        viewModel.setSelectedMoodForSort(null)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMoodSelectionDialog() {
        val moods = arrayOf(
            "😊 Happy",
            "😢 Sad",
            "😌 Calm",
            "⚡ Energetic",
            "🎯 Focused",
            "😠 Angry",
            "💖 Romantic"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Select Mood to Filter")
            .setItems(moods) { _, which ->
                val selectedMood = when (which) {
                    0 -> "Happy"
                    1 -> "Sad"
                    2 -> "Calm"
                    3 -> "Energetic"
                    4 -> "Focused"
                    5 -> "Angry"
                    6 -> "Romantic"
                    else -> null
                }
                // Set both sort option and selected mood
                viewModel.setSortOption("mood")
                viewModel.setSelectedMoodForSort(selectedMood)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupObservers() {
        // Collect the filtered mood logs Flow
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredMoodLogs.collectLatest { moodLogs ->
                    Log.d(TAG, "Flow updated with ${moodLogs.size} mood logs")
                    adapter.submitList(moodLogs)
                    updateEmptyState(moodLogs.isEmpty())

                    // Enable/disable analyze button based on whether we have data
                    btnAnalyze.isEnabled = moodLogs.isNotEmpty()
                }
            }
        }

        // Observe the current sort to update the display text
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sortOption.collect { sortOption ->
                    viewModel.selectedMoodForSort.collect { selectedMood ->
                        val displayText = viewModel.getCurrentSortDisplay()
                        tvCurrentSort.text = "Showing: $displayText"
                    }
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            val searchQuery = viewModel.searchQuery.value
            val sortOption = viewModel.sortOption.value
            val selectedMood = viewModel.selectedMoodForSort.value

            val emptyText = when {
                searchQuery.isNotBlank() -> "No songs found for \"$searchQuery\""
                sortOption == "mood" && selectedMood != null -> "No songs found for $selectedMood mood"
                else -> "No mood entries yet.\nSelect a mood to get started!"
            }

            tvEmptyState.text = emptyText
            tvEmptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val moodLog = adapter.currentList[position]
                showDeleteConfirmation(moodLog)
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showDeleteConfirmation(moodLog: MoodLog) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteMoodLog(moodLog)
            }
            .setNegativeButton("Cancel") { _, _ ->
                adapter.notifyDataSetChanged()
            }
            .setOnCancelListener {
                adapter.notifyDataSetChanged()
            }
            .show()
    }
}
