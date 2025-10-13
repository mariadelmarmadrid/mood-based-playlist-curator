package org.wit.mood.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.wit.mood.databinding.CardDailySummaryBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.DailyMoodSummary
import timber.log.Timber

class DailyMoodAdapter(
    private val summaries: List<DailyMoodSummary>,
    private val app: MainApp
) : RecyclerView.Adapter<DailyMoodAdapter.DailyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyHolder {
        val binding = CardDailySummaryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DailyHolder(binding, app)
    }

    override fun onBindViewHolder(holder: DailyHolder, position: Int) {
        holder.bind(summaries[position])
    }

    override fun getItemCount(): Int = summaries.size

    class DailyHolder(
        private val binding: CardDailySummaryBinding,
        private val app: MainApp
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(summary: DailyMoodSummary) {
            binding.dateText.text = summary.date

            // Clear old mood views before adding new ones
            binding.moodsContainer.removeAllViews()

            val inflater = LayoutInflater.from(binding.root.context)

            summary.moods.sortedByDescending { it.timestamp }.forEach { mood ->
                // 1. Inflate your custom layout: card_mood.xml
                val moodView = inflater.inflate(
                    org.wit.mood.R.layout.card_mood, // <-- USE YOUR CUSTOM LAYOUT HERE
                    binding.moodsContainer,
                    false
                )

                // 2. Populate the views from your custom card_mood.xml
                val title = moodView.findViewById<TextView>(org.wit.mood.R.id.moodTitle)
                val timestamp = moodView.findViewById<TextView>(org.wit.mood.R.id.moodTimestamp)
                val note = moodView.findViewById<TextView>(org.wit.mood.R.id.note)
                val sleep = moodView.findViewById<TextView>(org.wit.mood.R.id.sleep)
                val social = moodView.findViewById<TextView>(org.wit.mood.R.id.social)
                val hobby = moodView.findViewById<TextView>(org.wit.mood.R.id.hobby)
                val food = moodView.findViewById<TextView>(org.wit.mood.R.id.food)
                val deleteButton = moodView.findViewById<Button>(org.wit.mood.R.id.btnDelete)

                // Populate data
                val time = mood.timestamp.substring(11, 16)
                title.text = mood.type.label
                timestamp.text = time // The full timestamp is too long, using just time
                note.text = mood.note
                // Populate your detail TextViews separately to use the FlexboxLayout
                sleep.text = "🛌 ${mood.sleep.name.lowercase()}"
                social.text = "👥 ${mood.social.name.lowercase()}"
                hobby.text = "🎨 ${mood.hobby.name.lowercase()}"
                food.text = "🍽️ ${mood.food.name.lowercase()}"

                // 3. Set up the delete button logic on the newly inflated moodView's button
                deleteButton.setOnClickListener {
                    // ... (Your existing delete logic remains the same)
                    app.moods.delete(mood)
                    Timber.i("Mood Deleted: $mood")
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        (binding.root.context as? androidx.appcompat.app.AppCompatActivity)
                            ?.let { activity ->
                                if (activity is org.wit.mood.activities.MoodListActivity) {
                                    activity.updateRecyclerView()
                                }
                            }
                    }
                }

                // 4. Add the correctly formatted view to the container
                binding.moodsContainer.addView(moodView)
            }

            // Recalculate average mood for this day (rest of your existing code)
            val avgScore = if (summary.moods.isNotEmpty())
                summary.moods.map { it.type.score }.average() else 0.0
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
