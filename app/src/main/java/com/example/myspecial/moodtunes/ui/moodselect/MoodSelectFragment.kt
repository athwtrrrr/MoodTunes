package com.example.myspecial.moodtunes.ui.moodselect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myspecial.moodtunes.R
import com.example.myspecial.moodtunes.viewmodel.SharedViewModel
import androidx.lifecycle.ViewModelProvider

class MoodSelectFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood_select, container, false)

        // Initialize ViewModel
        viewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(SharedViewModel::class.java)

        setupClickListeners(view)
        return view
    }

    private fun setupClickListeners(view: View) {
        // Match these with the search queries in SpotifyRepository
        view.findViewById<Button>(R.id.btnHappy).setOnClickListener {
            navigateToRecommendations("happy")
        }
        view.findViewById<Button>(R.id.btnSad).setOnClickListener {
            navigateToRecommendations("sad")
        }
        view.findViewById<Button>(R.id.btnCalm).setOnClickListener {
            navigateToRecommendations("calm")
        }
        view.findViewById<Button>(R.id.btnEnergetic).setOnClickListener {
            navigateToRecommendations("energetic")
        }
        view.findViewById<Button>(R.id.btnChill).setOnClickListener {
            // Map "Chill" to "calm" or create a new query - let's use "focused" for now
            navigateToRecommendations("focused")
        }
        view.findViewById<Button>(R.id.btnAngry).setOnClickListener {
            navigateToRecommendations("angry")
        }
        view.findViewById<Button>(R.id.btnRomantic).setOnClickListener {
            navigateToRecommendations("romantic")
        }


        view.findViewById<Button>(R.id.btnViewHistory).setOnClickListener {
            findNavController().navigate(R.id.action_moodSelectFragment_to_moodHistoryFragment)
        }
    }

    private fun navigateToRecommendations(mood: String) {
        // Set the mood in ViewModel first
        viewModel.setSelectedMood(mood)

        // Then navigate
        val action = MoodSelectFragmentDirections.actionMoodSelectFragmentToRecommendationsFragment(mood)
        findNavController().navigate(action)
    }
}