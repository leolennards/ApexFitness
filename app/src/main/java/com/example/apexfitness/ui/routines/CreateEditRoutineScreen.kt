package com.example.apexfitness.ui.routines

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.data.DAYS_OF_WEEK
import com.example.apexfitness.data.ExerciseLibrary
import com.example.apexfitness.data.FirestoreRepository
import com.example.apexfitness.data.Routine
import com.example.apexfitness.data.RoutineExercise
import com.example.apexfitness.data.RoutineIcons
import com.example.apexfitness.ui.authentication.AuthService
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexPrimaryButton
import com.example.apexfitness.ui.theme.ApexShapes
import com.example.apexfitness.ui.theme.ApexText
import com.example.apexfitness.ui.theme.CardShape
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.LocalMotionEnabled
import com.example.apexfitness.ui.theme.PillShape
import com.example.apexfitness.ui.theme.Motion
import com.example.apexfitness.ui.theme.SharedKeys
import com.example.apexfitness.ui.theme.SkeletonBlock
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.glassPanel
import com.example.apexfitness.ui.theme.glassScreenBackground
import com.example.apexfitness.ui.theme.motionTween
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.sharedCardBounds
import com.example.apexfitness.ui.theme.staggeredEntrance
import kotlinx.coroutines.launch

// Routine editor. Every control is at least 48dp tall.

@Composable
fun CreateEditRoutineScreen(navController: NavHostController, routineId: String) {
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }
    val uid = authService.getCurrentUser()?.uid
    val coroutineScope = rememberCoroutineScope()
    val isNew = routineId == "new"

    var name by remember { mutableStateOf("") }
    var iconKey by remember { mutableStateOf("dumbbell") }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }
    val exercises = remember { mutableStateListOf<RoutineExercise>() }
    val glassState = rememberGlassState()

    var isLoading by remember { mutableStateOf(!isNew) }
    var isSaving by remember { mutableStateOf(false) }
    var showExercisePicker by remember { mutableStateOf(false) }
    var favoriteExerciseNames by remember { mutableStateOf<Set<String>>(emptySet()) }

    fun toggleFavoriteExercise(exerciseName: String) {
        val updated = if (favoriteExerciseNames.contains(exerciseName)) {
            favoriteExerciseNames - exerciseName
        } else {
            favoriteExerciseNames + exerciseName
        }
        favoriteExerciseNames = updated
        val currentUid = uid ?: return
        coroutineScope.launch {
            runCatching { FirestoreRepository.updateProfileFields(currentUid, mapOf("favoriteExerciseNames" to updated.toList())) }
        }
    }

    LaunchedEffect(uid) {
        if (uid != null) {
            favoriteExerciseNames = (FirestoreRepository.getProfile(uid)?.favoriteExerciseNames ?: emptyList()).toSet()
        }
    }

    LaunchedEffect(routineId, uid) {
        if (!isNew && uid != null) {
            val existing = FirestoreRepository.getRoutine(uid, routineId)
            if (existing != null) {
                name = existing.name
                iconKey = existing.iconKey
                selectedDays = existing.days.toSet()
                exercises.clear()
                exercises.addAll(existing.exercises.sortedBy { it.order })
            }
            isLoading = false
        }
    }

    fun save() {
        if (uid == null || name.isBlank()) return
        isSaving = true
        val routine = Routine(
            id = if (isNew) "" else routineId,
            name = name.trim(),
            iconKey = iconKey,
            days = selectedDays.sortedBy { DAYS_OF_WEEK.indexOf(it) },
            exercises = exercises.mapIndexed { index, ex -> ex.copy(order = index) }
        )
        coroutineScope.launch {
            runCatching { FirestoreRepository.saveRoutine(uid, routine) }
            isSaving = false
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .sharedCardBounds(if (isNew) "" else SharedKeys.lastEditor)
            .glassScreenBackground(glassState)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EditorHeader(
                title = if (isNew) "New Routine" else "Edit Routine",
                canSave = name.isNotBlank() && !isSaving && !isLoading,
                isSaving = isSaving,
                onBack = { navController.popBackStack() },
                onSave = { save() }
            )

            Crossfade(
                targetState = isLoading,
                animationSpec = motionTween(Motion.Standard),
                label = "editorLoadingCrossfade",
                modifier = Modifier.fillMaxSize()
            ) { loading ->
                if (loading) {
                    EditorSkeleton()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = Dimens.ScreenEdge,
                            end = Dimens.ScreenEdge,
                            top = Dimens.Space1,
                            bottom = Dimens.Space4
                        )
                    ) {
                        item(key = "editor-form") {
                            EditorForm(
                                name = name,
                                onNameChange = { name = it },
                                iconKey = iconKey,
                                onIconChange = { iconKey = it },
                                selectedDays = selectedDays,
                                onDayToggle = { day ->
                                    selectedDays = if (selectedDays.contains(day)) selectedDays - day else selectedDays + day
                                },
                                exerciseCount = exercises.size,
                                onAddExercise = { showExercisePicker = true },
                                glassState = glassState,
                                showEmpty = exercises.isEmpty(),
                                modifier = Modifier.staggeredEntrance(index = 0, key = "editor-form")
                            )
                        }

                        exerciseItems(exercises, glassState)
                    }
                }
            }
        }
    }

    if (showExercisePicker) {
        ExercisePickerDialog(
            favoriteExerciseNames = favoriteExerciseNames,
            onToggleFavorite = ::toggleFavoriteExercise,
            onDismiss = { showExercisePicker = false },
            onPick = { exerciseName ->
                exercises.add(RoutineExercise(name = exerciseName, order = exercises.size))
                showExercisePicker = false
            }
        )
    }
}

