package com.example.apexfitness.ui.achievements

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.data.FirestoreRepository
import com.example.apexfitness.ui.authentication.AuthService
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexPrimaryButton
import com.example.apexfitness.ui.theme.ApexProgressBar
import com.example.apexfitness.ui.theme.ApexScreenHeader
import com.example.apexfitness.ui.theme.ApexShapes
import com.example.apexfitness.ui.theme.ApexText
import com.example.apexfitness.ui.theme.CardShape
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.Motion
import com.example.apexfitness.ui.theme.SkeletonBlock
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.glassPanel
import com.example.apexfitness.ui.theme.glassScreenBackground
import com.example.apexfitness.ui.theme.motionTween
import com.example.apexfitness.ui.theme.overlayEntrance
import com.example.apexfitness.ui.theme.rememberCountUpInt
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.rememberOverlayProgress
import com.example.apexfitness.ui.theme.staggeredEntrance

// Shows every badge. Unlocked ones are gold, locked ones are grey with a progress bar.
@Composable
fun AchievementsScreen(
    navController: NavHostController,
    previewTotalWorkouts: Int? = null
) {
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }
    val uid = authService.getCurrentUser()?.uid

    var totalWorkouts by remember { mutableStateOf(previewTotalWorkouts ?: 0) }
    var isLoading by remember { mutableStateOf(previewTotalWorkouts == null) }

    val glassState = rememberGlassState()

    LaunchedEffect(uid) {
        if (previewTotalWorkouts == null && uid != null) {
            FirestoreRepository.observeWorkoutLogs(uid).collect { fetched ->
                totalWorkouts = fetched.size
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .glassScreenBackground(glassState)
    ) {
        ApexScreenHeader(
            title = "Achievements",
            label = "YOUR MILESTONES",
            onBack = { navController.popBackStack() }
        )

        Crossfade(
            targetState = isLoading,
            animationSpec = motionTween(Motion.Standard),
            label = "achievementsLoadingCrossfade",
            modifier = Modifier.fillMaxSize()
        ) { loading ->
            if (loading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.ScreenEdge),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space1 + 4.dp)
                ) {
                    SkeletonBlock(modifier = Modifier.fillMaxWidth().height(112.dp), shape = CardShape)
                    repeat(4) {
                        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(96.dp), shape = CardShape)
                    }
                }
            } else {
                val unlockedCount = AchievementTiers.count { totalWorkouts >= it.threshold }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Dimens.ScreenEdge,
                        end = Dimens.ScreenEdge,
                        bottom = Dimens.Space4
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space1 + 4.dp)
                ) {
                    item(key = "summary") {
                        AchievementSummaryCard(
                            unlocked = unlockedCount,
                            total = AchievementTiers.size,
                            glassState = glassState,
                            modifier = Modifier.staggeredEntrance(index = 0, key = "achievement-summary")
                        )
                    }
                    itemsIndexed(AchievementTiers, key = { _, tier -> tier.id }) { index, tier ->
                        AchievementCard(
                            tier = tier,
                            totalWorkouts = totalWorkouts,
                            glassState = glassState,
                            modifier = Modifier.staggeredEntrance(
                                index = (index + 1).coerceAtMost(8),
                                key = "achievement-${tier.id}"
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementSummaryCard(
    unlocked: Int,
    total: Int,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    modifier: Modifier = Modifier
) {
    val animatedUnlocked by rememberCountUpInt(unlocked)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space3)
    ) {
        Text(
            text = "UNLOCKED",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.apex.mutedText
        )
        Spacer(modifier = Modifier.height(Dimens.Space1))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$animatedUnlocked",
                style = ApexText.Numeral,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = " of $total",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.apex.mutedText,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Space1 + 4.dp))
        ApexProgressBar(
            progress = if (total == 0) 0f else unlocked.toFloat() / total.toFloat(),
            height = 6.dp
        )
    }
}

@Composable
private fun AchievementCard(
    tier: AchievementTier,
    totalWorkouts: Int,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    modifier: Modifier = Modifier
) {
    val isUnlocked = totalWorkouts >= tier.threshold
    val apex = MaterialTheme.apex

    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space2 + 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) apex.accentSoft else MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isUnlocked) tier.icon else Icons.Outlined.Lock,
                contentDescription = if (isUnlocked) "Unlocked" else "Locked",
                tint = if (isUnlocked) apex.accentText else apex.mutedText,
                modifier = Modifier.size(if (isUnlocked) 28.dp else 22.dp)
            )
        }

        Spacer(modifier = Modifier.width(Dimens.Space2))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tier.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else apex.mutedText
            )
            Text(
                text = tier.description,
                style = MaterialTheme.typography.bodySmall,
                color = apex.mutedText
            )
            if (!isUnlocked) {
                Spacer(modifier = Modifier.height(Dimens.Space1))
                ApexProgressBar(
                    progress = (totalWorkouts.toFloat() / tier.threshold.toFloat()).coerceIn(0f, 1f),
                    height = 4.dp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$totalWorkouts / ${tier.threshold} WORKOUTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = apex.mutedText
                )
            }
        }
    }
}

// Popup shown once when a badge is unlocked. Closing it marks the badge as seen.
@Composable
fun AchievementCelebrationOverlay(tier: AchievementTier, onDismiss: () -> Unit) {
    val progress = rememberOverlayProgress()
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
                text = "ACHIEVEMENT UNLOCKED",
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
                    imageVector = tier.icon,
                    contentDescription = null,
                    tint = MaterialTheme.apex.accentText,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(Dimens.Space2))
            Text(
                text = tier.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tier.description,
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

@Preview(showBackground = true, showSystemUi = true, name = "Achievements light")
@Composable
fun AchievementsScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        AchievementsScreen(navController = rememberNavController(), previewTotalWorkouts = 12)
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Achievements dark")
@Composable
private fun AchievementsScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        AchievementsScreen(navController = rememberNavController(), previewTotalWorkouts = 27)
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Achievement overlay")
@Composable
private fun AchievementCelebrationOverlayPreview() {
    ApexFitnessTheme(darkTheme = false) {
        AchievementCelebrationOverlay(tier = AchievementTiers[2], onDismiss = {})
    }
}
