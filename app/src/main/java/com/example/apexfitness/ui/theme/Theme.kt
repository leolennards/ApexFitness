package com.example.apexfitness.ui.theme

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val ApexLightColorScheme = lightColorScheme(
    primary = GoldLight,
    onPrimary = OnGold,
    primaryContainer = GoldSoftLight,
    onPrimaryContainer = GoldTextLight,
    inversePrimary = GoldDark,
    secondary = LightOnSurfaceVariant,
    onSecondary = LightSurface,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightOnSurface,
    tertiary = GoldLight,
    onTertiary = OnGold,
    tertiaryContainer = GoldSoftLight,
    onTertiaryContainer = GoldTextLight,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = LightSurface,  // no tint
    inverseSurface = LightOnSurface,
    inverseOnSurface = DarkOnSurface,
    error = ErrorLight,
    onError = LightSurface,
    errorContainer = Color(0xFFFBEAE8),
    onErrorContainer = Color(0xFF7A1F19),
    outline = LightOutline,
    outlineVariant = LightOutline,
    scrim = Color.Black,
    surfaceBright = LightSurface,
    surfaceDim = LightSurfaceVariant,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = Color(0xFFFBFAF7),
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurfaceVariant,
    surfaceContainerHighest = LightSurfaceHighest
)

private val ApexDarkColorScheme = darkColorScheme(
    primary = GoldDark,
    onPrimary = OnGold,
    primaryContainer = GoldSoftDark,
    onPrimaryContainer = GoldDark,
    inversePrimary = GoldLight,
    secondary = DarkOnSurfaceVariant,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkOnSurface,
    tertiary = GoldDark,
    onTertiary = OnGold,
    tertiaryContainer = GoldSoftDark,
    onTertiaryContainer = GoldDark,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkSurface,  // no tint
    inverseSurface = DarkOnSurface,
    inverseOnSurface = LightOnSurface,
    error = ErrorDark,
    onError = Color(0xFF3A0F0C),
    errorContainer = Color(0xFF4A1F1B),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    scrim = Color.Black,
    surfaceBright = DarkSurfaceVariant,
    surfaceDim = DarkBackground,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = Color(0xFF121216),
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceHighest
)

// Extra colours that Material does not have a slot for. Read them with MaterialTheme.apex.
// The accent is for fills and icons, accentText is the darker gold for small text.
@Immutable
class ApexColors(
    val accent: Color,
    val accentText: Color,
    val onAccent: Color,
    val accentSoft: Color,
    val hairline: Color,
    val mutedText: Color,
    val errorText: Color
)

private val LightApexColors = ApexColors(
    accent = GoldLight,
    accentText = GoldTextLight,
    onAccent = OnGold,
    accentSoft = GoldSoftLight,
    hairline = LightOutline,
    mutedText = LightOnSurfaceVariant,
    errorText = ErrorLight
)

private val DarkApexColors = ApexColors(
    accent = GoldDark,
    accentText = GoldDark,
    onAccent = OnGold,
    accentSoft = GoldSoftDark,
    hairline = DarkOutline,
    mutedText = DarkOnSurfaceVariant,
    errorText = ErrorDark
)

val LocalApexColors = compositionLocalOf { LightApexColors }

val MaterialTheme.apex: ApexColors
    @Composable
    @ReadOnlyComposable
    get() = LocalApexColors.current

private fun lerpApexColors(a: ApexColors, b: ApexColors, t: Float): ApexColors = when (t) {
    0f -> a
    1f -> b
    else -> ApexColors(
        accent = lerp(a.accent, b.accent, t),
        accentText = lerp(a.accentText, b.accentText, t),
        onAccent = lerp(a.onAccent, b.onAccent, t),
        accentSoft = lerp(a.accentSoft, b.accentSoft, t),
        hairline = lerp(a.hairline, b.hairline, t),
        mutedText = lerp(a.mutedText, b.mutedText, t),
        errorText = lerp(a.errorText, b.errorText, t)
    )
}

