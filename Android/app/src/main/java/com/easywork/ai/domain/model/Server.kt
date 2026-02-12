package com.easywork.ai.domain.model

/**
 * SSH服务器配置模型
 */
data class Server(
    val id: String = "",
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String,
    val password: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastConnectedAt: Long = 0L
) {
    /**
     * 验证服务器配置是否有效
     */
    fun isValid(): Boolean {
        return name.isNotBlank() &&
                host.isNotBlank() &&
                port in 1..65535 &&
                username.isNotBlank() &&
                password.isNotBlank()
    }

    /**
     * 获取显示地址
     */
    fun getDisplayAddress(): String {
        return if (port == 22) host else "$host:$port"
    }
}
