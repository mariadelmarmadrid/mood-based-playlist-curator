package org.wit.mood.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.wit.mood.databinding.CardDailySummaryBinding
import org.wit.mood.databinding.CardMoodBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.DailyMoodSummary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class DailyMoodAdapter(
    private var days: List<DailyMoodSummary>,
    private val app: MainApp,
    private val listener: MoodListener,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<DailyMoodAdapter.DayHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayHolder {
        val binding = CardDailySummaryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DayHolder(binding)
    }

    override fun onBindViewHolder(holder: DayHolder, position: Int) {
        holder.bind(days[holder.bindingAdapterPosition], app, listener, onDataChanged)
    }

    override fun getItemCount(): Int = days.size

    fun submitList(newDays: List<DailyMoodSummary>) {
        days = newDays
        notifyDataSetChanged()
    }

    class DayHolder(private val binding: CardDailySummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            day: DailyMoodSummary,
            app: MainApp,
            listener: MoodListener,
            onDataChanged: () -> Unit
        ) = with(binding) {

            // --- Header: "Sunday, 19 Oct 2025"
            dateText.text = formatDateForHeader(day.date)

            // --- Footer: "Average Mood: Neutral 🙂"
            val avgScore = if (day.moods.isNotEmpty())
                day.moods.map { it.type.score }.average()
            else 0.0

            val avgMoodLabel = when {
                avgScore >= 1.5  -> "Happy 😊"
                avgScore >= 0.5  -> "Relaxed 😌"
                avgScore >= -0.5 -> "Neutral 😐"
                avgScore >= -1.5 -> "Sad 😢"
                else             -> "Angry 😠"
            }
            averageMood.text = "Average Mood: $avgMoodLabel"

            // --- List the day's moods
            moodsContainer.removeAllViews()
            val inflater = LayoutInflater.from(root.context)

            day.moods.forEach { mood ->
                val moodView = CardMoodBinding.inflate(inflater, moodsContainer, false)

                // Bind core fields
                moodView.moodTitle.text = mood.type.label     // e.g., "Happy 😊" (from enums)
                moodView.moodTimestamp.text = onlyTime(mood.timestamp)
                moodView.note.text = mood.note

                // Optional rows only when present
                setRow(moodView.sleep,  "🛌",  mood.sleep)
                setRow(moodView.social, "👥",  mood.social)
                setRow(moodView.hobby,  "🎨",  mood.hobby)
                setRow(moodView.food,   "🍽️",  mood.food)

                // Card itself: inert (no accidental edit)
                moodView.root.isClickable = true
                moodView.root.isFocusable = false
                moodView.root.setOnClickListener { /* no-op */ }
                moodView.root.setOnLongClickListener { true }

                // ✏️ Explicit edit button
                moodView.btnEdit.setOnClickListener { listener.onMoodClick(mood) }

                // 🗑️ Delete with confirmation
                moodView.btnDelete.setOnClickListener {
                    AlertDialog.Builder(root.context)
                        .setTitle("Delete Mood")
                        .setMessage("Are you sure you want to delete this mood?")
                        .setPositiveButton("Yes") { _, _ ->
                            app.moods.delete(mood)
                            onDataChanged()
                        }
                        .setNegativeButton("No", null)
                        .show()
                }

                moodsContainer.addView(moodView.root)
            }
        }

        private fun onlyTime(ts: String): String =
            if (ts.length >= 16) ts.substring(11, 16) else ts // "yyyy-MM-dd HH:mm:ss" → "HH:mm"

        private fun <E : Enum<*>> setRow(view: TextView, emoji: String, value: E?) {
            if (value == null) {
                view.visibility = View.GONE
            } else {
                view.text = "$emoji ${value.name.prettyEnumLabel()}"
                view.visibility = View.VISIBLE
            }
        }

        private fun String.prettyEnumLabel(): String =
            lowercase().replace('_', ' ')
                .split(' ')
                .joinToString(" ") { it.replaceFirstChar { c -> c.titlecase() } }

        private fun formatDateForHeader(dateStr: String): String = try {
            val ld = LocalDate.parse(dateStr) // expects "yyyy-MM-dd"
            val fmt = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale.getDefault())
            ld.format(fmt)                    // e.g., "Sunday, 19 Oct 2025"
        } catch (e: Exception) {
            dateStr
        }
    }
}
