package com.example.apexfitness.ui.water

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.data.FirestoreRepository
import com.example.apexfitness.data.WaterLog
import com.example.apexfitness.ui.authentication.AuthService
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexProgressBar
import com.example.apexfitness.ui.theme.ApexScreenHeader
import com.example.apexfitness.ui.theme.ApexShapes
import com.example.apexfitness.ui.theme.ApexText
import com.example.apexfitness.ui.theme.ApexTextField
import com.example.apexfitness.ui.theme.CardShape
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.PillShape
import com.example.apexfitness.ui.theme.SkeletonBlock
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.glassPanel
import com.example.apexfitness.ui.theme.glassScreenBackground
import com.example.apexfitness.ui.theme.rememberAnimatedProgress
import com.example.apexfitness.ui.theme.rememberCountUpInt
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.staggeredEntrance
import androidx.compose.animation.Crossfade
import com.example.apexfitness.ui.theme.Motion
import com.example.apexfitness.ui.theme.motionTween
import kotlinx.coroutines.launch

private val QuickAddAmounts = listOf(250, 500, 750)

// Water tracking. One Firestore document per day, with a goal the user can change.
@Composable
fun WaterTrackingScreen(
    navController: NavHostController,
    previewTodayMl: Int? = null,
    previewGoalMl: Int = 2500,
    previewHistory: List<WaterLog> = emptyList()
) {
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }
    val uid = authService.getCurrentUser()?.uid
    val coroutineScope = rememberCoroutineScope()
    val glassState = rememberGlassState()

    var todayMl by remember { mutableStateOf(previewTodayMl ?: 0) }
    var goalMl by remember { mutableStateOf(previewGoalMl) }
    var history by remember { mutableStateOf(previewHistory) }
    var isLoading by remember { mutableStateOf(previewTodayMl == null) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showCustomAmountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (previewTodayMl != null) return@LaunchedEffect
        val id = uid
        if (id == null) {
            isLoading = false
            return@LaunchedEffect
        }
        launch {
            FirestoreRepository.observeProfile(id).collect { profile ->
                goalMl = profile?.dailyWaterGoalMl ?: 2500
            }
        }
        launch {
            FirestoreRepository.observeTodayWaterLog(id).collect { log ->
                todayMl = log?.millilitersConsumed ?: 0
                isLoading = false
            }
        }
        launch {
            FirestoreRepository.observeRecentWaterLogs(id).collect { history = it }
        }
    }

    fun addWater(deltaMl: Int) {
        val id = uid ?: return
        coroutineScope.launch { runCatching { FirestoreRepository.addWater(id, deltaMl) } }
    }

    fun saveGoal(newGoalMl: Int) {
        val id = uid
        goalMl = newGoalMl
        if (id != null) {
            coroutineScope.launch {
                runCatching { FirestoreRepository.updateProfileFields(id, mapOf("dailyWaterGoalMl" to newGoalMl)) }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .glassScreenBackground(glassState)
    ) {
        ApexScreenHeader(
            title = "Water Intake",
            label = "STAY HYDRATED",
            onBack = { navController.popBackStack() }
        )

        Crossfade(
            targetState = isLoading,
            animationSpec = motionTween(Motion.Standard),
            label = "waterLoadingCrossfade",
            modifier = Modifier.fillMaxSize()
        ) { loading ->
            if (loading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.ScreenEdge),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
                ) {
                    SkeletonBlock(modifier = Modifier.fillMaxWidth().height(200.dp), shape = CardShape)
                    SkeletonBlock(modifier = Modifier.fillMaxWidth().height(96.dp), shape = CardShape)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Dimens.ScreenEdge)
                        .padding(bottom = Dimens.Space4),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space3)
                ) {
                    WaterProgressCard(
                        todayMl = todayMl,
                        goalMl = goalMl,
                        glassState = glassState,
                        onEditGoal = { showGoalDialog = true },
                        modifier = Modifier.staggeredEntrance(index = 0, key = "water-progress")
                    )

                    Column(modifier = Modifier.staggeredEntrance(index = 1, key = "water-quickadd")) {
                        SectionLabel("QUICK ADD")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Dimens.Space1 + 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            QuickAddAmounts.forEach { amount ->
                                QuickAddButton(
                                    amountMl = amount,
                                    glassState = glassState,
                                    onClick = { addWater(amount) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(Dimens.Space1 + 4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .apexClickable { showCustomAmountDialog = true }
                                .heightIn(min = 52.dp)
                                .clip(PillShape)
                                .border(Dimens.Hairline, MaterialTheme.apex.mutedText.copy(alpha = 0.5f), PillShape),
                            horizontalArrangement = Arrangement.Center,
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
                                text = "Custom Amount",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (history.isNotEmpty()) {
                        Column(modifier = Modifier.staggeredEntrance(index = 2, key = "water-history")) {
                            WaterHistoryChart(history = history, goalMl = goalMl, glassState = glassState)
                        }
                    }
                }
            }
        }
    }

    if (showGoalDialog) {
        var goalInput by remember { mutableStateOf(goalMl.toString()) }
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = ApexShapes.large,
            title = {
                Text(
                    text = "Daily Goal (ml)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                ApexTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it.filter { ch -> ch.isDigit() } },
                    label = "Millilitres",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    container = MaterialTheme.colorScheme.background
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        goalInput.toIntOrNull()?.takeIf { it > 0 }?.let { saveGoal(it) }
                        showGoalDialog = false
                    },
                    modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
                ) {
                    Text(text = "Save", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.apex.accentText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showGoalDialog = false },
                    modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
                ) {
                    Text(text = "Cancel", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    if (showCustomAmountDialog) {
        var amountInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCustomAmountDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = ApexShapes.large,
            title = {
                Text(
                    text = "Add Water (ml)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                ApexTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { ch -> ch.isDigit() } },
                    label = "Amount, e.g. 300",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    container = MaterialTheme.colorScheme.background
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        amountInput.toIntOrNull()?.takeIf { it > 0 }?.let { addWater(it) }
                        showCustomAmountDialog = false
                    },
                    modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
                ) {
                    Text(text = "Add", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.apex.accentText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCustomAmountDialog = false },
                    modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
                ) {
                    Text(text = "Cancel", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.apex.mutedText,
        modifier = Modifier.padding(bottom = Dimens.Space1, start = 4.dp)
    )
}

@Composable
private fun WaterProgressCard(
    todayMl: Int,
    goalMl: Int,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onEditGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressFraction = if (goalMl <= 0) 0f else (todayMl.toFloat() / goalMl.toFloat()).coerceIn(0f, 1f)
    val animatedMl by rememberCountUpInt(todayMl)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space3),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .clip(CircleShape)
                .background(MaterialTheme.apex.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.WaterDrop,
                contentDescription = null,
                tint = MaterialTheme.apex.accentText,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Space2))
        Text(
            text = "$animatedMl",
            style = ApexText.HeroNumeral,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Text(
            text = "ML TODAY",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.apex.mutedText
        )
        Spacer(modifier = Modifier.height(Dimens.Space1))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "of $goalMl ml goal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.apex.mutedText
            )
            Box(
                modifier = Modifier
                    .size(Dimens.MinTouchTarget)
                    .apexClickable(onClick = onEditGoal),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit goal",
                    tint = MaterialTheme.apex.mutedText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(Dimens.Space1))
        ApexProgressBar(progress = progressFraction, height = 8.dp)
    }
}

@Composable
private fun QuickAddButton(
    amountMl: Int,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .apexClickable(onClick = onClick)
            .glassPanel(glassState, shape = CardShape)
            .heightIn(min = 88.dp)
            .padding(vertical = Dimens.Space2),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "+$amountMl",
            style = ApexText.NumeralSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Text(
            text = "ML",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.apex.mutedText
        )
    }
}

@Composable
private fun WaterHistoryChart(history: List<WaterLog>, goalMl: Int, glassState: com.example.apexfitness.ui.theme.GlassState) {
    val ordered = remember(history) { history.sortedBy { it.dateKey } }
    val maxMl = (ordered.maxOfOrNull { it.millilitersConsumed } ?: 0).coerceAtLeast(goalMl).coerceAtLeast(1)
    val progress = rememberAnimatedProgress(1f)
    val accent = MaterialTheme.apex.accent
    val emptyBar = MaterialTheme.apex.hairline

    Column {
        SectionLabel("LAST ${ordered.size} DAYS")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassPanel(glassState, shape = CardShape)
                .padding(Dimens.Space3)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
            ) {
                val slot = size.width / ordered.size.coerceAtLeast(1)
                val barWidth = 10.dp.toPx()
                val minBar = 4.dp.toPx()
                val radius = CornerRadius(barWidth / 2f)
                ordered.forEachIndexed { index, log ->
                    val ml = log.millilitersConsumed
                    val fraction = (ml.toFloat() / maxMl).coerceIn(0f, 1f)
                    val full = if (ml <= 0) minBar else minBar + (size.height - minBar) * fraction
                    val h = minBar + (full - minBar) * progress.value
                    val color = when {
                        ml <= 0 -> emptyBar
                        ml >= goalMl -> accent
                        else -> accent.copy(alpha = 0.45f)
                    }
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(slot * index + (slot - barWidth) / 2f, size.height - h),
                        size = Size(barWidth, h),
                        cornerRadius = radius
                    )
                }
            }
        }
        Text(
            text = "Full gold bars reached the daily goal",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.apex.mutedText,
            modifier = Modifier.padding(top = Dimens.Space1, start = 4.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Water light")
@Composable
fun WaterTrackingScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        WaterTrackingScreen(
            navController = rememberNavController(),
            previewTodayMl = 1750,
            previewGoalMl = 2500,
            previewHistory = listOf(
                WaterLog("2026-09-08", 1800),
                WaterLog("2026-09-09", 2600),
                WaterLog("2026-09-10", 900),
                WaterLog("2026-09-11", 2100),
                WaterLog("2026-09-12", 2500),
                WaterLog("2026-09-13", 1400),
                WaterLog("2026-09-14", 1750)
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Water dark")
@Composable
private fun WaterTrackingScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        WaterTrackingScreen(
            navController = rememberNavController(),
            previewTodayMl = 600,
            previewGoalMl = 2500,
            previewHistory = listOf(WaterLog("2026-09-13", 1400), WaterLog("2026-09-14", 600))
        )
    }
}
