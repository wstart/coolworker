package com.easywork.ai.domain.model

/**
 * 终端输出行类型
 */
enum class TerminalLineType {
    OUTPUT,      // 普通输出
    ERROR,       // 错误输出
    COMMAND,     // 命令输入
    SYSTEM       // 系统消息
}

/**
 * 终端输出行模型
 */
data class TerminalLine(
    val content: String,
    val type: TerminalLineType = TerminalLineType.OUTPUT,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * 获取格式化的时间戳
     */
    fun getFormattedTime(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
}
