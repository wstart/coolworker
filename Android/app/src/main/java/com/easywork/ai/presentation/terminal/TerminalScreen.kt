package com.easywork.ai.presentation.terminal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import com.easywork.ai.presentation.common.components.LoadingDialog
import kotlinx.coroutines.launch
import com.easywork.ai.di.AppContainer

@Composable
fun terminalViewModel(): TerminalViewModel {
    return remember {
        TerminalViewModel(
            AppContainer.provideServerRepository(),
            AppContainer.provideSendCommandUseCase()
        )
    }
}

/**
 * 终端屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    serverId: String,
    sessionName: String,
    viewModel: TerminalViewModel = terminalViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 初始化
    LaunchedEffect(serverId, sessionName) {
        viewModel.init(serverId, sessionName)
    }

    // 显示错误
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(error)
                viewModel.clearError()
            }
        }
    }

    // Tab补全建议对话框
    if (state.showSuggestions && state.suggestions.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.hideSuggestions() },
            title = { Text("Suggestions") },
            text = {
                Column {
                    state.suggestions.forEach { suggestion ->
                        Text(suggestion, modifier = Modifier.padding(4.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.hideSuggestions() }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Terminal")
                        Text(
                            sessionName,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.disconnect()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshOutput() },
                        enabled = state.isConnected
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(
                        onClick = { viewModel.clearOutput() },
                        enabled = state.isConnected
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding() // 添加 imePadding 来避开软键盘
        ) {
            // 终端输出
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                TerminalOutput(
                    output = state.output,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 命令输入
            CommandInput(
                value = state.currentInput,
                onValueChange = { viewModel.updateInput(it) },
                onSend = { viewModel.sendCommand(state.currentInput) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isConnected,
                placeholder = "Type command and press Enter...",
                onTabComplete = { viewModel.performTabCompletion() },
                onHistoryUp = { viewModel.previousCommand() },
                onHistoryDown = { viewModel.nextCommand() },
                onCtrlC = { viewModel.sendCtrlC() }
            )
        }
    }

    LoadingDialog(isVisible = state.isConnecting)
}
