package com.kresna.aitopicapp.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/** Small vocabulary of haptic cues for the feed, mapped to the best constant the device supports. */
class FeedHaptics(private val view: View) {
    fun correct() = perform(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY)

    fun wrong() = perform(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS)

    fun tap() = perform(HapticFeedbackConstants.KEYBOARD_TAP)

    fun celebrate() = perform(HapticFeedbackConstants.LONG_PRESS)

    private fun perform(constant: Int) {
        view.performHapticFeedback(constant)
    }
}

@Composable
fun rememberFeedHaptics(): FeedHaptics {
    val view = LocalView.current
    return remember(view) { FeedHaptics(view) }
}
