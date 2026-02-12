package com.easywork.ai.presentation.common.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light color scheme
private val LightColors = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF03A9F4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB3E5FC),
    onSecondaryContainer = Color(0xFF01579B),
    tertiary = Color(0xFF00BCD4),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2EBF2),
    onTertiaryContainer = Color(0xFF006064),
    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFDADF4),
    onErrorContainer = Color(0xFF31111D),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF121212),
    surface = Color.White,
    onSurface = Color(0xFF121212),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF424242)
)

// Dark color scheme
private val DarkColors = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1976D2),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF4FC3F7),
    onSecondary = Color(0xFF01579B),
    secondaryContainer = Color(0xFF03A9F4),
    onSecondaryContainer = Color(0xFFB3E5FC),
    tertiary = Color(0xFF4DD0E1),
    onTertiary = Color(0xFF006064),
    tertiaryContainer = Color(0xFF00BCD4),
    onTertiaryContainer = Color(0xFFB2EBF2),
    error = Color(0xFFCF6679),
    onError = Color(0xFF5C000D),
    errorContainer = Color(0xFFB00020),
    onErrorContainer = Color(0xFFFDADF4),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBDBDBD)
)

/**
 * App主题
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = false, // 可以根据系统设置自动切换
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
