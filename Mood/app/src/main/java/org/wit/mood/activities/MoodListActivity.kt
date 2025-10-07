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
        binding.recyclerView.adapter = MoodAdapter(app.moods)
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

class MoodAdapter(private var moods: List<MoodModel>) :
    RecyclerView.Adapter<MoodAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardMoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val mood = moods[holder.adapterPosition]
        holder.bind(mood)
    }

    override fun getItemCount(): Int = moods.size

    class MainHolder(private val binding: CardMoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mood: MoodModel) {
            binding.moodTitle.text = mood.type.label
            binding.note.text = mood.note
        }

    }
}
