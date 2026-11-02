package com.kresna.aitopicapp.util

import org.junit.Assert.assertEquals
import org.junit.Test

class OptionTextTest {

    @Test
    fun `strips enumerators when every option has one`() {
        val options = listOf("A. Paris", "B) Rome", "(c) Berlin", "4: Madrid ")
        assertEquals(listOf("Paris", "Rome", "Berlin", "Madrid"), OptionText.stripEnumerators(options))
    }

    @Test
    fun `leaves a lone match alone because it is real text`() {
        val options = listOf("C. elegans is a nematode", "A fruit fly", "A yeast", "A mouse")
        assertEquals(options, OptionText.stripEnumerators(options))
    }

    @Test
    fun `leaves numeric answers alone when the others are plain`() {
        val options = listOf("1. 5 million", "15 million", "150 million", "1.5 billion")
        assertEquals(options, OptionText.stripEnumerators(options))
    }

    @Test
    fun `a single option is never stripped`() {
        assertEquals(listOf("A. Only"), OptionText.stripEnumerators(listOf("A. Only")))
    }

    @Test
    fun `plain options are only trimmed`() {
        assertEquals(listOf("Yes", "No"), OptionText.stripEnumerators(listOf(" Yes", "No ")))
    }

    @Test
    fun `stripEnumerator works on one option`() {
        assertEquals("Quick sort", OptionText.stripEnumerator("B. Quick sort"))
        assertEquals("Quick sort", OptionText.stripEnumerator("Quick sort"))
    }
}
