package com.example.apexfitness.ui.gamification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexPrimaryButton
import com.example.apexfitness.ui.theme.ApexShapes
import com.example.apexfitness.ui.theme.ApexText
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.glassPanel
import com.example.apexfitness.ui.theme.overlayEntrance
import com.example.apexfitness.ui.theme.rememberCountUpInt
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.rememberOverlayProgress

// Popup shown once when the level goes up. Closing it saves the new level so it does not show again.
@Composable
fun LevelUpCelebrationOverlay(level: Int, title: String, onDismiss: () -> Unit) {
    val progress = rememberOverlayProgress()
    val animatedLevel by rememberCountUpInt(level)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = progress.value }
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(Dimens.Space4)
                .overlayEntrance(progress)
                .glassPanel(rememberGlassState(), shape = ApexShapes.large)
                .padding(Dimens.Space3),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LEVEL UP",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.apex.mutedText
            )
            Spacer(modifier = Modifier.height(Dimens.Space2))
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.apex.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MilitaryTech,
                    contentDescription = null,
                    tint = MaterialTheme.apex.accentText,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(Dimens.Space2))
            Text(
                text = "$animatedLevel",
                style = ApexText.HeroNumeral,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = "LEVEL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.apex.mutedText
            )
            Spacer(modifier = Modifier.height(Dimens.Space1))
            Text(
                text = "You are a $title now. Keep training.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.apex.mutedText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Dimens.Space3))
            ApexPrimaryButton(
                text = "Nice!",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, name = "Level up light")
@Composable
fun LevelUpCelebrationOverlayPreview() {
    ApexFitnessTheme(darkTheme = false) {
        LevelUpCelebrationOverlay(level = 6, title = "Contender", onDismiss = {})
    }
}

@Preview(showBackground = true, name = "Level up dark")
@Composable
private fun LevelUpCelebrationOverlayDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        LevelUpCelebrationOverlay(level = 6, title = "Contender", onDismiss = {})
    }
}
