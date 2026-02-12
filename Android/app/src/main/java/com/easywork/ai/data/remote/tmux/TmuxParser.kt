package com.easywork.ai.data.remote.tmux

import com.easywork.ai.domain.model.TmuxSession
import com.easywork.ai.domain.model.TmuxSessionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tmux输出解析器
 */
object TmuxParser {

    private val activityDateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())

    /**
     * 解析会话列表输出
     * 输入格式: session_name:window_count:last_activity
     * 输出: List<TmuxSession>
     */
    fun parseSessionList(output: String): List<TmuxSession> {
        if (output.isBlank()) return emptyList()

        return output.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { line -> parseSessionLine(line) }
    }

    /**
     * 解析单行会话信息
     * 格式: session_name:windows:last_activity
     */
    private fun parseSessionLine(line: String): TmuxSession? {
        return try {
            val parts = line.split(":")
            if (parts.size < 2) return null

            val name = parts[0]
            val windows = parts[1].toIntOrNull() ?: 1

            // 解析活动时间
            val lastActivityAt = if (parts.size >= 3) {
                parseActivityTime(parts[2])
            } else {
                System.currentTimeMillis()
            }

            TmuxSession(
                name = name,
                state = TmuxSessionState.INACTIVE,
                windows = windows,
                lastActivityAt = lastActivityAt
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 解析活动时间字符串
     * 格式:yyyyMMddHHmmss
     */
    private fun parseActivityTime(timeStr: String): Long {
        return try {
            // tmux返回的格式类似: [20250123153045
            val cleanTime = timeStr.replace("[", "").replace("]", "")
            if (cleanTime.matches(Regex("\\d{14}"))) {
                activityDateFormat.parse(cleanTime)?.time ?: System.currentTimeMillis()
            } else {
                System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    /**
     * 解析会话是否存在
     * 输出包含 "exists" 表示存在
     */
    fun parseSessionExists(output: String): Boolean {
        return output.contains("exists", ignoreCase = true)
    }

    /**
     * 解析附加状态
     * 返回附加到会话的客户端数量
     */
    fun parseAttachedCount(output: String): Int {
        return output.trim().toIntOrNull() ?: 0
    }

    /**
     * 解析tmux版本
     */
    fun parseTmuxVersion(output: String): String? {
        val regex = Regex("tmux\\s+(\\d+\\.\\d+)", RegexOption.IGNORE_CASE)
        val match = regex.find(output)
        return match?.groupValues?.get(1)
    }

    /**
     * 解析capture pane输出
     * 返回终端输出行列表
     */
    fun parseCapturePane(output: String): List<String> {
        return output.lines().filter { it.isNotEmpty() }
    }

    /**
     * 清理ANSI转义序列
     */
    fun cleanAnsiEscape(text: String): String {
        // 移除ANSI颜色代码和控制序列
        val ansiRegex = Regex("\\x1B\\[[0-9;]*[a-zA-Z]")
        return ansiRegex.replace(text, "")
    }

    /**
     * 检查tmux是否可用
     */
    fun isTmuxAvailable(output: String): Boolean {
        android.util.Log.d("TmuxParser", "Checking tmux availability in output: $output")

        // 检查是否包含 "tmux" 字样（版本信息、路径、或 whereis 输出）
        val hasTmux = output.contains("tmux", ignoreCase = true)
        // 检查是否包含路径
        val hasPath = output.contains("/") || output.contains("command")
        // 检查是否包含 "not found" 或 "not installed"
        val notFound = output.contains("not found", ignoreCase = true) ||
                       output.contains("not installed", ignoreCase = true) ||
                       output.contains("no tmux", ignoreCase = true)

        val available = hasTmux && !notFound
        android.util.Log.d("TmuxParser", "hasTmux: $hasTmux, hasPath: $hasPath, notFound: $notFound, Available: $available")
        return available
    }
}
