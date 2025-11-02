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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat


class MoodSelectFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel
    private var currentMoodIndex = 0

    // UI elements
    private lateinit var ivMoodImage: ImageView  // Changed from TextView
    private lateinit var tvMoodName: TextView
    private lateinit var btnLeftArrow: ImageButton
    private lateinit var btnRightArrow: ImageButton
    private lateinit var btnSelectMood: Button

    private lateinit var moodCard: androidx.cardview.widget.CardView

    // Mood data arrays
    private lateinit var moodImages: IntArray  // Changed from String array
    private lateinit var moodNames: Array<String>
    private lateinit var moodColors: Array<Int>
    private lateinit var moodQueries: Array<String>

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

        // Load mood data from resources
        loadMoodDataFromResources()

        initViews(view)
        setupClickListeners()
        updateMoodDisplay()

        return view
    }

    private fun loadMoodDataFromResources() {
        moodNames = resources.getStringArray(R.array.mood_names)
        moodQueries = resources.getStringArray(R.array.mood_queries)

        // Load image resource IDs
        val typedArrayImages = resources.obtainTypedArray(R.array.mood_images)
        moodImages = IntArray(typedArrayImages.length()) { i ->
            typedArrayImages.getResourceId(i, 0)
        }
        typedArrayImages.recycle()

        // Load color resource IDs
        val typedArrayColors = resources.obtainTypedArray(R.array.mood_colors)
        moodColors = Array(typedArrayColors.length()) { i ->
            typedArrayColors.getResourceId(i, 0)
        }
        typedArrayColors.recycle()
    }

    private fun initViews(view: View) {
        ivMoodImage = view.findViewById(R.id.ivMoodImage)  // ImageView now
        tvMoodName = view.findViewById(R.id.tvMoodName)
        btnLeftArrow = view.findViewById(R.id.btnLeftArrow)
        btnRightArrow = view.findViewById(R.id.btnRightArrow)
        btnSelectMood = view.findViewById(R.id.btnSelectMood)
        moodCard = view.findViewById(R.id.moodCard)
    }

    private fun setupClickListeners() {
        btnLeftArrow.setOnClickListener {
            navigateMoods(-1)
        }

        btnRightArrow.setOnClickListener {
            navigateMoods(1)
        }

        btnSelectMood.setOnClickListener {
            navigateToRecommendations(moodQueries[currentMoodIndex])
        }

    }

    private fun navigateMoods(direction: Int) {
        currentMoodIndex = (currentMoodIndex + direction).mod(moodNames.size)
        updateMoodDisplay()
    }

    private fun updateMoodDisplay() {
        // Update image
        ivMoodImage.setImageResource(moodImages[currentMoodIndex])

        // Update text content
        tvMoodName.text = moodNames[currentMoodIndex]

        // Update card background color dynamically
        val color = ContextCompat.getColor(requireContext(), moodColors[currentMoodIndex])
        moodCard.setCardBackgroundColor(color)

        // Update select button color to match mood
        btnSelectMood.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
    }

    private fun navigateToRecommendations(mood: String) {
        viewModel.setSelectedMood(mood)
        findNavController().navigate(R.id.action_moodSelectFragment_to_recommendationsFragment)
    }
}