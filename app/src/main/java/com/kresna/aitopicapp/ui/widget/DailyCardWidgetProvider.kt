package com.kresna.aitopicapp.ui.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.kresna.aitopicapp.MainActivity
import com.kresna.aitopicapp.R
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.DailyCard
import com.kresna.aitopicapp.domain.usecase.GetDailyCardUseCase
import com.kresna.aitopicapp.ui.navigation.StartCard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

private const val TAG = "DailyCardWidget"

/**
 * Home-screen widget with one card a day, picked from all of the user's topics
 * ([com.kresna.aitopicapp.util.DailyCardPicker]). Tapping it opens the app on that card.
 *
 * It refreshes when added, a minute after local midnight (an inexact alarm, rescheduled on every
 * refresh), every few hours as a fallback (`updatePeriodMillis`), when the clock or time zone
 * changes, and whenever the app goes to the background, so new or deleted topics show up.
 *
 * Force a refresh: `adb shell am broadcast -n com.kresna.aitopicapp/.ui.widget.DailyCardWidgetProvider -a com.kresna.aitopicapp.widget.REFRESH`
 */
@AndroidEntryPoint
class DailyCardWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var getDailyCard: GetDailyCardUseCase

    override fun onReceive(context: Context, intent: Intent) {
        // Hilt injects here, then AppWidgetProvider dispatches onUpdate/onDisabled.
        super.onReceive(context, intent)
        if (intent.action in REFRESH_ACTIONS) {
            refresh(context, widgetIds(context))
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        refresh(context, appWidgetIds)
    }

    override fun onDisabled(context: Context) {
        // Last widget removed: stop the midnight alarm.
        context.getSystemService(AlarmManager::class.java)?.cancel(refreshIntent(context))
    }

    private fun refresh(context: Context, ids: IntArray) {
        if (ids.isEmpty()) return
        val pending = goAsync()
        scope.launch {
            try {
                val result = getDailyCard()
                result
                    .onSuccess { card ->
                        Log.d(TAG, "Showing ${card?.let { "card ${it.contentId} (topic ${it.topicId}, ${it.contentType})" } ?: "empty state"} on ${ids.size} widget(s)")
                    }
                    .onFailure { Log.e(TAG, "Couldn't load today's card", it) }

                val manager = AppWidgetManager.getInstance(context)
                ids.forEach { id ->
                    manager.updateAppWidget(id, render(context, id, result.getOrNull(), failed = result.isFailure))
                }
                scheduleNextMidnight(context)
            } finally {
                pending.finish()
            }
        }
    }

    private fun render(context: Context, widgetId: Int, card: DailyCard?, failed: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_daily_card)
        val (icon, label, body, hint) = when {
            failed -> Texts(
                R.drawable.ic_widget_spark,
                context.getString(R.string.widget_label_default),
                context.getString(R.string.widget_error_body),
                context.getString(R.string.widget_error_hint)
            )
            card == null -> Texts(
                R.drawable.ic_widget_spark,
                context.getString(R.string.widget_label_default),
                context.getString(R.string.widget_empty_body),
                context.getString(R.string.widget_empty_hint)
            )
            else -> {
                val style = WidgetCardStyle.forType(card.contentType)
                Texts(
                    style.icon,
                    context.getString(R.string.widget_label_format, context.getString(style.label), card.topicName),
                    card.text,
                    context.getString(style.hint)
                )
            }
        }
        views.setImageViewResource(R.id.widget_type_icon, icon)
        views.setTextViewText(R.id.widget_label, label)
        views.setTextViewText(R.id.widget_body, body)
        views.setTextViewText(R.id.widget_hint, hint)
        views.setContentDescription(R.id.widget_root, context.getString(R.string.widget_content_description, label, body, hint))
        views.setOnClickPendingIntent(R.id.widget_root, openAppIntent(context, widgetId, card))
        return views
    }

    private data class Texts(val icon: Int, val label: String, val body: String, val hint: String)

    companion object {
        /** Our own refresh broadcast (midnight alarm, app going to the background, adb). */
        const val ACTION_REFRESH = "com.kresna.aitopicapp.widget.REFRESH"

        private val REFRESH_ACTIONS = setOf(
            ACTION_REFRESH,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        )

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /** Refreshes every placed widget; does nothing when none is placed. */
        fun refreshAll(context: Context) {
            if (widgetIds(context).isEmpty()) return
            context.sendBroadcast(Intent(context, DailyCardWidgetProvider::class.java).setAction(ACTION_REFRESH))
        }

        private fun widgetIds(context: Context): IntArray =
            AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, DailyCardWidgetProvider::class.java))

        private fun refreshIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, DailyCardWidgetProvider::class.java).setAction(ACTION_REFRESH),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        /** Inexact, so it needs no exact-alarm permission; a few minutes late is fine for a daily card. */
        private fun scheduleNextMidnight(context: Context) {
            val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
            val nextMidnight = LocalDate.now().plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli() + 60_000L
            alarmManager.set(AlarmManager.RTC, nextMidnight, refreshIntent(context))
        }

        /** Opens the feed on [card], or just the app when there is no card to show. */
        private fun openAppIntent(context: Context, widgetId: Int, card: DailyCard?): PendingIntent {
            val intent = Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            if (card != null) {
                DailyCardLink.write(intent, StartCard(card.topicId, card.contentType, card.contentId))
            }
            return PendingIntent.getActivity(
                context,
                widgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}

/** Icon, label and call to action for each card type, matching the feed's card styles. */
private enum class WidgetCardStyle(val icon: Int, val label: Int, val hint: Int) {
    QUIZ(R.drawable.ic_widget_quiz, R.string.card_type_quiz, R.string.widget_hint_quiz),
    FLASHCARD(R.drawable.ic_widget_flashcard, R.string.card_type_flashcard, R.string.widget_hint_flashcard),
    SPARK(R.drawable.ic_widget_spark, R.string.card_type_spark, R.string.widget_hint_spark);

    companion object {
        fun forType(type: ContentType): WidgetCardStyle = when (type) {
            ContentType.MCQ -> QUIZ
            ContentType.QA -> FLASHCARD
            ContentType.FUN_FACT, ContentType.MIX -> SPARK
        }
    }
}
