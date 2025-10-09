package org.wit.mood.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
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

            // Clear old mood views before adding new ones
            binding.moodsContainer.removeAllViews()

            val inflater = LayoutInflater.from(binding.root.context)

            // Add each mood entry dynamically
            summary.moods.sortedByDescending { it.timestamp }.forEach { mood ->
                val moodView = inflater.inflate(android.R.layout.simple_list_item_2, binding.moodsContainer, false)

                val title = moodView.findViewById<TextView>(android.R.id.text1)
                val subtitle = moodView.findViewById<TextView>(android.R.id.text2)

                val time = mood.timestamp.substring(11, 16) // extract HH:mm

                title.text = "${mood.type.label}  •  $time"
                subtitle.text = "${mood.note}\n🛌 ${mood.sleep.name.lowercase()} | 👥 ${mood.social.name.lowercase()} | 🎨 ${mood.hobby.name.lowercase()} | 🍽️ ${mood.food.name.lowercase()}"

                binding.moodsContainer.addView(moodView)
            }

            // Average mood label
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
