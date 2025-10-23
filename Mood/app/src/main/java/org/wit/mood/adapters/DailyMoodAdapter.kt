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
import org.wit.mood.models.MoodModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Adapter that displays a list of DailyMoodSummary objects — one card per day.
 *
 * Each "day card" shows:
 *  - A formatted date header (e.g., "Sunday, 19 Oct 2025")
 *  - An average mood label ("Average Mood: Relaxed 😌")
 *  - A list of individual moods for that day (each with edit/delete buttons)
 *
 * Works together with [MoodListActivity]:
 *  - onEditClick: launches edit screen for a mood
 *  - onDataChanged: triggers reload when a mood is deleted
 */
class DailyMoodAdapter(
    private var days: List<DailyMoodSummary>,      // data source: all daily summaries
    private val app: MainApp,                      // gives access to MoodJSONStore
    private val onEditClick: (MoodModel) -> Unit,  // callback for edit
    private val onDataChanged: () -> Unit          // callback for refresh after delete
) : RecyclerView.Adapter<DailyMoodAdapter.DayHolder>() {

    /** Inflates a day card layout when RecyclerView needs a new item. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayHolder {
        val binding = CardDailySummaryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DayHolder(binding)
    }

    /** Populates a DayHolder with one DailyMoodSummary’s data. */
    override fun onBindViewHolder(holder: DayHolder, position: Int) {
        holder.bind(days[holder.bindingAdapterPosition], app, onEditClick, onDataChanged)
    }

    /** Number of day cards displayed. */
    override fun getItemCount(): Int = days.size

    /**
     * ViewHolder representing one day summary card.
     * Responsible for rendering the date header, average, and nested moods list.
     */
    class DayHolder(private val binding: CardDailySummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Bind a DailyMoodSummary to its UI components.
         * - Formats date
         * - Calculates and shows average mood
         * - Inflates and binds inner list of moods for that day
         */
        fun bind(
            day: DailyMoodSummary,
            app: MainApp,
            onEditClick: (MoodModel) -> Unit,
            onDataChanged: () -> Unit
        ) = with(binding) {

            // --- Header: e.g. "Sunday, 19 Oct 2025"
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

            // --- Inner list of moods (one card per entry) ---
            moodsContainer.removeAllViews()
            val inflater = LayoutInflater.from(root.context)

            day.moods.forEach { mood ->
                val moodView = CardMoodBinding.inflate(inflater, moodsContainer, false)

                // Bind basic fields
                moodView.moodTitle.text = mood.type.label     // e.g., "Happy 😊" (from enums)
                moodView.moodTimestamp.text = onlyTime(mood.timestamp)
                moodView.note.text = mood.note

                // Optional attributes (only visible when present)
                setRow(moodView.sleep,  "🛌",  mood.sleep)
                setRow(moodView.social, "👥",  mood.social)
                setRow(moodView.hobby,  "🎨",  mood.hobby)
                setRow(moodView.food,   "🍽️",  mood.food)

                // Card itself is inert; buttons handle actions
                moodView.root.isClickable = true
                moodView.root.isFocusable = false
                moodView.root.setOnClickListener { /* no-op */ }
                moodView.root.setOnLongClickListener { true }

                // Edit button → forwards to callback (MoodListActivity)
                moodView.btnEdit.setOnClickListener { onEditClick(mood) }

                // Delete button → confirmation dialog then delete from store
                moodView.btnDelete.setOnClickListener {
                    AlertDialog.Builder(root.context)
                        .setTitle("Delete Mood")
                        .setMessage("Are you sure you want to delete this mood?")
                        .setPositiveButton("Yes") { _, _ ->
                            app.moods.delete(mood)
                            onDataChanged() // triggers list refresh in activity
                        }
                        .setNegativeButton("No", null)
                        .show()
                }

                // Add the prepared mood card to the day's container
                moodsContainer.addView(moodView.root)
            }
        }

        /** Extracts "HH:mm" from timestamp "yyyy-MM-dd HH:mm:ss". */
        private fun onlyTime(ts: String): String =
            if (ts.length >= 16) ts.substring(11, 16) else ts // "yyyy-MM-dd HH:mm:ss" → "HH:mm"

        /**
         * Shows emoji + readable label for an enum value or hides the TextView if null.
         * Example: "🎨 Painting"
         */
        private fun <E : Enum<*>> setRow(view: TextView, emoji: String, value: E?) {
            if (value == null) {
                view.visibility = View.GONE
            } else {
                view.text = "$emoji ${value.name.prettyEnumLabel()}"
                view.visibility = View.VISIBLE
            }
        }

        /** Converts enum name from UPPER_CASE → "Title Case" (e.g., FAST_FOOD → "Fast Food"). */
        private fun String.prettyEnumLabel(): String =
            lowercase().replace('_', ' ')
                .split(' ')
                .joinToString(" ") { it.replaceFirstChar { c -> c.titlecase() } }

        /**
         * Converts an ISO date (yyyy-MM-dd) to a long-form header, e.g.:
         * "2025-10-19" → "Sunday, 19 Oct 2025".
         * Falls back to the raw string if parsing fails.
         */
        private fun formatDateForHeader(dateStr: String): String = try {
            val ld = LocalDate.parse(dateStr)
            val fmt = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale.getDefault())
            ld.format(fmt)
        } catch (e: Exception) {
            dateStr
        }
    }
}
