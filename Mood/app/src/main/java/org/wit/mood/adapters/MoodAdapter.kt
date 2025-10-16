package org.wit.mood.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.wit.mood.databinding.CardMoodBinding
import org.wit.mood.models.MoodModel

class MoodAdapter(
    private val moods: MutableList<MoodModel>,
    private val onDeleteClick: (MoodModel) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardMoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val mood = moods[holder.adapterPosition]
        holder.bind(mood, onDeleteClick)
    }

    override fun getItemCount(): Int = moods.size

    fun updateMoods(newMoods: List<MoodModel>) {
        moods.clear()
        moods.addAll(newMoods)
        notifyDataSetChanged()
    }

    class MainHolder(private val binding: CardMoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mood: MoodModel, onDeleteClick: (MoodModel) -> Unit) {
            binding.moodTitle.text = mood.type.label
            binding.moodTimestamp.text = mood.timestamp
            binding.note.text = mood.note
            binding.sleep.text = "🛌 ${mood.sleep.name.lowercase()}"
            binding.social.text = "👥 ${mood.social.name.lowercase()}"
            binding.hobby.text = "🎨 ${mood.hobby.name.lowercase()}"
            binding.food.text = "🍽️ ${mood.food.name.lowercase()}"

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
    }
}
