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

class LocationPickerView : AppCompatActivity(), LocationPickerContract.View, OnMapReadyCallback {

    private lateinit var presenter: LocationPickerPresenter
    private var map: GoogleMap? = null
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_location_picker)

        presenter = LocationPickerPresenter(this)

        val initialLocation: Location? = intent.getParcelableExtra("location")
        presenter.init(initialLocation)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    presenter.onBackPressed()
                }
            }
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        presenter.onMapReady()
    }

    // ---------- Contract methods ----------

    override fun enableZoomControls(enable: Boolean) {
        map?.uiSettings?.isZoomControlsEnabled = enable
    }

    override fun moveCameraTo(latLng: LatLng, zoom: Float) {
        map?.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    override fun showMarker(latLng: LatLng, title: String) {
        marker?.remove()
        marker = map?.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
        )
    }

    override fun setOnMapClickListener(listener: (LatLng) -> Unit) {
        map?.setOnMapClickListener { pos -> listener(pos) }
    }

    override fun currentZoom(): Float = map?.cameraPosition?.zoom ?: 15f

    override fun finishWithOk(location: Location) {
        setResult(RESULT_OK, presenter.buildResultIntent(location))
        finish()
    }

    override fun finishCanceled() {
        setResult(RESULT_CANCELED)
        finish()
    }
}
