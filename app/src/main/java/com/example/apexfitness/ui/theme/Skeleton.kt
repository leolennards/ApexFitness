package com.example.apexfitness.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Shimmer used by every loading placeholder. It stops if system animations are off.
@Composable
fun Modifier.shimmerPlaceholder(shape: Shape = RoundedCornerShape(12.dp)): Modifier {
    // Quiet colours from the palette. With animations off it stays still.
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.5f
    val baseColor = if (isDark) scheme.surfaceContainerHigh else scheme.surfaceContainerHighest
    val highlightColor = if (isDark) scheme.surfaceContainerHighest else scheme.surfaceContainerLowest
    val motionEnabled = LocalMotionEnabled.current

    val translateAnim = if (motionEnabled) {
        val transition = rememberInfiniteTransition(label = "shimmerTransition")
        val value by transition.animateFloat(
            initialValue = -600f,
            targetValue = 600f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerTranslate"
        )
        value
    } else {
        0f
    }
    if (!motionEnabled) return this.clip(shape).background(baseColor)

    val brush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim + 200f, 200f)
    )
    return this.clip(shape).background(brush)
}

// One shimmering block
@Composable
fun SkeletonBlock(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(12.dp)) {
    Box(modifier = modifier.shimmerPlaceholder(shape))
}

// A shimmering circle, for avatars
@Composable
fun SkeletonCircle(size: Dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(size).shimmerPlaceholder(CircleShape))
}

// Preview of the shimmer
@Preview(showBackground = true)
@Composable
private fun SkeletonPreview() {
    ApexFitnessTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonCircle(size = 56.dp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    SkeletonBlock(modifier = Modifier.width(140.dp).height(16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    SkeletonBlock(modifier = Modifier.width(90.dp).height(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            SkeletonBlock(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
