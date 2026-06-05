package pl.nagrzyby.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = ForestGreenLight,
    onPrimaryContainer = ForestGreenDark,
    secondary = BarkBrown,
    onSecondary = Color.White,
    surface = MossSurface,
    onSurface = Color(0xFF1C1B1F),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE8E0E0),
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = Color(0xFF79747E),
)

private val DarkColors = darkColorScheme(
    primary = ForestGreenLight,
    onPrimary = Color(0xFF003300),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = ForestGreenLight,
    secondary = Color(0xFF8D6E63),
    onSecondary = Color(0xFF3E2723),
    secondaryContainer = Color(0xFF3E2723),
    onSecondaryContainer = Color(0xFFD7CCC8),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFE0E0E0),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBDBDBD),
    error = Color(0xFFEF5350),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF4A0000),
    onErrorContainer = Color(0xFFFFCDD2),
    outline = Color(0xFF616161),
)

@Composable
fun NaGrzybyTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && themeMode == "system" -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
