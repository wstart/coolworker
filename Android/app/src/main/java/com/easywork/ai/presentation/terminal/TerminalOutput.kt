package com.easywork.ai.presentation.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easywork.ai.domain.model.TerminalLine
import com.easywork.ai.domain.model.TerminalLineType
import com.easywork.ai.presentation.common.theme.TerminalBackground
import com.easywork.ai.presentation.common.theme.TerminalCommand
import com.easywork.ai.presentation.common.theme.TerminalError
import com.easywork.ai.presentation.common.theme.TerminalText

/**
 * 终端输出组件
 */
@Composable
fun TerminalOutput(
    output: List<TerminalLine>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // 自动滚动到底部
    LaunchedEffect(output.size) {
        if (output.isNotEmpty()) {
            listState.animateScrollToItem(output.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .background(TerminalBackground)
            .fillMaxWidth()
            .padding(4.dp),  // 减少内边距
        reverseLayout = false
    ) {
        items(output) { line ->
            TerminalLineItem(line)
        }
    }
}

/**
 * 终端行项
 */
@Composable
fun TerminalLineItem(line: TerminalLine) {
    val color = when (line.type) {
        TerminalLineType.OUTPUT -> TerminalText
        TerminalLineType.ERROR -> TerminalError
        TerminalLineType.COMMAND -> TerminalCommand
        TerminalLineType.SYSTEM -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Text(
        text = line.content,
        color = color,
        fontFamily = FontFamily.Monospace,
        style = TextStyle(
            fontSize = 12.sp,  // 使用更小的字体
            lineHeight = (12.sp * 1.2f)  // 减少行高，更紧凑
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 0.dp)  // 减少行间距
    )
}
