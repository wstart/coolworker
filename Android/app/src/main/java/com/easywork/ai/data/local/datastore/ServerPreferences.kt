package com.easywork.ai.data.local.datastore

import androidx.datastore.core.Serializer
import com.easywork.ai.data.local.entity.ServerEntity
import java.io.InputStream
import java.io.OutputStream

/**
 * 服务器列表序列化器
 */
object ServerListSerializer : Serializer<List<ServerEntity>> {
    override val defaultValue: List<ServerEntity>
        get() = emptyList()

    override suspend fun readFrom(input: InputStream): List<ServerEntity> {
        return try {
            val jsonString = input.readBytes().decodeToString()
            if (jsonString.isBlank()) {
                return emptyList()
            }

            // 简单的JSON数组解析
            val servers = mutableListOf<ServerEntity>()
            val content = jsonString.trim()

            if (content == "[]") {
                return emptyList()
            }

            // 移除数组括号
            val arrayContent = if (content.startsWith("[") && content.endsWith("]")) {
                content.substring(1, content.length - 1)
            } else {
                content
            }

            // 分割JSON对象（考虑嵌套情况，简单处理）
            val items = splitJsonObjects(arrayContent)

            items.forEach { item ->
                val trimmed = item.trim()
                if (trimmed.isNotEmpty()) {
                    ServerEntity.fromJson(trimmed)?.let { servers.add(it) }
                }
            }

            servers
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 分割JSON对象
     */
    private fun splitJsonObjects(input: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var depth = 0
        var inString = false
        var escape = false

        for (char in input) {
            when {
                escape -> {
                    current.append(char)
                    escape = false
                }
                char == '\\' -> {
                    current.append(char)
                    escape = true
                }
                char == '"' -> {
                    current.append(char)
                    inString = !inString
                }
                char == '{' && !inString -> {
                    current.append(char)
                    depth++
                }
                char == '}' && !inString -> {
                    current.append(char)
                    depth--
                    if (depth == 0) {
                        result.add(current.toString())
                        current = StringBuilder()
                    }
                }
                char == ',' && depth == 0 && !inString -> {
                    // 跳过顶级逗号
                }
                else -> {
                    current.append(char)
                }
            }
        }

        if (current.isNotEmpty()) {
            result.add(current.toString())
        }

        return result
    }

    override suspend fun writeTo(t: List<ServerEntity>, output: OutputStream) {
        val jsonArray = t.joinToString(",", prefix = "[", postfix = "]") { it.toJson() }
        output.write(jsonArray.toByteArray())
    }
}
