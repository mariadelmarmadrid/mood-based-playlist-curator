package org.wit.mood.views.insights

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar
import org.wit.mood.R
import org.wit.mood.databinding.ActivityInsightsBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.MoodType
import org.wit.mood.views.mood.MoodView
import org.wit.mood.views.moodlist.MoodListView

/**
 * InsightsView
 *
 * View layer for the Insights screen (MVP pattern).
 *
 * Responsibilities:
 *  - Display mood statistics for each day (average, counts, legend, ring chart)
 *  - Provide day navigation (previous/next)
 *  - Provide playlist links based on average mood
 *  - Navigate to Home or Add Mood screens
 *  - Handle empty state when no mood data exists
 */
class InsightsView : AppCompatActivity(), InsightsContract.View {

    private lateinit var binding: ActivityInsightsBinding
    private lateinit var presenter: InsightsPresenter

    // Launcher for adding a new mood and refreshing data on return
    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) presenter.load()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsightsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize presenter with view and MainApp reference
        presenter = InsightsPresenter(this, application as MainApp)

        // --- Bottom Navigation ---
        binding.bottomNav.selectedItemId = R.id.nav_chart
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { openHome(); true }  // Navigate to home (mood list)
                R.id.nav_chart -> true                  // Current screen
                else -> false
            }
        }
        binding.bottomNav.setOnItemReselectedListener { }

        // Floating action button to add a new mood
        binding.fabAdd.setOnClickListener { openAddMood() }

        // Day navigation buttons
        binding.btnPrevDay.setOnClickListener { presenter.onPrevDay() }
        binding.btnNextDay.setOnClickListener { presenter.onNextDay() }

        // Playlist button
        binding.btnOpenPlaylist.setOnClickListener { presenter.onOpenPlaylist() }

        // Load initial data
        presenter.load()
    }

    // ---------- Contract Methods ----------

    /**
     * Show empty state when there is no mood data.
     */
    override fun showEmptyState() {
        binding.tvDate.text = getString(R.string.app_name)
        binding.tvAverage.text = "No data yet"
        binding.moodRing.setData(emptyMap(), "")
        binding.legend.removeAllViews()
    }

    /**
     * Display a day's date and its average mood label.
     */
    override fun showDay(date: String, averageLabel: String) {
        binding.tvDate.text = date
        binding.tvAverage.text = "Average: $averageLabel"
    }

    /**
     * Show or hide the playlist button based on availability.
     */
    override fun showPlaylistButton(show: Boolean) {
        binding.btnOpenPlaylist.visibility = if (show) View.VISIBLE else View.GONE
    }

    /**
     * Enable/disable previous and next day navigation buttons.
     */
    override fun enableDayNav(prevEnabled: Boolean, nextEnabled: Boolean) {
        binding.btnPrevDay.isEnabled = prevEnabled
        binding.btnNextDay.isEnabled = nextEnabled
    }

    /**
     * Update the mood ring chart with mood counts and average label.
     */
    override fun updateRing(counts: Map<MoodType, Int>, averageLabel: String) {
        binding.moodRing.setData(counts, averageLabel)
    }

    /**
     * Render the legend showing icons, labels, and counts for each mood type.
     */
    override fun renderLegend(counts: Map<MoodType, Int>) {
        binding.legend.removeAllViews()

        // Map mood types to icon resources and colors
        val moodToIcon = mapOf(
            MoodType.HAPPY to R.drawable.ic_mood_happy_selector,
            MoodType.RELAXED to R.drawable.ic_mood_relaxed_selector,
            MoodType.NEUTRAL to R.drawable.ic_mood_neutral_selector,
            MoodType.SAD to R.drawable.ic_mood_sad_selector,
            MoodType.ANGRY to R.drawable.ic_mood_angry_selector
        )

        val moodToColor = mapOf(
            MoodType.HAPPY to ContextCompat.getColor(this, R.color.mood_happy),
            MoodType.RELAXED to ContextCompat.getColor(this, R.color.mood_relaxed),
            MoodType.NEUTRAL to ContextCompat.getColor(this, R.color.mood_neutral),
            MoodType.SAD to ContextCompat.getColor(this, R.color.mood_sad),
            MoodType.ANGRY to ContextCompat.getColor(this, R.color.mood_angry)
        )

        // Populate legend dynamically
        MoodType.values().forEach { mood ->
            val item = layoutInflater.inflate(R.layout.view_legend_item, binding.legend, false)
            val icon = item.findViewById<ImageView>(R.id.icon)
            val badge = item.findViewById<TextView>(R.id.badge)
            val label = item.findViewById<TextView>(R.id.label)

            icon.setImageResource(moodToIcon[mood]!!)
            icon.imageTintList = ColorStateList.valueOf(moodToColor[mood]!!)

            label.text = mood.label.lowercase()

            val n = counts[mood] ?: 0
            badge.text = n.toString()
            ViewCompat.setBackgroundTintList(badge, ColorStateList.valueOf(moodToColor[mood]!!))
            badge.visibility = if (n > 0) View.VISIBLE else View.INVISIBLE

            binding.legend.addView(item)
        }
    }

    /**
     * Open a Spotify playlist in the app if installed, or fallback to browser.
     */
    override fun openUrlInSpotifyOrBrowser(url: String) {
        val uri = Uri.parse(url)

        // Attempt to open in Spotify app
        val spotifyIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.spotify.music")
        }
        try {
            startActivity(spotifyIntent)
            return
        } catch (_: ActivityNotFoundException) { }

        // Fallback to web browser
        val webIntent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(webIntent)
        } catch (_: Exception) {
            Snackbar.make(binding.root, getString(R.string.error_no_spotify), Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    /** Navigate back to the home (mood list) screen. */
    override fun openHome() {
        startActivity(Intent(this, MoodListView::class.java))
        overridePendingTransition(0, 0)
        finish()
    }

    /** Launch the Add Mood screen using ActivityResultLauncher. */
    override fun openAddMood() {
        getResult.launch(Intent(this, MoodView::class.java))
    }
}
