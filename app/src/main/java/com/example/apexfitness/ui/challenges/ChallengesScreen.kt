package com.example.apexfitness.ui.challenges

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.data.ChallengeCalculations
import com.example.apexfitness.data.FirestoreRepository
import com.example.apexfitness.data.UserChallenge
import com.example.apexfitness.data.WorkoutLog
import com.example.apexfitness.ui.authentication.AuthService
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexProgressBar
import com.example.apexfitness.ui.theme.ApexScreenHeader
import com.example.apexfitness.ui.theme.ApexText
import com.example.apexfitness.ui.theme.CardShape
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.Motion
import com.example.apexfitness.ui.theme.PillShape
import com.example.apexfitness.ui.theme.SkeletonBlock
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.glassPanel
import com.example.apexfitness.ui.theme.glassScreenBackground
import com.example.apexfitness.ui.theme.motionTween
import com.example.apexfitness.ui.theme.rememberCountUpInt
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.staggeredEntrance
import kotlinx.coroutines.launch

private const val DAY_MILLIS = 24L * 60 * 60 * 1000

// Challenges screen. The user picks from a fixed list of challenges (see ChallengeTemplates).
@Composable
fun ChallengesScreen(
    navController: NavHostController,
    previewChallenges: List<UserChallenge>? = null,
    previewLogs: List<WorkoutLog> = emptyList()
) {
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }
    val uid = authService.getCurrentUser()?.uid
    val coroutineScope = rememberCoroutineScope()
    val glassState = rememberGlassState()

    var challenges by remember { mutableStateOf(previewChallenges ?: emptyList()) }
    var logs by remember { mutableStateOf(previewLogs) }
    var hasLoadedChallenges by remember { mutableStateOf(previewChallenges != null) }
    var hasLoadedLogs by remember { mutableStateOf(previewChallenges != null) }
    val isLoading = previewChallenges == null && !(hasLoadedChallenges && hasLoadedLogs)

    LaunchedEffect(uid) {
        if (previewChallenges != null) return@LaunchedEffect
        val id = uid
        if (id == null) {
            hasLoadedChallenges = true
            hasLoadedLogs = true
            return@LaunchedEffect
        }
        launch {
            FirestoreRepository.observeChallenges(id).collect {
                challenges = it
                hasLoadedChallenges = true
            }
        }
        launch {
            FirestoreRepository.observeWorkoutLogs(id).collect {
                logs = it
                hasLoadedLogs = true
            }
        }
    }

    // Marks a challenge as completed as soon as it is reached, so a streak challenge does not
    // un-complete itself if the streak breaks later
    LaunchedEffect(challenges, logs) {
        val id = uid ?: return@LaunchedEffect
        challenges.forEach { challenge ->
            if (challenge.completed) return@forEach
            val template = ChallengeTemplates.find { it.id == challenge.templateId } ?: return@forEach
            if (!ChallengeCalculations.isExpired(challenge) &&
                ChallengeCalculations.isComplete(challenge, template.type, logs)
            ) {
                runCatching { FirestoreRepository.markChallengeCompleted(id, challenge.id) }
            }
        }
    }

    fun startOrRestart(template: ChallengeTemplate) {
        val id = uid ?: return
        coroutineScope.launch {
            runCatching {
                FirestoreRepository.startChallenge(id, template.id, template.targetValue, template.durationDays)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .glassScreenBackground(glassState)
    ) {
        ApexScreenHeader(
            title = "Challenges",
            label = "PUSH YOURSELF",
            onBack = { navController.popBackStack() }
        )

        Crossfade(
            targetState = isLoading,
            animationSpec = motionTween(Motion.Standard),
            label = "challengesLoadingCrossfade",
            modifier = Modifier.fillMaxSize()
        ) { loading ->
            if (loading) {
                ChallengesSkeleton()
            } else {
                val challengesByTemplateId = challenges.associateBy { it.templateId }
                val active = ChallengeTemplates.mapNotNull { template ->
                    val challenge = challengesByTemplateId[template.id] ?: return@mapNotNull null
                    if (challenge.completed || ChallengeCalculations.isExpired(challenge)) null else template to challenge
                }
                val finished = ChallengeTemplates.mapNotNull { template ->
                    val challenge = challengesByTemplateId[template.id] ?: return@mapNotNull null
                    if (challenge.completed || ChallengeCalculations.isExpired(challenge)) template to challenge else null
                }
                val available = ChallengeTemplates.filter { challengesByTemplateId[it.id] == null }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Dimens.ScreenEdge,
                        end = Dimens.ScreenEdge,
                        bottom = Dimens.Space4
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space1 + 4.dp)
                ) {
                    var index = 0

                    if (active.isNotEmpty()) {
                        item(key = "label-active") { ChallengesSectionLabel(text = "ACTIVE") }
                        active.forEach { (template, challenge) ->
                            val i = index++
                            item(key = "active-${template.id}") {
                                ActiveChallengeCard(
                                    template = template,
                                    challenge = challenge,
                                    progress = ChallengeCalculations.progressValue(challenge, template.type, logs),
                                    glassState = glassState,
                                    modifier = Modifier
                                        .staggeredEntrance(index = i.coerceAtMost(8), key = "challenge-active-${template.id}")
                                        .animateItem()
                                )
                            }
                        }
                    }

                    if (available.isNotEmpty()) {
                        item(key = "label-available") { ChallengesSectionLabel(text = "AVAILABLE") }
                        available.forEach { template ->
                            val i = index++
                            item(key = "available-${template.id}") {
                                AvailableChallengeCard(
                                    template = template,
                                    glassState = glassState,
                                    onStart = { startOrRestart(template) },
                                    modifier = Modifier
                                        .staggeredEntrance(index = i.coerceAtMost(8), key = "challenge-available-${template.id}")
                                        .animateItem()
                                )
                            }
                        }
                    }

                    if (finished.isNotEmpty()) {
                        item(key = "label-finished") { ChallengesSectionLabel(text = "COMPLETED & PAST") }
                        finished.forEach { (template, challenge) ->
                            val i = index++
                            item(key = "finished-${template.id}") {
                                FinishedChallengeCard(
                                    template = template,
                                    challenge = challenge,
                                    glassState = glassState,
                                    onRestart = { startOrRestart(template) },
                                    modifier = Modifier
                                        .staggeredEntrance(index = i.coerceAtMost(8), key = "challenge-finished-${template.id}")
                                        .animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengesSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ScreenEdge),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space1 + 4.dp)
    ) {
        repeat(4) {
            SkeletonBlock(
                modifier = Modifier.fillMaxWidth().height(104.dp),
                shape = CardShape
            )
        }
    }
}

@Composable
private fun ChallengesSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.apex.mutedText,
        modifier = Modifier.padding(top = Dimens.Space1, start = 4.dp)
    )
}

private fun daysRemaining(challenge: UserChallenge): Int {
    val remainingMillis = ChallengeCalculations.endDateMillis(challenge) - System.currentTimeMillis()
    return (remainingMillis / DAY_MILLIS).toInt().coerceAtLeast(0)
}

@Composable
private fun ChallengeIcon(icon: ImageVector, done: Boolean = false, muted: Boolean = false) {
    val apex = MaterialTheme.apex
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (muted) MaterialTheme.colorScheme.surfaceContainerHighest else apex.accentSoft),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (done) Icons.Outlined.Check else icon,
            contentDescription = null,
            tint = if (muted) apex.mutedText else apex.accentText,
            modifier = Modifier.size(24.dp)
        )
    }
}

