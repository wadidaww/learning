package com.kresna.aitopicapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kresna.aitopicapp.ui.screens.content.ContentPagerScreen
import com.kresna.aitopicapp.ui.screens.home.HomeScreen
import com.kresna.aitopicapp.ui.screens.settings.SettingsScreen
import com.kresna.aitopicapp.ui.screens.topic.TopicDetailScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object TopicDetail : Screen("topic/{topicId}") {
        fun createRoute(topicId: Long) = "topic/$topicId"
    }
    object ContentPager : Screen("content/{topicId}/{contentType}") {
        fun createRoute(topicId: Long, contentType: String) = "content/$topicId/$contentType"
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
                onTopicClick = { topic ->
                    navController.navigate(Screen.TopicDetail.createRoute(topic.id))
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
                navArgument("topicId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getLong("topicId") ?: 0L
            TopicDetailScreen(
                onBackClick = { navController.popBackStack() },
                onViewContent = { contentType ->
                    navController.navigate(Screen.ContentPager.createRoute(topicId, contentType))
                }
            )
        }

        composable(
            route = Screen.ContentPager.route,
            arguments = listOf(
                navArgument("topicId") { type = NavType.LongType },
                navArgument("contentType") { type = NavType.StringType }
            )
        ) {
            ContentPagerScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
