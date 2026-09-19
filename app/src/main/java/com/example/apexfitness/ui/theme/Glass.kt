package com.example.apexfitness.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Stable

// Card styling used across the app: solid surface, thin border, no shadows.
//
// The names (glassPanel and so on) come from an older glass look that I dropped because live blur
// caused glitches. I kept the names so I did not have to change every screen.

// Card corner radius
val GlassCornerRadius = Dimens.CardRadius

// Not used any more, kept so existing calls still compile
@Stable
class GlassState

@Composable
fun rememberGlassState(): GlassState = remember { GlassState() }

// Does nothing, kept so existing calls still compile
fun Modifier.glassBackdrop(state: GlassState): Modifier = this

// Card surface: solid colour, thin border, 20dp corners.
// If a tint is passed the panel gets the soft gold look instead.
@Composable
fun Modifier.glassPanel(
    state: GlassState,
    shape: Shape = RoundedCornerShape(GlassCornerRadius),
    tint: Color? = null
): Modifier {
    val apex = MaterialTheme.apex
    val fill = if (tint != null) apex.accentSoft else MaterialTheme.colorScheme.surface
    val border = if (tint != null) apex.accent.copy(alpha = 0.45f) else apex.hairline
    return this
        .clip(shape)
        .background(fill)
        .border(width = Dimens.Hairline, color = border, shape = shape)
}

// Background used by every screen
@Composable
fun Modifier.glassScreenBackground(state: GlassState): Modifier =
    this.background(MaterialTheme.colorScheme.background)

// Main text colour inside a panel
@Composable
fun glassContentColor(): Color = MaterialTheme.colorScheme.onSurface

// Muted text colour inside a panel
@Composable
fun glassMutedContentColor(): Color = MaterialTheme.apex.mutedText

// Preview of a normal panel and a gold one
@Preview(showBackground = true)
@Composable
private fun GlassPanelPreview() {
    ApexFitnessTheme(darkTheme = false) {
        val glassState = rememberGlassState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassScreenBackground(glassState)
                .padding(Dimens.ScreenEdge)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .glassPanel(glassState)
                ) {
                    Column(modifier = Modifier.padding(Dimens.Space2)) {
                        Text(text = "Panel", style = MaterialTheme.typography.titleMedium, color = glassContentColor())
                        Text(text = "Muted label text", style = MaterialTheme.typography.bodySmall, color = glassMutedContentColor())
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.Space2))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .glassPanel(glassState, tint = GoldLight)
                ) {
                    Column(modifier = Modifier.padding(Dimens.Space2)) {
                        Text(text = "Accent panel", style = MaterialTheme.typography.titleMedium, color = glassContentColor())
                        Text(text = "Tint hue is ignored", style = MaterialTheme.typography.bodySmall, color = glassMutedContentColor())
                    }
                }
            }
        }
    }
}
