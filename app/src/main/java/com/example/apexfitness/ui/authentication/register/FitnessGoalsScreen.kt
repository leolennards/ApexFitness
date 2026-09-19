package com.example.apexfitness.ui.authentication.register

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.ui.authentication.OnboardingFormState
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexPrimaryButton
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.glassScreenBackground
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.staggeredEntrance

// Onboarding step 2: goals (pick as many as you like)

@Composable
fun FitnessGoalsScreen(navController: NavController, formState: OnboardingFormState) {
    val selectedGoals = formState.goals
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
                .padding(top = Dimens.Space3, bottom = Dimens.Space3)
        ) {
            OnboardingStepHeader(step = 2, total = 3, onBack = { navController.popBackStack() })

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(Dimens.Space4))

                Column(modifier = Modifier.staggeredEntrance(index = 0, key = "onb2-title")) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.apex.accentSoft)
                            .border(Dimens.Hairline, MaterialTheme.apex.accent.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TrackChanges,
                            contentDescription = "Fitness Goals",
                            tint = MaterialTheme.apex.accentText,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.Space3))
                    Text(
                        text = "Set Your Goals",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(Dimens.Space1))
                    Text(
                        text = "Choose your primary fitness goals to get customized workout plans and track your progress effectively.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.apex.mutedText
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Space4))

                Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space1 + 4.dp)) {
                    val goals = listOf(
                        GoalItem(Icons.Outlined.FitnessCenter, "Build Muscle", "Gain strength and muscle mass"),
                        GoalItem(Icons.Outlined.LocalFireDepartment, "Lose Weight", "Burn fat and get lean"),
                        GoalItem(Icons.Outlined.MonitorHeart, "Improve Endurance", "Boost cardiovascular health"),
                        GoalItem(Icons.AutoMirrored.Outlined.DirectionsRun, "General Fitness", "Stay active and healthy"),
                        GoalItem(Icons.Outlined.SelfImprovement, "Flexibility & Mobility", "Improve range of motion"),
                        GoalItem(Icons.Outlined.EmojiEvents, "Sports Performance", "Enhance athletic abilities")
                    )

                    goals.forEachIndexed { index, goal ->
                        GoalOptionItem(
                            icon = goal.icon,
                            title = goal.title,
                            description = goal.description,
                            isSelected = selectedGoals.contains(goal.title),
                            onSelect = {
                                formState.goals = if (selectedGoals.contains(goal.title)) {
                                    selectedGoals - goal.title
                                } else {
                                    selectedGoals + goal.title
                                }
                            },
                            modifier = Modifier.staggeredEntrance(index = 1 + index, key = "onb2-goal-${goal.title}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Space2))
            }

            Spacer(modifier = Modifier.height(Dimens.Space1))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ApexPrimaryButton(
                    text = "Continue",
                    onClick = { navController.navigate("workoutSchedule") },
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .apexClickable {
                            navController.navigate("main") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        }
                        .heightIn(min = 56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Skip for now",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.apex.mutedText
                    )
                }
            }
        }
    }
}

data class GoalItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)

@Composable
fun GoalOptionItem(
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    SelectableOptionCard(
        icon = icon,
        title = title,
        description = description,
        isSelected = isSelected,
        onSelect = onSelect,
        modifier = modifier
    )
}

@Preview(showBackground = true, showSystemUi = true, name = "Step 2 light")
@Composable
fun FitnessGoalsScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        FitnessGoalsScreen(
            navController = rememberNavController(),
            formState = OnboardingFormState().apply { goals = setOf("Build Muscle", "General Fitness") }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Step 2 dark")
@Composable
private fun FitnessGoalsScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        FitnessGoalsScreen(
            navController = rememberNavController(),
            formState = OnboardingFormState().apply { goals = setOf("Lose Weight") }
        )
    }
}
