import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import coil.load
import org.wit.mood.activities.MoodPresenter
import org.wit.mood.activities.MoodView
import org.wit.mood.databinding.ActivityMoodBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.Location
import org.wit.mood.models.MoodModel


class MoodActivity : AppCompatActivity(), MoodView {

    private lateinit var binding: ActivityMoodBinding
    private lateinit var presenter: MoodPresenter

    private lateinit var pickImageLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var mapLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = MoodPresenter(this, application as MainApp)

        registerLaunchers()
        wireUi()

        presenter.init(intent.getParcelableExtra("mood_edit"))
    }

    private fun wireUi() {
        binding.btnAdd.setOnClickListener { presenter.onSaveClicked(readFormState()) }
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnAddPhoto.setOnClickListener { presenter.onAddPhotoClicked() }
        binding.btnRemovePhoto.setOnClickListener { presenter.onRemovePhotoClicked() }
        binding.btnSetLocation.setOnClickListener { presenter.onPickLocationClicked() }
    }

    // MoodView methods: update UI, launch intents when presenter asks, etc.
}
