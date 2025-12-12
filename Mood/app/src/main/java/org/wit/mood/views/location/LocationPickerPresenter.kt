package org.wit.mood.views.location

import android.content.Intent
import com.google.android.gms.maps.model.LatLng
import org.wit.mood.models.Location

class LocationPickerPresenter(private val view: LocationPickerContract.View) {

    private var initialLocation: Location? = null
    private var selectedLatLng: LatLng? = null

    fun init(initial: Location?) {
        initialLocation = initial
    }

    fun onMapReady() {
        view.enableZoomControls(true)

        val startLatLng = initialLocation?.let { LatLng(it.lat, it.lng) }
            ?: LatLng(52.2460, -7.1390) // SETU default

        val startZoom = initialLocation?.zoom ?: 15f

        view.moveCameraTo(startLatLng, startZoom)

        // show existing marker if editing
        initialLocation?.let {
            view.showMarker(LatLng(it.lat, it.lng), "Saved location")
        }

        view.setOnMapClickListener { pos ->
            selectedLatLng = pos
            view.showMarker(pos, "Selected location")
        }
    }

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

    fun buildResultIntent(location: Location): Intent =
        Intent().putExtra("location", location)
}
