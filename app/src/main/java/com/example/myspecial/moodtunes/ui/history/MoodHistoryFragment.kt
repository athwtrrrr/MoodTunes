package com.example.myspecial.moodtunes.ui.history

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myspecial.moodtunes.R
import com.example.myspecial.moodtunes.data.model.MoodLog
import com.example.myspecial.moodtunes.viewmodel.SharedViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MoodHistoryFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MoodHistoryAdapter
    private lateinit var tvEmptyState: TextView
    private lateinit var tvStats: TextView

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

        viewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(SharedViewModel::class.java)

        initViews(view)
        setupRecyclerView()
        setupFilterButtons()
        setupObservers()

        Log.d(TAG, "Fragment created")
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.rvMoodHistory)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        tvStats = view.findViewById(R.id.tvStats)
    }

    private fun setupRecyclerView() {
        adapter = MoodHistoryAdapter(
            onEditClick = { moodLog -> showEditNoteDialog(moodLog) },
            onDeleteClick = { moodLog -> showDeleteConfirmation(moodLog) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        setupSwipeToDelete()
    }

    private fun setupFilterButtons() {
        val buttons = mapOf(
            R.id.btnFilterAll to "All",
            R.id.btnFilterHappy to "Happy",
            R.id.btnFilterSad to "Sad",
            R.id.btnFilterCalm to "Calm",
            R.id.btnFilterEnergetic to "Energetic",
            R.id.btnFilterChill to "Chill"
        )

        buttons.forEach { (buttonId, mood) ->
            requireView().findViewById<View>(buttonId).setOnClickListener {
                Log.d(TAG, "Filter button clicked: $mood")
                viewModel.filterMoodLogs(mood)
            }
        }
    }

    private fun setupObservers() {
        // Collect the filtered mood logs Flow
        lifecycleScope.launch {
            viewModel.filteredMoodLogs.collectLatest { moodLogs ->
                Log.d(TAG, "Flow updated with ${moodLogs.size} mood logs")
                adapter.submitList(moodLogs)
                updateEmptyState(moodLogs.isEmpty())
                updateStats(moodLogs)
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        tvEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun updateStats(moodLogs: List<MoodLog>) {
        val stats = viewModel.getMoodStats(moodLogs)
        val total = moodLogs.size
        val statsText = if (total > 0) {
            "Total entries: $total • " + stats.entries.joinToString(" • ") { "${it.key}: ${it.value}" }
        } else {
            "No mood entries yet"
        }
        tvStats.text = statsText
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

    private fun showEditNoteDialog(moodLog: MoodLog) {
        val input = EditText(requireContext()).apply {
            setText(moodLog.note)
            inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setSingleLine(false)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Note")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newNote = input.text.toString().trim()
                viewModel.updateMoodLogNote(moodLog, newNote)
            }
            .setNegativeButton("Cancel", null)
            .show()
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