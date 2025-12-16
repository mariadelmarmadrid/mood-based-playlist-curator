package org.wit.mood.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.wit.mood.R
import org.wit.mood.databinding.ActivityMoodMapBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.MoodModel

/**
 * MoodMapActivity
 *
 * This activity displays a Google Map showing markers for all moods
 * that have a saved geographic location.
 *
 * Each marker includes:
 *  - Mood type
 *  - Date recorded
 *  - Optional user note
 */
class MoodMapActivity : AppCompatActivity(), OnMapReadyCallback {

    // ViewBinding for accessing layout views safely
    private lateinit var binding: ActivityMoodMapBinding

    // Reference to the custom Application class for shared data access
    private lateinit var app: MainApp

    // GoogleMap instance used to interact with the map
    private lateinit var map: GoogleMap

    /**
     * Called when the activity is first created.
     * Sets up view binding and initializes the Google Map fragment.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout using ViewBinding
        binding = ActivityMoodMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Access the global application state
        app = application as MainApp

        // Retrieve the SupportMapFragment and register callback
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Called when the Google Map is ready to be used.
     * This is where markers are added and camera positioning is handled.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Enable zoom controls for better usability
        map.uiSettings.isZoomControlsEnabled = true

        // Retrieve all moods that have a valid location saved
        val moodsWithLocation: List<MoodModel> =
            app.moods.findAll().filter { it.location != null }

        // If no moods have locations, center the map on SETU (default location)
        if (moodsWithLocation.isEmpty()) {
            val setu = LatLng(52.2460, -7.1390)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(setu, 15f))
            return
        }

        // Add a marker for each mood with a location
        moodsWithLocation.forEach { mood ->
            val loc = mood.location ?: return@forEach
            val pos = LatLng(loc.lat, loc.lng)

            // Extract date from timestamp (yyyy-MM-dd HH:mm:ss → yyyy-MM-dd)
            val shortDate = mood.timestamp.take(10)

            // Marker title displays mood type and date
            val title = "${mood.type.label} • $shortDate"

            // Marker snippet displays the note if available
            val snippet = if (mood.note.isNotBlank()) {
                "Note: ${mood.note}"
            } else {
                "No note"
            }

            // Add marker to the map
            map.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title(title)
                    .snippet(snippet)
            )
        }

        // Move the camera to the most recently added mood location
        val latest = moodsWithLocation.maxByOrNull { it.timestamp }
        latest?.location?.let { loc ->
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(loc.lat, loc.lng),
                    loc.zoom
                )
            )
        }
    }
}
