package com.easywork.ai.presentation.terminal

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
/**
 * 命令输入组件
 */
@Composable
fun CommandInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Enter command...",
    enabled: Boolean = true,
    onTabComplete: () -> Unit = {},
    onHistoryUp: () -> Unit = {},
    onHistoryDown: () -> Unit = {},
    onCtrlC: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        // 快捷键工具栏（当输入包含"cb"时显示）
        if (value.contains("cb", ignoreCase = true)) {
            ShortcutBar(
                onTabComplete = onTabComplete,
                onHistoryUp = onHistoryUp,
                onHistoryDown = onHistoryDown,
                onCtrlC = onCtrlC,
                enabled = enabled
            )
        }

        // 输入框和发送按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),  // 减少间距
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { onSend() },
                    onGo = { onSend() }
                ),
                enabled = enabled,
                singleLine = true,
                maxLines = 1,
                textStyle = MaterialTheme.typography.bodySmall  // 使用默认的小字体
            )

            IconButton(
                onClick = onSend,
                enabled = enabled && value.isNotBlank(),
                modifier = Modifier.size(32.dp)  // 固定小尺寸
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(18.dp)  // 更小的图标
                )
            }
        }
    }
}

/**
 * 快捷键工具栏
 */
@Composable
private fun ShortcutBar(
    onTabComplete: () -> Unit,
    onHistoryUp: () -> Unit,
    onHistoryDown: () -> Unit,
    onCtrlC: () -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,  // 减少阴影
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 1.dp),  // 大幅减少内边距
            horizontalArrangement = Arrangement.spacedBy(2.dp),  // 大幅减少按钮间距
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 上箭头（上一条命令）
            IconButton(
                onClick = onHistoryUp,
                enabled = enabled,
                modifier = Modifier
                    .padding(0.dp)
                    .size(24.dp)  // 固定小尺寸
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Previous command",
                    modifier = Modifier.size(16.dp)  // 更小的图标
                )
            }

            // 下箭头（下一条命令）
            IconButton(
                onClick = onHistoryDown,
                enabled = enabled,
                modifier = Modifier
                    .padding(0.dp)
                    .size(24.dp)  // 固定小尺寸
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Next command",
                    modifier = Modifier.size(16.dp)  // 更小的图标
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Tab 补全 - 改用图标按钮
            IconButton(
                onClick = onTabComplete,
                enabled = enabled,
                modifier = Modifier
                    .padding(0.dp)
                    .size(24.dp)  // 固定小尺寸
            ) {
                Text(
                    "⇥",  // 使用 Unicode 符号代替 Tab 文字
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                )
            }

            // Ctrl+C - 缩小按钮
            ShortcutButton(
                text = "^C",  // 使用简短文本
                onClick = onCtrlC,
                enabled = enabled
            )
        }
    }
}

/**
 * 快捷键按钮
 */
@Composable
private fun ShortcutButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.padding(horizontal = 1.dp, vertical = 1.dp),  // 大幅减少内边距
        contentPadding = PaddingValues(  // 大幅减少按钮内容内边距
            horizontal = 4.dp,
            vertical = 1.dp
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),  // 更小的字体
            maxLines = 1
        )
    }
}
