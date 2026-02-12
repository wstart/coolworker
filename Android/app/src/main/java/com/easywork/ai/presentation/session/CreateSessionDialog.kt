package com.easywork.ai.presentation.session

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 创建会话对话框
 */
@Composable
fun CreateSessionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isCreating: Boolean = false
) {
    var sessionName by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Session") },
        text = {
            Column {
                OutlinedTextField(
                    value = sessionName,
                    onValueChange = {
                        sessionName = it
                        isError = false
                    },
                    label = { Text("Session Name") },
                    placeholder = { Text("e.g., work, development") },
                    isError = isError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isCreating
                )
                if (isError) {
                    Text(
                        text = "Session name cannot be empty",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (sessionName.isBlank()) {
                        isError = true
                    } else {
                        onConfirm(sessionName.trim())
                    }
                },
                enabled = !isCreating && sessionName.isNotBlank()
            ) {
                Text(if (isCreating) "Creating..." else "Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isCreating
            ) {
                Text("Cancel")
            }
        }
    )
}
