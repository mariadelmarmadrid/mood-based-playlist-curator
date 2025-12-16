package org.wit.mood.views.location

import com.google.android.gms.maps.model.LatLng
import org.wit.mood.models.Location

/**
 * Contract for the Location Picker screen (MVP pattern)
 *
 * Defines the interface between the View and Presenter.
 */
interface LocationPickerContract {

    interface View {

        /**
         * Enable or disable the zoom controls on the map UI.
         *
         * @param enable true to show zoom controls, false to hide them
         */
        fun enableZoomControls(enable: Boolean)

        /**
         * Move the camera to a specific location with a specified zoom level.
         *
         * @param latLng The target latitude and longitude
         * @param zoom   Zoom level to apply
         */
        fun moveCameraTo(latLng: LatLng, zoom: Float)

        /**
         * Display a marker on the map at the given location with a title.
         *
         * @param latLng The position for the marker
         * @param title  The text label for the marker
         */
        fun showMarker(latLng: LatLng, title: String)

        /**
         * Set a listener for map click events.
         *
         * @param listener Callback function that receives the LatLng of the clicked point
         */
        fun setOnMapClickListener(listener: (LatLng) -> Unit)

        /**
         * Get the current zoom level of the map.
         *
         * @return Current zoom level as Float
         */
        fun currentZoom(): Float

        /**
         * Finish the activity and return the selected location as result (OK).
         *
         * @param location The chosen Location object (latitude, longitude, zoom)
         */
        fun finishWithOk(location: Location)

        /**
         * Finish the activity without selecting a location (canceled).
         */
        fun finishCanceled()
    }
}
