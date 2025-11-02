
package com.example.myspecial.moodtunes

    import androidx.test.espresso.Espresso
    import androidx.test.espresso.Espresso.onView
    import androidx.test.espresso.action.ViewActions.click
    import androidx.test.espresso.assertion.ViewAssertions.matches
    import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
    import androidx.test.espresso.matcher.ViewMatchers.*
    import androidx.test.ext.junit.rules.ActivityScenarioRule
    import androidx.test.ext.junit.runners.AndroidJUnit4
    import androidx.test.filters.LargeTest
    import org.junit.Rule
    import org.junit.Test
    import org.junit.runner.RunWith

    @LargeTest
    @RunWith(AndroidJUnit4::class)
    class SelectActivityTest {

        @Rule
        @JvmField
        var activityRule = ActivityScenarioRule(MainActivity::class.java)

        @Test
        fun testMoodSelectionAndSongSave() {
            // Step 1: Navigate through moods
            onView(withId(R.id.btnLeftArrow)).perform(click())
            onView(withId(R.id.btnRightArrow)).perform(click())

            // Step 2: Select a mood
            onView(withId(R.id.btnSelectMood)).perform(click())

            // Step 3: Wait a moment for songs to load
            Thread.sleep(2000)

            // Step 4: Click play button on first song
            onView(withId(R.id.rvSongs))
                .perform(actionOnItemAtPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(0, click()))

            // Step 5: Save second song to mood log
            onView(withId(R.id.rvSongs))
                .perform(actionOnItemAtPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(1, click()))

            onView(withId(R.id.btnSaveToLog)).perform(click())
        }

        @Test
        fun testBasicMoodNavigation() {
            // Just test that we can navigate between moods
            onView(withId(R.id.tvMoodName)).check(matches(isDisplayed()))
            onView(withId(R.id.btnRightArrow)).perform(click())
            onView(withId(R.id.btnLeftArrow)).perform(click())
        }
    }