// Gold pill for the main button on a card
@Composable
private fun ChallengePill(text: String, onClick: () -> Unit, filled: Boolean) {
    val apex = MaterialTheme.apex
    Box(
        modifier = Modifier
            .apexClickable(onClick = onClick)
            .heightIn(min = Dimens.MinTouchTarget)
            .clip(PillShape)
            .then(
                if (filled) Modifier.background(MaterialTheme.colorScheme.primary)
                else Modifier.border(Dimens.Hairline, apex.mutedText.copy(alpha = 0.5f), PillShape)
            )
            .padding(horizontal = Dimens.Space2 + 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ActiveChallengeCard(
    template: ChallengeTemplate,
    challenge: UserChallenge,
    progress: Int,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    modifier: Modifier = Modifier
) {
    val animatedProgress by rememberCountUpInt(progress)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space2 + 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ChallengeIcon(icon = template.icon)
            Spacer(modifier = Modifier.width(Dimens.Space2))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.apex.mutedText
                )
            }
            Spacer(modifier = Modifier.width(Dimens.Space1))
            Text(
                text = "${daysRemaining(challenge)}D LEFT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.apex.mutedText
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Space2))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$animatedProgress",
                style = ApexText.NumeralSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = " / ${template.targetValue}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.apex.mutedText,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Space1))
        ApexProgressBar(
            progress = (progress.toFloat() / template.targetValue.toFloat()).coerceIn(0f, 1f),
            height = 6.dp
        )
    }
}

