package com.example.apexfitness.ui.Profile

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import com.example.apexfitness.ui.theme.MetricBlock
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.apexfitness.data.GamificationCalculations
import com.example.apexfitness.data.UserProfile
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexProgressBar
import com.example.apexfitness.ui.theme.ApexShapes
import com.example.apexfitness.ui.theme.ApexText
import com.example.apexfitness.ui.theme.CardShape
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.Motion
import com.example.apexfitness.ui.theme.PillShape
import com.example.apexfitness.ui.theme.SkeletonBlock
import com.example.apexfitness.ui.theme.SkeletonCircle
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.glassPanel
import com.example.apexfitness.ui.theme.motionTween
import com.example.apexfitness.ui.theme.rememberCountUpInt
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.staggeredEntrance
import androidx.compose.animation.Crossfade
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Health Data is still a "coming soon" page, so I hide it until Health Connect is in.
// The screen and its route are kept, flip this to true to show it again.
private const val SHOW_HEALTH_DATA = false

// Profile tab

@Composable
fun ProfilePage(
    profile: UserProfile?,
    totalWorkouts: Int,
    streak: Int,
    levelProgress: GamificationCalculations.LevelProgress,
    onSignOut: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenActivityHistory: () -> Unit = {},
    onOpenChallenges: () -> Unit = {},
    onOpenCardio: () -> Unit = {},
    onOpenBody: () -> Unit = {},
    onOpenPhotos: () -> Unit = {},
    onOpenHealthData: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onShareApp: () -> Unit = {},
    onOpenAchievements: () -> Unit = {},
    isLoading: Boolean = false,
    glassState: com.example.apexfitness.ui.theme.GlassState = rememberGlassState(),
    topContentPadding: Dp = 0.dp,
    bottomContentPadding: Dp = 0.dp,
    listState: LazyListState = rememberLazyListState()
) {
    Crossfade(
        targetState = isLoading,
        animationSpec = motionTween(Motion.Standard),
        label = "profileLoadingCrossfade"
    ) { loading ->
        if (loading) {
            ProfileSkeleton(topContentPadding = topContentPadding, bottomContentPadding = bottomContentPadding)
        } else {
            ProfileContent(
                profile = profile,
                totalWorkouts = totalWorkouts,
                streak = streak,
                levelProgress = levelProgress,
                onSignOut = onSignOut,
                onEditProfile = onEditProfile,
                onOpenActivityHistory = onOpenActivityHistory,
                onOpenChallenges = onOpenChallenges,
                onOpenCardio = onOpenCardio,
                onOpenBody = onOpenBody,
                onOpenPhotos = onOpenPhotos,
                onOpenHealthData = onOpenHealthData,
                onOpenSettings = onOpenSettings,
                onShareApp = onShareApp,
                onOpenAchievements = onOpenAchievements,
                glassState = glassState,
                topContentPadding = topContentPadding,
                bottomContentPadding = bottomContentPadding,
                listState = listState
            )
        }
    }
}

private data class MenuEntry(val title: String, val icon: ImageVector, val onClick: () -> Unit, val detail: String? = null)

// Menu rows grouped by what they are about, so it reads as three short lists instead of one long one
private data class MenuGroup(val label: String, val entries: List<MenuEntry>)

