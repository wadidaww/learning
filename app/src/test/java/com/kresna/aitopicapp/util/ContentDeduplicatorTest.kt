package com.kresna.aitopicapp.util

import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentDeduplicatorTest {

    private fun qa(question: String) = GeneratedContent(topicId = 1, contentType = ContentType.QA, question = question, answer = "a")
    private fun fact(text: String) = GeneratedContent(
        topicId = 1, contentType = ContentType.FUN_FACT, question = ContentParser.FUN_FACT_TITLE, answer = text
    )

    @Test
    fun `ignores case, spacing and punctuation`() {
        val result = ContentDeduplicator.filterNew(
            listOf(qa("What is a Stack?"), qa("What is a queue?")),
            existingTexts = listOf("what is a   stack")
        )
        assertEquals(listOf("What is a queue?"), result.map { it.question })
    }

    @Test
    fun `removes duplicates within the batch`() {
        val result = ContentDeduplicator.filterNew(listOf(qa("Same?"), qa("same"), qa("Other?")), emptyList())
        assertEquals(listOf("Same?", "Other?"), result.map { it.question })
    }

    @Test
    fun `fun facts are compared by fact text, not the shared title`() {
        // Regression: all fun facts share the "Did you know?" question, which used to filter out every new fact.
        val result = ContentDeduplicator.filterNew(
            listOf(fact("Octopuses have three hearts."), fact("Honey never spoils.")),
            existingTexts = listOf("Honey never spoils")
        )
        assertEquals(listOf("Octopuses have three hearts."), result.map { it.answer })
    }

    @Test
    fun `drops blank items`() {
        assertEquals(0, ContentDeduplicator.filterNew(listOf(qa("?!")), emptyList()).size)
    }
}
