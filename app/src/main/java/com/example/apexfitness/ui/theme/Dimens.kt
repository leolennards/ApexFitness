package com.example.apexfitness.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// 8dp spacing grid and the screen padding
object Dimens {
    val Space1 = 8.dp
    val Space2 = 16.dp
    val Space3 = 24.dp
    val Space4 = 32.dp
    val Space5 = 40.dp
    val Space6 = 48.dp

    // Screen edge padding
    val ScreenEdge = 20.dp

    // Smallest size for anything tappable
    val MinTouchTarget = 48.dp

    // Thin border width
    val Hairline = 1.dp

    val CardRadius = 20.dp
}

// Cards are 20dp, buttons are pills
val CardShape = RoundedCornerShape(Dimens.CardRadius)
val PillShape = RoundedCornerShape(percent = 50)

val ApexShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),  // text fields, chips
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(Dimens.CardRadius),  // cards
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)  // dialogs, sheets
)
