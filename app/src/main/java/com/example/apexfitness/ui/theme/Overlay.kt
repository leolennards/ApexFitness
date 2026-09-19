package com.example.apexfitness.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

// Goes from 0 to 1 when an overlay opens (jumps to 1 if animations are off)
@Composable
fun rememberOverlayProgress(): State<Float> {
    val enabled = LocalMotionEnabled.current
    val progress = remember { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(enabled) {
        if (enabled) progress.animateTo(1f, tween(Motion.Standard, easing = FastOutSlowInEasing))
        else progress.snapTo(1f)
    }
    return progress.asState()
}

// Fades in and scales from 0.92 to 1.0
fun Modifier.overlayEntrance(progress: State<Float>): Modifier = graphicsLayer {
    val p = progress.value
    alpha = p
    val s = 0.92f + 0.08f * p
    scaleX = s
    scaleY = s
}
