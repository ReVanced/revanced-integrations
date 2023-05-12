package app.revanced.integrations.shared

import app.revanced.integrations.utils.Event

/**
 * WatchWhile player type
 */
@Suppress("unused")
enum class PlayerType {
    /**
     * Includes Shorts and Stories playback.
     */
    NONE,
    /**
     * A Shorts or Stories, if a regular video is minimized and a Short/Story is then opened.
     */
    HIDDEN,
    WATCH_WHILE_MINIMIZED,
    WATCH_WHILE_MAXIMIZED,
    WATCH_WHILE_FULLSCREEN,
    WATCH_WHILE_SLIDING_MAXIMIZED_FULLSCREEN,
    WATCH_WHILE_SLIDING_MINIMIZED_MAXIMIZED,
    /**
     * When opening a short while a regular video is opened, the type can momentarily be this.
     */
    WATCH_WHILE_SLIDING_MINIMIZED_DISMISSED,
    WATCH_WHILE_SLIDING_FULLSCREEN_DISMISSED,
    INLINE_MINIMAL, // home feed video playback
    VIRTUAL_REALITY_FULLSCREEN,
    WATCH_WHILE_PICTURE_IN_PICTURE;

    companion object {

        private val nameToPlayerType = values().associateBy { it.name }

        /**
         * safely parse from a string
         *
         * @param name the name to find
         * @return the enum constant, or null if not found
         */
        @JvmStatic
        fun safeParseFromString(name: String): PlayerType? {
            return nameToPlayerType[name]
        }

        /**
         * The current player type.
         */
        @JvmStatic
        var current
            get() = currentPlayerType
            set(value) {
                currentPlayerType = value
                onChange(currentPlayerType)
            }
        @Volatile // value is read/write from different threads
        private var currentPlayerType = NONE

        /**
         * player type change listener
         */
        @JvmStatic
        val onChange = Event<PlayerType>()
    }

    /**
     * Check if the current player type is [NONE] or [HIDDEN]
     *
     * Does not include the first second after a short is opened when a regular video was already on screen.
     * To include that situation instead use [isNoneHiddenOrDismissed].
     */
    fun isNoneOrHidden(): Boolean {
        return this == NONE || this == HIDDEN
    }

    /**
     * Check if the current player type is [NONE], [HIDDEN], or [WATCH_WHILE_SLIDING_MINIMIZED_DISMISSED]
     * Useful to check if a short is playing.
     *
     * @return If nothing, a Short, a Story,
     *         or a regular minimized video is sliding off screen to a dismissed or hidden state
     */
    fun isNoneHiddenOrDismissed(): Boolean {
        return this == NONE || this == HIDDEN || this == WATCH_WHILE_SLIDING_MINIMIZED_DISMISSED
    }

}