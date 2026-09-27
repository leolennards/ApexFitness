package com.example.apexfitness.ui.theme

import androidx.compose.ui.graphics.Color

// Colours: "Alpenglow". Cool snow and granite neutrals with one warm red-orange accent,
// the colour of the light on a mountain peak at sunrise. I only use the accent for the main button,
// the active tab, progress rings and bars, and a few key labels.
//
// Contrast notes (WCAG AA needs 4.5:1 for small text):
//  - Light accent #B93E22 is 5.6:1 with white text on it and 5.0:1 on the snow background,
//    so it works for fills and small text.
//  - Dark accent #F0663F is 6.2:1 with granite text on it and 5.7:1 on the dark surface.
//  - Errors are a crimson that is clearly different from the accent.

// Accent (alpenglow)
val AccentLight = Color(0xFFB93E22)
val AccentDark = Color(0xFFF0663F)
val OnAccentLight = Color(0xFFFFFFFF)
val OnAccentDark = Color(0xFF0C0D0F)
val AccentSoftLight = Color(0xFFF6E7E1)  // accent at about 12% over white
val AccentSoftDark = Color(0xFF2A1912)  // accent at about 14% over the dark surface

// Light theme (snow)
val LightBackground = Color(0xFFF4F3F0)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEFEEEA)
val LightSurfaceHighest = Color(0xFFE9E7E2)
val LightOnBackground = Color(0xFF111214)
val LightOnSurface = Color(0xFF111214)
val LightOnSurfaceVariant = Color(0xFF6B6D72)
val LightOutline = Color(0xFFE4E2DD)  // thin borders

// Dark theme (granite)
val DarkBackground = Color(0xFF0C0D0F)
val DarkSurface = Color(0xFF16171A)
val DarkSurfaceVariant = Color(0xFF1D1E22)
val DarkSurfaceHighest = Color(0xFF25272B)
val DarkOnBackground = Color(0xFFF2F1EE)
val DarkOnSurface = Color(0xFFF2F1EE)
val DarkOnSurfaceVariant = Color(0xFF8C8E94)
val DarkOutline = Color(0xFF25272B)  // thin borders

// Errors (crimson, so they never look like the accent)
val ErrorLight = Color(0xFFB0204E)
val ErrorDark = Color(0xFFFF7D98)
