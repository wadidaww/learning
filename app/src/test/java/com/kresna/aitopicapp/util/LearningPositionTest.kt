package com.kresna.aitopicapp.util

import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent
import org.junit.Assert.assertEquals
import org.junit.Test

class LearningPositionTest {

    private fun list(vararg viewed: Boolean) = viewed.mapIndexed { i, v ->
        GeneratedContent(id = i.toLong(), topicId = 1, contentType = ContentType.QA, question = "q$i", isViewed = v)
    }

    @Test
    fun `empty list starts at zero`() = assertEquals(0, LearningPosition.startIndex(emptyList()))

    @Test
    fun `nothing viewed starts at the beginning`() = assertEquals(0, LearningPosition.startIndex(list(false, false)))

    @Test
    fun `resumes at the first new item appended after viewed content`() =
        assertEquals(3, LearningPosition.startIndex(list(true, true, true, false, false)))

    @Test
    fun `resumes after the furthest viewed item when earlier items were skipped`() =
        assertEquals(4, LearningPosition.startIndex(list(true, false, true, true, false, false)))

    @Test
    fun `falls back to an earlier skipped item when the end is viewed`() =
        assertEquals(1, LearningPosition.startIndex(list(true, false, true)))

    @Test
    fun `everything viewed opens the last item`() =
        assertEquals(2, LearningPosition.startIndex(list(true, true, true)))
}
