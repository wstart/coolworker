package com.easywork.ai.domain.model

/**
 * Tmux会话状态
 */
enum class TmuxSessionState {
    ACTIVE,      // 活动状态
    INACTIVE     // 非活动状态
}

/**
 * Tmux会话模型
 */
data class TmuxSession(
    val name: String,
    val state: TmuxSessionState = TmuxSessionState.INACTIVE,
    val windows: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActivityAt: Long = System.currentTimeMillis()
) {
    /**
     * 检查会话是否活跃
     */
    val isActive: Boolean
        get() = state == TmuxSessionState.ACTIVE

    /**
     * 获取状态显示文本
     */
    fun getStateText(): String {
        return if (isActive) "Active" else "Inactive"
    }
}
