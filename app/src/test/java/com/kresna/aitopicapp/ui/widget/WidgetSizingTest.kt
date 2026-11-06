package com.kresna.aitopicapp.ui.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetSizingTest {

    @Test
    fun `unknown size uses the default`() {
        assertEquals(WidgetSizing.DEFAULT_BODY_LINES, WidgetSizing.bodyMaxLines(0f))
    }

    @Test
    fun `a two-row widget fits three lines`() {
        assertEquals(3, WidgetSizing.bodyMaxLines(150f))
    }

    @Test
    fun `the smallest widget still shows one line`() {
        assertEquals(1, WidgetSizing.bodyMaxLines(110f))
        assertEquals(1, WidgetSizing.bodyMaxLines(40f))
    }

    @Test
    fun `taller widgets show more lines, up to a cap`() {
        assertTrue(WidgetSizing.bodyMaxLines(250f) > WidgetSizing.bodyMaxLines(150f))
        assertEquals(8, WidgetSizing.bodyMaxLines(2000f))
    }

    @Test
    fun `larger text fits fewer lines`() {
        assertTrue(WidgetSizing.bodyMaxLines(250f, fontScale = 1.3f) < WidgetSizing.bodyMaxLines(250f))
    }
}