@Composable
private fun ProfileContent(
    profile: UserProfile?,
    totalWorkouts: Int,
    streak: Int,
    levelProgress: GamificationCalculations.LevelProgress,
    onSignOut: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenActivityHistory: () -> Unit,
    onOpenChallenges: () -> Unit,
    onOpenCardio: () -> Unit,
    onOpenBody: () -> Unit,
    onOpenPhotos: () -> Unit,
    onOpenHealthData: () -> Unit,
    onOpenSettings: () -> Unit,
    onShareApp: () -> Unit,
    onOpenAchievements: () -> Unit,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    topContentPadding: Dp,
    bottomContentPadding: Dp,
    listState: LazyListState
) {
    var showSignOutConfirm by remember { mutableStateOf(false) }
    val badgeCount = remember(totalWorkouts) { listOf(1, 5, 10, 25, 50, 100).count { totalWorkouts >= it } }
    val memberSince = remember(profile?.joinDateMillis) {
        profile?.joinDateMillis?.let { SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(it)) } ?: "-"
    }
    val displayName = profile?.name?.trim().takeUnless { it.isNullOrBlank() } ?: "Your Profile"
    val initials = displayName.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

    val menuGroups = listOf(
        MenuGroup(
            "Training",
            listOf(
                MenuEntry("Activity History", Icons.Outlined.History, onOpenActivityHistory),
                MenuEntry("Achievements", Icons.Outlined.MilitaryTech, onOpenAchievements, detail = "$badgeCount of 6"),
                MenuEntry("Challenges", Icons.Outlined.Flag, onOpenChallenges),
                MenuEntry("Cardio", Icons.AutoMirrored.Outlined.DirectionsRun, onOpenCardio)
            )
        ),
        MenuGroup(
            "Body",
            listOfNotNull(
                MenuEntry("Body Progress", Icons.Outlined.MonitorWeight, onOpenBody),
                MenuEntry("Progress Photos", Icons.Outlined.PhotoCamera, onOpenPhotos),
                if (SHOW_HEALTH_DATA) MenuEntry("Health Data", Icons.Outlined.MonitorHeart, onOpenHealthData) else null
            )
        ),
        MenuGroup(
            "Account",
            listOf(
                MenuEntry("Edit Profile", Icons.Outlined.EditNote, onEditProfile),
                MenuEntry("Settings", Icons.Outlined.Settings, onOpenSettings),
                MenuEntry("Share App", Icons.Outlined.Share, onShareApp)
            )
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = Dimens.ScreenEdge,
            end = Dimens.ScreenEdge,
            top = Dimens.Space2 + topContentPadding,
            bottom = Dimens.Space3 + bottomContentPadding
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
    ) {
        item(key = "profile-identity") {
            IdentityHeader(
                displayName = displayName,
                initials = initials,
                email = profile?.email.orEmpty(),
                memberSince = memberSince,
                fitnessLevel = profile?.fitnessLevel.orEmpty(),
                onEditProfile = onEditProfile,
                modifier = Modifier.staggeredEntrance(index = 0, key = "profile-identity")
            )
        }

        item(key = "profile-level") {
            LevelProgressCard(
                progress = levelProgress,
                glassState = glassState,
                modifier = Modifier
                    .padding(top = Dimens.Space1)
                    .staggeredEntrance(index = 1, key = "profile-level")
            )
        }

        item(key = "profile-stats") {
            ProfileStatsRow(
                totalWorkouts = totalWorkouts,
                streak = streak,
                badges = badgeCount,
                modifier = Modifier
                    .padding(vertical = Dimens.Space1)
                    .staggeredEntrance(index = 2, key = "profile-stats")
            )
        }

        menuGroups.forEachIndexed { groupIndex, group ->
            item(key = "profile-group-${group.label}") {
                ProfileMenuGroup(
                    group = group,
                    glassState = glassState,
                    modifier = Modifier.staggeredEntrance(index = 3 + groupIndex, key = "profile-group-${group.label}")
                )
            }
        }

        item(key = "profile-signout") {
            SignOutButton(
                onClick = { showSignOutConfirm = true },
                modifier = Modifier
                    .padding(top = Dimens.Space1)
                    .staggeredEntrance(index = 3 + menuGroups.size, key = "profile-signout")
            )
        }
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = ApexShapes.large,
            title = {
                Text(
                    text = "Sign out?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Your data stays saved. You'll just need to sign back in to see it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.apex.mutedText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showSignOutConfirm = false; onSignOut() },
                    modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
                ) {
                    Text(
                        text = "Sign out",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.apex.errorText
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSignOutConfirm = false },
                    modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }
}

// ---- Identity ----

// Who you are, sitting on the background: avatar, name, email and a quiet details line
@Composable
private fun IdentityHeader(
    displayName: String,
    initials: String,
    email: String,
    memberSince: String,
    fitnessLevel: String,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val details = buildList {
        if (fitnessLevel.isNotBlank()) add(fitnessLevel.replaceFirstChar { it.uppercase() })
        add("Member since $memberSince")
    }.joinToString("  ·  ")
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(Dimens.Hairline, MaterialTheme.apex.hairline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (initials.isBlank()) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(30.dp)
                )
            } else {
                Text(
                    text = initials,
                    style = ApexText.NumeralSmall.copy(fontSize = 28.sp, lineHeight = 30.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (email.isNotBlank()) {
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.apex.mutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.apex.mutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .apexClickable(onClick = onEditProfile)
                .clip(CircleShape)
                .border(Dimens.Hairline, MaterialTheme.apex.hairline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit profile",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ---- Level ----

// Level is the one accent moment on Profile: big level number, title, and the XP bar
@Composable
private fun LevelProgressCard(
    progress: GamificationCalculations.LevelProgress,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    modifier: Modifier = Modifier
) {
    val animatedXp by rememberCountUpInt(progress.xpIntoLevel)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = RoundedCornerShape(24.dp))
            .padding(Dimens.Space3)
    ) {
        Text(
            text = GamificationCalculations.levelTitle(progress.level).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.apex.accentText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "Level ",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "${progress.level}",
                style = ApexText.HeroNumeral.copy(fontSize = 56.sp, lineHeight = 58.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${groupThousands(animatedXp)} / ${groupThousands(progress.xpForNextLevel)} XP",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.apex.mutedText,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Space1))
        ApexProgressBar(progress = progress.progressFraction, height = 6.dp)
    }
}

// ---- Stats and menu ----

// Lifetime numbers on the background with thin dividers. They are information, not buttons.
@Composable
private fun ProfileStatsRow(
    totalWorkouts: Int,
    streak: Int,
    badges: Int,
    modifier: Modifier = Modifier
) {
    val shownWorkouts by rememberCountUpInt(totalWorkouts)
    val shownStreak by rememberCountUpInt(streak)
    val shownBadges by rememberCountUpInt(badges)
    val valueStyle = ApexText.Numeral.copy(fontSize = 36.sp, lineHeight = 40.sp)
    val valueColor = MaterialTheme.colorScheme.onBackground
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        MetricBlock(value = groupThousands(shownWorkouts), label = "WORKOUTS", modifier = Modifier.weight(1f), valueStyle = valueStyle, valueColor = valueColor)
        StatDivider()
        MetricBlock(value = "$shownStreak", label = "WEEK STREAK", modifier = Modifier.weight(1f), valueStyle = valueStyle, valueColor = valueColor)
        StatDivider()
        MetricBlock(value = "$shownBadges", label = "BADGES", modifier = Modifier.weight(1f), valueStyle = valueStyle, valueColor = valueColor)
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(Dimens.Hairline)
            .height(36.dp)
            .background(MaterialTheme.apex.hairline)
    )
}

@Composable
private fun ProfileMenuGroup(
    group: MenuGroup,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = group.label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.apex.mutedText,
            modifier = Modifier.padding(start = 4.dp, bottom = Dimens.Space1)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassPanel(glassState, shape = CardShape)
        ) {
            group.entries.forEachIndexed { index, entry ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = 56.dp)
                            .fillMaxWidth()
                            .height(Dimens.Hairline)
                            .background(MaterialTheme.apex.hairline)
                    )
                }
                ProfileMenuRow(entry = entry)
            }
        }
    }
}

@Composable
private fun ProfileMenuRow(entry: MenuEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .apexClickable(pressedScale = 0.99f, onClick = entry.onClick)
            .heightIn(min = 56.dp)
            .padding(horizontal = Dimens.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = entry.icon,
            contentDescription = null,
            tint = MaterialTheme.apex.mutedText,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Text(
            text = entry.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (entry.detail != null) {
            Text(
                text = entry.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.apex.mutedText
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.apex.mutedText
        )
    }
}

// Sign out: quiet text in the error colour, it does not need to shout
@Composable
private fun SignOutButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val errorColor = MaterialTheme.apex.errorText
    Row(
        modifier = modifier
            .fillMaxWidth()
            .apexClickable(onClick = onClick)
            .heightIn(min = 52.dp)
            .clip(PillShape)
            .border(Dimens.Hairline, MaterialTheme.apex.hairline, PillShape),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Logout,
            contentDescription = null,
            tint = errorColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(Dimens.Space1))
        Text(
            text = "Sign out",
            style = MaterialTheme.typography.labelLarge,
            color = errorColor
        )
    }
}

private fun groupThousands(value: Int): String = "%,d".format(value)

// ---- Loading skeleton ----

@Composable
private fun ProfileSkeleton(topContentPadding: Dp, bottomContentPadding: Dp) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = Dimens.ScreenEdge,
                end = Dimens.ScreenEdge,
                top = Dimens.Space2 + topContentPadding,
                bottom = bottomContentPadding
            ),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeletonCircle(size = 72.dp)
            Spacer(modifier = Modifier.width(Dimens.Space2))
            Column {
                SkeletonBlock(modifier = Modifier.width(160.dp).height(24.dp))
                Spacer(modifier = Modifier.height(Dimens.Space1))
                SkeletonBlock(modifier = Modifier.width(120.dp).height(12.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(128.dp), shape = RoundedCornerShape(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space3)) {
            repeat(3) { SkeletonBlock(modifier = Modifier.weight(1f).height(52.dp)) }
        }
        SkeletonBlock(modifier = Modifier.width(80.dp).height(12.dp))
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(224.dp), shape = CardShape)
    }
}

// ---- Previews ----

private fun previewProfile() = UserProfile(
    name = "Alex Morgan",
    email = "alex@example.com",
    fitnessLevel = "Intermediate",
    joinDateMillis = System.currentTimeMillis() - (120L * 24 * 60 * 60 * 1000)
)

private fun previewLevel() = GamificationCalculations.LevelProgress(
    level = 6,
    totalXp = 4200,
    xpIntoLevel = 300,
    xpForNextLevel = 900
)

@Preview(showBackground = true, showSystemUi = true, name = "Profile light")
@Composable
private fun ProfilePageLightPreview() {
    ApexFitnessTheme(darkTheme = false) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            ProfilePage(
                profile = previewProfile(),
                totalWorkouts = 47,
                streak = 12,
                levelProgress = previewLevel(),
                onSignOut = {},
                onEditProfile = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Profile dark")
@Composable
private fun ProfilePageDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            ProfilePage(
                profile = previewProfile(),
                totalWorkouts = 47,
                streak = 12,
                levelProgress = previewLevel(),
                onSignOut = {},
                onEditProfile = {}
            )
        }
    }
}