// ---- Header and form ----

@Composable
private fun EditorHeader(
    title: String,
    canSave: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenEdge, vertical = Dimens.Space2),
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
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        ApexPrimaryButton(
            text = if (isSaving) "Saving" else "Save",
            onClick = onSave,
            enabled = canSave
        )
    }
}

@Composable
private fun EditorForm(
    name: String,
    onNameChange: (String) -> Unit,
    iconKey: String,
    onIconChange: (String) -> Unit,
    selectedDays: Set<String>,
    onDayToggle: (String) -> Unit,
    exerciseCount: Int,
    onAddExercise: () -> Unit,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    showEmpty: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.apex.accentSoft)
                    .border(Dimens.Hairline, MaterialTheme.apex.accent.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = RoutineIcons.iconFor(iconKey),
                    contentDescription = null,
                    tint = MaterialTheme.apex.accentText,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(Dimens.Space2))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp),
                placeholder = {
                    Text("Routine name, e.g. Chest Day", color = MaterialTheme.apex.mutedText)
                },
                singleLine = true,
                colors = editorFieldColors(container = MaterialTheme.colorScheme.surface),
                shape = ApexShapes.small
            )
        }

        Spacer(modifier = Modifier.height(Dimens.Space3))

        FieldLabel("ICON")
        Spacer(modifier = Modifier.height(Dimens.Space1))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space1)
        ) {
            RoutineIcons.options.forEach { (key, icon, _) ->
                SelectChip(
                    selected = key == iconKey,
                    onClick = { onIconChange(key) },
                    shape = CircleShape,
                    modifier = Modifier.size(Dimens.MinTouchTarget)
                ) { selected ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.apex.accentText else MaterialTheme.apex.mutedText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.Space3))

        FieldLabel("ASSIGN TO DAY(S)")
        Spacer(modifier = Modifier.height(Dimens.Space1))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DAYS_OF_WEEK.forEach { day ->
                SelectChip(
                    selected = selectedDays.contains(day),
                    onClick = { onDayToggle(day) },
                    shape = ApexShapes.small,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = Dimens.MinTouchTarget)
                ) { selected ->
                    Text(
                        text = day.take(2).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) MaterialTheme.apex.accentText else MaterialTheme.apex.mutedText,
                        maxLines = 1
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(Dimens.Space1))
        Text(
            text = "Leave empty to keep it unscheduled",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.apex.mutedText
        )

        Spacer(modifier = Modifier.height(Dimens.Space4))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Exercises",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "$exerciseCount IN THIS ROUTINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.apex.mutedText
                )
            }
            Row(
                modifier = Modifier
                    .apexClickable(onClick = onAddExercise)
                    .heightIn(min = Dimens.MinTouchTarget)
                    .clip(PillShape)
                    .border(Dimens.Hairline, MaterialTheme.apex.hairline, PillShape)
                    .padding(horizontal = Dimens.Space2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = MaterialTheme.apex.accentText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Dimens.Space1))
                Text(
                    text = "Add",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(Dimens.Space2))

        if (showEmpty) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassPanel(glassState, shape = CardShape)
            ) {
                Text(
                    text = "No exercises yet - tap Add to build this routine.",
                    modifier = Modifier.padding(Dimens.Space3),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.apex.mutedText
                )
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.apex.mutedText
    )
}

