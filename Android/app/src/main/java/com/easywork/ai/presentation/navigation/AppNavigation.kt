package com.easywork.ai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.easywork.ai.presentation.server.ServerEditScreen
import com.easywork.ai.presentation.server.ServerListScreen
import com.easywork.ai.presentation.session.SessionListScreen
import com.easywork.ai.presentation.terminal.TerminalScreen

/**
 * 应用导航
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoute.ServerList.route
    ) {
        // 服务器列表
        composable(route = NavRoute.ServerList.route) {
            ServerListScreen(
                onNavigateToEdit = { serverId ->
                    android.util.Log.d("AppNavigation", "Navigating to ServerEdit with serverId: $serverId")
                    val route = NavRoute.ServerEdit.createRoute(serverId)
                    android.util.Log.d("AppNavigation", "Route: $route")
                    navController.navigate(route)
                },
                onNavigateToSessions = { serverId ->
                    navController.navigate(NavRoute.SessionList.createRoute(serverId))
                }
            )
        }

        // 服务器编辑
        composable(
            route = NavRoute.ServerEdit.route,
            arguments = listOf(
                navArgument("serverId") {
                    type = NavType.StringType
                    defaultValue = "new"
                }
            )
        ) { backStackEntry ->
            val serverId = backStackEntry.arguments?.getString("serverId") ?: ""
            // 将 "new" 转换为空字符串，表示新建模式
            val actualServerId = if (serverId == "new") "" else serverId
            ServerEditScreen(
                serverId = actualServerId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 会话列表
        composable(
            route = NavRoute.SessionList.route,
            arguments = listOf(
                navArgument("serverId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val serverId = backStackEntry.arguments?.getString("serverId") ?: ""
            SessionListScreen(
                serverId = serverId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTerminal = { serverId, sessionName ->
                    navController.navigate(NavRoute.Terminal.createRoute(serverId, sessionName))
                }
            )
        }

        // 终端
        composable(
            route = NavRoute.Terminal.route,
            arguments = listOf(
                navArgument("serverId") {
                    type = NavType.StringType
                },
                navArgument("sessionName") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val serverId = backStackEntry.arguments?.getString("serverId") ?: ""
            val sessionName = backStackEntry.arguments?.getString("sessionName") ?: ""
            TerminalScreen(
                serverId = serverId,
                sessionName = sessionName,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
