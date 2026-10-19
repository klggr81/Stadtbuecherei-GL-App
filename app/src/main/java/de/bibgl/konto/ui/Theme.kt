package de.bibgl.konto.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import de.bibgl.konto.data.ThemeMode

/*
 * Eigenes Farbschema, wenn "Dynamische Farben" aus ist (oder vor Android 12).
 * Vollstaendig aus der Markenfarbe #1B5E4A erzeugt (Material Color Utilities,
 * Tonal Spot, Spec 2021) - frueher waren nur die Grundfarben gesetzt, und
 * Kartenflaechen, Umrisse usw. fielen auf das lila Standardschema zurueck.
 * Bei Aenderungen alle Rollen neu erzeugen statt einzelne Werte zu schieben.
 */
private val LightColors = lightColorScheme(
    primary = Color(0xFF176B53),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA5F2D4),
    onPrimaryContainer = Color(0xFF00513D),
    inversePrimary = Color(0xFF89D6B9),
    secondary = Color(0xFF4C6359),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCEE9DC),
    onSecondaryContainer = Color(0xFF344C42),
    tertiary = Color(0xFF3F6375),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC3E8FE),
    onTertiaryContainer = Color(0xFF274B5D),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFF5FBF6),
    onBackground = Color(0xFF171D1A),
    surface = Color(0xFFF5FBF6),
    onSurface = Color(0xFF171D1A),
    surfaceVariant = Color(0xFFDBE5DE),
    onSurfaceVariant = Color(0xFF404944),
    surfaceTint = Color(0xFF176B53),
    inverseSurface = Color(0xFF2C322F),
    inverseOnSurface = Color(0xFFECF2ED),
    outline = Color(0xFF707974),
    outlineVariant = Color(0xFFBFC9C3),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFF5FBF6),
    surfaceDim = Color(0xFFD5DBD7),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEFF5F0),
    surfaceContainer = Color(0xFFE9EFEA),
    surfaceContainerHigh = Color(0xFFE4EAE5),
    surfaceContainerHighest = Color(0xFFDEE4DF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF89D6B9),
    onPrimary = Color(0xFF003829),
    primaryContainer = Color(0xFF00513D),
    onPrimaryContainer = Color(0xFFA5F2D4),
    inversePrimary = Color(0xFF176B53),
    secondary = Color(0xFFB2CCC0),
    onSecondary = Color(0xFF1E352C),
    secondaryContainer = Color(0xFF344C42),
    onSecondaryContainer = Color(0xFFCEE9DC),
    tertiary = Color(0xFFA7CCE1),
    onTertiary = Color(0xFF0B3445),
    tertiaryContainer = Color(0xFF274B5D),
    onTertiaryContainer = Color(0xFFC3E8FE),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0F1512),
    onBackground = Color(0xFFDEE4DF),
    surface = Color(0xFF0F1512),
    onSurface = Color(0xFFDEE4DF),
    surfaceVariant = Color(0xFF404944),
    onSurfaceVariant = Color(0xFFBFC9C3),
    surfaceTint = Color(0xFF89D6B9),
    inverseSurface = Color(0xFFDEE4DF),
    inverseOnSurface = Color(0xFF2C322F),
    outline = Color(0xFF89938D),
    outlineVariant = Color(0xFF404944),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF343B37),
    surfaceDim = Color(0xFF0F1512),
    surfaceContainerLowest = Color(0xFF0A0F0D),
    surfaceContainerLow = Color(0xFF171D1A),
    surfaceContainer = Color(0xFF1B211E),
    surfaceContainerHigh = Color(0xFF252B28),
    surfaceContainerHighest = Color(0xFF303633),
)

/**
 * Ampelfarben fuer den Frist-Countdown - bewusst unabhaengig vom Theme-Akzent.
 * Die hellen Varianten erreichen mind. 4.5:1 (WCAG AA) auch auf der dunkelsten
 * hellen Kartenflaeche und im eingefaerbten Countdown-Chip.
 */
object DueColors {
    val overdue = Color(0xFFBA1A1A)
    val overdueDark = Color(0xFFFF8A80)
    val urgent = Color(0xFF9C4A00)
    val urgentDark = Color(0xFFFFB74D)
    val soon = Color(0xFF705B00)
    val soonDark = Color(0xFFE8C547)
    val ok = Color(0xFF1B6E4A)
    val okDark = Color(0xFF7FD1AC)
}

/** Dynamische Farben gibt es erst ab Android 12 - darunter wird der Schalter ausgeblendet. */
val supportsDynamicColor: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/** Ob die App dunkel erscheint - aus der Einstellung bzw. der Systemvorgabe. */
@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
fun BibTheme(
    dark: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors = when {
        // Ab Android 12 kann sich die App an das Wallpaper-Farbschema anpassen.
        dynamicColor && supportsDynamicColor ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors) {
        // Die Surface ist nicht bloss Deko: sie setzt LocalContentColor auf
        // onBackground. Ohne sie faellt jeder Text ohne eigene Farbangabe auf
        // Schwarz zurueck - im Dunkelmodus also schwarz auf schwarz. Bildschirme
        // mit Scaffold fiel das nicht auf, weil Scaffold selbst eine Surface mitbringt;
        // der Anmeldebildschirm hat keine und war deshalb unlesbar.
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            content = content,
        )
    }
}
