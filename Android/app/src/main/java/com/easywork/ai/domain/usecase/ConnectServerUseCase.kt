package com.easywork.ai.domain.usecase

import com.easywork.ai.data.remote.ssh.SSHClient
import com.easywork.ai.data.remote.ssh.SSHConfig
import com.easywork.ai.data.remote.tmux.TmuxServiceImpl
import com.easywork.ai.domain.model.Server

/**
 * 连接到服务器用例
 */
class ConnectServerUseCase constructor() {

    /**
     * 执行连接
     * @return Pair<SSHClient, TmuxService>
     */
    suspend operator fun invoke(server: Server): Result<Pair<SSHClient, TmuxServiceImpl>> {
        return try {
            android.util.Log.d("ConnectServerUseCase", "Connecting to server: ${server.name}")
            android.util.Log.d("ConnectServerUseCase", "Host: ${server.host}, Port: ${server.port}, User: ${server.username}")

            val config = SSHConfig.fromServer(server)
            val sshClient = SSHClient(config)
            val session = sshClient.connect()

            android.util.Log.d("ConnectServerUseCase", "SSH connected, checking tmux...")
            val tmuxService = TmuxServiceImpl(sshClient)

            // 检查tmux是否安装
            val isTmuxInstalled = tmuxService.isTmuxInstalled()
            android.util.Log.d("ConnectServerUseCase", "Tmux installed: $isTmuxInstalled")

            if (!isTmuxInstalled) {
                sshClient.disconnect()
                return Result.failure(Exception("Tmux is not installed on the server"))
            }

            // 启动tmux服务器
            tmuxService.startTmuxServer()

            android.util.Log.d("ConnectServerUseCase", "Connection successful")
            Result.success(Pair(sshClient, tmuxService))
        } catch (e: Exception) {
            android.util.Log.e("ConnectServerUseCase", "Connection failed", e)
            Result.failure(e)
        }
    }
}
