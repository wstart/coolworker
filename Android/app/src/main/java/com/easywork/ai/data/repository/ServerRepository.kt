package com.easywork.ai.data.repository

import com.easywork.ai.data.local.datastore.ServerDataStore
import com.easywork.ai.domain.model.Server
import com.easywork.ai.domain.repository.IServerRepository
import kotlinx.coroutines.flow.Flow

/**
 * 服务器仓库实现
 */
class ServerRepository(
    private val serverDataStore: ServerDataStore
) : IServerRepository {

    override fun getAllServers(): Flow<List<Server>> {
        return serverDataStore.serverList
    }

    override fun getServerById(id: String): Flow<Server?> {
        return serverDataStore.getServerById(id)
    }

    override suspend fun addServer(server: Server) {
        serverDataStore.addServer(server)
    }

    override suspend fun updateServer(server: Server) {
        serverDataStore.updateServer(server)
    }

    override suspend fun deleteServer(id: String) {
        serverDataStore.deleteServer(id)
    }

    override suspend fun updateLastConnectedTime(id: String) {
        serverDataStore.updateLastConnectedTime(id)
    }
}
