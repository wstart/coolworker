package com.easywork.ai.data.remote.tmux

/**
 * Tmux命令封装
 */
object TmuxCommand {
    // tmux 命令前缀，包含完整的 PATH 环境变量
    private fun tmuxCmd() = "export PATH=/usr/local/bin:/usr/local/sbin:/opt/homebrew/bin:/opt/homebrew/sbin:/usr/bin:/usr/sbin:/bin:/sbin && tmux"

    /**
     * 列出所有会话
     * 格式: session_name:window_count
     */
    fun listSessions(): String {
        return "${tmuxCmd()} list-sessions -F '#{session_name}:#{session_windows}:#{session_activity_string}'"
    }

    /**
     * 创建新会话
     */
    fun createSession(name: String): String {
        return "${tmuxCmd()} new-session -d -s \"$name\""
    }

    /**
     * 删除会话
     */
    fun deleteSession(name: String): String {
        return "${tmuxCmd()} kill-session -t \"$name\""
    }

    /**
     * 重命名会话
     */
    fun renameSession(oldName: String, newName: String): String {
        return "${tmuxCmd()} rename-session -t \"$oldName\" \"$newName\""
    }

    /**
     * 获取会话活动状态
     * 检查会话是否有活动（有窗口被附加）
     */
    fun getSessionActivity(name: String): String {
        return "${tmuxCmd()} display -p -t \"$name\" '#{session_attached}'"
    }

    /**
     * 附加到会话（用于检查会话是否存在）
     */
    fun hasSession(name: String): String {
        return "${tmuxCmd()} has-session -t \"$name\" 2>/dev/null && echo 'exists' || echo 'not_exists'"
    }

    /**
     * 发送命令到指定会话（不包含 Enter）
     */
    fun sendKeys(sessionName: String, command: String): String {
        return "${tmuxCmd()} send-keys -t \"$sessionName\" \"$command\""
    }

    /**
     * 发送 Enter 键到指定会话
     */
    fun sendEnter(sessionName: String): String {
        return "${tmuxCmd()} send-keys -t \"$sessionName\" Enter"
    }

    /**
     * 发送上箭头键到指定会话
     */
    fun sendUp(sessionName: String): String {
        return "${tmuxCmd()} send-keys -t \"$sessionName\" Up"
    }

    /**
     * 发送下箭头键到指定会话
     */
    fun sendDown(sessionName: String): String {
        return "${tmuxCmd()} send-keys -t \"$sessionName\" Down"
    }

    /**
     * 发送 Tab 键到指定会话
     */
    fun sendTab(sessionName: String): String {
        return "${tmuxCmd()} send-keys -t \"$sessionName\" Tab"
    }

    /**
     * 捕获会话输出
     */
    fun capturePane(sessionName: String, lines: Int = 100): String {
        return "${tmuxCmd()} capture-pane -t \"$sessionName\" -p -S -$lines"
    }

    /**
     * 获取会话信息
     */
    fun getSessionInfo(name: String): String {
        return "${tmuxCmd()} display-message -t \"$name\" -p '#{session_name}:#{session_windows}:#{session_created}:#{session_activity_string}'"
    }

    /**
     * 检查tmux是否安装
     */
    fun checkTmuxInstalled(): String {
        // 使用完整 PATH 检查 tmux
        return "export PATH=/usr/local/bin:/usr/local/sbin:/opt/homebrew/bin:/opt/homebrew/sbin:/usr/bin:/usr/sbin:/bin:/sbin && tmux -V 2>&1"
    }

    /**
     * 启动tmux服务器（如果未运行）
     */
    fun startServer(): String {
        return "${tmuxCmd()} start-server"
    }
}
