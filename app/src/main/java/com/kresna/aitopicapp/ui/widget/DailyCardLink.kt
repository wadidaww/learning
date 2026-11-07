package com.kresna.aitopicapp.ui.widget

import android.content.Intent
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.ui.navigation.StartCard

/** The card a widget tap should open, carried as extras on the launch intent. */
object DailyCardLink {
    private const val ACTION_OPEN_CARD = "com.kresna.aitopicapp.widget.OPEN_CARD"
    private const val EXTRA_TOPIC_ID = "daily_card_topic_id"
    private const val EXTRA_CONTENT_TYPE = "daily_card_content_type"
    private const val EXTRA_CONTENT_ID = "daily_card_content_id"

    fun write(intent: Intent, card: StartCard) {
        intent.action = ACTION_OPEN_CARD
        intent.putExtra(EXTRA_TOPIC_ID, card.topicId)
        intent.putExtra(EXTRA_CONTENT_TYPE, card.contentType.name)
        intent.putExtra(EXTRA_CONTENT_ID, card.contentId)
    }

    fun read(intent: Intent?): StartCard? {
        if (intent?.action != ACTION_OPEN_CARD) return null
        val topicId = intent.getLongExtra(EXTRA_TOPIC_ID, -1L)
        val contentId = intent.getLongExtra(EXTRA_CONTENT_ID, -1L)
        if (topicId <= 0 || contentId <= 0) return null
        return StartCard(topicId, ContentType.fromString(intent.getStringExtra(EXTRA_CONTENT_TYPE)), contentId)
    }
}
