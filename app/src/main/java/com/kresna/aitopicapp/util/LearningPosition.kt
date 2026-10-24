package com.kresna.aitopicapp.util

import com.kresna.aitopicapp.domain.model.GeneratedContent

object LearningPosition {

    /**
     * Page to open when the user (re)enters a topic, given content ordered oldest first.
     *
     * Resumes at the first unviewed item after the furthest item the user has seen, so newly
     * generated content (appended at the end) is picked up where the user left off. Falls back to
     * any earlier unviewed item, and to the last item when everything has been viewed.
     */
    fun startIndex(content: List<GeneratedContent>): Int {
        if (content.isEmpty()) return 0
        val lastViewed = content.indexOfLast { it.isViewed }
        if (lastViewed == -1) return 0

        val nextUnviewed = (lastViewed + 1 until content.size).firstOrNull { !content[it].isViewed }
        return nextUnviewed
            ?: content.indexOfFirst { !it.isViewed }.takeIf { it >= 0 }
            ?: content.lastIndex
    }
}
