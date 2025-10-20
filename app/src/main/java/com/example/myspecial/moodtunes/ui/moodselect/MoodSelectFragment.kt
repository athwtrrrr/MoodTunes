package com.example.myspecial.moodtunes.ui.moodselect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myspecial.moodtunes.R
import com.example.myspecial.moodtunes.ui.history.MoodHistoryFragment




class MoodSelectFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood_select, container, false)

        setupClickListeners(view)

        return view
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<Button>(R.id.btnHappy).setOnClickListener {
            navigateToRecommendations("Happy")
        }
        view.findViewById<Button>(R.id.btnSad).setOnClickListener {
            navigateToRecommendations("Sad")
        }
        view.findViewById<Button>(R.id.btnCalm).setOnClickListener {
            navigateToRecommendations("Calm")
        }
        view.findViewById<Button>(R.id.btnEnergetic).setOnClickListener {
            navigateToRecommendations("Energetic")
        }
        view.findViewById<Button>(R.id.btnChill).setOnClickListener {
            navigateToRecommendations("Chill")
        }

        view.findViewById<Button>(R.id.btnViewHistory).setOnClickListener {
            findNavController().navigate(R.id.action_moodSelectFragment_to_moodHistoryFragment)
        }
    }

    private fun navigateToRecommendations(mood: String) {
        val action = MoodSelectFragmentDirections.actionMoodSelectFragmentToRecommendationsFragment(mood)
        findNavController().navigate(action)
    }
}