// Selected chip has a gold border, the others just a thin line
@Composable
private fun SelectChip(
    selected: Boolean,
    onClick: () -> Unit,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier,
    content: @Composable (Boolean) -> Unit
) {
    val fill by animateColorAsState(
        targetValue = if (selected) MaterialTheme.apex.accentSoft else Color.Transparent,
        animationSpec = motionTween(Motion.Micro),
        label = "chipFill"
    )
    val border by animateColorAsState(
        targetValue = if (selected) MaterialTheme.apex.accent else MaterialTheme.apex.hairline,
        animationSpec = motionTween(Motion.Micro),
        label = "chipBorder"
    )
    Box(
        modifier = modifier
            .apexClickable(onClick = onClick)
            .clip(shape)
            .background(fill)
            .border(Dimens.Hairline, border, shape),
        contentAlignment = Alignment.Center
    ) {
        content(selected)
    }
}

// Text field colours, with borders that are easy to see in both themes
@Composable
private fun editorFieldColors(container: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.apex.accent,
    unfocusedBorderColor = MaterialTheme.apex.mutedText.copy(alpha = 0.5f),
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.apex.accent,
    focusedContainerColor = container,
    unfocusedContainerColor = container
)

// ---- Exercise cards ----

private fun androidx.compose.foundation.lazy.LazyListScope.exerciseItems(
    exercises: SnapshotStateList<RoutineExercise>,
    glassState: com.example.apexfitness.ui.theme.GlassState
) {
    items(count = exercises.size, key = { exercises[it].id }) { index ->
        ExerciseEditorCard(
            exercise = exercises[index],
            index = index,
            total = exercises.size,
            glassState = glassState,
            onChange = { exercises[index] = it },
            onDelete = { exercises.removeAt(index) },
            onMoveUp = { if (index > 0) { val tmp = exercises[index - 1]; exercises[index - 1] = exercises[index]; exercises[index] = tmp } },
            onMoveDown = { if (index < exercises.size - 1) { val tmp = exercises[index + 1]; exercises[index + 1] = exercises[index]; exercises[index] = tmp } },
            modifier = Modifier
                .animateItem()
                .padding(bottom = Dimens.Space2)
        )
    }
}

