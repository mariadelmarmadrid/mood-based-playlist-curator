package org.wit.mood.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.wit.mood.databinding.CardDailySummaryBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.DailyMoodSummary

class DailyMoodAdapter(
    private val days: List<DailyMoodSummary>,
    private val app: MainApp,
    private val listener: MoodListener,
    private val onDataChanged: () -> Unit          // ← already here
) : RecyclerView.Adapter<DailyMoodAdapter.DayHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayHolder {
        val binding = CardDailySummaryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DayHolder(binding)
    }

    override fun onBindViewHolder(holder: DayHolder, position: Int) {
        val day = days[holder.adapterPosition]
        holder.bind(day, app, listener, onDataChanged)   // ← pass it in
    }

    override fun getItemCount(): Int = days.size

    class DayHolder(private val binding: CardDailySummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            day: DailyMoodSummary,
            app: MainApp,
            listener: MoodListener,
            onDataChanged: () -> Unit                    // ← receive it here
        ) {
            binding.dateText.text = day.date

            // Average as mood label
            val avgScore = if (day.moods.isNotEmpty())
                day.moods.map { it.type.score }.average()
            else 0.0

            val avgMoodLabel = when {
                avgScore >= 1.5 -> "Happy 😊"
                avgScore >= 0.5 -> "Relaxed 😌"
                avgScore >= -0.5 -> "Neutral 😐"
                avgScore >= -1.5 -> "Sad 😢"
                else -> "Angry 😠"
            }
            binding.averageMood.text = "Average Mood: $avgMoodLabel"

            // Clear + add mood cards
            binding.moodsContainer.removeAllViews()
            val inflater = LayoutInflater.from(binding.root.context)

            day.moods.forEach { mood ->
                val moodView = org.wit.mood.databinding.CardMoodBinding
                    .inflate(inflater, binding.moodsContainer, false)

                moodView.moodTitle.text = mood.type.label
                moodView.moodTimestamp.text = onlyTime(mood.timestamp) // ← time only (HH:mm)
                moodView.note.text = mood.note
                moodView.sleep.text = "🛌 ${mood.sleep.name.lowercase()}"
                moodView.social.text = "👥 ${mood.social.name.lowercase()}"
                moodView.hobby.text = "🎨 ${mood.hobby.name.lowercase()}"
                moodView.food.text = "🍽️ ${mood.food.name.lowercase()}"

                moodView.root.setOnClickListener { listener.onMoodClick(mood) }

                moodView.btnDelete.setOnClickListener {
                    val context = binding.root.context
                    android.app.AlertDialog.Builder(context)
                        .setTitle("Delete Mood")
                        .setMessage("Are you sure you want to delete this mood?")
                        .setPositiveButton("Yes") { _, _ ->
                            app.moods.delete(mood)   // ensure delete uses id + serialize()
                            onDataChanged()          // ← trigger list refresh
                        }
                        .setNegativeButton("No", null)
                        .show()
                }

                binding.moodsContainer.addView(moodView.root)
            }
        }

        private fun onlyTime(ts: String): String =
            if (ts.length >= 16) ts.substring(11, 16) else ts // "yyyy-MM-dd HH:mm:ss" → "HH:mm"
    }
}
