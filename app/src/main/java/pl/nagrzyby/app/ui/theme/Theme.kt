package pl.nagrzyby.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = ForestGreen,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = ForestGreenLight,
    onPrimaryContainer = ForestGreenDark,
    secondary = BarkBrown,
    surface = MossSurface,
    background = androidx.compose.ui.graphics.Color(0xFFFAFAFA),
)

private val DarkColors = darkColorScheme(
    primary = ForestGreenLight,
    onPrimary = ForestGreenDark,
    primaryContainer = ForestGreenDark,
    secondary = ForestGreenLight,
)

@Composable
fun NaGrzybyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
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
