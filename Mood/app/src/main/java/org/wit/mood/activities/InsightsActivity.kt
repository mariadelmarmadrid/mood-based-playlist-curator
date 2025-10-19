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

class InsightsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInsightsBinding
    private lateinit var app: MainApp

    private var days: List<DailyMoodSummary> = emptyList()
    private var index = 0 // 0 = newest day

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) reloadDays()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsightsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MainApp

        // --- Bottom Nav ---
        binding.bottomNav.selectedItemId = R.id.nav_chart
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MoodListActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_chart -> true // already here
                else -> false
            }
        }
        binding.bottomNav.setOnItemReselectedListener { /* no-op */ }

        // --- FAB -> add new mood ---
        binding.fabAdd.setOnClickListener {
            getResult.launch(Intent(this, MoodActivity::class.java))
        }

        // --- Day navigation (chevrons) ---
        binding.btnPrevDay.setOnClickListener {
            if (index < days.lastIndex) { index++; renderDay() }
        }
        binding.btnNextDay.setOnClickListener {
            if (index > 0) { index--; renderDay() }
        }

        // Initial load
        reloadDays()
    }

    private fun reloadDays() {
        days = getDailySummaries()
        index = 0 // jump to newest
        renderDay()
    }

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

        val avgLabel = when {
            day.averageScore >= 1.5 -> "Happy 😊"
            day.averageScore >= 0.5 -> "Relaxed 😌"
            day.averageScore >= -0.5 -> "Neutral 😐"
            day.averageScore >= -1.5 -> "Sad 😢"
            else -> "Angry 😠"
        }
        binding.tvAverage.text = "Average: $avgLabel"

        // counts for ring + legend
        val counts = MoodType.values().associateWith { m ->
            day.moods.count { it.type == m }
        }
        binding.moodRing.setData(counts, avgLabel)
        renderLegend(counts)

        // enable/disable chevrons at ends
        binding.btnPrevDay.isEnabled = index < days.lastIndex
        binding.btnNextDay.isEnabled = index > 0
    }

    private fun renderLegend(counts: Map<MoodType, Int>) {
        binding.legend.removeAllViews()

        val moodToIcon = mapOf(
            MoodType.HAPPY   to R.drawable.ic_mood_happy_selector,
            MoodType.RELAXED to R.drawable.ic_mood_relaxed_selector,
            MoodType.NEUTRAL to R.drawable.ic_mood_neutral_selector,
            MoodType.SAD     to R.drawable.ic_mood_sad_selector,
            MoodType.ANGRY   to R.drawable.ic_mood_angry_selector
        )

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

            // icon + tint
            icon.setImageResource(moodToIcon[mood]!!)
            icon.imageTintList = ColorStateList.valueOf(moodToColor[mood]!!)

            // label
            label.text = mood.label.lowercase()

            // badge number + tint
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