// Mixes two colour schemes for the light and dark fade
private fun lerpColorScheme(a: ColorScheme, b: ColorScheme, t: Float): ColorScheme = when (t) {
    0f -> a
    1f -> b
    else -> lightColorScheme(
        primary = lerp(a.primary, b.primary, t),
        onPrimary = lerp(a.onPrimary, b.onPrimary, t),
        primaryContainer = lerp(a.primaryContainer, b.primaryContainer, t),
        onPrimaryContainer = lerp(a.onPrimaryContainer, b.onPrimaryContainer, t),
        inversePrimary = lerp(a.inversePrimary, b.inversePrimary, t),
        secondary = lerp(a.secondary, b.secondary, t),
        onSecondary = lerp(a.onSecondary, b.onSecondary, t),
        secondaryContainer = lerp(a.secondaryContainer, b.secondaryContainer, t),
        onSecondaryContainer = lerp(a.onSecondaryContainer, b.onSecondaryContainer, t),
        tertiary = lerp(a.tertiary, b.tertiary, t),
        onTertiary = lerp(a.onTertiary, b.onTertiary, t),
        tertiaryContainer = lerp(a.tertiaryContainer, b.tertiaryContainer, t),
        onTertiaryContainer = lerp(a.onTertiaryContainer, b.onTertiaryContainer, t),
        background = lerp(a.background, b.background, t),
        onBackground = lerp(a.onBackground, b.onBackground, t),
        surface = lerp(a.surface, b.surface, t),
        onSurface = lerp(a.onSurface, b.onSurface, t),
        surfaceVariant = lerp(a.surfaceVariant, b.surfaceVariant, t),
        onSurfaceVariant = lerp(a.onSurfaceVariant, b.onSurfaceVariant, t),
        surfaceTint = lerp(a.surfaceTint, b.surfaceTint, t),
        inverseSurface = lerp(a.inverseSurface, b.inverseSurface, t),
        inverseOnSurface = lerp(a.inverseOnSurface, b.inverseOnSurface, t),
        error = lerp(a.error, b.error, t),
        onError = lerp(a.onError, b.onError, t),
        errorContainer = lerp(a.errorContainer, b.errorContainer, t),
        onErrorContainer = lerp(a.onErrorContainer, b.onErrorContainer, t),
        outline = lerp(a.outline, b.outline, t),
        outlineVariant = lerp(a.outlineVariant, b.outlineVariant, t),
        scrim = lerp(a.scrim, b.scrim, t),
        surfaceBright = lerp(a.surfaceBright, b.surfaceBright, t),
        surfaceDim = lerp(a.surfaceDim, b.surfaceDim, t),
        surfaceContainerLowest = lerp(a.surfaceContainerLowest, b.surfaceContainerLowest, t),
        surfaceContainerLow = lerp(a.surfaceContainerLow, b.surfaceContainerLow, t),
        surfaceContainer = lerp(a.surfaceContainer, b.surfaceContainer, t),
        surfaceContainerHigh = lerp(a.surfaceContainerHigh, b.surfaceContainerHigh, t),
        surfaceContainerHighest = lerp(a.surfaceContainerHighest, b.surfaceContainerHighest, t)
    )
}

@Composable
fun ApexFitnessTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val motionEnabled = rememberMotionEnabled()

    // One value (0 to 1) drives all the colours, so switching theme is a short fade
    val fraction by animateFloatAsState(
        targetValue = if (darkTheme) 1f else 0f,
        animationSpec = apexTween(motionEnabled, Motion.ThemeCrossfade),
        label = "themeCrossfade"
    )
    val colorScheme = lerpColorScheme(ApexLightColorScheme, ApexDarkColorScheme, fraction)
    val apexColors = lerpApexColors(LightApexColors, DarkApexColors, fraction)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalMotionEnabled provides motionEnabled,
        LocalApexColors provides apexColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = ApexShapes,
            content = content
        )
    }
}

@Composable
private fun ThemeSample() {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(Dimens.ScreenEdge)
    ) {
        Text(text = "142.5", style = ApexText.HeroNumeral, color = MaterialTheme.colorScheme.onBackground)
        Text(text = "TOTAL VOLUME (KG)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.apex.mutedText)
        Spacer(modifier = Modifier.height(Dimens.Space3))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(Dimens.Hairline, MaterialTheme.apex.hairline, CardShape)
                .padding(Dimens.Space3)
        ) {
            Text(text = "Upper Body Strength", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "Five exercises, about 45 minutes.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.apex.mutedText)
            Spacer(modifier = Modifier.height(Dimens.Space2))
            Text(text = "PERSONAL BEST", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.apex.accentText)
        }
        Spacer(modifier = Modifier.height(Dimens.Space3))
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.MinTouchTarget),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(text = "Start workout")
        }
    }
}

// Preview of the colours, type, card and button in both themes
@Preview(showBackground = true, widthDp = 360, name = "Light")
@Composable
private fun ThemePreviewLight() {
    ApexFitnessTheme(darkTheme = false) { ThemeSample() }
}

@Preview(showBackground = true, widthDp = 360, name = "Dark")
@Composable
private fun ThemePreviewDark() {
    ApexFitnessTheme(darkTheme = true) { ThemeSample() }
}
