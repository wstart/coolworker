package com.easywork.ai.util

/**
 * Tab补全工具
 */
object TabCompleter {

    /**
     * 执行Tab补全
     * @param input 当前输入
     * @param suggestions 建议列表
     * @return 补全后的文本或最长公共前缀
     */
    fun complete(input: String, suggestions: List<String>): String {
        if (suggestions.isEmpty()) return input
        if (suggestions.size == 1) return applyCompletion(input, suggestions[0])

        // 找到最长公共前缀
        val prefix = findLongestCommonPrefix(suggestions)
        return if (prefix.isNotEmpty()) {
            applyCompletion(input, prefix)
        } else {
            input
        }
    }

    /**
     * 应用补全到输入
     */
    private fun applyCompletion(input: String, completion: String): String {
        val parts = input.split(" ")
        val lastPart = parts.lastOrNull() ?: ""
        val prefix = input.substringBeforeLast(lastPart)

        return if (completion.startsWith(lastPart)) {
            prefix + completion
        } else {
            completion
        }
    }

    /**
     * 查找最长公共前缀
     */
    private fun findLongestCommonPrefix(strings: List<String>): String {
        if (strings.isEmpty()) return ""
        if (strings.size == 1) return strings[0]

        val first = strings[0]
        var prefix = first

        for (i in 1 until strings.size) {
            while (!strings[i].startsWith(prefix)) {
                prefix = prefix.substring(0, prefix.length - 1)
                if (prefix.isEmpty()) return ""
            }
        }

        return prefix
    }

    /**
     * 获取补全显示文本（多个匹配时）
     */
    fun formatSuggestions(suggestions: List<String>): String {
        return suggestions.joinToString("\n") { "  • $it" }
    }
}
