package org.wit.mood.views.mood

import org.wit.mood.models.*

interface MoodContract {

    interface View {
        fun showMood(mood: MoodModel)
        fun updatePhoto(uriString: String)
        fun hidePhoto()
        fun showLocationTick(show: Boolean)
        fun showError(message: String)
        fun finishView()
    }

    interface Presenter {
        fun doAddOrSave(
            type: MoodType,
            note: String,
            sleep: SleepQuality?,
            social: SocialActivity?,
            hobby: Hobby?,
            food: FoodType?
        )

        fun doCancel()
        fun doSelectImage()
        fun doSetLocation()
        fun doRemovePhoto()

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
