package com.easywork.ai.presentation.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easywork.ai.domain.model.Server
import com.easywork.ai.domain.repository.IServerRepository
import com.easywork.ai.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


/**
 * 服务器列表状态
 */
data class ServerListState(
    val servers: List<Server> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 服务器列表ViewModel
 */

class ServerListViewModel (
    private val serverRepository: IServerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ServerListState())
    val state: StateFlow<ServerListState> = _state.asStateFlow()

    private val _navigationEvents = MutableStateFlow<ServerListNavigationEvent?>(null)
    val navigationEvents: StateFlow<ServerListNavigationEvent?> = _navigationEvents.asStateFlow()

    init {
        loadServers()
    }

    /**
     * 加载服务器列表
     */
    private fun loadServers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            serverRepository.getAllServers().collect { servers ->
                _state.value = _state.value.copy(
                    servers = servers,
                    isLoading = false
                )
            }
        }
    }

    /**
     * 导航到编辑页面
     */
    fun onNavigateToEdit(serverId: String) {
        android.util.Log.d("ServerListViewModel", "onNavigateToEdit called with serverId: $serverId")
        _navigationEvents.value = ServerListNavigationEvent.NavigateToEdit(serverId)
        android.util.Log.d("ServerListViewModel", "Navigation event set: ${_navigationEvents.value}")
    }

    /**
     * 导航到会话列表
     */
    fun onNavigateToSessions(server: Server) {
        if (server.isValid()) {
            _navigationEvents.value = ServerListNavigationEvent.NavigateToSessions(server)
        } else {
            _state.value = _state.value.copy(error = "Invalid server configuration")
        }
    }

    /**
     * 删除服务器
     */
    fun onDeleteServer(server: Server) {
        viewModelScope.launch {
            try {
                serverRepository.deleteServer(server.id)
                _state.value = _state.value.copy(
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to delete server: ${e.message}"
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

    /**
     * 清除导航事件
     */
    fun clearNavigationEvent() {
        _navigationEvents.value = null
    }
}

/**
 * 导航事件
 */
sealed class ServerListNavigationEvent {
    data class NavigateToEdit(val serverId: String) : ServerListNavigationEvent()
    data class NavigateToSessions(val server: Server) : ServerListNavigationEvent()
}
