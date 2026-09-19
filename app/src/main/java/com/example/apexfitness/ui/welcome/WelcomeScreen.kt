package com.example.apexfitness.ui.welcome

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

// Welcome screen. The main button is at the bottom so it is easy to reach.

@Composable
fun WelcomeScreen(navController: NavController) {
    val glassState = rememberGlassState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .glassScreenBackground(glassState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.ScreenEdge)
                .padding(top = Dimens.Space4, bottom = Dimens.Space3)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo sits on a white card so it looks right in light and dark
                val logoShape = RoundedCornerShape(24.dp)
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(108.dp)
                        .staggeredEntrance(index = 0, key = "welcome-logo")
                        .clip(logoShape)
                        .background(Color.White)
                        .border(Dimens.Hairline, MaterialTheme.apex.hairline, logoShape)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LogoImage()
                }

                Spacer(modifier = Modifier.height(Dimens.Space4))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.staggeredEntrance(index = 1, key = "welcome-title")
                ) {
                    Text(
                        text = "WELCOME TO",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.apex.mutedText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Dimens.Space1))
                    Text(
                        text = "ApexFitness",
                        style = ApexText.Numeral,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Dimens.Space1))
                    Text(
                        text = "Elevate Your Fitness Journey",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.apex.mutedText,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Space4))

                Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space3)) {
                    FeatureItem(
                        icon = Icons.AutoMirrored.Outlined.TrendingUp,
                        title = "Track Progress",
                        description = "Monitor your fitness journey with detailed analytics",
                        modifier = Modifier.staggeredEntrance(index = 2, key = "welcome-feature-1")
                    )
                    FeatureItem(
                        icon = Icons.Outlined.FitnessCenter,
                        title = "Personalized Workouts",
                        description = "Custom training programs tailored for you",
                        modifier = Modifier.staggeredEntrance(index = 3, key = "welcome-feature-2")
                    )
                    FeatureItem(
                        icon = Icons.Outlined.StarOutline,
                        title = "Achieve Goals",
                        description = "Unlock achievements and reach new heights",
                        modifier = Modifier.staggeredEntrance(index = 4, key = "welcome-feature-3")
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Space2))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(index = 5, key = "welcome-actions"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ApexPrimaryButton(
                    text = "Get Started",
                    onClick = { navController.navigate("signup") },
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .apexClickable { navController.navigate("signin") }
                        .heightIn(min = 56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Already have an account? Sign In",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun LogoImage() {
    val context = LocalContext.current
    val logoResourceId = context.resources
        .getIdentifier("apex_fitness_logo", "drawable", context.packageName)

    if (logoResourceId != 0) {
        Image(
            painter = painterResource(id = logoResourceId),
            contentDescription = "Apex Fitness Logo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    } else {
        // Placeholder if the logo file is missing
        Icon(
            imageVector = Icons.Outlined.FitnessCenter,
            contentDescription = "Apex Fitness Logo",
            tint = MaterialTheme.apex.accent,
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .clip(CircleShape)
                .background(MaterialTheme.apex.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.apex.accentText,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(Dimens.Space2))

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
