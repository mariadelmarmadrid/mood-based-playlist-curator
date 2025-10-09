package org.wit.mood.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.wit.mood.databinding.CardDailySummaryBinding
import org.wit.mood.models.DailyMoodSummary

class DailyMoodAdapter(private val summaries: List<DailyMoodSummary>) :
    RecyclerView.Adapter<DailyMoodAdapter.DailyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyHolder {
        val binding = CardDailySummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DailyHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyHolder, position: Int) {
        holder.bind(summaries[position])
    }

    override fun getItemCount(): Int = summaries.size

    class DailyHolder(private val binding: CardDailySummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(summary: DailyMoodSummary) {
            binding.dateText.text = summary.date
            binding.moodCount.text = "Entries: ${summary.moods.size}"

            val avgScore = summary.averageScore
            val avgMoodLabel = when {
                avgScore >= 1.5 -> "Happy 😊"
                avgScore >= 0.5 -> "Relaxed 😌"
                avgScore >= -0.5 -> "Neutral 😐"
                avgScore >= -1.5 -> "Sad 😢"
                else -> "Angry 😠"
            }
            binding.averageMood.text = "Average Mood: $avgMoodLabel"
        }
    }
}
