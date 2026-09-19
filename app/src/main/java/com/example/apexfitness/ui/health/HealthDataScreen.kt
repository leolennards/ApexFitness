package com.example.apexfitness.ui.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexPrimaryButton
import com.example.apexfitness.ui.theme.ApexScreenHeader
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.glassScreenBackground
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.staggeredEntrance

// Placeholder until Health Connect is added
@Composable
fun HealthDataScreen(navController: NavHostController) {
    val glassState = rememberGlassState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .glassScreenBackground(glassState)
    ) {
        ApexScreenHeader(
            title = "Health Data",
            label = "COMING SOON",
            onBack = { navController.popBackStack() }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = Dimens.Space4),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .staggeredEntrance(index = 0, key = "health-icon")
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.apex.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MonitorHeart,
                    contentDescription = null,
                    tint = MaterialTheme.apex.accentText,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.Space3))

            Text(
                text = "Health data sync is on the way",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.staggeredEntrance(index = 1, key = "health-title")
            )

            Spacer(modifier = Modifier.height(Dimens.Space1 + 4.dp))

            Text(
                text = "We are working on connecting ApexFitness to Health Connect, so your steps, heart rate and sleep can show up alongside your workouts. Not quite ready yet, check back soon.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.apex.mutedText,
                textAlign = TextAlign.Center,
                modifier = Modifier.staggeredEntrance(index = 2, key = "health-body")
            )
        }

        ApexPrimaryButton(
            text = "Got it",
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenEdge, vertical = Dimens.Space2)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Health light")
@Composable
fun HealthDataScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        HealthDataScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Health dark")
@Composable
private fun HealthDataScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        HealthDataScreen(navController = rememberNavController())
    }
}
