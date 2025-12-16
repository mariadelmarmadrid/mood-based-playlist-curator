package org.wit.mood.views.location

import android.content.Intent
import com.google.android.gms.maps.model.LatLng
import org.wit.mood.models.Location

/**
 * Presenter for the Location Picker screen (MVP pattern)
 *
 * Handles the map logic, user interactions, and returning results
 * without the View needing to know about the data handling.
 */
class LocationPickerPresenter(private val view: LocationPickerContract.View) {

    // The location passed to the screen initially (for editing)
    private var initialLocation: Location? = null

    // The location selected by the user during map interaction
    private var selectedLatLng: LatLng? = null

    /**
     * Initialize the presenter with an optional existing location.
     *
     * @param initial The location previously saved, or null for a default
     */
    fun init(initial: Location?) {
        initialLocation = initial
    }

    /**
     * Called when the map is ready to interact.
     *
     * - Enables zoom controls
     * - Moves camera to initial or default location
     * - Displays a marker if editing an existing location
     * - Sets a listener to capture user-selected location on map clicks
     */
    fun onMapReady() {
        view.enableZoomControls(true)

        val startLatLng = initialLocation?.let { LatLng(it.lat, it.lng) }
            ?: LatLng(52.2460, -7.1390) // Default location (SETU)

        val startZoom = initialLocation?.zoom ?: 15f

        // Move the map camera to start position
        view.moveCameraTo(startLatLng, startZoom)

        // Show marker if editing an existing location
        initialLocation?.let {
            view.showMarker(LatLng(it.lat, it.lng), "Saved location")
        }

        // Capture clicks on the map and show a marker at the selected position
        view.setOnMapClickListener { pos ->
            selectedLatLng = pos
            view.showMarker(pos, "Selected location")
        }
    }

    /**
     * Called when the user presses the back button or finishes picking.
     *
     * - Returns the selected location if available
     * - Falls back to initial location if nothing was selected
     * - Finishes the view with OK result or cancels
     */
    fun onBackPressed() {
        val locToReturn: Location? =
            selectedLatLng?.let {
                Location(
                    lat = it.latitude,
                    lng = it.longitude,
                    zoom = view.currentZoom()
                )
            } ?: initialLocation

        if (locToReturn != null) view.finishWithOk(locToReturn)
        else view.finishCanceled()
    }

    /**
     * Build an Intent to return the chosen location.
     *
     * @param location The location to return
     * @return Intent containing the location as an extra
     */
    fun buildResultIntent(location: Location): Intent =
        Intent().putExtra("location", location)
}
