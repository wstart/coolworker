package com.easywork.ai.data.remote.ssh

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.Properties

/**
 * SSH会话包装类
 */
class SSHSession(
    private val session: Session,
    private val config: SSHConfig
) {
    private var currentChannel: Channel? = null
    private var inputStream: InputStream? = null
    private var outputStream: java.io.OutputStream? = null
    private var shellJob: Job? = null

    val isConnected: Boolean
        get() = session.isConnected

    val sessionId: String
        get() = "${config.host}:${config.port}@${config.username}"

    /**
     * 执行命令并返回输出
     */
    suspend fun executeCommand(command: String): String = withContext(Dispatchers.IO) {
        ensureConnected()

        val channelExec = session.openChannel("exec") as ChannelExec
        currentChannel = channelExec

        channelExec.setCommand(command)

        val inputStream = channelExec.inputStream
        val errorStream = channelExec.errStream

        channelExec.connect()

        val output = StringBuilder()
        val buffer = ByteArray(1024)

        // 读取标准输出
        while (true) {
            while (inputStream.available() > 0) {
                val len = inputStream.read(buffer)
                if (len > 0) {
                    output.append(String(buffer, 0, len))
                }
            }
            if (channelExec.isClosed) {
                if (inputStream.available() > 0) continue
                break
            }
            Thread.sleep(100)
        }

        // 读取错误输出
        val error = StringBuilder()
        while (errorStream.available() > 0) {
            val len = errorStream.read(buffer)
            if (len > 0) {
                error.append(String(buffer, 0, len))
            }
        }

        channelExec.disconnect()

        if (error.isNotEmpty() && output.isEmpty()) {
            throw Exception("SSH Command Error: $error")
        }

        output.toString().trim()
    }

    /**
     * 执行命令（流式输出）
     */
    suspend fun executeCommandStream(
        command: String,
        onOutput: (String) -> Unit,
        onError: (String) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        ensureConnected()

        val channelExec = session.openChannel("exec") as ChannelExec
        currentChannel = channelExec

        channelExec.setCommand(command)

        val inputStream = channelExec.inputStream
        val errorStream = channelExec.errStream
        val outputStream = channelExec.outputStream

        channelExec.connect()

        val buffer = ByteArray(1024)

        try {
            // 读取标准输出
            while (!channelExec.isClosed) {
                while (inputStream.available() > 0) {
                    val len = inputStream.read(buffer)
                    if (len > 0) {
                        onOutput(String(buffer, 0, len))
                    }
                }

                while (errorStream.available() > 0) {
                    val len = errorStream.read(buffer)
                    if (len > 0) {
                        onError(String(buffer, 0, len))
                    }
                }

                Thread.sleep(50)
            }

            // 读取剩余数据
            while (inputStream.available() > 0) {
                val len = inputStream.read(buffer)
                if (len > 0) {
                    onOutput(String(buffer, 0, len))
                }
            }

            return@withContext channelExec.exitStatus
        } finally {
            channelExec.disconnect()
        }
    }

    /**
     * 打开交互式Shell
     */
    suspend fun openShell(
        onOutput: (String) -> Unit,
        onError: (String) -> Unit
    ): Pair<java.io.OutputStream, InputStream> = withContext(Dispatchers.IO) {
        ensureConnected()

        val channelShell = session.openChannel("shell") as com.jcraft.jsch.ChannelShell
        currentChannel = channelShell

        // 设置PTY
        channelShell.setPtyType("xterm")
        channelShell.setPtySize(80, 24, 640, 480)

        inputStream = channelShell.inputStream
        outputStream = channelShell.outputStream

        channelShell.connect()

        // 启动输出读取协程
        shellJob = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(1024)
            while (!channelShell.isClosed) {
                try {
                    val len = inputStream?.read(buffer) ?: -1
                    if (len > 0) {
                        onOutput(String(buffer, 0, len))
                    }
                } catch (e: Exception) {
                    onError("Read error: ${e.message}")
                    break
                }
            }
        }

        Pair(outputStream!!, inputStream!!)
    }

    /**
     * 发送命令到Shell
     */
    fun sendToShell(command: String) {
        outputStream?.write("$command\n".toByteArray())
        outputStream?.flush()
    }

    /**
     * 确保连接有效
     */
    private fun ensureConnected() {
        if (!session.isConnected) {
            throw Exception("SSH session is not connected")
        }
    }

    /**
     * 关闭会话
     */
    fun close() {
        shellJob?.cancel()
        currentChannel?.disconnect()
        inputStream?.close()
        outputStream?.close()
        session.disconnect()
    }
}
