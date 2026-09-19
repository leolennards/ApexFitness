package com.example.apexfitness.ui.theme

import androidx.compose.ui.graphics.Color

// Colours. Warm neutrals with one gold accent that I only use for buttons, the active tab, progress and key numbers.
//
// Contrast notes:
//  - Gold (#B8975A) is only about 2.5:1 on the cream background, so I use it for fills, rings and icons.
//    Small gold text uses GoldTextLight (#866629) instead.
//  - Text on a gold fill is near black, because white on gold is too low contrast.
//  - Muted text on light is a bit darker than my first choice so it passes AA.

// Accent (gold)
val GoldLight = Color(0xFFB8975A)
val GoldDark = Color(0xFFD4B27A)
val GoldTextLight = Color(0xFF866629)
val OnGold = Color(0xFF16161A)
val GoldSoftLight = Color(0xFFF5F0E8)  // accent at about 14% over white
val GoldSoftDark = Color(0xFF35302B)  // accent at about 16% over the dark surface

// Light theme
val LightBackground = Color(0xFFF7F5F0)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1EEE7)
val LightSurfaceHighest = Color(0xFFEBE7DE)
val LightOnBackground = Color(0xFF16161A)
val LightOnSurface = Color(0xFF16161A)
val LightOnSurfaceVariant = Color(0xFF6B6B74)
val LightOutline = Color(0xFFE6E2D9)  // thin borders

// Dark theme
val DarkBackground = Color(0xFF0E0E11)
val DarkSurface = Color(0xFF17171C)
val DarkSurfaceVariant = Color(0xFF1F1F26)
val DarkSurfaceHighest = Color(0xFF26262D)
val DarkOnBackground = Color(0xFFF4F1EA)
val DarkOnSurface = Color(0xFFF4F1EA)
val DarkOnSurfaceVariant = Color(0xFF8E8E96)
val DarkOutline = Color(0xFF26262D)  // thin borders

// Errors (the only colour that is not gold)
val ErrorLight = Color(0xFFB3372F)
val ErrorDark = Color(0xFFE5877E)
