package com.easywork.ai.presentation.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easywork.ai.domain.model.Server
import com.easywork.ai.domain.repository.IServerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 服务器编辑状态
 */
data class ServerEditState(
    val server: Server? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

/**
 * 服务器编辑ViewModel
 */
class ServerEditViewModel constructor(
    private val serverRepository: IServerRepository,
    private val serverId: String
) : ViewModel() {

    private val _state = MutableStateFlow(ServerEditState())
    val state: StateFlow<ServerEditState> = _state.asStateFlow()

    init {
        if (serverId.isNotEmpty()) {
            loadServer()
        }
    }

    /**
     * 加载服务器信息
     */
    private fun loadServer() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                serverRepository.getServerById(serverId).collect { server ->
                    if (server != null) {
                        _state.value = _state.value.copy(
                            server = server,
                            isLoading = false
                        )
                    } else {
                        _state.value = _state.value.copy(
                            error = "Server not found",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to load server: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * 保存服务器
     */
    fun saveServer(
        name: String,
        host: String,
        port: String,
        username: String,
        password: String
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)

            try {
                // 验证输入
                if (name.isBlank()) {
                    _state.value = _state.value.copy(
                        error = "Server name is required",
                        isSaving = false
                    )
                    return@launch
                }

                if (host.isBlank()) {
                    _state.value = _state.value.copy(
                        error = "Host is required",
                        isSaving = false
                    )
                    return@launch
                }

                val portInt = port.toIntOrNull()
                if (portInt == null || portInt !in 1..65535) {
                    _state.value = _state.value.copy(
                        error = "Port must be between 1 and 65535",
                        isSaving = false
                    )
                    return@launch
                }

                if (username.isBlank()) {
                    _state.value = _state.value.copy(
                        error = "Username is required",
                        isSaving = false
                    )
                    return@launch
                }

                if (password.isBlank()) {
                    _state.value = _state.value.copy(
                        error = "Password is required",
                        isSaving = false
                    )
                    return@launch
                }

                // 创建或更新服务器
                val server = Server(
                    id = serverId,
                    name = name.trim(),
                    host = host.trim(),
                    port = portInt,
                    username = username.trim(),
                    password = password,
                    createdAt = _state.value.server?.createdAt ?: System.currentTimeMillis(),
                    lastConnectedAt = _state.value.server?.lastConnectedAt ?: 0L
                )

                if (serverId.isEmpty()) {
                    serverRepository.addServer(server)
                } else {
                    serverRepository.updateServer(server)
                }

                _state.value = _state.value.copy(
                    isSaving = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to save server: ${e.message}",
                    isSaving = false
                )
            }
        }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
