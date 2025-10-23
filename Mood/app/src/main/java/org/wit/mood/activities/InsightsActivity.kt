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
                    // Navigate to list without animation flicker
                    startActivity(Intent(this, MoodListActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_chart -> true // already here
                else -> false
            }
        }
        binding.bottomNav.setOnItemReselectedListener { /* keep current day */ }

        // --- FAB → add a new mood (comes back via getResult) ---
        binding.fabAdd.setOnClickListener {
            getResult.launch(Intent(this, MoodActivity::class.java))
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
            return
        }

        val day = days[index]
        binding.tvDate.text = day.date

        // Human-friendly label derived from numeric average
        val avgLabel = when {
            day.averageScore >= 1.5 -> "Happy 😊"
            day.averageScore >= 0.5 -> "Relaxed 😌"
            day.averageScore >= -0.5 -> "Neutral 😐"
            day.averageScore >= -1.5 -> "Sad 😢"
            else -> "Angry 😠"
        }
        binding.tvAverage.text = "Average: $avgLabel"

        // Build counts per mood for the ring + legend
        val counts = MoodType.values().associateWith { m ->
            day.moods.count { it.type == m }
        }
        binding.moodRing.setData(counts, avgLabel)
        renderLegend(counts)

        // Enable/disable day-nav chevrons at ends
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
}
