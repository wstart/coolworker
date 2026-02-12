package com.easywork.ai.domain.usecase

import com.easywork.ai.data.remote.ssh.SSHClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Tab补全用例
 */
class AutoCompleteUseCase constructor() {

    /**
     * 获取Tab补全建议
     * @param input 当前输入的命令
     * @param sshClient SSH客户端
     * @return 补全建议列表
     */
    suspend operator fun invoke(
        sshClient: SSHClient,
        input: String
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            if (input.isBlank()) {
                return@withContext Result.success(emptyList())
            }

            // 获取当前目录文件列表用于补全
            val parts = input.split(" ")
            val lastPart = parts.lastOrNull() ?: ""

            // 如果是命令补全（第一个词）
            if (parts.size == 1) {
                val commands = listOf(
                    "ls", "cd", "pwd", "cat", "less", "grep", "find",
                    "tmux", "vim", "nano", "git", "npm", "node", "python",
                    "python3", "pip", "pip3", "yarn", "docker", "docker-compose",
                    "curl", "wget", "ssh", "scp", "rsync", "tar", "zip",
                    "unzip", "chmod", "chown", "mkdir", "rm", "cp", "mv",
                    "ps", "top", "htop", "kill", "killall", "systemctl",
                    "service", "journalctl", "tail", "head", "sort", "uniq"
                )

                val suggestions = commands.filter { it.startsWith(lastPart) }
                return@withContext Result.success(suggestions)
            }

            // 文件/目录补全
            val currentDir = if (parts.size > 1) {
                // 提取路径部分
                val pathPart = lastPart.substringBeforeLast("/", "")
                if (pathPart.startsWith("~") || pathPart.startsWith("/")) {
                    pathPart
                } else {
                    "./$pathPart"
                }
            } else {
                "."
            }

            try {
                // 获取目录内容
                val command = "ls -1 -a $currentDir 2>/dev/null"
                val output = sshClient.executeCommand(command)

                val files = output.lines().filter { it.isNotBlank() }
                val prefix = lastPart.substringAfterLast("/", "")

                val suggestions = files.filter { it.startsWith(prefix) }

                Result.success(suggestions)
            } catch (e: Exception) {
                // 如果获取失败，返回空列表
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取Tmux会话名补全
     */
    suspend fun completeTmuxSessions(
        sshClient: SSHClient,
        input: String
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            // 获取所有tmux会话
            val output = sshClient.executeCommand("tmux list-sessions -F '#{session_name}' 2>/dev/null")
            val sessions = output.lines().filter { it.isNotBlank() }

            val suggestions = sessions.filter { it.startsWith(input) }
            Result.success(suggestions)
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }
}
