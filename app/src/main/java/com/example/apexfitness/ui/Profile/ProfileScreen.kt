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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.EditNote
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
    bottomContentPadding: Dp = 0.dp
) {
    Crossfade(
        targetState = isLoading,
        animationSpec = motionTween(Motion.Standard),
        label = "profileLoadingCrossfade"
    ) { loading ->
        if (loading) {
            ProfileSkeleton(bottomContentPadding = bottomContentPadding)
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
                bottomContentPadding = bottomContentPadding
            )
        }
    }
}

private data class MenuEntry(val title: String, val icon: ImageVector, val onClick: () -> Unit)

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
    bottomContentPadding: Dp
) {
    var showSignOutConfirm by remember { mutableStateOf(false) }
    val badgeCount = remember(totalWorkouts) { listOf(1, 5, 10, 25, 50, 100).count { totalWorkouts >= it } }
    val memberSince = remember(profile?.joinDateMillis) {
        profile?.joinDateMillis?.let { SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(it)) } ?: "-"
    }
    val displayName = profile?.name?.trim().takeUnless { it.isNullOrBlank() } ?: "Your Profile"
    val initials = displayName.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

    val menuItems = listOf(
        MenuEntry("Edit Profile", Icons.Outlined.EditNote, onEditProfile),
        MenuEntry("Activity History", Icons.Outlined.History, onOpenActivityHistory),
        MenuEntry("Challenges", Icons.Outlined.Flag, onOpenChallenges),
        MenuEntry("Cardio", Icons.AutoMirrored.Outlined.DirectionsRun, onOpenCardio),
        MenuEntry("Body Progress", Icons.Outlined.MonitorWeight, onOpenBody),
        MenuEntry("Progress Photos", Icons.Outlined.PhotoCamera, onOpenPhotos),
        MenuEntry("Health Data", Icons.Outlined.MonitorHeart, onOpenHealthData),
        MenuEntry("Settings", Icons.Outlined.Settings, onOpenSettings),
        MenuEntry("Share App", Icons.Outlined.Share, onShareApp)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ScreenEdge,
            end = Dimens.ScreenEdge,
            top = Dimens.Space3,
            bottom = Dimens.Space2 + bottomContentPadding
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
    ) {
        item(key = "profile-header") {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.staggeredEntrance(index = 0, key = "profile-header")
            )
        }

        item(key = "profile-identity") {
            IdentityCard(
                displayName = displayName,
                initials = initials,
                email = profile?.email.orEmpty(),
                memberSince = memberSince,
                fitnessLevel = profile?.fitnessLevel.orEmpty(),
                glassState = glassState,
                modifier = Modifier.staggeredEntrance(index = 1, key = "profile-identity")
            )
        }

        item(key = "profile-level") {
            LevelProgressCard(
                progress = levelProgress,
                glassState = glassState,
                modifier = Modifier.staggeredEntrance(index = 2, key = "profile-level")
            )
        }

        item(key = "profile-stats") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(index = 3, key = "profile-stats"),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space1 + 4.dp)
            ) {
                ProfileStatCard(
                    value = badgeCount,
                    label = "BADGES",
                    glassState = glassState,
                    onClick = onOpenAchievements,
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    value = totalWorkouts,
                    label = "WORKOUTS",
                    glassState = glassState,
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    value = streak,
                    label = "STREAK",
                    glassState = glassState,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        items(menuItems, key = { "menu-${it.title}" }) { entry ->
            val index = menuItems.indexOf(entry)
            ProfileMenuRow(
                title = entry.title,
                icon = entry.icon,
                glassState = glassState,
                onClick = entry.onClick,
                modifier = Modifier.staggeredEntrance(index = 4 + index, key = "profile-menu-${entry.title}")
            )
        }

        item(key = "profile-signout") {
            SignOutButton(
                onClick = { showSignOutConfirm = true },
                modifier = Modifier
                    .padding(top = Dimens.Space1)
                    .staggeredEntrance(index = 8, key = "profile-signout")
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
                    text = "Sign Out?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "You'll need to sign back in to see your data.",
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
                        text = "Sign Out",
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

@Composable
private fun IdentityCard(
    displayName: String,
    initials: String,
    email: String,
    memberSince: String,
    fitnessLevel: String,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space3),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            if (initials.isBlank()) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Text(
                    text = initials,
                    style = ApexText.NumeralSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.Space2))

        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
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
        Spacer(modifier = Modifier.height(Dimens.Space1))
        Text(
            text = "MEMBER SINCE ${memberSince.uppercase()}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.apex.mutedText
        )

        if (fitnessLevel.isNotBlank()) {
            Box(
                modifier = Modifier
                    .padding(top = Dimens.Space2)
                    .clip(PillShape)
                    .background(MaterialTheme.apex.accentSoft)
                    .border(Dimens.Hairline, MaterialTheme.apex.accent.copy(alpha = 0.45f), PillShape)
            ) {
                Text(
                    text = fitnessLevel.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.apex.accentText,
                    modifier = Modifier.padding(horizontal = Dimens.Space2, vertical = Dimens.Space1)
                )
            }
        }
    }
}

// ---- Level ----

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
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space3)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space2)
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimens.MinTouchTarget)
                        .clip(CircleShape)
                        .background(MaterialTheme.apex.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MilitaryTech,
                        contentDescription = null,
                        tint = MaterialTheme.apex.accentText,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Level ${progress.level}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = GamificationCalculations.levelTitle(progress.level).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.apex.mutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = "$animatedXp / ${progress.xpForNextLevel} XP",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.apex.mutedText,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Space2))
        ApexProgressBar(progress = progress.progressFraction, height = 6.dp)
    }
}

