package com.example.apexfitness.ui.Profile

import androidx.compose.runtime.collectAsState
import com.example.apexfitness.ui.settings.UnitPreferences
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.data.DAYS_OF_WEEK
import com.example.apexfitness.data.FirestoreRepository
import com.example.apexfitness.ui.authentication.AuthService
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexPrimaryButton
import com.example.apexfitness.ui.theme.ApexScreenHeader
import com.example.apexfitness.ui.theme.ApexTextField
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.Motion
import com.example.apexfitness.ui.theme.PillShape
import com.example.apexfitness.ui.theme.SkeletonBlock
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.glassScreenBackground
import com.example.apexfitness.ui.theme.motionTween
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.staggeredEntrance
import kotlinx.coroutines.launch

// Edit profile screen. Save sits at the bottom so it is easy to reach with a thumb.
@Composable
fun EditProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }
    val uid = authService.getCurrentUser()?.uid
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var fitnessLevel by remember { mutableStateOf("") }
    var goals by remember { mutableStateOf(setOf<String>()) }
    var scheduleDays by remember { mutableStateOf(setOf<String>()) }
    var preferredTime by remember { mutableStateOf("Morning") }
    val useLbs = UnitPreferences.useLbs.collectAsState().value
    // What the user typed, in kg or lb depending on the unit setting (it is converted to kg on save)
    var weightInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (uid != null) {
            val profile = FirestoreRepository.getProfile(uid)
            if (profile != null) {
                name = profile.name
                fitnessLevel = profile.fitnessLevel
                goals = profile.goals.toSet()
                scheduleDays = profile.scheduleDays.toSet()
                preferredTime = profile.preferredTime.ifBlank { "Morning" }
                weightInput = if (profile.weightKg > 0) UnitPreferences.format(UnitPreferences.fromKg(profile.weightKg, useLbs)) else ""
            }
        }
        isLoading = false
    }

    val levels = listOf("Beginner", "Intermediate", "Advanced")
    val goalOptions = listOf("Build Muscle", "Lose Weight", "Improve Endurance", "General Fitness", "Flexibility & Mobility", "Sports Performance")
    val times = listOf("Morning", "Afternoon", "Evening", "Flexible")
    val glassState = rememberGlassState()

    fun save() {
        val id = uid ?: return
        isSaving = true
        coroutineScope.launch {
            runCatching {
                FirestoreRepository.updateProfileFields(
                    id,
                    mapOf(
                        "name" to name.trim(),
                        "fitnessLevel" to fitnessLevel,
                        "goals" to goals.toList(),
                        "scheduleDays" to scheduleDays.toList(),
                        "preferredTime" to preferredTime,
                        "weightKg" to UnitPreferences.toKg(weightInput.toDoubleOrNull() ?: 0.0, useLbs)
                    )
                )
            }
            isSaving = false
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .glassScreenBackground(glassState)
            .imePadding()
    ) {
        ApexScreenHeader(
            title = "Edit Profile",
            label = "YOUR DETAILS",
            onBack = { navController.popBackStack() }
        )

        Crossfade(
            targetState = isLoading,
            animationSpec = motionTween(Motion.Standard),
            label = "editProfileLoadingCrossfade",
            modifier = Modifier.weight(1f)
        ) { loading ->
            if (loading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.ScreenEdge),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
                ) {
                    SkeletonBlock(modifier = Modifier.fillMaxWidth().height(56.dp))
                    SkeletonBlock(modifier = Modifier.fillMaxWidth().height(104.dp))
                    SkeletonBlock(modifier = Modifier.fillMaxWidth().height(160.dp))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Dimens.ScreenEdge)
                        .padding(bottom = Dimens.Space2),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space3)
                ) {
                    FormSection(label = "NAME", index = 0) {
                        ApexTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Name",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    FormSection(label = "FITNESS LEVEL", index = 1) {
                        ChipGrid(
                            items = levels,
                            isSelected = { fitnessLevel == it },
                            onClick = { fitnessLevel = it }
                        )
                    }

                    FormSection(label = "GOALS", index = 2) {
                        ChipGrid(
                            items = goalOptions,
                            isSelected = { goals.contains(it) },
                            onClick = { goals = if (goals.contains(it)) goals - it else goals + it }
                        )
                    }

                    FormSection(label = "WORKOUT DAYS", index = 3) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DAYS_OF_WEEK.forEach { day ->
                                ChoiceChip(
                                    text = day.take(2),
                                    selected = scheduleDays.contains(day),
                                    onClick = {
                                        scheduleDays = if (scheduleDays.contains(day)) scheduleDays - day else scheduleDays + day
                                    },
                                    modifier = Modifier.weight(1f),
                                    horizontalPadding = 0.dp
                                )
                            }
                        }
                    }

                    FormSection(label = "PREFERRED TIME", index = 4) {
                        ChipGrid(
                            items = times,
                            isSelected = { preferredTime == it },
                            onClick = { preferredTime = it }
                        )
                    }

                    FormSection(label = "WEIGHT", index = 5) {
                        ApexTextField(
                            value = weightInput,
                            onValueChange = { input -> weightInput = input.filter { it.isDigit() || it == '.' } },
                            label = "Weight (${UnitPreferences.label(useLbs)})",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Optional. Improves your calorie estimates.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.apex.mutedText,
                            modifier = Modifier.padding(start = 4.dp, top = Dimens.Space1)
                        )
                    }
                }
            }
        }

        if (!isLoading) {
            ApexPrimaryButton(
                text = if (isSaving) "Saving" else "Save Changes",
                onClick = { save() },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenEdge, vertical = Dimens.Space2)
            )
        }
    }
}

@Composable
private fun FormSection(label: String, index: Int, content: @Composable () -> Unit) {
    Column(modifier = Modifier.staggeredEntrance(index = index, key = "editprofile-$label")) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.apex.mutedText,
            modifier = Modifier.padding(start = 4.dp, bottom = Dimens.Space1)
        )
        content()
    }
}

@Composable
private fun ChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: androidx.compose.ui.unit.Dp = Dimens.Space1
) {
    val apex = MaterialTheme.apex
    val fill by animateColorAsState(
        targetValue = if (selected) apex.accentSoft else MaterialTheme.colorScheme.surface,
        animationSpec = motionTween(Motion.Micro),
        label = "choiceChipFill"
    )
    val border by animateColorAsState(
        targetValue = if (selected) apex.accent else apex.hairline,
        animationSpec = motionTween(Motion.Micro),
        label = "choiceChipBorder"
    )
    Box(
        modifier = modifier
            .apexClickable(onClick = onClick)
            .heightIn(min = Dimens.MinTouchTarget)
            .clip(PillShape)
            .background(fill)
            .border(Dimens.Hairline, border, PillShape)
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) apex.accentText else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

// Chips in rows of two
@Composable
private fun ChipGrid(items: List<String>, isSelected: (String) -> Boolean, onClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space1)) {
        items.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.Space1)) {
                rowItems.forEach { item ->
                    ChoiceChip(
                        text = item,
                        selected = isSelected(item),
                        onClick = { onClick(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Edit profile light")
@Composable
fun EditProfileScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        EditProfileScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Edit profile dark")
@Composable
private fun EditProfileScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        EditProfileScreen(navController = rememberNavController())
    }
}
