package com.easywork.ai.presentation.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.easywork.ai.domain.model.Server
import com.easywork.ai.presentation.common.components.LoadingDialog
import com.easywork.ai.presentation.common.components.ServerItem
import com.easywork.ai.di.AppContainer
import kotlinx.coroutines.launch

/**
 * ViewModel工厂函数
 */
@Composable
fun serverListViewModel(): ServerListViewModel {
    return remember {
        ServerListViewModel(AppContainer.provideServerRepository())
    }
}

/**
 * 服务器列表屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(
    onNavigateToEdit: (String) -> Unit,
    onNavigateToSessions: (String) -> Unit
) {
    val viewModel: ServerListViewModel = serverListViewModel()
    android.util.Log.d("ServerListScreen", "ServerListScreen recomposed, viewModel: $viewModel")
    val state by viewModel.state.collectAsState()
    val navEvent by viewModel.navigationEvents.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf<Server?>(null) }

    // 处理导航事件
    LaunchedEffect(navEvent) {
        android.util.Log.d("ServerListScreen", "Navigation event: $navEvent")
        navEvent?.let { event ->
            android.util.Log.d("ServerListScreen", "Processing navigation event: $event")
            when (event) {
                is ServerListNavigationEvent.NavigateToEdit -> {
                    android.util.Log.d("ServerListScreen", "Navigating to edit with serverId: ${event.serverId}")
                    onNavigateToEdit(event.serverId)
                    viewModel.clearNavigationEvent()
                }
                is ServerListNavigationEvent.NavigateToSessions -> {
                    onNavigateToSessions(event.server.id)
                    viewModel.clearNavigationEvent()
                }
            }
        }
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

    // 删除确认对话框
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Server") },
            text = { Text("Are you sure you want to delete ${showDeleteDialog?.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog?.let { viewModel.onDeleteServer(it) }
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EasyWork with AI") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    android.util.Log.d("ServerListScreen", "FAB clicked, viewModel: $viewModel")
                    try {
                        android.util.Log.d("ServerListScreen", "Calling onNavigateToEdit...")
                        viewModel.onNavigateToEdit("")
                        android.util.Log.d("ServerListScreen", "onNavigateToEdit called successfully")
                    } catch (e: Exception) {
                        android.util.Log.e("ServerListScreen", "Error calling onNavigateToEdit", e)
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Server")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading && state.servers.isEmpty()) {
                LoadingDialog(isVisible = true)
            } else if (state.servers.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No servers configured",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to add a new server",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.servers) { server ->
                        ServerItem(
                            server = server,
                            onClick = { viewModel.onNavigateToSessions(server) },
                            onEdit = { viewModel.onNavigateToEdit(server.id) },
                            onDelete = { showDeleteDialog = server }
                        )
                    }
                }
            }
        }
    }
}
