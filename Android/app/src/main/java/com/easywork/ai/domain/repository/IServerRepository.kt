package com.easywork.ai.domain.repository

import com.easywork.ai.domain.model.Server
import kotlinx.coroutines.flow.Flow

/**
 * 服务器仓库接口
 */
interface IServerRepository {
    /**
     * 获取所有服务器
     */
    fun getAllServers(): Flow<List<Server>>

    /**
     * 根据ID获取服务器
     */
    fun getServerById(id: String): Flow<Server?>

    /**
     * 添加服务器
     */
    suspend fun addServer(server: Server)

    /**
     * 更新服务器
     */
    suspend fun updateServer(server: Server)

    /**
     * 删除服务器
     */
    suspend fun deleteServer(id: String)

    /**
     * 更新最后连接时间
     */
    suspend fun updateLastConnectedTime(id: String)
}
