package com.easywork.ai.data.remote.tmux

import com.easywork.ai.data.remote.ssh.SSHClient
import com.easywork.ai.domain.model.TmuxSession
import com.easywork.ai.domain.model.TmuxSessionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Tmux服务实现
 */
class TmuxServiceImpl(
    private val sshClient: SSHClient
) {
    private var isMonitoring = false
    private var lastSessions: List<TmuxSession> = emptyList()

    /**
     * 检查tmux是否安装
     */
    suspend fun isTmuxInstalled(): Boolean = withContext(Dispatchers.IO) {
        try {
            val output = sshClient.executeCommand(TmuxCommand.checkTmuxInstalled())
            android.util.Log.d("TmuxServiceImpl", "checkTmuxInstalled output: $output")
            val available = TmuxParser.isTmuxAvailable(output)
            android.util.Log.d("TmuxServiceImpl", "isTmuxAvailable: $available")
            available
        } catch (e: Exception) {
            android.util.Log.e("TmuxServiceImpl", "checkTmuxInstalled failed", e)
            false
        }
    }

    /**
     * 启动tmux服务器
     */
    suspend fun startTmuxServer(): Boolean = withContext(Dispatchers.IO) {
        try {
            sshClient.executeCommand(TmuxCommand.startServer())
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 列出所有会话
     */
    suspend fun listSessions(): Result<List<TmuxSession>> = withContext(Dispatchers.IO) {
        try {
            val output = sshClient.executeCommand(TmuxCommand.listSessions())
            val sessions = TmuxParser.parseSessionList(output)

            // 检查每个会话的活动状态
            val sessionsWithState = sessions.map { session ->
                val activityOutput = sshClient.executeCommand(TmuxCommand.getSessionActivity(session.name))
                val attachedCount = TmuxParser.parseAttachedCount(activityOutput)
                session.copy(
                    state = if (attachedCount > 0) TmuxSessionState.ACTIVE else TmuxSessionState.INACTIVE
                )
            }

            lastSessions = sessionsWithState
            Result.success(sessionsWithState)
        } catch (e: Exception) {
            // 如果没有会话，tmux会返回错误，这是正常的
            if (e.message?.contains("no sessions") == true ||
                e.message?.contains("can't find session") == true) {
                Result.success(emptyList())
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * 创建新会话
     */
    suspend fun createSession(name: String): Result<TmuxSession> = withContext(Dispatchers.IO) {
        try {
            // 检查会话是否已存在
            val checkOutput = sshClient.executeCommand(TmuxCommand.hasSession(name))
            if (TmuxParser.parseSessionExists(checkOutput)) {
                return@withContext Result.failure(Exception("Session '$name' already exists"))
            }

            // 创建会话
            sshClient.executeCommand(TmuxCommand.createSession(name))

            // 获取创建的会话信息
            val sessions = listSessions().getOrNull()
            val newSession = sessions?.find { it.name == name }

            if (newSession != null) {
                Result.success(newSession)
            } else {
                Result.failure(Exception("Failed to create session"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除会话
     */
    suspend fun deleteSession(name: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            sshClient.executeCommand(TmuxCommand.deleteSession(name))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 重命名会话
     */
    suspend fun renameSession(oldName: String, newName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 检查新名称是否已存在
            val checkOutput = sshClient.executeCommand(TmuxCommand.hasSession(newName))
            if (TmuxParser.parseSessionExists(checkOutput)) {
                return@withContext Result.failure(Exception("Session '$newName' already exists"))
            }

            sshClient.executeCommand(TmuxCommand.renameSession(oldName, newName))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 发送命令到会话
     */
    suspend fun sendKeys(sessionName: String, command: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            sshClient.executeCommand(TmuxCommand.sendKeys(sessionName, command))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 发送 Enter 键到会话
     */
    suspend fun sendEnter(sessionName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("TmuxServiceImpl", "Sending Enter to session: $sessionName")
            sshClient.executeCommand(TmuxCommand.sendEnter(sessionName))
            android.util.Log.d("TmuxServiceImpl", "Enter sent successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("TmuxServiceImpl", "Failed to send Enter", e)
            Result.failure(e)
        }
    }

    /**
     * 发送上箭头键到会话
     */
    suspend fun sendUp(sessionName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("TmuxServiceImpl", "Sending Up to session: $sessionName")
            sshClient.executeCommand(TmuxCommand.sendUp(sessionName))
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("TmuxServiceImpl", "Failed to send Up", e)
            Result.failure(e)
        }
    }

    /**
     * 发送下箭头键到会话
     */
    suspend fun sendDown(sessionName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("TmuxServiceImpl", "Sending Down to session: $sessionName")
            sshClient.executeCommand(TmuxCommand.sendDown(sessionName))
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("TmuxServiceImpl", "Failed to send Down", e)
            Result.failure(e)
        }
    }

    /**
     * 发送 Tab 键到会话
     */
    suspend fun sendTab(sessionName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("TmuxServiceImpl", "Sending Tab to session: $sessionName")
            sshClient.executeCommand(TmuxCommand.sendTab(sessionName))
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("TmuxServiceImpl", "Failed to send Tab", e)
            Result.failure(e)
        }
    }

    /**
     * 捕获会话输出
     */
    suspend fun capturePane(sessionName: String, lines: Int = 100): Result<String> = withContext(Dispatchers.IO) {
        try {
            val output = sshClient.executeCommand(TmuxCommand.capturePane(sessionName, lines))
            val cleaned = TmuxParser.cleanAnsiEscape(output)
            Result.success(cleaned)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 监控会话活动
     * 返回Flow定期推送会话列表
     */
    fun monitorSessions(intervalMs: Long = 5000): Flow<List<TmuxSession>> = flow {
        isMonitoring = true
        while (isMonitoring) {
            try {
                val result = listSessions()
                result.getOrNull()?.let { sessions ->
                    emit(sessions)
                }
            } catch (e: Exception) {
                // 忽略错误，继续监控
            }
            delay(intervalMs)
        }
    }

    /**
     * 停止监控
     */
    fun stopMonitoring() {
        isMonitoring = false
    }

    /**
     * 检查会话是否有变化（新增、删除、状态改变）
     */
    fun hasSessionChanges(): Boolean {
        return true // 简化实现，始终返回true
    }

    /**
     * 获取新增的会话
     */
    fun getNewSessions(current: List<TmuxSession>): List<TmuxSession> {
        return current.filter { new ->
            lastSessions.none { it.name == new.name }
        }
    }

    /**
     * 获取删除的会话
     */
    fun getDeletedSessions(current: List<TmuxSession>): List<TmuxSession> {
        return lastSessions.filter { old ->
            current.none { it.name == old.name }
        }
    }

    /**
     * 获取状态改变的会话
     */
    fun getChangedSessions(current: List<TmuxSession>): List<TmuxSession> {
        return current.filter { new ->
            lastSessions.find { it.name == new.name }?.state != new.state
        }
    }
}
