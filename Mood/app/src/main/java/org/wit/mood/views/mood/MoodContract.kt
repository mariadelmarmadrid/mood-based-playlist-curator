package org.wit.mood.views.mood

import org.wit.mood.models.*

/**
 * MoodContract
 *
 * Defines the contract for the Mood feature using the
 * Model–View–Presenter (MVP) architectural pattern.
 *
 * This contract clearly separates responsibilities:
 *  - View: handles UI rendering and user interaction feedback
 *  - Presenter: contains business logic and coordinates between
 *    the View and the data layer (Model)
 *
 * By using this contract, the Mood feature becomes easier to
 * test, maintain, and extend.
 */
interface MoodContract {

    /**
     * View interface implemented by MoodActivity.
     *
     * Responsible only for:
     *  - Displaying data
     *  - Launching system UI (intents, dialogs)
     *  - Showing feedback to the user
     *
     * The View must not contain business logic.
     */
    interface View {

        /**
         * Launches an Android share intent with the provided text.
         */
        fun launchShareIntent(text: String)

        /**
         * Displays an existing mood in the UI fields.
         */
        fun showMood(mood: MoodModel)

        /**
         * Displays the selected photo associated with the mood.
         */
        fun updatePhoto(uriString: String)

        /**
         * Hides the photo preview when no image is selected.
         */
        fun hidePhoto()

        /**
         * Shows or hides a visual indicator that a location has been set.
         */
        fun showLocationTick(show: Boolean)

        /**
         * Displays an error message to the user.
         */
        fun showError(message: String)

        /**
         * Closes the current view (e.g., after saving or cancelling).
         */
        fun finishView()
    }

    /**
     * Presenter interface implemented by MoodPresenter.
     *
     * Responsible for:
     *  - Handling user actions
     *  - Validating input
     *  - Updating the model
     *  - Instructing the View to update
     */
    interface Presenter {

        /**
         * Creates a new mood or updates an existing one.
         */
        fun doAddOrSave(
            type: MoodType,
            note: String,
            sleep: SleepQuality?,
            social: SocialActivity?,
            hobby: Hobby?,
            food: FoodType?
        )

        /**
         * Shares the current mood via an external application.
         */
        fun doShare()

        /**
         * Cancels the current operation and exits the view.
         */
        fun doCancel()

        /**
         * Initiates image selection from the device.
         */
        fun doSelectImage()

        /**
         * Launches location selection for the mood.
         */
        fun doSetLocation()

        /**
         * Removes the currently attached photo from the mood.
         */
        fun doRemovePhoto()

        /**
         * Temporarily caches user input while navigating away
         * from the screen (e.g., when selecting an image or location).
         */
        fun cacheMood(
            type: MoodType?,
            note: String,
            sleep: SleepQuality?,
            social: SocialActivity?,
            hobby: Hobby?,
            food: FoodType?
        )
    }
}