@Composable
private fun AvailableChallengeCard(
    template: ChallengeTemplate,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space2 + 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChallengeIcon(icon = template.icon)
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = template.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = template.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.apex.mutedText
            )
        }
        Spacer(modifier = Modifier.width(Dimens.Space1))
        ChallengePill(text = "Start", onClick = onStart, filled = true)
    }
}

@Composable
private fun FinishedChallengeCard(
    template: ChallengeTemplate,
    challenge: UserChallenge,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space2 + 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChallengeIcon(icon = template.icon, done = challenge.completed, muted = !challenge.completed)
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = template.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (challenge.completed) "COMPLETED" else "EXPIRED",
                style = MaterialTheme.typography.labelSmall,
                color = if (challenge.completed) MaterialTheme.apex.accentText else MaterialTheme.apex.mutedText
            )
        }
        Spacer(modifier = Modifier.width(Dimens.Space1))
        ChallengePill(text = "Restart", onClick = onRestart, filled = false)
    }
}

private fun previewChallengeData(): Pair<List<UserChallenge>, List<WorkoutLog>> {
    val now = System.currentTimeMillis()
    val challenges = listOf(
        UserChallenge(id = "weekly_3", templateId = "weekly_3", startDateMillis = now - (2L * DAY_MILLIS), targetValue = 3, durationDays = 7),
        UserChallenge(id = "streak_7", templateId = "streak_7", startDateMillis = now - (10L * DAY_MILLIS), targetValue = 7, durationDays = 7, completed = true, completedAtMillis = now - (3L * DAY_MILLIS)),
        UserChallenge(id = "monthly_10", templateId = "monthly_10", startDateMillis = now - (40L * DAY_MILLIS), targetValue = 10, durationDays = 30)
    )
    val logs = listOf(
        WorkoutLog(id = "1", dateMillis = now - (1L * DAY_MILLIS)),
        WorkoutLog(id = "2", dateMillis = now - (2L * DAY_MILLIS))
    )
    return challenges to logs
}

@Preview(showBackground = true, showSystemUi = true, name = "Challenges light")
@Composable
fun ChallengesScreenPreview() {
    val (challenges, logs) = previewChallengeData()
    ApexFitnessTheme(darkTheme = false) {
        ChallengesScreen(navController = rememberNavController(), previewChallenges = challenges, previewLogs = logs)
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Challenges dark")
@Composable
private fun ChallengesScreenDarkPreview() {
    val (challenges, logs) = previewChallengeData()
    ApexFitnessTheme(darkTheme = true) {
        ChallengesScreen(navController = rememberNavController(), previewChallenges = challenges, previewLogs = logs)
    }
}
