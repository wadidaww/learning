package com.kresna.aitopicapp.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TapZonesTest {

    @Test
    fun `left half goes back and right half goes forward`() {
        assertEquals(TapZone.PREVIOUS, TapZones.zoneFor(x = 10f, width = 400f))
        assertEquals(TapZone.PREVIOUS, TapZones.zoneFor(x = 199f, width = 400f))
        assertEquals(TapZone.NEXT, TapZones.zoneFor(x = 200f, width = 400f))
        assertEquals(TapZone.NEXT, TapZones.zoneFor(x = 390f, width = 400f))
    }

    @Test
    fun `sides swap in right-to-left layouts`() {
        assertEquals(TapZone.NEXT, TapZones.zoneFor(x = 10f, width = 400f, isRtl = true))
        assertEquals(TapZone.PREVIOUS, TapZones.zoneFor(x = 390f, width = 400f, isRtl = true))
    }

    @Test
    fun `ignores taps outside the page or on an unmeasured page`() {
        assertNull(TapZones.zoneFor(x = -1f, width = 400f))
        assertNull(TapZones.zoneFor(x = 401f, width = 400f))
        assertNull(TapZones.zoneFor(x = 0f, width = 0f))
    }

    @Test
    fun `stays put at either end of the feed`() {
        assertNull(TapZones.targetPage(TapZone.PREVIOUS, fromPage = 0, pageCount = 5))
        assertNull(TapZones.targetPage(TapZone.NEXT, fromPage = 4, pageCount = 5))
        assertEquals(3, TapZones.targetPage(TapZone.NEXT, fromPage = 2, pageCount = 5))
        assertEquals(1, TapZones.targetPage(TapZone.PREVIOUS, fromPage = 2, pageCount = 5))
    }
}
