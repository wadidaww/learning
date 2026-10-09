package com.kresna.aitopicapp.ui.navigation

import androidx.compose.runtime.Composable
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
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object TopicDetail : Screen("topic/{${NavArgs.TOPIC_ID}}") {
        fun createRoute(topicId: Long) = "topic/$topicId"
    }
    object ContentPager : Screen("content/{${NavArgs.TOPIC_ID}}/{${NavArgs.CONTENT_TYPE}}?${NavArgs.FILTER}={${NavArgs.FILTER}}") {
        fun createRoute(topicId: Long, contentType: ContentType, filter: ContentFilter = ContentFilter.ALL) =
            "content/$topicId/${contentType.name}?${NavArgs.FILTER}=${filter.name}"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
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
}
