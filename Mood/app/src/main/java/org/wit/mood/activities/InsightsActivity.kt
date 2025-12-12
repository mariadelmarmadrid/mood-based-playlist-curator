package org.wit.mood.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import org.wit.mood.R
import org.wit.mood.databinding.ActivityInsightsBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodType
import android.content.ActivityNotFoundException
import android.net.Uri
import com.google.android.material.snackbar.Snackbar
import org.wit.mood.views.mood.MoodView
import org.wit.mood.views.moodlist.MoodListView


/**
 * Insights screen:
 * - Shows one day's mood breakdown at a time (newest first).
 * - Displays a ring chart of counts per mood + a legend.
 * - Calculates a human-friendly average label (e.g., "Relaxed 😌").
 * - Allows day-to-day navigation via chevrons.
 */
class InsightsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInsightsBinding
    private lateinit var app: MainApp

    // Full list of day summaries and the current index being shown (0 = newest)
    private var days: List<DailyMoodSummary> = emptyList()
    private var index = 0 // 0 = newest day

    private var currentPlaylistUrl: String? = null


    // Return from add/edit screen → if OK, reload summaries
    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) reloadDays()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsightsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MainApp

        // --- Bottom navigation (Insights is the current tab) ---
        binding.bottomNav.selectedItemId = R.id.nav_chart
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MoodListView::class.java))
                    overridePendingTransition(0, 0)
                    finish()   // remove Insights from back stack
                    true
                }
                R.id.nav_chart -> true   // already here, do nothing
                else -> false
            }
        }


        binding.bottomNav.setOnItemReselectedListener { /* keep current day */ }

        // --- FAB → add a new mood (comes back via getResult) ---
        binding.fabAdd.setOnClickListener {
            getResult.launch(Intent(this, MoodView::class.java))
        }

        // --- Day navigation (chevrons) ---
        binding.btnPrevDay.setOnClickListener {
            // move towards older days if available
            if (index < days.lastIndex) { index++; renderDay() }
        }
        binding.btnNextDay.setOnClickListener {
            // move towards newer days if available
            if (index > 0) { index--; renderDay() }
        }

        binding.btnOpenPlaylist.setOnClickListener {
            val url = currentPlaylistUrl ?: return@setOnClickListener
            openSpotifyPlaylist(url)
        }

        // Initial load and render
        reloadDays()
    }

    /**
     * Reload the list of day summaries from storage and jump to newest.
     */
    private fun reloadDays() {
        days = getDailySummaries()
        index = 0 // newest day
        renderDay()
    }

    /**
     * Render the currently selected day:
     * - update date label
     * - compute and show average label
     * - update ring chart + legend
     * - enable/disable chevrons appropriately
     */
    private fun renderDay() {
        if (days.isEmpty()) {
            binding.tvDate.text = getString(R.string.app_name)
            binding.tvAverage.text = "No data yet"
            binding.moodRing.setData(emptyMap(), "")
            binding.legend.removeAllViews()
            binding.btnPrevDay.isEnabled = false
            binding.btnNextDay.isEnabled = false

            // no playlist for empty state
            currentPlaylistUrl = null
            binding.btnOpenPlaylist.visibility = View.GONE
            return
        }

        val day = days[index]
        binding.tvDate.text = day.date

        val avgScore = day.averageScore
        val avgLabel = when {
            avgScore >= 1.5 -> "Happy 😊"
            avgScore >= 0.5 -> "Relaxed 😌"
            avgScore >= -0.5 -> "Neutral 😐"
            avgScore >= -1.5 -> "Sad 😢"
            else -> "Angry 😠"
        }
        binding.tvAverage.text = "Average: $avgLabel"

        // ---- NEW: pick playlist based on avgScore ----
        currentPlaylistUrl = playlistUrlFor(avgScore)
        if (currentPlaylistUrl == null) {
            binding.btnOpenPlaylist.visibility = View.GONE
        } else {
            binding.btnOpenPlaylist.visibility = View.VISIBLE
        }

        // existing ring + legend code stays the same
        val counts = MoodType.values().associateWith { m ->
            day.moods.count { it.type == m }
        }
        binding.moodRing.setData(counts, avgLabel)
        renderLegend(counts)

        binding.btnPrevDay.isEnabled = index < days.lastIndex
        binding.btnNextDay.isEnabled = index > 0
    }


    /**
     * Inflate and bind a legend row per mood:
     * - mood icon (tinted)
     * - textual label
     * - small count badge (hidden when zero)
     */
    private fun renderLegend(counts: Map<MoodType, Int>) {
        binding.legend.removeAllViews()

        // Icon resources used for each mood type
        val moodToIcon = mapOf(
            MoodType.HAPPY   to R.drawable.ic_mood_happy_selector,
            MoodType.RELAXED to R.drawable.ic_mood_relaxed_selector,
            MoodType.NEUTRAL to R.drawable.ic_mood_neutral_selector,
            MoodType.SAD     to R.drawable.ic_mood_sad_selector,
            MoodType.ANGRY   to R.drawable.ic_mood_angry_selector
        )

        // Solid colors matching each mood (for icon tint + badge bg)
        val moodToColor = mapOf(
            MoodType.HAPPY   to ContextCompat.getColor(this, R.color.mood_happy),
            MoodType.RELAXED to ContextCompat.getColor(this, R.color.mood_relaxed),
            MoodType.NEUTRAL to ContextCompat.getColor(this, R.color.mood_neutral),
            MoodType.SAD     to ContextCompat.getColor(this, R.color.mood_sad),
            MoodType.ANGRY   to ContextCompat.getColor(this, R.color.mood_angry)
        )

        MoodType.values().forEach { mood ->
            val item = layoutInflater.inflate(R.layout.view_legend_item, binding.legend, false)

            val icon = item.findViewById<ImageView>(R.id.icon)
            val badge = item.findViewById<TextView>(R.id.badge)
            val label = item.findViewById<TextView>(R.id.label)

            // Icon + tint
            icon.setImageResource(moodToIcon[mood]!!)
            icon.imageTintList = ColorStateList.valueOf(moodToColor[mood]!!)

            // Text label (lowercase to match design)
            label.text = mood.label.lowercase()

            // Badge number + tint; hide when 0
            val n = counts[mood] ?: 0
            badge.text = n.toString()
            ViewCompat.setBackgroundTintList(
                badge,
                ColorStateList.valueOf(moodToColor[mood]!!)
            )
            badge.visibility = if (n > 0) View.VISIBLE else View.INVISIBLE

            binding.legend.addView(item)
        }
    }

    /**
     * Build daily summaries by:
     * - grouping all moods by yyyy-MM-dd
     * - sorting each group by time desc (latest first)
     * - computing per-day average score
     * - sorting days desc (newest first)
     */
    private fun getDailySummaries(): List<DailyMoodSummary> {
        val all = app.moods.findAll()
        val grouped = all.groupBy { it.timestamp.take(10) } // yyyy-MM-dd
        return grouped.map { (date, moods) ->
            val sorted = moods.sortedByDescending { it.timestamp }
            val avg = if (sorted.isNotEmpty()) sorted.map { it.type.score }.average() else 0.0
            DailyMoodSummary(date = date, moods = sorted, averageScore = avg)
        }.sortedByDescending { it.date } // newest first
    }

    // Map numeric average to one of your 5 playlist URLs
    private fun playlistUrlFor(avgScore: Double): String? = when {
        avgScore >= 1.5  -> "https://open.spotify.com/playlist/0jrlHA5UmxRxJjoykf7qRY?si=nIQRZZctSweuaJ-hFQ0ezA&pi=wgl1rOciSwSfm"
        avgScore >= 0.5  -> "https://open.spotify.com/playlist/7mBi5NbnmRIw60o8GCWHDg?si=6aFrE9oBQ3KYxkXErRcSFg&pi=EHZ5weKLSWyBn"
        avgScore >= -0.5 -> "https://open.spotify.com/playlist/37i9dQZF1EIcJuX6lvhrpW?si=UQi7HmGwQHmSKT-8ZJhBMQ&pi=w8QhyZiIRAmC8"
        avgScore >= -1.5 -> "https://open.spotify.com/playlist/4bRQf8bwAIVgCb6Lcoursx?si=VHWLE9zPTSGNxfVk1OZ2JQ&pi=PoH0HGsGQVSKH"
        else             -> "https://open.spotify.com/playlist/67STztGl7srSMNn6hVYPFR?si=YzgRdjzXTK-_pxca1ijBtA&pi=PSAWWS2PT_O8A"
        // You can return null if you ever want "no playlist" for some range
    }

    // Try Spotify app first; if missing, fall back to browser
    private fun openSpotifyPlaylist(url: String) {
        val uri = Uri.parse(url)

        // 1) Try open directly in Spotify app
        val spotifyIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.spotify.music")
        }
        try {
            startActivity(spotifyIntent)
            return
        } catch (_: ActivityNotFoundException) {
            // Spotify not installed – fall back to browser
        }

        // 2) Fallback: open in any browser
        val webIntent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(webIntent)
        } catch (_: Exception) {
            // nothing can handle it
            Snackbar.make(binding.root, getString(R.string.error_no_spotify), Snackbar.LENGTH_SHORT)
                .show()
        }
    }

}
