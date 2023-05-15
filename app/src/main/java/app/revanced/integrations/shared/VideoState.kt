package app.revanced.integrations.shared

import app.revanced.integrations.utils.LogHelper
import app.revanced.integrations.patches.VideoInformation

/**
 * VideoState playback state.
 */
enum class VideoState {
    NEW,
    PLAYING,
    PAUSED,
    RECOVERABLE_ERROR,
    UNRECOVERABLE_ERROR,
    /**
     * @see [VideoInformation.isAtEndOfVideo]
     */
    ENDED;

    companion object {

        private val nameToVideoState = values().associateBy { it.name }

        @JvmStatic
        fun setFromString(enumName: String) {
            val state = nameToVideoState[enumName]
            if (state == null) {
                LogHelper.printException { "Unknown VideoState encountered: $enumName" }
            } else if (currentVideoState != state) {
                LogHelper.printDebug { "VideoState changed to: $state" }
                currentVideoState = state
            }
        }

        /**
         * Depending on which hook this is called from,
         * this value may not be up to date with the actual playback state.
         */
        @JvmStatic
        var current: VideoState?
            get() = currentVideoState
            private set(value) {
                currentVideoState = value
            }

        private var currentVideoState : VideoState? = null
    }
}