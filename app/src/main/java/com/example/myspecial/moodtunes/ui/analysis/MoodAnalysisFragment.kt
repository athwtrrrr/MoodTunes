package com.example.myspecial.moodtunes.ui.analysis

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
import androidx.navigation.fragment.findNavController
import com.example.myspecial.moodtunes.R
import com.example.myspecial.moodtunes.viewmodel.SharedViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.myspecial.moodtunes.data.model.MoodAnalysis


class MoodAnalysisFragment : Fragment() {

    private val viewModel: SharedViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    private lateinit var progressBar: ProgressBar
    private lateinit var tvDominantMood: TextView
    private lateinit var tvConfidence: TextView
    private lateinit var tvTotalLogs: TextView
    private lateinit var tvInsights: TextView
    private lateinit var tvRecommendations: TextView
    private lateinit var btnAnalyze: Button
    private lateinit var btnBackToHistory: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood_analysis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
        setupObservers()

        // Perform initial analysis
        viewModel.analyzeMoodPatterns()
    }

    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.progressBar)
        tvDominantMood = view.findViewById(R.id.tvDominantMood)
        tvConfidence = view.findViewById(R.id.tvConfidence)
        tvTotalLogs = view.findViewById(R.id.tvTotalLogs)
        tvInsights = view.findViewById(R.id.tvInsights)
        tvRecommendations = view.findViewById(R.id.tvRecommendations)
        btnAnalyze = view.findViewById(R.id.btnAnalyze)
        btnBackToHistory = view.findViewById(R.id.btnBackToHistory)



    }

    private fun setupClickListeners() {
        btnAnalyze.setOnClickListener {
            viewModel.analyzeMoodPatterns()
        }

        btnBackToHistory.setOnClickListener {
            findNavController().navigate(R.id.moodHistoryFragment)
        }

    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.moodAnalysis.collectLatest { analysis ->
                    analysis?.let { updateUI(it) }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isAnalyzing.collectLatest { isLoading ->
                    progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    btnAnalyze.isEnabled = !isLoading
                }
            }
        }
    }

    private fun updateUI(analysis: MoodAnalysis) {
        // Update dominant mood
        tvDominantMood.text = analysis.dominantMood
        tvConfidence.text = "Confidence: ${(analysis.confidence * 100).toInt()}%"
        tvTotalLogs.text = "Based on ${analysis.totalLogs} logged songs"

        // Update insights
        val insightsText = analysis.insights.joinToString("\n\n") { "• $it" }
        tvInsights.text = insightsText

        // Update recommendations
        val recommendationsText = analysis.recommendations.joinToString("\n\n") { "✨ $it" }
        tvRecommendations.text = recommendationsText

        // Update confidence color based on level
        when {
            analysis.confidence > 0.7 -> tvConfidence.setTextColor(
                resources.getColor(android.R.color.holo_green_dark, null)
            )
            analysis.confidence > 0.4 -> tvConfidence.setTextColor(
                resources.getColor(android.R.color.holo_orange_dark, null)
            )
            else -> tvConfidence.setTextColor(
                resources.getColor(android.R.color.holo_red_dark, null)
            )
        }

        // Show mood distribution in debug (you could display this visually later)
        if (analysis.moodDistribution.isNotEmpty()) {
            println("Mood Distribution: ${analysis.moodDistribution}")
        }
    }
}