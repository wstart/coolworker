package com.easywork.ai.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easywork.ai.data.remote.ssh.SSHClient
import com.easywork.ai.data.remote.ssh.SSHConfig
import com.easywork.ai.data.remote.tmux.TmuxServiceImpl
import com.easywork.ai.domain.model.Server
import com.easywork.ai.domain.model.TmuxSession
import com.easywork.ai.domain.repository.IServerRepository
import com.easywork.ai.domain.usecase.ConnectServerUseCase
import com.easywork.ai.domain.usecase.CreateTmuxSessionUseCase
import com.easywork.ai.domain.usecase.ListTmuxSessionsUseCase
import com.easywork.ai.domain.usecase.SendCommandUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 会话列表状态
 */
data class SessionListState(
    val server: Server? = null,
    val sessions: List<TmuxSession> = emptyList(),
    val isLoading: Boolean = false,
    val isConnecting: Boolean = false,
    val isCreating: Boolean = false,
    val showCreateDialog: Boolean = false,
    val error: String? = null
)

/**
 * 会话列表ViewModel
 */
class SessionListViewModel constructor(
    private val serverRepository: IServerRepository,
    private val connectServerUseCase: ConnectServerUseCase,
    private val listTmuxSessionsUseCase: ListTmuxSessionsUseCase,
    private val createTmuxSessionUseCase: CreateTmuxSessionUseCase,
    private val sendCommandUseCase: SendCommandUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SessionListState())
    val state: StateFlow<SessionListState> = _state.asStateFlow()

    private val _navigationEvents = MutableStateFlow<SessionListNavigationEvent?>(null)
    val navigationEvents: StateFlow<SessionListNavigationEvent?> = _navigationEvents.asStateFlow()

    private var sshClient: SSHClient? = null
    private var tmuxService: TmuxServiceImpl? = null

    /**
     * 初始化
     */
    fun init(serverId: String) {
        android.util.Log.d("SessionListViewModel", "init called with serverId: $serverId, current server: ${_state.value.server?.name}")
        if (_state.value.server == null) {
            loadServer(serverId)
        } else {
            android.util.Log.d("SessionListViewModel", "Server already loaded, skipping loadServer")
        }
    }

    /**
     * 加载服务器信息并连接
     */
    private fun loadServer(serverId: String) {
        viewModelScope.launch {
            android.util.Log.d("SessionListViewModel", "loadServer called with serverId: $serverId")
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                // 只获取一次数据，而不是持续监听
                serverRepository.getServerById(serverId).first()?.let { server ->
                    android.util.Log.d("SessionListViewModel", "Server loaded: ${server.name}")
                    _state.value = _state.value.copy(server = server)
                    connectToServer(server)
                } ?: run {
                    _state.value = _state.value.copy(
                        error = "Server not found",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("SessionListViewModel", "Failed to load server", e)
                _state.value = _state.value.copy(
                    error = "Failed to load server: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * 连接到服务器
     */
    private fun connectToServer(server: Server) {
        viewModelScope.launch {
            android.util.Log.d("SessionListViewModel", "connectToServer called, current sshClient: $sshClient")
            _state.value = _state.value.copy(isConnecting = true)

            try {
                val result = connectServerUseCase(server)
                result.getOrNull()?.let { (client, service) ->
                    android.util.Log.d("SessionListViewModel", "Connection successful")
                    sshClient = client
                    tmuxService = service

                    // 更新最后连接时间
                    serverRepository.updateLastConnectedTime(server.id)

                    // 加载会话列表
                    loadSessions()
                } ?: run {
                    android.util.Log.e("SessionListViewModel", "Connection result is null")
                    _state.value = _state.value.copy(
                        error = "Connection failed",
                        isConnecting = false,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("SessionListViewModel", "Connection error", e)
                _state.value = _state.value.copy(
                    error = "Failed to connect: ${e.message}",
                    isConnecting = false,
                    isLoading = false
                )
            }
        }
    }

    /**
     * 加载会话列表
     */
    private fun loadSessions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                tmuxService?.let { service ->
                    val result = listTmuxSessionsUseCase(service)
                    result.getOrNull()?.let { sessions ->
                        _state.value = _state.value.copy(
                            sessions = sessions,
                            isLoading = false,
                            isConnecting = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to load sessions: ${e.message}",
                    isLoading = false,
                    isConnecting = false
                )
            }
        }
    }

    /**
     * 刷新会话列表
     */
    fun refreshSessions() {
        loadSessions()
    }

    /**
     * 显示创建会话对话框
     */
    fun showCreateDialog() {
        _state.value = _state.value.copy(showCreateDialog = true)
    }

    /**
     * 隐藏创建会话对话框
     */
    fun hideCreateDialog() {
        _state.value = _state.value.copy(showCreateDialog = false)
    }

    /**
     * 创建会话
     */
    fun createSession(name: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCreating = true, error = null)

            try {
                tmuxService?.let { service ->
                    val result = createTmuxSessionUseCase(service, name)
                    result.getOrNull()?.let {
                        _state.value = _state.value.copy(
                            isCreating = false,
                            showCreateDialog = false
                        )
                        loadSessions()
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to create session: ${e.message}",
                    isCreating = false
                )
            }
        }
    }

    /**
     * 删除会话
     */
    fun deleteSession(session: TmuxSession) {
        viewModelScope.launch {
            try {
                tmuxService?.deleteSession(session.name)
                loadSessions()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to delete session: ${e.message}"
                )
            }
        }
    }

    /**
     * 重命名会话
     */
    fun renameSession(oldName: String, newName: String) {
        viewModelScope.launch {
            try {
                tmuxService?.renameSession(oldName, newName)
                loadSessions()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to rename session: ${e.message}"
                )
            }
        }
    }

    /**
     * 导航到终端
     */
    fun onNavigateToTerminal(session: TmuxSession) {
        val server = _state.value.server ?: return
        _navigationEvents.value = SessionListNavigationEvent.NavigateToTerminal(
            server.id,
            session.name
        )
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    /**
     * 清除导航事件
     */
    fun clearNavigationEvent() {
        _navigationEvents.value = null
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        sshClient?.disconnect()
        sshClient = null
        tmuxService = null
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}

/**
 * 导航事件
 */
sealed class SessionListNavigationEvent {
    data class NavigateToTerminal(val serverId: String, val sessionName: String) :
        SessionListNavigationEvent()
}
