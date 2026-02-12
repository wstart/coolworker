package com.easywork.ai.domain.usecase

import com.easywork.ai.data.remote.tmux.TmuxServiceImpl
import com.easywork.ai.domain.model.TmuxSession

/**
 * 列出Tmux会话用例
 */
class ListTmuxSessionsUseCase constructor() {

    suspend operator fun invoke(tmuxService: TmuxServiceImpl): Result<List<TmuxSession>> {
        return tmuxService.listSessions()
    }
}
