package com.kresna.aitopicapp.util

/** Which way a tap on a feed card moves the feed, like tapping through stories. */
enum class TapZone { PREVIOUS, NEXT }

object TapZones {
    /**
     * Left half goes back, right half goes forward. Mirrored for right-to-left layouts, so
     * "forward" is always on the side the text reads towards.
     */
    fun zoneFor(x: Float, width: Float, isRtl: Boolean = false): TapZone? {
        if (width <= 0f || x < 0f || x > width) return null
        val onLeft = x < width / 2f
        return if (onLeft != isRtl) TapZone.PREVIOUS else TapZone.NEXT
    }

    /** Page to go to for a tap, or null when there is nowhere to go. */
    fun targetPage(zone: TapZone, fromPage: Int, pageCount: Int): Int? {
        val target = if (zone == TapZone.NEXT) fromPage + 1 else fromPage - 1
        return target.takeIf { it in 0 until pageCount }
    }
}
