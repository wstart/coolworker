package com.easywork.ai.di

import com.easywork.ai.data.local.datastore.ServerDataStore
import com.easywork.ai.data.repository.ServerRepository
import com.easywork.ai.data.repository.TmuxRepository
import com.easywork.ai.domain.repository.IServerRepository
import com.easywork.ai.domain.repository.ITmuxRepository
import com.easywork.ai.domain.usecase.AutoCompleteUseCase
import com.easywork.ai.domain.usecase.ConnectServerUseCase
import com.easywork.ai.domain.usecase.CreateTmuxSessionUseCase
import com.easywork.ai.domain.usecase.ListTmuxSessionsUseCase
import com.easywork.ai.domain.usecase.SendCommandUseCase

/**
 * 简单的依赖注入容器
 * 替代 Hilt，避免兼容性问题
 */
object AppContainer {

    // DataStore
    private lateinit var serverDataStore: ServerDataStore

    // Repositories
    private val serverRepository: ServerRepository by lazy { ServerRepository(serverDataStore) }
    private val tmuxRepository: TmuxRepository by lazy { TmuxRepository() }

    // Use Cases
    private val connectServerUseCase: ConnectServerUseCase by lazy { ConnectServerUseCase() }
    private val listTmuxSessionsUseCase: ListTmuxSessionsUseCase by lazy { ListTmuxSessionsUseCase() }
    private val createTmuxSessionUseCase: CreateTmuxSessionUseCase by lazy { CreateTmuxSessionUseCase() }
    private val sendCommandUseCase: SendCommandUseCase by lazy { SendCommandUseCase() }
    private val autoCompleteUseCase: AutoCompleteUseCase by lazy { AutoCompleteUseCase() }

    /**
     * 初始化容器（在 Application.onCreate 中调用）
     */
    fun init(context: android.content.Context) {
        serverDataStore = ServerDataStore(context)
    }

    // 提供依赖的方法
    fun provideServerRepository(): IServerRepository = serverRepository
    fun provideTmuxRepository(): ITmuxRepository = tmuxRepository
    fun provideConnectServerUseCase(): ConnectServerUseCase = connectServerUseCase
    fun provideListTmuxSessionsUseCase(): ListTmuxSessionsUseCase = listTmuxSessionsUseCase
    fun provideCreateTmuxSessionUseCase(): CreateTmuxSessionUseCase = createTmuxSessionUseCase
    fun provideSendCommandUseCase(): SendCommandUseCase = sendCommandUseCase
    fun provideAutoCompleteUseCase(): AutoCompleteUseCase = autoCompleteUseCase
}
