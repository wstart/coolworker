package com.easywork.ai.domain.usecase

import com.easywork.ai.data.remote.tmux.TmuxServiceImpl
import com.easywork.ai.data.remote.ssh.SSHClient

/**
 * 发送命令用例
 */
class SendCommandUseCase constructor() {

    /**
     * 发送命令到Tmux会话
     */
    suspend operator fun invoke(
        tmuxService: TmuxServiceImpl,
        sessionName: String,
        command: String
    ): Result<Unit> {
        // 只检查是否为空字符串，允许特殊字符如 Enter (\r)、Tab (\t) 等
        if (command.isEmpty()) {
            return Result.failure(Exception("Command cannot be empty"))
        }

        return tmuxService.sendKeys(sessionName, command)
    }

    /**
     * 发送原始按键（包括 Enter、Ctrl+C 等特殊字符）
     */
    suspend fun sendKey(
        tmuxService: TmuxServiceImpl,
        sessionName: String,
        key: String
    ): Result<Unit> {
        if (key.isEmpty()) {
            return Result.failure(Exception("Key cannot be empty"))
        }
        return tmuxService.sendKeys(sessionName, key)
    }

    /**
     * 通过SSH执行命令
     */
    suspend fun executeCommand(
        sshClient: SSHClient,
        command: String
    ): Result<String> {
        return try {
            val output = sshClient.executeCommand(command)
            Result.success(output)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 通过SSH执行命令（流式输出）
     */
    suspend fun executeCommandStream(
        sshClient: SSHClient,
        command: String,
        onOutput: (String) -> Unit,
        onError: (String) -> Unit
    ): Result<Int> {
        return try {
            val exitCode = sshClient.executeCommandStream(command, onOutput, onError)
            Result.success(exitCode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
