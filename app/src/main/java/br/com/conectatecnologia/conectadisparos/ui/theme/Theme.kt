package br.com.conectatecnologia.conectadisparos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(primary = Color(0xFF005E5A), secondary = Color(0xFF2D5BFF), tertiary = Color(0xFF00A88E), background = Color(0xFFF7FAFC), surface = Color.White, error = Color(0xFFBA1A1A))
private val DarkColors = darkColorScheme(primary = Color(0xFF41D8CD), secondary = Color(0xFF9DB5FF), tertiary = Color(0xFF35E0BF), background = Color(0xFF07111F), surface = Color(0xFF101B2B), error = Color(0xFFFFB4AB))

@Composable
fun ConectaDisparosTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, typography = Typography(), content = content)
}
