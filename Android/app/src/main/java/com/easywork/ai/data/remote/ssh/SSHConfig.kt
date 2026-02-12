package com.easywork.ai.data.remote.ssh

import com.easywork.ai.domain.model.Server

/**
 * SSH配置类
 */
data class SSHConfig(
    val host: String,
    val port: Int = 22,
    val username: String,
    val password: String,
    val timeout: Int = 30000, // 30 seconds
    val keepAliveInterval: Int = 60000 // 60 seconds
) {
    companion object {
        /**
         * 从Server模型创建SSH配置
         */
        fun fromServer(server: Server): SSHConfig {
            return SSHConfig(
                host = server.host,
                port = server.port,
                username = server.username,
                password = server.password
            )
        }
    }
}
