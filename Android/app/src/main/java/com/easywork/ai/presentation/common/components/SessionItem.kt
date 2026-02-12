package com.easywork.ai.presentation.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.easywork.ai.domain.model.TmuxSession
import com.easywork.ai.presentation.common.theme.ActiveSessionColor
import com.easywork.ai.presentation.common.theme.InactiveSessionColor

/**
 * 会话列表项
 */
@Composable
fun SessionItem(
    session: TmuxSession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 状态指示器
            Surface(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape),
                color = if (session.isActive) ActiveSessionColor else InactiveSessionColor
            ) {}

            Spacer(modifier = Modifier.width(16.dp))

            // 会话图标
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Session",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 会话信息
            androidx.compose.foundation.layout.Column {
                Text(
                    text = session.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${session.windows} window${if (session.windows > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = session.getStateText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (session.isActive) ActiveSessionColor else InactiveSessionColor
                    )
                }
            }
        }
    }
}
