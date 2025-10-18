package org.wit.mood.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
            onDataChanged: () -> Unit
        ) {
            binding.dateText.text = day.date

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

            binding.moodsContainer.removeAllViews()
            val inflater = LayoutInflater.from(binding.root.context)

            day.moods.forEach { mood ->
                val moodView = org.wit.mood.databinding.CardMoodBinding
                    .inflate(inflater, binding.moodsContainer, false)

                moodView.moodTitle.text = mood.type.label
                moodView.moodTimestamp.text = onlyTime(mood.timestamp)
                moodView.note.text = mood.note

                // ✅ Show rows only if selected (non-null)
                setRow(moodView.sleep, "🛌", mood.sleep)
                setRow(moodView.social, "👥", mood.social)
                setRow(moodView.hobby, "🎨", mood.hobby)
                setRow(moodView.food,  "🍽️", mood.food)

                moodView.root.setOnClickListener { listener.onMoodClick(mood) }

                moodView.btnDelete.setOnClickListener {
                    val context = binding.root.context
                    android.app.AlertDialog.Builder(context)
                        .setTitle("Delete Mood")
                        .setMessage("Are you sure you want to delete this mood?")
                        .setPositiveButton("Yes") { _, _ ->
                            app.moods.delete(mood)
                            onDataChanged()
                        }
                        .setNegativeButton("No", null)
                        .show()
                }

                binding.moodsContainer.addView(moodView.root)
            }
        }


        // Helpers in DayHolder
        private fun onlyTime(ts: String): String =
            if (ts.length >= 16) ts.substring(11, 16) else ts

        private fun <E : Enum<*>> setRow(view: TextView, emoji: String, value: E?) {
            if (value == null) {
                view.visibility = View.GONE
            } else {
                view.text = "$emoji ${value.name.prettyEnumLabel()}"
                view.visibility = View.VISIBLE
            }
        }

        // Title-case + spaces for enum names, e.g. FAST_FOOD -> "Fast Food"
        private fun String.prettyEnumLabel(): String =
            lowercase().replace('_', ' ')
                .split(' ')
                .joinToString(" ") { it.replaceFirstChar { c -> c.titlecase() } }
    }
}
