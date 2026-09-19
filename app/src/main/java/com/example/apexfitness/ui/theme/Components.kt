package com.example.apexfitness.ui.theme

import androidx.compose.foundation.Canvas
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

// The main button of a screen: gold pill, at least 52dp tall. Use one per screen.
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
            ApexProgressBar(progress = 0.6f)
            ApexPrimaryButton(
                text = "Begin Workout",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Outlined.PlayArrow
            )
        }
    }
}
