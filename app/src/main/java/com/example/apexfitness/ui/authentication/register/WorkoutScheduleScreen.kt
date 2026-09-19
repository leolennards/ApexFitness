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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.data.FirestoreRepository
import com.example.apexfitness.ui.authentication.AuthService
import com.example.apexfitness.ui.authentication.OnboardingFormState
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexPrimaryButton
import com.example.apexfitness.ui.theme.ApexShapes
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.Motion
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.glassScreenBackground
import com.example.apexfitness.ui.theme.motionTween
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.staggeredEntrance
import kotlinx.coroutines.launch

// Onboarding step 3: workout schedule

@Composable
fun WorkoutScheduleScreen(navController: NavController, formState: OnboardingFormState) {
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    val selectedDays = formState.scheduleDays
    val workoutTime = formState.preferredTime
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
            OnboardingStepHeader(step = 3, total = 3, onBack = { navController.popBackStack() })

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(Dimens.Space4))

                Column(modifier = Modifier.staggeredEntrance(index = 0, key = "onb3-title")) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.apex.accentSoft)
                            .border(Dimens.Hairline, MaterialTheme.apex.accent.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = "Workout Schedule",
                            tint = MaterialTheme.apex.accentText,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.Space3))
                    Text(
                        text = "Workout Schedule",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(Dimens.Space1))
                    Text(
                        text = "Set your preferred workout days and time to receive personalized reminders and optimize your fitness routine.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.apex.mutedText
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Space4))

                Column(modifier = Modifier.staggeredEntrance(index = 1, key = "onb3-days")) {
                    Text(
                        text = "WORKOUT DAYS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.apex.mutedText
                    )
                    Spacer(modifier = Modifier.height(Dimens.Space1 + 4.dp))

                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        days.forEach { day ->
                            DayChip(
                                day = day,
                                isSelected = selectedDays.contains(day),
                                onSelect = {
                                    formState.scheduleDays = if (selectedDays.contains(day)) {
                                        selectedDays - day
                                    } else {
                                        selectedDays + day
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Space4))

                Text(
                    text = "PREFERRED WORKOUT TIME",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.apex.mutedText,
                    modifier = Modifier.staggeredEntrance(index = 2, key = "onb3-time-label")
                )
                Spacer(modifier = Modifier.height(Dimens.Space1 + 4.dp))

                Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space1 + 4.dp)) {
                    val times = listOf("Morning", "Afternoon", "Evening", "Flexible")
                    times.forEachIndexed { index, time ->
                        TimeOptionItem(
                            time = time,
                            isSelected = workoutTime == time,
                            onSelect = { formState.preferredTime = time },
                            modifier = Modifier.staggeredEntrance(index = 3 + index, key = "onb3-time-$time")
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
                    text = if (isSaving) "Saving" else "Complete Setup",
                    onClick = {
                        val uid = authService.getCurrentUser()?.uid
                        if (uid == null) {
                            navController.navigate("main") { popUpTo("welcome") { inclusive = true } }
                        } else {
                            isSaving = true
                            coroutineScope.launch {
                                runCatching {
                                    FirestoreRepository.updateProfileFields(
                                        uid,
                                        mapOf(
                                            "fitnessLevel" to formState.fitnessLevel,
                                            "goals" to formState.goals.toList(),
                                            "scheduleDays" to formState.scheduleDays.toList(),
                                            "preferredTime" to formState.preferredTime,
                                            "onboardingComplete" to true
                                        )
                                    )
                                }
                                isSaving = false
                                navController.navigate("main") { popUpTo("welcome") { inclusive = true } }
                            }
                        }
                    },
                    enabled = !isSaving,
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

// One day toggle
@Composable
fun DayChip(
    day: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = ApexShapes.small
    val fill by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.apex.accentSoft else MaterialTheme.colorScheme.surface,
        animationSpec = motionTween(Motion.Micro),
        label = "dayFill"
    )
    val border by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.apex.accent else MaterialTheme.apex.hairline,
        animationSpec = motionTween(Motion.Micro),
        label = "dayBorder"
    )
    Box(
        modifier = modifier
            .apexClickable(onClick = onSelect)
            .clip(shape)
            .background(fill)
            .border(Dimens.Hairline, border, shape)
            .heightIn(min = 56.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.take(2).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.apex.accentText else MaterialTheme.apex.mutedText,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// One time option (single choice)
@Composable
fun TimeOptionItem(
    time: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, description) = when (time) {
        "Morning" -> Icons.Outlined.WbSunny to "Start the day strong"
        "Afternoon" -> Icons.Outlined.LightMode to "A midday session"
        "Evening" -> Icons.Outlined.NightsStay to "Wind down with a workout"
        else -> Icons.Outlined.Schedule to "Whenever it fits"
    }
    SelectableOptionCard(
        icon = icon,
        title = time,
        description = description,
        isSelected = isSelected,
        onSelect = onSelect,
        modifier = modifier
    )
}

@Preview(showBackground = true, showSystemUi = true, name = "Step 3 light")
@Composable
fun WorkoutScheduleScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        WorkoutScheduleScreen(
            navController = rememberNavController(),
            formState = OnboardingFormState().apply {
                scheduleDays = setOf("Mon", "Wed", "Fri")
                preferredTime = "Evening"
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Step 3 dark")
@Composable
private fun WorkoutScheduleScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        WorkoutScheduleScreen(
            navController = rememberNavController(),
            formState = OnboardingFormState().apply { scheduleDays = setOf("Tue", "Sat") }
        )
    }
}
