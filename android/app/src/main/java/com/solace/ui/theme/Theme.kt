package com.solace.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// TODO: backend drives themeVariant selection via UiConfig.themeVariant
enum class SolaceThemeVariant { DEFAULT, DARK }

private val LightColors = lightColorScheme(
    primary = SolaceTeal,
    onPrimary = TextOnPrimary,
    primaryContainer = ChipSelectedBg,
    onPrimaryContainer = ChipSelectedText,
    secondary = SolaceTealDark,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = Error,
)

private val DarkColors = darkColorScheme(
    primary = SolaceTeal,
    onPrimary = TextOnPrimary,
    background = BackgroundDark,
    onBackground = TextOnDark,
    surface = BackgroundDark,
    onSurface = TextOnDark,
    error = Error,
)

val SolaceShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

@Composable
fun SolaceTheme(
    variant: SolaceThemeVariant = SolaceThemeVariant.DEFAULT,
    content: @Composable () -> Unit,
) {
    val colors = when (variant) {
        SolaceThemeVariant.DARK -> DarkColors
        SolaceThemeVariant.DEFAULT -> LightColors
    }
    MaterialTheme(
        colorScheme = colors,
        typography = SolaceTypography,
        shapes = SolaceShapes,
        content = content,
    )
}

object SolaceSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

object SolaceRadius {
    val button = 12.dp
    val chip = 20.dp
    val card = 16.dp
    val small = 8.dp
}

object SolaceSizes {
    val buttonHeight = 52.dp
    val bottomNavHeight = 64.dp
    val profileImageHeight = 200.dp
    val chipHeight = 36.dp
}
