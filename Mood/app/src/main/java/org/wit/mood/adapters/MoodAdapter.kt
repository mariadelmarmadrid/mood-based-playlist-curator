package org.wit.mood.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.wit.mood.databinding.CardMoodBinding
import org.wit.mood.models.MoodModel
import coil.load


/**
 * Adapter that displays individual mood entries inside a day's card (RecyclerView).
 *
 * Responsibilities:
 * - Inflate and bind each "mood card" layout (CardMoodBinding)
 * - Display mood details (type, note, optional fields like sleep/social/hobby/food)
 * - Handle Edit and Delete button actions using lambda callbacks
 *
 * Note:
 * This adapter is simple and stateless — it receives data, binds it, and reports user actions
 * back to the parent (via onEditClick / onDeleteClick).
 */
class MoodAdapter(
    private val moods: MutableList<MoodModel>,        // List of moods shown in the RecyclerView
    private val onEditClick: (MoodModel) -> Unit,     // Called when the user taps Edit
    private val onDeleteClick: (MoodModel) -> Unit    // Called when the user confirms Delete
) : RecyclerView.Adapter<MoodAdapter.MainHolder>() {

    /**
     * Creates a new ViewHolder (card) when needed by RecyclerView.
     * Each card layout is inflated from card_mood.xml.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardMoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    /**
     * Binds a MoodModel object to an existing ViewHolder (populates UI with data).
     */
    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val mood = moods[holder.bindingAdapterPosition]
        holder.bind(mood, onEditClick, onDeleteClick)
    }

    /** Returns how many moods are currently displayed. */
    override fun getItemCount(): Int = moods.size

    /**
     * Inner ViewHolder class responsible for binding one mood card’s layout.
     * Each instance represents one "row" or mood item inside a daily summary.
     */
    class MainHolder(private val binding: CardMoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds a single MoodModel to its UI elements.
         * - Displays main info (type, time, note)
         * - Shows optional attributes (sleep, social, hobby, food)
         * - Wires Edit and Delete buttons
         */
        fun bind(
            mood: MoodModel,
            onEditClick: (MoodModel) -> Unit,
            onDeleteClick: (MoodModel) -> Unit
        ) = with(binding) {
            // --- Basic fields ---
            moodTitle.text = mood.type.label                     // Mood title
            moodTimestamp.text = onlyTime(mood.timestamp)   // Extract "HH:mm" from full timestamp
            note.text = mood.note

            val uri = mood.photoUri
            with(moodImage) {
                if (!uri.isNullOrEmpty()) {
                    visibility = View.VISIBLE
                    load(Uri.parse(uri))
                } else {
                    visibility = View.GONE
                }
            }

            // --- Optional attributes (hide if null) ---
            // Each helper adds an emoji + label or hides the TextView entirely.
            setRow(sleep,  "🛌",  mood.sleep)
            setRow(social, "👥",  mood.social)
            setRow(hobby,  "🎨",  mood.hobby)
            setRow(food,   "🍽️",  mood.food)

            // --- Card background behavior ---
            // The card itself doesn’t respond to taps (only the buttons do).
            root.isClickable = true
            root.isFocusable = false
            root.setOnClickListener { /* no-op */ }
            root.setOnLongClickListener { true }

            // --- Edit button ---
            // Calls the onEditClick lambda provided by the parent Activity.
            btnEdit.isClickable = true
            btnEdit.isFocusable = true
            btnEdit.setOnClickListener { onEditClick(mood) }

            // --- Delete button ---
            // Shows confirmation dialog before triggering onDeleteClick.
            btnDelete.setOnClickListener {
                val context = root.context
                android.app.AlertDialog.Builder(context)
                    .setTitle("Delete Mood")
                    .setMessage("Are you sure you want to delete this mood?")
                    .setPositiveButton("Yes") { _, _ -> onDeleteClick(mood) }
                    .setNegativeButton("No", null)
                    .show()
            }
        }

        /**
         * Utility: extracts just the time (HH:mm) from a timestamp string "yyyy-MM-dd HH:mm:ss".
         */
        private fun onlyTime(ts: String): String =
            ts.substring(11).take(5) // robust: takes "HH:mm" from "... HH:mm:ss"

        /**
         * Shows an emoji + human-readable label for an enum field,
         * or hides the row if the value is null.
         *
         * Example:
         *   "🛌 Good" or "🎨 Painting"
         */
        private fun <E : Enum<*>> setRow(view: TextView, emoji: String, value: E?) {
            if (value == null) {
                view.visibility = android.view.View.GONE
            } else {
                view.text = "$emoji ${value.name.prettyEnumLabel()}"
                view.visibility = android.view.View.VISIBLE
            }
        }

        /**
         * Converts enum name from UPPER_CASE to "Title Case".
         * e.g. "FAST_FOOD" → "Fast Food"
         */
        private fun String.prettyEnumLabel(): String =
            lowercase().replace('_', ' ')
                .split(' ')
                .joinToString(" ") { it.replaceFirstChar { c -> c.titlecase() } }
    }
}
