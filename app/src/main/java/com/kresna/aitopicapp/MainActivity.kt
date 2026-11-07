package com.kresna.aitopicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.kresna.aitopicapp.ui.navigation.NavGraph
import com.kresna.aitopicapp.ui.theme.AITopicLearningTheme
import com.kresna.aitopicapp.ui.widget.DailyCardLink
import com.kresna.aitopicapp.ui.widget.DailyCardWidgetProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // System bar icons follow the system light/dark setting, like the app theme.
        enableEdgeToEdge()
        // Opened from the home-screen widget: go to its card, but only on a fresh start, not
        // when the activity is recreated (rotation, process restore) and already has a back stack.
        val startCard = if (savedInstanceState == null) DailyCardLink.read(intent) else null
        setContent {
            AITopicLearningTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController, startCard = startCard)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Topics or cards may have been added or deleted; let the widget catch up.
        DailyCardWidgetProvider.refreshAll(this)
    }
}
