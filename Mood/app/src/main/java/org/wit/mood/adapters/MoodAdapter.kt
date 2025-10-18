package org.wit.mood.adapters

import android.view.LayoutInflater
import android.view.View
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
        val mood = moods[holder.adapterPosition]
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
        ) {
            binding.moodTitle.text = mood.type.label
            binding.moodTimestamp.text = onlyTime(mood.timestamp) // show HH:mm only
            binding.note.text = mood.note

            // Show each detail only if present (nullable-safe)
            setRow(binding.sleep, "🛌",  mood.sleep)
            setRow(binding.social, "👥", mood.social)
            setRow(binding.hobby, "🎨",  mood.hobby)
            setRow(binding.food,  "🍽️",  mood.food)

            // Row click → edit
            binding.root.setOnClickListener { listener.onMoodClick(mood) }

            // Delete with confirm dialog
            binding.btnDelete.setOnClickListener {
                val context = binding.root.context
                android.app.AlertDialog.Builder(context)
                    .setTitle("Delete Mood")
                    .setMessage("Are you sure you want to delete this mood?")
                    .setPositiveButton("Yes") { _, _ -> onDeleteClick(mood) }
                    .setNegativeButton("No", null)
                    .show()
            }
        }

        private fun onlyTime(ts: String): String =
            if (ts.length >= 16) ts.substring(11, 16) else ts // "yyyy-MM-dd HH:mm:ss" → "HH:mm"

        // Hide view if value is null; pretty-print enum names if present
        private fun <E : Enum<*>> setRow(view: TextView, emoji: String, value: E?) {
            if (value == null) {
                view.visibility = View.GONE
            } else {
                view.text = "$emoji ${value.name.prettyEnumLabel()}"
                view.visibility = View.VISIBLE
            }
        }

        // FAST_FOOD -> "Fast Food"
        private fun String.prettyEnumLabel(): String =
            lowercase().replace('_', ' ')
                .split(' ')
                .joinToString(" ") { it.replaceFirstChar { c -> c.titlecase() } }
    }
}
