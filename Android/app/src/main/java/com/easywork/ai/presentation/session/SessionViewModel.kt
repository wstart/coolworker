package com.easywork.ai.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easywork.ai.data.remote.ssh.SSHClient
import com.easywork.ai.data.remote.tmux.TmuxServiceImpl
import com.easywork.ai.domain.model.TmuxSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 会话详情状态
 */
data class SessionDetailState(
    val session: TmuxSession? = null,
    val isConnected: Boolean = false,
    val error: String? = null
)

/**
 * 会话详情ViewModel
 */
class SessionViewModel constructor() : ViewModel() {

    private val _state = MutableStateFlow(SessionDetailState())
    val state: StateFlow<SessionDetailState> = _state.asStateFlow()

    private var sshClient: SSHClient? = null
    private var tmuxService: TmuxServiceImpl? = null

    /**
     * 初始化SSH和Tmux服务
     */
    fun initServices(client: SSHClient, service: TmuxServiceImpl) {
        sshClient = client
        tmuxService = service
        _state.value = _state.value.copy(isConnected = true)
    }

    /**
     * 设置当前会话
     */
    fun setSession(session: TmuxSession) {
        _state.value = _state.value.copy(session = session)
    }

    /**
     * 重命名会话
     */
    fun renameSession(newName: String) {
        viewModelScope.launch {
            try {
                val oldName = _state.value.session?.name ?: return@launch
                tmuxService?.renameSession(oldName, newName)
                _state.value = _state.value.copy(
                    session = _state.value.session?.copy(name = newName)
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Failed to rename: ${e.message}")
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
