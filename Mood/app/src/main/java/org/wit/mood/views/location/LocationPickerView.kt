package org.wit.mood.views.location

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.wit.mood.R
import org.wit.mood.models.Location

/**
 * Activity for picking a location on a Google Map.
 *
 * Implements the [LocationPickerContract.View] interface to follow MVP pattern.
 * Handles map display, user interactions, and returning the selected location.
 */
class LocationPickerView : AppCompatActivity(), LocationPickerContract.View, OnMapReadyCallback {

    private lateinit var presenter: LocationPickerPresenter  // Handles the map logic
    private var map: GoogleMap? = null                       // GoogleMap reference
    private var marker: Marker? = null                       // Currently displayed marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_location_picker)

        // Initialize the presenter
        presenter = LocationPickerPresenter(this)

        // Retrieve optional initial location (for editing an existing mood)
        val initialLocation: Location? = intent.getParcelableExtra("location")
        presenter.init(initialLocation)

        // Get the map fragment and request asynchronous map loading
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Handle back button presses via the presenter
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    presenter.onBackPressed()
                }
            }
        )
    }

    /**
     * Called when GoogleMap is ready.
     * Stores the map reference and delegates further setup to the presenter.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        presenter.onMapReady()
    }

    // ---------- Contract methods implemented from LocationPickerContract.View ----------

    /**
     * Enable or disable zoom controls on the map.
     */
    override fun enableZoomControls(enable: Boolean) {
        map?.uiSettings?.isZoomControlsEnabled = enable
    }

    /**
     * Move the camera to a specific LatLng position with zoom level.
     */
    override fun moveCameraTo(latLng: LatLng, zoom: Float) {
        map?.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    /**
     * Display a single marker on the map.
     * Removes any existing marker first.
     */
    override fun showMarker(latLng: LatLng, title: String) {
        marker?.remove()
        marker = map?.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
        )
    }

    /**
     * Set a listener for map clicks, calling the provided lambda with the LatLng.
     */
    override fun setOnMapClickListener(listener: (LatLng) -> Unit) {
        map?.setOnMapClickListener { pos -> listener(pos) }
    }

    /**
     * Return the current zoom level of the map.
     */
    override fun currentZoom(): Float = map?.cameraPosition?.zoom ?: 15f

    /**
     * Finish the activity successfully, returning the selected location.
     */
    override fun finishWithOk(location: Location) {
        setResult(RESULT_OK, presenter.buildResultIntent(location))
        finish()
    }

    /**
     * Finish the activity without selecting a location.
     */
    override fun finishCanceled() {
        setResult(RESULT_CANCELED)
        finish()
    }
}
