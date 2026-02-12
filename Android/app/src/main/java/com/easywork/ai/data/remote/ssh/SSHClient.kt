package com.easywork.ai.data.remote.ssh

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties

/**
 * SSH客户端
 */
class SSHClient(private val config: SSHConfig) {

    private var session: Session? = null
    private var sshSession: SSHSession? = null

    val isConnected: Boolean
        get() = session?.isConnected == true

    val activeSession: SSHSession?
        get() = sshSession

    /**
     * 连接到SSH服务器
     */
    suspend fun connect(): SSHSession = withContext(Dispatchers.IO) {
        if (isConnected) {
            return@withContext sshSession ?: throw Exception("Session is null")
        }

        try {
            android.util.Log.d("SSHClient", "Connecting to ${config.host}:${config.port} with user ${config.username}")
            android.util.Log.d("SSHClient", "Password length: ${config.password.length}")

            val jsch = JSch()
            val newSession = jsch.getSession(config.username, config.host, config.port)

            // 配置会话
            val properties = Properties()
            properties["StrictHostKeyChecking"] = "no"
            properties["UserKnownHostsFile"] = "/dev/null"
            properties["PreferredAuthentications"] = "password"
            newSession.setConfig(properties)

            newSession.timeout = config.timeout
            newSession.setPassword(config.password)

            android.util.Log.d("SSHClient", "Starting connection...")
            // 连接
            newSession.connect()
            android.util.Log.d("SSHClient", "Connected successfully")

            // 设置keep-alive
            newSession.setServerAliveInterval(config.keepAliveInterval)

            session = newSession
            sshSession = SSHSession(newSession, config)

            return@withContext sshSession!!
        } catch (e: Exception) {
            android.util.Log.e("SSHClient", "Connection failed", e)
            disconnect()
            throw Exception("Failed to connect to SSH server: ${e.message}", e)
        }
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        try {
            sshSession?.close()
            session?.disconnect()
        } catch (e: Exception) {
            // Ignore
        } finally {
            sshSession = null
            session = null
        }
    }

    /**
     * 测试连接
     */
    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val session = connect()
            val result = session.executeCommand("echo 'test'")
            disconnect()
            return@withContext result.contains("test")
        } catch (e: Exception) {
            return@withContext false
        }
    }

    /**
     * 执行命令
     */
    suspend fun executeCommand(command: String): String {
        val session = sshSession ?: throw Exception("Not connected")
        return session.executeCommand(command)
    }

    /**
     * 执行命令（流式）
     */
    suspend fun executeCommandStream(
        command: String,
        onOutput: (String) -> Unit,
        onError: (String) -> Unit
    ): Int {
        val session = sshSession ?: throw Exception("Not connected")
        return session.executeCommandStream(command, onOutput, onError)
    }

    /**
     * 打开交互式Shell
     */
    suspend fun openShell(
        onOutput: (String) -> Unit,
        onError: (String) -> Unit
    ): Pair<java.io.OutputStream, java.io.InputStream> {
        val session = sshSession ?: throw Exception("Not connected")
        return session.openShell(onOutput, onError)
    }

    /**
     * 重新连接
     */
    suspend fun reconnect(): SSHSession {
        disconnect()
        return connect()
    }
}
