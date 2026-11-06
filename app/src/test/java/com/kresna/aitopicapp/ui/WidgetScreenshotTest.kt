package com.kresna.aitopicapp.ui

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import com.kresna.aitopicapp.R
import org.junit.Rule
import org.junit.Test

/**
 * Renders the home-screen widget layout (the same XML the RemoteViews use) in its three states:
 * a Spark card, a quiz card and the empty state. Light here, dark in [WidgetDarkScreenshotTest].
 */
class WidgetScreenshotTest {
    @get:Rule
    val paparazzi = widgetPaparazzi(NightMode.NOTNIGHT)

    @Test
    fun widgetLight() = paparazzi.snapshot(widgetSheet(paparazzi))
}

class WidgetDarkScreenshotTest {
    @get:Rule
    val paparazzi = widgetPaparazzi(NightMode.NIGHT)

    @Test
    fun widgetDark() = paparazzi.snapshot(widgetSheet(paparazzi))
}

private fun widgetPaparazzi(nightMode: NightMode) = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_5.copy(nightMode = nightMode),
    theme = "android:Theme.Material.NoActionBar"
)

private fun widgetSheet(paparazzi: Paparazzi): View {
    val context = paparazzi.context
    val dp = context.resources.displayMetrics.density
    val column = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(context.getColor(R.color.app_background))
        setPadding((16 * dp).toInt(), (24 * dp).toInt(), (16 * dp).toInt(), (24 * dp).toInt())
    }

    fun add(icon: Int, label: String, body: String, hint: String) {
        val widget = paparazzi.inflate<View>(R.layout.widget_daily_card)
        widget.findViewById<ImageView>(R.id.widget_type_icon).setImageResource(icon)
        widget.findViewById<TextView>(R.id.widget_label).text = label
        widget.findViewById<TextView>(R.id.widget_body).text = body
        widget.findViewById<TextView>(R.id.widget_hint).text = hint
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (150 * dp).toInt())
        params.bottomMargin = (16 * dp).toInt()
        column.addView(widget, params)
    }

    add(
        R.drawable.ic_widget_spark,
        "Spark · Ancient Rome",
        "Roman harbour concrete gets stronger in seawater: the water reacts with volcanic ash in it and grows new minerals in the cracks.",
        context.getString(R.string.widget_hint_spark)
    )
    add(
        R.drawable.ic_widget_quiz,
        "Quick quiz · Data structures",
        "Why does a hash map offer O(1) average lookups?",
        context.getString(R.string.widget_hint_quiz)
    )
    add(
        R.drawable.ic_widget_flashcard,
        "Flashcard · Data structures",
        "What problem does a bloom filter solve?",
        context.getString(R.string.widget_hint_flashcard)
    )
    add(
        R.drawable.ic_widget_spark,
        context.getString(R.string.widget_label_default),
        context.getString(R.string.widget_empty_body),
        context.getString(R.string.widget_empty_hint)
    )

    return FrameLayout(context).apply {
        setBackgroundColor(context.getColor(R.color.app_background))
        addView(column, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP))
    }
}