// ---- Stats and menu ----

@Composable
private fun ProfileStatCard(
    value: Int,
    label: String,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val animated by rememberCountUpInt(value)
    Column(
        modifier = modifier
            .then(if (onClick != null) Modifier.apexClickable(onClick = onClick) else Modifier)
            .glassPanel(glassState, shape = CardShape)
            .heightIn(min = Dimens.MinTouchTarget)
            .padding(vertical = Dimens.Space2, horizontal = Dimens.Space1),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$animated",
            style = ApexText.NumeralSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.apex.mutedText,
            maxLines = 1
        )
    }
}

@Composable
private fun ProfileMenuRow(
    title: String,
    icon: ImageVector,
    glassState: com.example.apexfitness.ui.theme.GlassState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .apexClickable(onClick = onClick)
            .glassPanel(glassState, shape = CardShape)
            .heightIn(min = 64.dp)
            .padding(horizontal = Dimens.Space2 + 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.apex.accentText,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.apex.mutedText
        )
    }
}

// Sign out button, outlined so it is not too loud
@Composable
private fun SignOutButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val errorColor = MaterialTheme.apex.errorText
    Row(
        modifier = modifier
            .fillMaxWidth()
            .apexClickable(onClick = onClick)
            .heightIn(min = 52.dp)
            .clip(PillShape)
            .border(Dimens.Hairline, errorColor.copy(alpha = 0.6f), PillShape),
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
            text = "Sign Out",
            style = MaterialTheme.typography.labelLarge,
            color = errorColor
        )
    }
}

// ---- Loading skeleton ----

@Composable
private fun ProfileSkeleton(bottomContentPadding: Dp) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ScreenEdge,
            end = Dimens.ScreenEdge,
            top = Dimens.Space3,
            bottom = Dimens.Space2 + bottomContentPadding
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
    ) {
        item { SkeletonBlock(modifier = Modifier.width(140.dp).height(32.dp)) }
        item {
            SkeletonBlock(modifier = Modifier.fillMaxWidth().height(232.dp), shape = CardShape)
        }
        item {
            SkeletonBlock(modifier = Modifier.fillMaxWidth().height(112.dp), shape = CardShape)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space2)) {
                repeat(3) {
                    SkeletonBlock(modifier = Modifier.weight(1f).height(80.dp), shape = CardShape)
                }
            }
        }
        items(4) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth().height(64.dp), shape = CardShape)
        }
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
