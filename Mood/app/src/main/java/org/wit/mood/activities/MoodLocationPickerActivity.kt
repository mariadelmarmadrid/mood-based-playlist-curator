package org.wit.mood.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.wit.mood.R
import org.wit.mood.models.Location

class MoodLocationPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var marker: Marker? = null

    // If editing, we may receive an existing location
    private var initialLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_location_picker)

        // Read existing location (if any) passed from MoodActivity
        initialLocation = intent.getParcelableExtra("location")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Handle system back button to return selected (or original) location
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Priority: new marker → original location → nothing
                    val locToReturn: Location? = when {
                        marker != null -> {
                            Location(
                                lat = marker!!.position.latitude,
                                lng = marker!!.position.longitude,
                                zoom = map.cameraPosition.zoom
                            )
                        }
                        initialLocation != null -> {
                            initialLocation
                        }
                        else -> null
                    }

                    if (locToReturn != null) {
                        setResult(
                            Activity.RESULT_OK,
                            Intent().putExtra("location", locToReturn)
                        )
                    } else {
                        setResult(Activity.RESULT_CANCELED)
                    }

                    finish()
                }
            }
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Enable + / − zoom buttons
        map.uiSettings.isZoomControlsEnabled = true

        // Center either on existing location or default to SETU
        val startLoc = initialLocation
        val center = if (startLoc != null) {
            LatLng(startLoc.lat, startLoc.lng)
        } else {
            LatLng(52.2460, -7.1390) // Default: SETU area
        }
        val zoom = startLoc?.zoom ?: 15f

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoom))

        // If we had a previous location, show its marker
        if (startLoc != null) {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(startLoc.lat, startLoc.lng))
                    .title("Saved location")
            )
        }

        // Let user pick a new point by tapping
        map.setOnMapClickListener { pos ->
            marker?.remove()
            marker = map.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title("Selected location")
            )
        }
    }
}
