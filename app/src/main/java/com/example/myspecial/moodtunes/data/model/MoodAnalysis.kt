package com.example.myspecial.moodtunes.data.model

data class MoodAnalysis(
    val dominantMood: String,
    val confidence: Float, // 0.0 to 1.0
    val moodPatterns: List<MoodPattern>,
    val insights: List<String>,
    val recommendations: List<String>,
    val totalLogs: Int,
    val moodDistribution: Map<String, Int>
)

data class MoodPattern(
    val patternType: String,
    val description: String,
    val frequency: Int
)