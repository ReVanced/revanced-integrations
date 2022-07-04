package app.revanced.integrations.fenster.util

fun Float.clamp(min: Float, max: Float): Float {
    if (this < min) return min
    if (this > max) return max
    return this
}

fun Int.clamp(min: Int, max: Int): Int {
    if (this < min) return min
    if (this > max) return max
    return this
}
