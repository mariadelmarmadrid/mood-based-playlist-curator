package org.wit.mood.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.wit.mood.databinding.CardMoodBinding
import org.wit.mood.models.MoodModel

interface MoodListener {
    fun onMoodClick(mood: MoodModel)
}

class MoodAdapter(
    private val moods: MutableList<MoodModel>,
    private val listener: MoodListener,
    private val onDeleteClick: (MoodModel) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardMoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val mood = moods[holder.bindingAdapterPosition]
        holder.bind(mood, listener, onDeleteClick)
    }

    override fun getItemCount(): Int = moods.size

    fun updateMoods(newMoods: List<MoodModel>) {
        moods.clear()
        moods.addAll(newMoods)
        notifyDataSetChanged()
    }

    class MainHolder(private val binding: CardMoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            mood: MoodModel,
            listener: MoodListener,
            onDeleteClick: (MoodModel) -> Unit
        ) = with(binding) {
            // Basic fields
            moodTitle.text = mood.type.label
            moodTimestamp.text = onlyTime(mood.timestamp)   // "HH:mm"
            note.text = mood.note

            // Optional rows (hide if null)
            setRow(sleep,  "🛌",  mood.sleep)
            setRow(social, "👥",  mood.social)
            setRow(hobby,  "🎨",  mood.hobby)
            setRow(food,   "🍽️",  mood.food)

            // Make the card itself inert (tap/long-press do nothing)
            root.isClickable = true
            root.isFocusable = false
            root.setOnClickListener { /* no-op */ }
            root.setOnLongClickListener { true }

            // ✏️ Explicit edit button
            btnEdit.isClickable = true
            btnEdit.isFocusable = true
            btnEdit.setOnClickListener { listener.onMoodClick(mood) }

            // 🗑️ Delete with confirmation
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

        private fun onlyTime(ts: String): String =
            ts.substring(11).take(5) // robust: takes "HH:mm" from "... HH:mm:ss"

        private fun <E : Enum<*>> setRow(view: TextView, emoji: String, value: E?) {
            if (value == null) {
                view.visibility = android.view.View.GONE
            } else {
                view.text = "$emoji ${value.name.prettyEnumLabel()}"
                view.visibility = android.view.View.VISIBLE
            }
        }

        private fun String.prettyEnumLabel(): String =
            lowercase().replace('_', ' ')
                .split(' ')
                .joinToString(" ") { it.replaceFirstChar { c -> c.titlecase() } }
    }
}
