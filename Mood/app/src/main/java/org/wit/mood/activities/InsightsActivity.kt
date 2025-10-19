package org.wit.mood.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.wit.mood.R
import org.wit.mood.databinding.ActivityInsightsBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodType

class InsightsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInsightsBinding
    private lateinit var app: MainApp

    private var days: List<DailyMoodSummary> = emptyList()
    private var index = 0 // 0 = latest day

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsightsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MainApp
        days = getDailySummaries() // same grouping as list screen, newest first

        binding.btnPrevDay.setOnClickListener {
            if (index < days.lastIndex) { index++; renderDay() }
        }
        binding.btnNextDay.setOnClickListener {
            if (index > 0) { index--; renderDay() }
        }

        renderDay()
    }

    private fun renderDay() {
        if (days.isEmpty()) {
            binding.tvDate.text = getString(R.string.app_name)
            binding.tvAverage.text = "No data yet"
            binding.moodRing.setData(emptyMap(), "")
            binding.legend.removeAllViews()
            return
        }

        val day = days[index]
        binding.tvDate.text = day.date

        val avg = when {
            day.averageScore >= 1.5 -> "Happy 😊"
            day.averageScore >= 0.5 -> "Relaxed 😌"
            day.averageScore >= -0.5 -> "Neutral 😐"
            day.averageScore >= -1.5 -> "Sad 😢"
            else -> "Angry 😠"
        }
        binding.tvAverage.text = "Average: $avg"

        // Build counts for the ring
        val counts = MoodType.values().associateWith { m ->
            day.moods.count { it.type == m }
        }
        binding.moodRing.setData(counts, avg)

        // Tiny legend (optional): label + count
        binding.legend.removeAllViews()
        MoodType.values().forEach { m ->
            val tv = android.widget.TextView(this).apply {
                text = "${m.label} — ${counts[m]}"
                setTextColor(getColor(R.color.black))
            }
            binding.legend.addView(tv)
        }

        // Enable/disable nav buttons at ends
        binding.btnPrevDay.isEnabled = index < days.lastIndex
        binding.btnNextDay.isEnabled = index > 0
    }

    private fun getDailySummaries(): List<DailyMoodSummary> {
        val all = app.moods.findAll()
        val grouped = all.groupBy { it.timestamp.take(10) } // yyyy-MM-dd per-day
        return grouped.map { (date, moods) ->
            val sorted = moods.sortedByDescending { it.timestamp }
            val avg = if (sorted.isNotEmpty()) sorted.map { it.type.score }.average() else 0.0
            DailyMoodSummary(date = date, moods = sorted, averageScore = avg)
        }.sortedByDescending { it.date } // newest first
    }
}
