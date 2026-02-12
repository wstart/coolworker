package com.easywork.ai.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.easywork.ai.data.local.entity.ServerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore 扩展
 */
private val Context.serverDataStore: DataStore<List<ServerEntity>> by dataStore(
    fileName = "servers.json",
    serializer = ServerListSerializer
)

/**
 * 服务器数据仓库
 */
class ServerDataStore(private val context: Context) {

    /**
     * 获取所有服务器
     */
    val servers: Flow<List<ServerEntity>> = context.serverDataStore.data

    /**
     * 获取服务器列表转换为领域模型
     */
    val serverList: Flow<List<com.easywork.ai.domain.model.Server>> = context.serverDataStore.data.map { entities ->
        entities.map { it.toDomain() }
    }

    /**
     * 添加服务器
     */
    suspend fun addServer(server: com.easywork.ai.domain.model.Server) {
        context.serverDataStore.updateData { currentList ->
            val entity = ServerEntity.fromDomain(server)
            currentList + entity
        }
    }

    /**
     * 更新服务器
     */
    suspend fun updateServer(server: com.easywork.ai.domain.model.Server) {
        context.serverDataStore.updateData { currentList ->
            val entity = ServerEntity.fromDomain(server)
            currentList.map {
                if (it.id == server.id) entity else it
            }
        }
    }

    /**
     * 删除服务器
     */
    suspend fun deleteServer(serverId: String) {
        context.serverDataStore.updateData { currentList ->
            currentList.filter { it.id != serverId }
        }
    }

    /**
     * 根据ID获取服务器
     */
    fun getServerById(serverId: String): Flow<com.easywork.ai.domain.model.Server?> {
        return context.serverDataStore.data.map { entities ->
            entities.firstOrNull { it.id == serverId }?.toDomain()
        }
    }

    /**
     * 更新最后连接时间
     */
    suspend fun updateLastConnectedTime(serverId: String) {
        context.serverDataStore.updateData { currentList ->
            currentList.map { entity ->
                if (entity.id == serverId) {
                    entity.copy(lastConnectedAt = System.currentTimeMillis())
                } else {
                    entity
                }
            }
        }
    }
}
