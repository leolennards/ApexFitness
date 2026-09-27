package com.example.apexfitness.ui.theme

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.key
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Shared building blocks so the screens stay consistent

// The main button of a screen: accent pill, at least 52dp tall. Use one per screen.
@Composable
fun ApexPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptics = rememberHaptics()
    Button(
        onClick = {
            haptics.tick()
            onClick()
        },
        modifier = modifier
            .heightIn(min = 52.dp)
            .pressScale(interactionSource),
        enabled = enabled,
        interactionSource = interactionSource,
        shape = PillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = Dimens.Space3, vertical = 14.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(Dimens.Space1))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

// The quiet button: outlined pill on the surface colour. For a second action next to (or instead of)
// the main button, e.g. "Finish workout" while there are still sets to do.
@Composable
fun ApexSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .heightIn(min = 52.dp)
            .apexClickable(enabled = enabled, onClick = onClick)
            .clip(PillShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(Dimens.Hairline, MaterialTheme.apex.hairline, PillShape)
            .alpha(if (enabled) 1f else 0.5f)
            .padding(horizontal = Dimens.Space3, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(Dimens.Space1))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

// Text where only the characters that change roll up, like a watch face. For timers.
// Each character is keyed from the right, so 9:59 to 10:00 only rolls the digits that changed.
// Use a tabular number style so the digits never shift sideways.
@Composable
fun RollingText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier
) {
    val motionEnabled = LocalMotionEnabled.current
    Row(modifier = modifier) {
        text.forEachIndexed { index, char ->
            key(text.length - index) {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        if (motionEnabled) {
                            (slideInVertically(apexSpring(true)) { -it / 2 } + fadeIn(apexTween(true, Motion.Micro))) togetherWith
                                (slideOutVertically(apexSpring(true)) { it / 2 } + fadeOut(apexTween(true, Motion.Micro))) using
                                SizeTransform(clip = true)
                        } else {
                            EnterTransition.None togetherWith ExitTransition.None
                        }
                    },
                    label = "rollingChar"
                ) { shown ->
                    Text(text = shown.toString(), style = style, color = color, maxLines = 1)
                }
            }
        }
    }
}

// Thin progress bar. It grows from 0 the first time it shows and moves smoothly after that.
@Composable
fun ApexProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp,
    color: Color = MaterialTheme.apex.accent,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest
) {
    val animated by rememberAnimatedProgress(progress)
    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        val radius = CornerRadius(size.height / 2f)
        drawRoundRect(color = trackColor, cornerRadius = radius)
        val filled = size.width * animated
        if (filled > 0f) {
            drawRoundRect(
                color = color,
                size = Size(maxOf(filled, size.height), size.height),
                cornerRadius = radius
            )
        }
    }
}

// Big number with a small label under it
@Composable
fun MetricBlock(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueStyle: TextStyle = ApexText.Numeral,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        Text(text = value, style = valueStyle, color = valueColor, maxLines = 1)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.apex.mutedText, maxLines = 1)
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ComponentsPreview() {
    ApexFitnessTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(Dimens.ScreenEdge),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space3)
        ) {
            MetricBlock(value = "12", label = "DAY STREAK")
            RollingText(text = "1:30", style = ApexText.Numeral, color = MaterialTheme.colorScheme.onSurface)
            ApexProgressBar(progress = 0.6f)
            ApexPrimaryButton(
                text = "Begin Workout",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Outlined.PlayArrow
            )
            ApexSecondaryButton(text = "Finish workout", onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}
