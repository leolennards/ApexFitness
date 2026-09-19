package com.example.apexfitness.ui.authentication.register

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.ui.authentication.OnboardingFormState
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexPrimaryButton
import com.example.apexfitness.ui.theme.ApexProgressBar
import com.example.apexfitness.ui.theme.CardShape
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.Motion
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.glassScreenBackground
import com.example.apexfitness.ui.theme.motionTween
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.staggeredEntrance

// Onboarding step 1: fitness level. The header and option card are reused by the next two steps.

@Composable
fun GetStartedScreen(navController: NavController, formState: OnboardingFormState) {
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
            OnboardingStepHeader(step = 1, total = 3, onBack = { navController.popBackStack() })

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(Dimens.Space4))

                Column(modifier = Modifier.staggeredEntrance(index = 0, key = "onb1-title")) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.apex.accentSoft)
                            .border(Dimens.Hairline, MaterialTheme.apex.accent.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Profile Setup",
                            tint = MaterialTheme.apex.accentText,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.Space3))
                    Text(
                        text = "Create Your Profile",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(Dimens.Space1))
                    Text(
                        text = "Tell us about yourself to get personalized workout recommendations and track your progress effectively.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.apex.mutedText
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Space4))

                Text(
                    text = "FITNESS LEVEL",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.apex.mutedText,
                    modifier = Modifier.staggeredEntrance(index = 1, key = "onb1-label")
                )

                Spacer(modifier = Modifier.height(Dimens.Space1 + 4.dp))

                Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space1 + 4.dp)) {
                    val levels = listOf(
                        Triple("Beginner", "New to the gym or getting back into it", Icons.Outlined.Spa),
                        Triple("Intermediate", "Training consistently for a while", Icons.Outlined.FitnessCenter),
                        Triple("Advanced", "Experienced lifter chasing PRs", Icons.Outlined.LocalFireDepartment)
                    )

                    levels.forEachIndexed { index, (level, description, icon) ->
                        LevelOptionItem(
                            icon = icon,
                            title = level,
                            description = description,
                            isSelected = formState.fitnessLevel == level,
                            onSelect = { formState.fitnessLevel = level },
                            modifier = Modifier.staggeredEntrance(index = 2 + index, key = "onb1-level-$level")
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
                    onClick = {
                        if (formState.fitnessLevel.isBlank()) {
                            formState.fitnessLevel = "Beginner"
                        }
                        navController.navigate("fitnessGoals")
                    },
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

// Back button, "STEP n OF total" and a thin progress bar
@Composable
fun OnboardingStepHeader(step: Int, total: Int, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.MinTouchTarget)
                    .apexClickable(onClick = onBack)
                    .clip(CircleShape)
                    .border(Dimens.Hairline, MaterialTheme.apex.hairline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = "STEP $step OF $total",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.apex.mutedText
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Space1))
        ApexProgressBar(progress = step.toFloat() / total.toFloat(), height = 3.dp)
    }
}

// Tappable option card for single or multiple choice. Selected cards get a gold border and a check.
@Composable
fun SelectableOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fill by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.apex.accentSoft else MaterialTheme.colorScheme.surface,
        animationSpec = motionTween(Motion.Micro),
        label = "optionFill"
    )
    val border by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.apex.accent else MaterialTheme.apex.hairline,
        animationSpec = motionTween(Motion.Micro),
        label = "optionBorder"
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .apexClickable(onClick = onSelect)
            .clip(CardShape)
            .background(fill)
            .border(Dimens.Hairline, border, CardShape)
            .heightIn(min = 72.dp)
            .padding(Dimens.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .clip(CircleShape)
                .background(if (isSelected) Color.Transparent else MaterialTheme.apex.accentSoft)
                .border(Dimens.Hairline, MaterialTheme.apex.accent.copy(alpha = 0.45f), CircleShape),
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

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.apex.mutedText
            )
        }

        Spacer(modifier = Modifier.width(Dimens.Space1))

        Icon(
            imageVector = if (isSelected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = if (isSelected) "Selected" else "Not selected",
            tint = if (isSelected) MaterialTheme.apex.accentText else MaterialTheme.apex.mutedText
        )
    }
}

@Composable
fun LevelOptionItem(
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

@Preview(showBackground = true, showSystemUi = true, name = "Step 1 light")
@Composable
fun GetStartedScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        GetStartedScreen(
            navController = rememberNavController(),
            formState = OnboardingFormState().apply { fitnessLevel = "Intermediate" }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Step 1 dark")
@Composable
private fun GetStartedScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        GetStartedScreen(
            navController = rememberNavController(),
            formState = OnboardingFormState().apply { fitnessLevel = "Beginner" }
        )
    }
}
