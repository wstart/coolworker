package com.easywork.ai.presentation.navigation

/**
 * 导航路由定义
 */
sealed class NavRoute(val route: String) {
    /**
     * 服务器列表
     */
    data object ServerList : NavRoute("server_list")

    /**
     * 服务器编辑
     */
    data object ServerEdit : NavRoute("server_edit/{serverId}") {
        fun createRoute(serverId: String = ""): String {
            // 如果 serverId 为空，使用 "new" 作为占位符
            val id = if (serverId.isEmpty()) "new" else serverId
            return "server_edit/$id"
        }
    }

    /**
     * 会话列表
     */
    data object SessionList : NavRoute("session_list/{serverId}") {
        fun createRoute(serverId: String): String {
            return "session_list/$serverId"
        }
    }

    /**
     * 终端
     */
    data object Terminal : NavRoute("terminal/{serverId}/{sessionName}") {
        fun createRoute(serverId: String, sessionName: String): String {
            return "terminal/$serverId/$sessionName"
        }
    }
}
