package com.example.apexfitness.ui.welcome

import com.example.apexfitness.R
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import com.example.apexfitness.ui.theme.ApexSecondaryButton
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexPrimaryButton
import com.example.apexfitness.ui.theme.ApexText
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.glassScreenBackground
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.staggeredEntrance

// Welcome screen. A big confident line, three short steps, and both buttons at the bottom within thumb reach.

@Composable
fun WelcomeScreen(navController: NavController) {
    val glassState = rememberGlassState()
    val accent = MaterialTheme.apex.accent
    Box(
        modifier = Modifier
            .fillMaxSize()
            .glassScreenBackground(glassState)
    ) {
        // The mountain from the logo, big and very faint, sitting behind everything at the bottom.
        // It is decoration only, so screen readers skip it.
        Image(
            painter = painterResource(id = R.drawable.apex_fitness_logo),
            contentDescription = null,
            colorFilter = ColorFilter.tint(accent),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 96.dp, y = 150.dp)
                .requiredSize(460.dp)
                .alpha(0.07f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.ScreenEdge)
                .padding(top = Dimens.Space3, bottom = Dimens.Space2)
        ) {
            // Small brand mark at the top
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.staggeredEntrance(index = 0, key = "welcome-logo")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.apex_fitness_logo),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(accent),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(Dimens.Space1))
                Text(
                    text = "APEXFITNESS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Train with\nintent.",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 68.sp, lineHeight = 66.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .semantics { heading() }
                        .staggeredEntrance(index = 1, key = "welcome-title")
                )
                Spacer(modifier = Modifier.height(Dimens.Space2))
                Text(
                    text = "Plan your week, log every set, and watch your numbers climb.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.apex.mutedText,
                    modifier = Modifier.staggeredEntrance(index = 2, key = "welcome-subtitle")
                )

                Spacer(modifier = Modifier.height(Dimens.Space5))

                Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space2)) {
                    WelcomeStep(
                        number = "01",
                        title = "Plan",
                        description = "Build routines for the days you train.",
                        modifier = Modifier.staggeredEntrance(index = 3, key = "welcome-step-1")
                    )
                    WelcomeStep(
                        number = "02",
                        title = "Train",
                        description = "Big numbers, a rest timer, one tap per set.",
                        modifier = Modifier.staggeredEntrance(index = 4, key = "welcome-step-2")
                    )
                    WelcomeStep(
                        number = "03",
                        title = "Track",
                        description = "Streaks, records and weekly volume.",
                        modifier = Modifier.staggeredEntrance(index = 5, key = "welcome-step-3")
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Space2))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(index = 6, key = "welcome-actions"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ApexPrimaryButton(
                    text = "Get started",
                    onClick = { navController.navigate("signup") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Dimens.Space1))
                ApexSecondaryButton(
                    text = "I already have an account",
                    onClick = { navController.navigate("signin") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// One numbered line on the welcome screen: "01  Plan  Build routines for..."
@Composable
private fun WelcomeStep(
    number: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = number,
            style = ApexText.NumeralSmall.copy(fontSize = 20.sp, lineHeight = 24.sp),
            color = MaterialTheme.apex.mutedText,
            modifier = Modifier.width(40.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.apex.mutedText
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Welcome light")
@Composable
fun WelcomeScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        WelcomeScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Welcome dark")
@Composable
private fun WelcomeScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        WelcomeScreen(navController = rememberNavController())
    }
}
