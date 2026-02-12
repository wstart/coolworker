package com.easywork.ai.domain.repository

import com.easywork.ai.data.remote.tmux.TmuxServiceImpl
import com.easywork.ai.data.remote.ssh.SSHClient
import com.easywork.ai.domain.model.TmuxSession
import kotlinx.coroutines.flow.Flow

/**
 * Tmux仓库接口
 */
interface ITmuxRepository {
    /**
     * 初始化SSH连接
     */
    suspend fun initialize(sshClient: SSHClient): Result<Unit>

    /**
     * 检查tmux是否可用
     */
    suspend fun isTmuxAvailable(): Boolean

    /**
     * 获取所有会话
     */
    suspend fun listSessions(): Result<List<TmuxSession>>

    /**
     * 创建会话
     */
    suspend fun createSession(name: String): Result<TmuxSession>

    /**
     * 删除会话
     */
    suspend fun deleteSession(name: String): Result<Unit>

    /**
     * 重命名会话
     */
    suspend fun renameSession(oldName: String, newName: String): Result<Unit>

    /**
     * 发送命令到会话
     */
    suspend fun sendKeys(sessionName: String, command: String): Result<Unit>

    /**
     * 捕获会话输出
     */
    suspend fun capturePane(sessionName: String, lines: Int): Result<String>

    /**
     * 监控会话
     */
    fun monitorSessions(intervalMs: Long): Flow<List<TmuxSession>>

    /**
     * 停止监控
     */
    fun stopMonitoring()

    /**
     * 获取Tmux服务实例
     */
    fun getTmuxService(): TmuxServiceImpl?
}
