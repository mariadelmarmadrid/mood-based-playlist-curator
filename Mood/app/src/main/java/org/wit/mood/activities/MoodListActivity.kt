package org.wit.mood.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.wit.mood.R
import org.wit.mood.databinding.ActivityMoodListBinding
import org.wit.mood.databinding.CardMoodBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.MoodModel
import timber.log.Timber.i

class MoodListActivity : AppCompatActivity() {

    lateinit var app: MainApp
    private lateinit var binding: ActivityMoodListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = title
        setSupportActionBar(binding.toolbar)

        app = application as MainApp

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = MoodAdapter(app.moods, onDeleteClick = { mood ->
            app.moods.remove(mood)
            binding.recyclerView.adapter?.notifyDataSetChanged()

            i("Mood Deleted: $mood")
            for (i in app.moods.indices) {
                i("Mood[$i]:${this.app.moods[i]}")
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_add -> {
                val launcherIntent = Intent(this, MoodActivity::class.java)
                getResult.launch(launcherIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                binding.recyclerView.adapter?.notifyItemRangeChanged(0, app.moods.size)
            }
        }
}

class MoodAdapter(
    private var moods: MutableList<MoodModel>,
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
