package com.example.apexfitness.ui.settings

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.data.FirestoreRepository
import com.example.apexfitness.ui.authentication.AuthService
import com.example.apexfitness.ui.notifications.NotificationScheduler
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.CardShape
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.Motion
import com.example.apexfitness.ui.theme.LocalMotionEnabled
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.apexSpring
import com.example.apexfitness.ui.theme.apexTween
import com.example.apexfitness.ui.theme.glassPanel
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.rememberHaptics
import com.example.apexfitness.ui.theme.staggeredEntrance

// Settings screen

@Composable
fun SettingsScreen(
    navController: NavHostController,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    notificationsEnabled: Boolean,
    reminderHour: Int,
    reminderMinute: Int,
    onToggleNotifications: (Boolean) -> Unit,
    onReminderTimeChange: (Int, Int) -> Unit
) {
    val glassState = rememberGlassState()
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onToggleNotifications(true)
        }
    }

    // Keeps the reminder alarms in sync with the switch, the time and the days routines are assigned to
    LaunchedEffect(notificationsEnabled, reminderHour, reminderMinute) {
        val appContext = context.applicationContext
        if (notificationsEnabled) {
            val uid = authService.getCurrentUser()?.uid
            if (uid != null) {
                val routines = FirestoreRepository.getRoutines(uid)
                val days = routines
                    .flatMap { it.days }
                    .mapNotNull { NotificationScheduler.dayCodeToCalendarDay(it) }
                    .toSet()
                NotificationScheduler.scheduleAll(appContext, days, reminderHour, reminderMinute)
            }
        } else {
            NotificationScheduler.cancelAll(appContext)
        }
    }

    val motionEnabled = LocalMotionEnabled.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeader(onBack = { navController.popBackStack() })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.ScreenEdge)
        ) {
            Spacer(modifier = Modifier.height(Dimens.Space1))

            SettingsSection(
                label = "Appearance",
                modifier = Modifier.staggeredEntrance(index = 0, key = "settings-appearance")
            ) {
                SettingsCard(glassState = glassState) {
                    SettingsToggleRow(
                        icon = Icons.Outlined.DarkMode,
                        title = "Dark Mode",
                        subtitle = if (isDarkMode) "On" else "Off",
                        checked = isDarkMode,
                        onCheckedChange = onToggleDarkMode
                    )
                }
            }

            SettingsSection(
                label = "Notifications",
                modifier = Modifier.staggeredEntrance(index = 1, key = "settings-notifications")
            ) {
                SettingsCard(glassState = glassState) {
                    SettingsToggleRow(
                        icon = Icons.Outlined.NotificationsNone,
                        title = "Workout Reminders",
                        subtitle = if (notificationsEnabled) {
                            "On · ${formatReminderTime(reminderHour, reminderMinute)}"
                        } else {
                            "Off"
                        },
                        checked = notificationsEnabled,
                        onCheckedChange = { checked ->
                            val needsPermission = checked &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            if (needsPermission) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onToggleNotifications(checked)
                            }
                        }
                    )
                    AnimatedVisibility(
                        visible = notificationsEnabled,
                        enter = fadeIn(apexTween(motionEnabled, Motion.Fade)) +
                            expandVertically(apexSpring(motionEnabled)),
                        exit = fadeOut(apexTween(motionEnabled, Motion.Micro)) +
                            shrinkVertically(apexSpring(motionEnabled))
                    ) {
                        Column {
                            RowDivider()
                            SettingsNavRow(
                                icon = Icons.Outlined.AccessTime,
                                title = "Reminder Time · ${formatReminderTime(reminderHour, reminderMinute)}",
                                onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute -> onReminderTimeChange(hour, minute) },
                                        reminderHour,
                                        reminderMinute,
                                        false
                                    ).show()
                                }
                            )
                        }
                    }
                }
            }

            SettingsSection(
                label = "Account",
                modifier = Modifier.staggeredEntrance(index = 2, key = "settings-account")
            ) {
                SettingsCard(glassState = glassState) {
                    SettingsNavRow(
                        icon = Icons.Outlined.Person,
                        title = "Edit Profile",
                        onClick = { navController.navigate("editProfile") }
                    )
                }
            }

            SettingsSection(
                label = "About",
                modifier = Modifier.staggeredEntrance(index = 3, key = "settings-about")
            ) {
                SettingsCard(glassState = glassState) {
                    SettingsInfoRow(
                        icon = Icons.Outlined.Info,
                        title = "Version",
                        value = "1.0.0"
                    )
                }
            }

            Text(
                text = "ApexFitness - built to help you plan, train and track, all in one place.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.apex.mutedText,
                modifier = Modifier
                    .padding(vertical = Dimens.Space2)
                    .staggeredEntrance(index = 4, key = "settings-footer")
            )

            Spacer(modifier = Modifier.height(Dimens.Space3))
        }
    }
}

// Formats the time for display, e.g. "6:00 PM"
private fun formatReminderTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format("%d:%02d %s", displayHour, minute, amPm)
}

@Composable
private fun SettingsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Space1, vertical = Dimens.Space1),
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
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun SettingsSection(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth().padding(bottom = Dimens.Space3)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.apex.mutedText,
            modifier = Modifier.padding(start = 4.dp, bottom = Dimens.Space1)
        )
        content()
    }
}

@Composable
private fun SettingsCard(glassState: com.example.apexfitness.ui.theme.GlassState, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape),
        content = content
    )
}

@Composable
private fun RowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Space2)
            .height(Dimens.Hairline)
            .background(MaterialTheme.apex.hairline)
    )
}

@Composable
private fun RowIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.apex.accentText,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val haptics = rememberHaptics()
    val toggle = {
        haptics.toggle(!checked)
        onCheckedChange(!checked)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .apexClickable(haptic = false, onClick = toggle)
            .heightIn(min = 64.dp)
            .padding(horizontal = Dimens.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowIcon(icon)
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.apex.mutedText
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,  // the whole row is tappable
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedBorderColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.apex.mutedText,
                uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                uncheckedBorderColor = MaterialTheme.apex.mutedText.copy(alpha = 0.6f)
            ),
            modifier = Modifier.semantics { role = Role.Switch }
        )
    }
}

@Composable
private fun SettingsNavRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .apexClickable(onClick = onClick)
            .heightIn(min = 64.dp)
            .padding(horizontal = Dimens.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowIcon(icon)
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

@Composable
private fun SettingsInfoRow(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = Dimens.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowIcon(icon)
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.apex.mutedText
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Settings light")
@Composable
fun SettingsScreenPreview() {
    var darkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var reminderHour by remember { mutableStateOf(18) }
    var reminderMinute by remember { mutableStateOf(0) }
    ApexFitnessTheme(darkTheme = darkMode) {
        SettingsScreen(
            navController = rememberNavController(),
            isDarkMode = darkMode,
            onToggleDarkMode = { darkMode = it },
            notificationsEnabled = notificationsEnabled,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute,
            onToggleNotifications = { notificationsEnabled = it },
            onReminderTimeChange = { hour, minute ->
                reminderHour = hour
                reminderMinute = minute
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Settings dark")
@Composable
private fun SettingsScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        SettingsScreen(
            navController = rememberNavController(),
            isDarkMode = true,
            onToggleDarkMode = {},
            notificationsEnabled = true,
            reminderHour = 6,
            reminderMinute = 30,
            onToggleNotifications = {},
            onReminderTimeChange = { _, _ -> }
        )
    }
}