@Composable
private fun ExerciseEditorCard(
    exercise: RoutineExercise,
    index: Int,
    total: Int,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onChange: (RoutineExercise) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val muted = MaterialTheme.apex.mutedText
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space2)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(Dimens.Space2))
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(Dimens.Space2))

        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space1 + 4.dp)) {
            NumberStepperField(
                label = "SETS",
                value = exercise.sets,
                onValueChange = { onChange(exercise.copy(sets = it.coerceIn(1, 20))) },
                modifier = Modifier.weight(1f)
            )
            NumberStepperField(
                label = "REST (S)",
                value = exercise.restSeconds,
                step = 15,
                onValueChange = { onChange(exercise.copy(restSeconds = it.coerceIn(0, 600))) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(Dimens.Space1 + 4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space1 + 4.dp)) {
            CompactTextField(
                label = "REPS",
                value = exercise.reps,
                onValueChange = { onChange(exercise.copy(reps = it)) },
                modifier = Modifier.weight(1f)
            )
            CompactTextField(
                label = "TARGET WEIGHT",
                value = exercise.targetWeight,
                onValueChange = { onChange(exercise.copy(targetWeight = it)) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(Dimens.Space1))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardIconButton(
                icon = Icons.Outlined.KeyboardArrowUp,
                description = "Move up",
                enabled = index > 0,
                tint = muted,
                onClick = onMoveUp
            )
            CardIconButton(
                icon = Icons.Outlined.KeyboardArrowDown,
                description = "Move down",
                enabled = index < total - 1,
                tint = muted,
                onClick = onMoveDown
            )
            CardIconButton(
                icon = Icons.Outlined.Close,
                description = "Remove ${exercise.name}",
                enabled = true,
                tint = muted,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun CardIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    enabled: Boolean,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(Dimens.MinTouchTarget)
            .apexClickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (enabled) tint else tint.copy(alpha = 0.35f),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun NumberStepperField(
    label: String,
    value: Int,
    step: Int = 1,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = ApexShapes.small
    Column(modifier = modifier) {
        FieldLabel(label)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.background)
                .border(Dimens.Hairline, MaterialTheme.apex.mutedText.copy(alpha = 0.5f), shape),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.MinTouchTarget)
                    .apexClickable { onValueChange(value - step) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Remove,
                    contentDescription = "Decrease $label",
                    tint = MaterialTheme.apex.accentText,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "$value",
                style = ApexText.NumeralSmall.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Box(
                modifier = Modifier
                    .size(Dimens.MinTouchTarget)
                    .apexClickable { onValueChange(value + step) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Increase $label",
                    tint = MaterialTheme.apex.accentText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CompactTextField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        FieldLabel(label)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Dimens.MinTouchTarget),
            colors = editorFieldColors(container = MaterialTheme.colorScheme.background),
            shape = ApexShapes.small
        )
    }
}

// ---- Exercise picker ----

@Composable
private fun ExercisePickerDialog(
    favoriteExerciseNames: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }

    // Popup fades in and scales up slightly (instant if animations are off)
    val motionEnabled = LocalMotionEnabled.current
    val entrance = remember { Animatable(if (motionEnabled) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (motionEnabled) entrance.animateTo(1f, tween(Motion.Standard, easing = FastOutSlowInEasing))
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .graphicsLayer {
                    alpha = entrance.value
                    val s = 0.92f + 0.08f * entrance.value
                    scaleX = s
                    scaleY = s
                }
                .clip(ApexShapes.large)
                .background(MaterialTheme.colorScheme.surface)
                .border(Dimens.Hairline, MaterialTheme.apex.hairline, ApexShapes.large)
                .padding(Dimens.Space3)
        ) {
            Text(
                text = "Add Exercise",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimens.Space2))
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search exercises", color = MaterialTheme.apex.mutedText) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.apex.mutedText
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                colors = editorFieldColors(container = MaterialTheme.colorScheme.background),
                shape = ApexShapes.small
            )
            Spacer(modifier = Modifier.height(Dimens.Space1))

            LazyColumn(modifier = Modifier.weight(1f)) {
                val favoritesMatching = favoriteExerciseNames
                    .filter { it.contains(search, ignoreCase = true) }
                    .sorted()
                if (favoritesMatching.isNotEmpty()) {
                    item { PickerSectionLabel("FAVORITES") }
                    items(favoritesMatching) { exerciseName ->
                        ExercisePickerRow(
                            name = exerciseName,
                            isFavorite = true,
                            onClick = { onPick(exerciseName) },
                            onToggleFavorite = { onToggleFavorite(exerciseName) }
                        )
                    }
                }
                ExerciseLibrary.exercisesByCategory.forEach { (category, list) ->
                    val filtered = list.filter { it.contains(search, ignoreCase = true) }
                    if (filtered.isNotEmpty()) {
                        item { PickerSectionLabel(category.uppercase()) }
                        items(filtered) { exerciseName ->
                            ExercisePickerRow(
                                name = exerciseName,
                                isFavorite = favoriteExerciseNames.contains(exerciseName),
                                onClick = { onPick(exerciseName) },
                                onToggleFavorite = { onToggleFavorite(exerciseName) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Space1))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    placeholder = { Text("Can't find it? Type a custom one", color = MaterialTheme.apex.mutedText) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp),
                    colors = editorFieldColors(container = MaterialTheme.colorScheme.background),
                    shape = ApexShapes.small
                )
                Spacer(modifier = Modifier.width(Dimens.Space1))
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .apexClickable { if (customName.isNotBlank()) onPick(customName.trim()) }
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add custom exercise",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.apex.accentText,
        modifier = Modifier.padding(top = Dimens.Space2, bottom = 4.dp)
    )
}

@Composable
private fun ExercisePickerRow(
    name: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .apexClickable(onClick = onClick)
            .heightIn(min = Dimens.MinTouchTarget),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp)
        )
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .apexClickable(onClick = onToggleFavorite),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (isFavorite) MaterialTheme.apex.accent else MaterialTheme.apex.mutedText,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ---- Loading skeleton ----

@Composable
private fun EditorSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ScreenEdge)
            .padding(top = Dimens.Space1),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeletonBlock(modifier = Modifier.size(56.dp), shape = CircleShape)
            Spacer(modifier = Modifier.width(Dimens.Space2))
            SkeletonBlock(modifier = Modifier.weight(1f).height(56.dp), shape = ApexShapes.small)
        }
        SkeletonBlock(modifier = Modifier.width(80.dp).height(12.dp))
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(48.dp), shape = ApexShapes.small)
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(160.dp), shape = CardShape)
    }
}

// ---- Previews ----

@Preview(showBackground = true, showSystemUi = true, name = "Editor light")
@Composable
fun CreateEditRoutineScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        CreateEditRoutineScreen(
            navController = rememberNavController(),
            routineId = "new"
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Editor dark")
@Composable
private fun CreateEditRoutineScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        CreateEditRoutineScreen(
            navController = rememberNavController(),
            routineId = "new"
        )
    }
}

@Preview(showBackground = true, widthDp = 360, name = "Exercise card")
@Composable
private fun ExerciseEditorCardPreview() {
    ApexFitnessTheme(darkTheme = false) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(Dimens.ScreenEdge)) {
            ExerciseEditorCard(
                exercise = RoutineExercise(name = "Incline Dumbbell Press", sets = 3, reps = "10", restSeconds = 90),
                index = 1,
                total = 3,
                glassState = rememberGlassState(),
                onChange = {},
                onDelete = {},
                onMoveUp = {},
                onMoveDown = {}
            )
        }
    }
}
