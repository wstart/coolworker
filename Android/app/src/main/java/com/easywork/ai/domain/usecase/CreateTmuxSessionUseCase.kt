package com.easywork.ai.domain.usecase

import com.easywork.ai.data.remote.tmux.TmuxServiceImpl
import com.easywork.ai.domain.model.TmuxSession

/**
 * 创建Tmux会话用例
 */
class CreateTmuxSessionUseCase constructor() {

    suspend operator fun invoke(tmuxService: TmuxServiceImpl, name: String): Result<TmuxSession> {
        // 验证会话名称
        if (name.isBlank()) {
            return Result.failure(Exception("Session name cannot be empty"))
        }

        // 检查名称是否包含非法字符
        if (name.contains(Regex("[^a-zA-Z0-9_-]"))) {
            return Result.failure(Exception("Session name can only contain letters, numbers, underscore and hyphen"))
        }

        return tmuxService.createSession(name)
    }
}
