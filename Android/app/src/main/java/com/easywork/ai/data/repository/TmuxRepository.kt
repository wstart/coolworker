package com.easywork.ai.data.repository

import com.easywork.ai.data.remote.ssh.SSHClient
import com.easywork.ai.data.remote.tmux.TmuxServiceImpl
import com.easywork.ai.domain.model.TmuxSession
import com.easywork.ai.domain.repository.ITmuxRepository
import kotlinx.coroutines.flow.Flow

/**
 * Tmux仓库实现
 */
class TmuxRepository : ITmuxRepository {

    private var tmuxService: TmuxServiceImpl? = null

    override suspend fun initialize(sshClient: SSHClient): Result<Unit> {
        return try {
            tmuxService = TmuxServiceImpl(sshClient)

            // 检查tmux是否安装
            val isInstalled = tmuxService?.isTmuxInstalled() == true
            if (!isInstalled) {
                tmuxService = null
                return Result.failure(Exception("Tmux is not installed on the server"))
            }

            // 启动tmux服务器
            tmuxService?.startTmuxServer()

            Result.success(Unit)
        } catch (e: Exception) {
            tmuxService = null
            Result.failure(e)
        }
    }

    override suspend fun isTmuxAvailable(): Boolean {
        return tmuxService?.isTmuxInstalled() == true
    }

    override suspend fun listSessions(): Result<List<TmuxSession>> {
        val service = tmuxService ?: return Result.failure(Exception("Tmux service not initialized"))
        return service.listSessions()
    }

    override suspend fun createSession(name: String): Result<TmuxSession> {
        val service = tmuxService ?: return Result.failure(Exception("Tmux service not initialized"))
        return service.createSession(name)
    }

    override suspend fun deleteSession(name: String): Result<Unit> {
        val service = tmuxService ?: return Result.failure(Exception("Tmux service not initialized"))
        return service.deleteSession(name)
    }

    override suspend fun renameSession(oldName: String, newName: String): Result<Unit> {
        val service = tmuxService ?: return Result.failure(Exception("Tmux service not initialized"))
        return service.renameSession(oldName, newName)
    }

    override suspend fun sendKeys(sessionName: String, command: String): Result<Unit> {
        val service = tmuxService ?: return Result.failure(Exception("Tmux service not initialized"))
        return service.sendKeys(sessionName, command)
    }

    override suspend fun capturePane(sessionName: String, lines: Int): Result<String> {
        val service = tmuxService ?: return Result.failure(Exception("Tmux service not initialized"))
        return service.capturePane(sessionName, lines)
    }

    override fun monitorSessions(intervalMs: Long): Flow<List<TmuxSession>> {
        val service = tmuxService ?: throw IllegalStateException("Tmux service not initialized")
        return service.monitorSessions(intervalMs)
    }

    override fun stopMonitoring() {
        tmuxService?.stopMonitoring()
    }

    override fun getTmuxService(): TmuxServiceImpl? {
        return tmuxService
    }
}
