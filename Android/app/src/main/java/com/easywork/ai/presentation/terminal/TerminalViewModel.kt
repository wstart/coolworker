package com.easywork.ai.presentation.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easywork.ai.data.remote.ssh.SSHClient
import com.easywork.ai.data.remote.tmux.TmuxServiceImpl
import com.easywork.ai.domain.model.Server
import com.easywork.ai.domain.model.TerminalLine
import com.easywork.ai.domain.model.TerminalLineType
import com.easywork.ai.domain.repository.IServerRepository
import com.easywork.ai.domain.usecase.SendCommandUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * 终端状态
 */
data class TerminalState(
    val serverId: String = "",
    val sessionName: String = "",
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val output: List<TerminalLine> = emptyList(),
    val currentInput: String = "",
    val suggestions: List<String> = emptyList(),
    val showSuggestions: Boolean = false,
    val error: String? = null
)

/**
 * 终端ViewModel
 */
class TerminalViewModel constructor(
    private val serverRepository: IServerRepository,
    private val sendCommandUseCase: SendCommandUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TerminalState())
    val state: StateFlow<TerminalState> = _state.asStateFlow()

    private var sshClient: SSHClient? = null
    private var tmuxService: TmuxServiceImpl? = null

    /**
     * 初始化
     */
    fun init(serverId: String, sessionName: String) {
        _state.value = _state.value.copy(
            serverId = serverId,
            sessionName = sessionName
        )
        loadServerAndConnect(serverId)
    }

    /**
     * 加载服务器并连接
     */
    private fun loadServerAndConnect(serverId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isConnecting = true, error = null)

            try {
                android.util.Log.d("TerminalViewModel", "Loading server: $serverId")
                // 只获取一次数据，而不是持续监听
                val server = serverRepository.getServerById(serverId).first()
                if (server != null) {
                    android.util.Log.d("TerminalViewModel", "Server loaded: ${server.name}")
                    connectToServer(server)
                } else {
                    _state.value = _state.value.copy(
                        error = "Server not found",
                        isConnecting = false
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("TerminalViewModel", "Failed to load server", e)
                _state.value = _state.value.copy(
                    error = "Failed to load server: ${e.message}",
                    isConnecting = false
                )
            }
        }
    }

    /**
     * 连接到服务器
     */
    private fun connectToServer(server: Server) {
        viewModelScope.launch {
            try {
                android.util.Log.d("TerminalViewModel", "Connecting to server...")
                _state.value = _state.value.copy(isConnecting = true)

                // 重新建立 SSH 和 tmux 连接
                val config = com.easywork.ai.data.remote.ssh.SSHConfig.fromServer(server)
                val client = com.easywork.ai.data.remote.ssh.SSHClient(config)
                val session = client.connect()

                val service = com.easywork.ai.data.remote.tmux.TmuxServiceImpl(client)

                // 检查 tmux 是否可用
                val isAvailable = service.isTmuxInstalled()
                if (!isAvailable) {
                    throw Exception("Tmux is not available")
                }

                sshClient = client
                tmuxService = service

                _state.value = _state.value.copy(
                    isConnected = true,
                    isConnecting = false
                )

                // 添加欢迎消息
                addOutput(
                    "Connected to ${server.getDisplayAddress()}",
                    TerminalLineType.SYSTEM
                )
                addOutput(
                    "Session: ${_state.value.sessionName}",
                    TerminalLineType.SYSTEM
                )

                android.util.Log.d("TerminalViewModel", "Connection established, starting refresh")
                // 立即获取终端内容
                refreshOutput()

                // 启动定期刷新（每2秒刷新一次）
                startAutoRefresh()
            } catch (e: Exception) {
                android.util.Log.e("TerminalViewModel", "Failed to connect", e)
                _state.value = _state.value.copy(
                    error = "Failed to connect: ${e.message}",
                    isConnecting = false
                )
            }
        }
    }

    /**
     * 启动自动刷新
     */
    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (_state.value.isConnected) {
                delay(2000) // 每2秒刷新一次
                if (_state.value.isConnected) {
                    refreshOutput()
                }
            }
        }
    }

    /**
     * 设置SSH和Tmux服务（从SessionList传递）
     */
    fun setServices(client: SSHClient, service: TmuxServiceImpl) {
        sshClient = client
        tmuxService = service
        _state.value = _state.value.copy(isConnected = true)
    }

    /**
     * 添加输出
     */
    private fun addOutput(content: String, type: TerminalLineType = TerminalLineType.OUTPUT) {
        _state.value = _state.value.copy(
            output = _state.value.output + TerminalLine(content, type)
        )
    }

    /**
     * 发送命令
     */
    fun sendCommand(command: String) {
        viewModelScope.launch {
            if (command.isBlank()) return@launch

            // 显示命令
            addOutput("$ ${command.trim()}", TerminalLineType.COMMAND)

            try {
                sshClient?.let { client ->
                    tmuxService?.let { service ->
                        // 第一步：发送命令（不包含 Enter）
                        android.util.Log.d("TerminalViewModel", "Sending command: $command")
                        val sendResult = sendCommandUseCase.invoke(service, _state.value.sessionName, command)
                        sendResult.getOrNull()?.let {
                            android.util.Log.d("TerminalViewModel", "Command sent successfully")
                        } ?: sendResult.exceptionOrNull()?.let { e ->
                            android.util.Log.e("TerminalViewModel", "Failed to send command", e)
                            addOutput("Error: ${e.message}", TerminalLineType.ERROR)
                        }

                        // 第二步：单独发送 Enter 键
                        android.util.Log.d("TerminalViewModel", "Sending Enter key")
                        val enterResult = service.sendEnter(_state.value.sessionName)
                        enterResult.getOrNull()?.let {
                            android.util.Log.d("TerminalViewModel", "Enter sent successfully")
                        } ?: enterResult.exceptionOrNull()?.let { e ->
                            android.util.Log.e("TerminalViewModel", "Failed to send Enter", e)
                            addOutput("Error: Failed to send Enter: ${e.message}", TerminalLineType.ERROR)
                        }

                        // 立即刷新输出
                        refreshOutput()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TerminalViewModel", "Exception while sending command", e)
                addOutput("Error: ${e.message}", TerminalLineType.ERROR)
            }

            _state.value = _state.value.copy(currentInput = "")
        }
    }

    /**
     * 上一个命令（发送上箭头到终端）
     */
    fun previousCommand() {
        viewModelScope.launch {
            try {
                sshClient?.let { client ->
                    tmuxService?.let { service ->
                        // 发送上箭头键到终端
                        android.util.Log.d("TerminalViewModel", "Sending Up arrow to terminal")
                        val result = service.sendUp(_state.value.sessionName)
                        result.getOrNull()?.let {
                            android.util.Log.d("TerminalViewModel", "Up arrow sent")
                            refreshOutput()
                        } ?: result.exceptionOrNull()?.let { e ->
                            android.util.Log.e("TerminalViewModel", "Failed to send Up", e)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TerminalViewModel", "Exception sending Up", e)
            }
        }
    }

    /**
     * 下一个命令（发送下箭头到终端）
     */
    fun nextCommand() {
        viewModelScope.launch {
            try {
                sshClient?.let { client ->
                    tmuxService?.let { service ->
                        // 发送下箭头键到终端
                        android.util.Log.d("TerminalViewModel", "Sending Down arrow to terminal")
                        val result = service.sendDown(_state.value.sessionName)
                        result.getOrNull()?.let {
                            android.util.Log.d("TerminalViewModel", "Down arrow sent")
                            refreshOutput()
                        } ?: result.exceptionOrNull()?.let { e ->
                            android.util.Log.e("TerminalViewModel", "Failed to send Down", e)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TerminalViewModel", "Exception sending Down", e)
            }
        }
    }

    /**
     * 发送 Ctrl+C (中断信号)
     */
    fun sendCtrlC() {
        viewModelScope.launch {
            try {
                sshClient?.let { client ->
                    tmuxService?.let { service ->
                        // 发送 Ctrl+C (ASCII 3)
                        android.util.Log.d("TerminalViewModel", "Sending Ctrl+C")
                        val result = sendCommandUseCase(service, _state.value.sessionName, "\u0003")
                        result.getOrNull()?.let {
                            android.util.Log.d("TerminalViewModel", "Ctrl+C sent")
                            addOutput("^C", TerminalLineType.COMMAND)
                            refreshOutput()
                        } ?: result.exceptionOrNull()?.let { e ->
                            android.util.Log.e("TerminalViewModel", "Failed to send Ctrl+C", e)
                            addOutput("Error: ${e.message}", TerminalLineType.ERROR)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TerminalViewModel", "Exception while sending Ctrl+C", e)
                addOutput("Error: ${e.message}", TerminalLineType.ERROR)
            }
        }
    }

    /**
     * 执行Tab补全（发送 Tab 键到终端）
     */
    fun performTabCompletion() {
        viewModelScope.launch {
            try {
                sshClient?.let { client ->
                    tmuxService?.let { service ->
                        // 发送 Tab 键到终端
                        android.util.Log.d("TerminalViewModel", "Sending Tab to terminal")
                        val result = service.sendTab(_state.value.sessionName)
                        result.getOrNull()?.let {
                            android.util.Log.d("TerminalViewModel", "Tab sent")
                            refreshOutput()
                        } ?: result.exceptionOrNull()?.let { e ->
                            android.util.Log.e("TerminalViewModel", "Failed to send Tab", e)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TerminalViewModel", "Exception sending Tab", e)
            }
        }
    }

    /**
     * 隐藏建议
     */
    fun hideSuggestions() {
        _state.value = _state.value.copy(showSuggestions = false)
    }

    /**
     * 更新输入
     */
    fun updateInput(input: String) {
        _state.value = _state.value.copy(currentInput = input)
    }

    /**
     * 清除输出
     */
    fun clearOutput() {
        _state.value = _state.value.copy(output = emptyList())
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    /**
     * 刷新终端输出
     */
    fun refreshOutput() {
        viewModelScope.launch {
            try {
                android.util.Log.d("TerminalViewModel", "Refreshing output for session: ${_state.value.sessionName}")
                val result = tmuxService?.capturePane(_state.value.sessionName, 100)
                result?.getOrNull()?.let { output ->
                    android.util.Log.d("TerminalViewModel", "Captured output: ${output.lines().size} lines")
                    _state.value = _state.value.copy(
                        output = output.lines().map { line ->
                            TerminalLine(line, TerminalLineType.OUTPUT)
                        }
                    )
                } ?: run {
                    android.util.Log.e("TerminalViewModel", "Failed to capture pane: tmuxService is null or result is null")
                }
            } catch (e: Exception) {
                android.util.Log.e("TerminalViewModel", "Failed to refresh output", e)
                _state.value = _state.value.copy(error = "Failed to refresh: ${e.message}")
            }
        }
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        sshClient = null
        tmuxService = null
        _state.value = _state.value.copy(isConnected = false)
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
