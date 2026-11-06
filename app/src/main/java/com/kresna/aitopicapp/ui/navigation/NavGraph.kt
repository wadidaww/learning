package com.kresna.aitopicapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kresna.aitopicapp.domain.model.ContentFilter
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.ui.screens.content.ContentPagerScreen
import com.kresna.aitopicapp.ui.screens.home.HomeScreen
import com.kresna.aitopicapp.ui.screens.settings.SettingsScreen
import com.kresna.aitopicapp.ui.screens.topic.TopicDetailScreen

object NavArgs {
    const val TOPIC_ID = "topicId"
    const val CONTENT_TYPE = "contentType"
    const val FILTER = "filter"
    /** Card to open the feed on (e.g. from the home-screen widget); -1 resumes where the user left off. */
    const val START_CONTENT_ID = "startContentId"
}

/** A card to open on launch, from the home-screen widget. */
data class StartCard(val topicId: Long, val contentType: ContentType, val contentId: Long)

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object TopicDetail : Screen("topic/{${NavArgs.TOPIC_ID}}") {
        fun createRoute(topicId: Long) = "topic/$topicId"
    }
    object ContentPager : Screen(
        "content/{${NavArgs.TOPIC_ID}}/{${NavArgs.CONTENT_TYPE}}" +
            "?${NavArgs.FILTER}={${NavArgs.FILTER}}&${NavArgs.START_CONTENT_ID}={${NavArgs.START_CONTENT_ID}}"
    ) {
        fun createRoute(
            topicId: Long,
            contentType: ContentType,
            filter: ContentFilter = ContentFilter.ALL,
            startContentId: Long? = null
        ) = "content/$topicId/${contentType.name}?${NavArgs.FILTER}=${filter.name}" +
            (startContentId?.let { "&${NavArgs.START_CONTENT_ID}=$it" } ?: "")
    }
}

/**
 * @param startCard card to open on top of Home, once; null for a normal launch.
 */
@Composable
fun NavGraph(navController: NavHostController, startCard: StartCard? = null) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onTopicDetails = { topic ->
                    navController.navigate(Screen.TopicDetail.createRoute(topic.id))
                },
                onContinueLearning = { topic ->
                    navController.navigate(Screen.ContentPager.createRoute(topic.id, topic.defaultContentType))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TopicDetail.route,
            arguments = listOf(
                navArgument(NavArgs.TOPIC_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getLong(NavArgs.TOPIC_ID) ?: 0L
            TopicDetailScreen(
                onBackClick = { navController.popBackStack() },
                onOpenContent = { contentType, filter ->
                    navController.navigate(Screen.ContentPager.createRoute(topicId, contentType, filter))
                }
            )
        }

        composable(
            route = Screen.ContentPager.route,
            arguments = listOf(
                navArgument(NavArgs.TOPIC_ID) { type = NavType.LongType },
                navArgument(NavArgs.CONTENT_TYPE) { type = NavType.StringType },
                navArgument(NavArgs.FILTER) {
                    type = NavType.StringType
                    defaultValue = ContentFilter.ALL.name
                },
                navArgument(NavArgs.START_CONTENT_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getLong(NavArgs.TOPIC_ID) ?: 0L
            ContentPagerScreen(
                onBackClick = { navController.popBackStack() },
                onTopicDetails = {
                    // Reuse the topic screen if we came from it instead of stacking another copy.
                    val route = Screen.TopicDetail.createRoute(topicId)
                    if (!navController.popBackStack(Screen.TopicDetail.route, inclusive = false)) {
                        navController.navigate(route)
                    }
                }
            )
        }
    }

    // After NavHost so the graph is set before navigating; Home stays underneath for Back.
    LaunchedEffect(startCard) {
        startCard?.let {
            navController.navigate(Screen.ContentPager.createRoute(it.topicId, it.contentType, startContentId = it.contentId))
        }
    }
}
