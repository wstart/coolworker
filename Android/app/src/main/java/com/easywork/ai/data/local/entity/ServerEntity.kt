package com.easywork.ai.data.local.entity

import com.easywork.ai.domain.model.Server

/**
 * 服务器配置实体，用于数据持久化
 */
data class ServerEntity(
    val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val createdAt: Long,
    val lastConnectedAt: Long
) {
    /**
     * 转换为领域模型
     */
    fun toDomain(): Server {
        return Server(
            id = id,
            name = name,
            host = host,
            port = port,
            username = username,
            password = password,
            createdAt = createdAt,
            lastConnectedAt = lastConnectedAt
        )
    }

    /**
     * 转换为存储字符串（JSON格式）
     */
    fun toJson(): String {
        return """{"id":"$id","name":"$name","host":"$host","port":$port,"username":"$username","password":"$password","createdAt":$createdAt,"lastConnectedAt":$lastConnectedAt}"""
    }

    companion object {
        /**
         * 从领域模型转换为实体
         */
        fun fromDomain(server: Server): ServerEntity {
            return ServerEntity(
                id = server.id.ifEmpty { generateId() },
                name = server.name,
                host = server.host,
                port = server.port,
                username = server.username,
                password = server.password,
                createdAt = server.createdAt,
                lastConnectedAt = server.lastConnectedAt
            )
        }

        private fun generateId(): String {
            return System.currentTimeMillis().toString()
        }

        /**
         * 从JSON字符串解析
         */
        fun fromJson(json: String): ServerEntity? {
            return try {
                val cleanJson = json.trim()
                val id = cleanJson.extract("id")
                val name = cleanJson.extract("name")
                val host = cleanJson.extract("host")
                val port = cleanJson.extract("port").toIntOrNull() ?: 22
                val username = cleanJson.extract("username")
                val password = cleanJson.extract("password")
                val createdAt = cleanJson.extract("createdAt").toLongOrNull() ?: System.currentTimeMillis()
                val lastConnectedAt = cleanJson.extract("lastConnectedAt").toLongOrNull() ?: 0L

                ServerEntity(
                    id = id,
                    name = name,
                    host = host,
                    port = port,
                    username = username,
                    password = password,
                    createdAt = createdAt,
                    lastConnectedAt = lastConnectedAt
                )
            } catch (e: Exception) {
                null
            }
        }

        private fun String.extract(key: String): String {
            val pattern = """"$key"\s*:\s*"([^"]*)"""".toRegex()
            val result = pattern.find(this)
            if (result != null) return result.groupValues[1]

            // 尝试数字格式
            val numPattern = """"$key"\s*:\s*(\d+)""".toRegex()
            val numResult = numPattern.find(this)
            return numResult?.groupValues?.get(1) ?: ""
        }
    }
}
