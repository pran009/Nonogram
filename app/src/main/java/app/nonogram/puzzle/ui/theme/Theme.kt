package app.nonogram.puzzle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Navy = Color(0xFF1F3A5F)
val NavyLight = Color(0xFF3B5B85)
val Amber = Color(0xFFF2A93B)
val AmberDark = Color(0xFFB8781A)

private val LightScheme: ColorScheme = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3F7),
    onPrimaryContainer = Color(0xFF0B1D33),
    secondary = AmberDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE3B8),
    onSecondaryContainer = Color(0xFF3B2600),
    background = Color(0xFFF6F7FA),
    onBackground = Color(0xFF1A1C1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1F),
    surfaceVariant = Color(0xFFE6EAF0),
    onSurfaceVariant = Color(0xFF444952),
    outline = Color(0xFFB9C1CC),
    error = Color(0xFFC62828),
)

private val DarkScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFA9C4EA),
    onPrimary = Color(0xFF0B1D33),
    primaryContainer = Color(0xFF2E4A70),
    onPrimaryContainer = Color(0xFFD6E3F7),
    secondary = Amber,
    onSecondary = Color(0xFF3B2600),
    secondaryContainer = Color(0xFF5C3F0A),
    onSecondaryContainer = Color(0xFFFFE3B8),
    background = Color(0xFF121417),
    onBackground = Color(0xFFE3E5E9),
    surface = Color(0xFF1A1D22),
    onSurface = Color(0xFFE3E5E9),
    surfaceVariant = Color(0xFF2A2F37),
    onSurfaceVariant = Color(0xFFC3C8D1),
    outline = Color(0xFF5B6270),
    error = Color(0xFFFF6B6B),
)

/** Colors used by the board canvas. Kept separate from Material so the grid always stays crisp. */
@Immutable
data class BoardColors(
    val boardBackground: Color,
    val clueBackground: Color,
    val gridLine: Color,
    val gridThick: Color,
    val filled: Color,
    val cross: Color,
    val clueText: Color,
    val clueDone: Color,
    val crosshair: Color,
    val hint: Color,
    val mistake: Color,
    val solvedFill: Color,
)

val LightBoardColors = BoardColors(
    boardBackground = Color.White,
    clueBackground = Color(0xFFEDF1F7),
    gridLine = Color(0xFFCBD3DE),
    gridThick = Color(0xFF55627A),
    filled = Navy,
    cross = Color(0xFF9AA5B5),
    clueText = Color(0xFF1A1C1F),
    clueDone = Color(0xFFB0B8C4),
    crosshair = Color(0x22F2A93B),
    hint = Color(0x88F2A93B),
    mistake = Color(0xFFE53935),
    solvedFill = Navy,
)

val DarkBoardColors = BoardColors(
    boardBackground = Color(0xFF1E2229),
    clueBackground = Color(0xFF262B33),
    gridLine = Color(0xFF3A414D),
    gridThick = Color(0xFF8B96A8),
    filled = Color(0xFFA9C4EA),
    cross = Color(0xFF6A7382),
    clueText = Color(0xFFE3E5E9),
    clueDone = Color(0xFF5B6270),
    crosshair = Color(0x33F2A93B),
    hint = Color(0x99F2A93B),
    mistake = Color(0xFFFF6B6B),
    solvedFill = Color(0xFFA9C4EA),
)

val LocalBoardColors = staticCompositionLocalOf { LightBoardColors }

@Composable
fun NonogramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    val board = if (darkTheme) DarkBoardColors else LightBoardColors
    androidx.compose.runtime.CompositionLocalProvider(LocalBoardColors provides board) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
