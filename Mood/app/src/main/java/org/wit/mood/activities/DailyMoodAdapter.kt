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
                // Container for mood + delete button
                val container = LinearLayout(binding.root.context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                // Inflate mood view (title + subtitle)
                val moodView = inflater.inflate(
                    android.R.layout.simple_list_item_2,
                    binding.moodsContainer,
                    false
                )
                val title = moodView.findViewById<TextView>(android.R.id.text1)
                val subtitle = moodView.findViewById<TextView>(android.R.id.text2)

                val time = mood.timestamp.substring(11, 16)
                title.text = "${mood.type.label}  •  $time"
                subtitle.text =
                    "${mood.note}\n🛌 ${mood.sleep.name.lowercase()} | 👥 ${mood.social.name.lowercase()} | 🎨 ${mood.hobby.name.lowercase()} | 🍽️ ${mood.food.name.lowercase()}"

                // Add mood view to container (weight=1 to fill remaining space)
                container.addView(
                    moodView,
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                )

                // Create delete button
                val deleteButton = Button(binding.root.context).apply {
                    text = "Delete"
                    setOnClickListener {
                        // Delete mood from store
                        app.moods.delete(mood)

                        // Log to Logcat
                        Timber.i("Mood Deleted: $mood")
                        app.moods.findAll().forEachIndexed { index, m ->
                            Timber.i("Mood[$index]: $m")
                        }

                        // Refresh RecyclerView
                        if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                            (binding.root.context as? androidx.appcompat.app.AppCompatActivity)
                                ?.let { activity ->
                                    if (activity is org.wit.mood.activities.MoodListActivity) {
                                        activity.updateRecyclerView()
                                    }
                                }
                        }
                    }
                }

                container.addView(deleteButton)

                // Add container to moodsContainer
                binding.moodsContainer.addView(container)
            }

            // Recalculate average mood for this day
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
